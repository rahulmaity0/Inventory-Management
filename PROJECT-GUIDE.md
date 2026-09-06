# Project guide

Every file in this repository, what it does, and why it is there.

The project is a Spring Boot backend for a gold manufacturing business. It
tracks how much pure gold is in the locker, how much each client owes, and
every issue and receipt that moves metal between the two.

---

## Contents

- [The business problem](#the-business-problem)
- [Running it](#running-it)
- [What happens when you save a transaction](#what-happens-when-you-save-a-transaction)
- [The files](#the-files)
  - [Entry point](#entry-point)
  - [model — the database tables](#model--the-database-tables)
  - [repository — reading and writing rows](#repository--reading-and-writing-rows)
  - [dto — what crosses the network](#dto--what-crosses-the-network)
  - [service — the business logic](#service--the-business-logic)
  - [controller — the URLs](#controller--the-urls)
  - [security — logging in and staying in](#security--logging-in-and-staying-in)
  - [resources — config, seed data, the page](#resources--config-seed-data-the-page)
  - [test](#test)
- [Annotation glossary](#annotation-glossary)
- [Questions you should be able to answer](#questions-you-should-be-able-to-answer)

---

## The business problem

Gold leaves the locker and goes to a client as an ornament. Later some comes
back. Two numbers have to stay honest at all times:

- **Locker pure gold** — how much pure metal is physically held.
- **Client balance** — how much pure metal each client owes.

The wrinkle is the **making charge**. When gold is issued, the client is billed
at *purity plus making charge*, but the locker only loses the metal that
physically left, at its real purity. The gap between those two numbers is the
business's margin. That single rule is why this project is not a CRUD app.

Two rules are enforced in code, never assumed:

1. An issue can never exceed the gold in the locker.
2. A receipt can never exceed what the client owes.

---

## Running it

```bash
./mvnw spring-boot:run     # http://localhost:8082
./mvnw test
```

The database is H2, in memory, rebuilt on every start from `data.sql`. Nothing
is installed and nothing persists between runs. Log in as `admin` / `password`.

---

## What happens when you save a transaction

Follow this once and the whole app makes sense.

```
Browser POST /api/transactions
  └─ JwtAuthFilter            reads the token, says who you are
      └─ SecurityConfig       decides the request is allowed
          └─ TransactionController   maps the URL, validates the body
              └─ TransactionService  @Transactional starts here
                  ├─ ClientRepository        load the client
                  ├─ InventoryStateRepository load the locker
                  ├─ work out effective purity and pure gold
                  ├─ check the two rules, throw if either breaks
                  ├─ update locker and client balance together
                  └─ save all three rows
              └─ CreateTransactionResponse   both new balances
  └─ ApiExceptionHandler      turns any thrown exception into tidy JSON
```

---

## The files

### Entry point

#### `InventoryBillingApplication.java`
Twelve lines. `main` starts Spring Boot, which scans this package, builds one
object of every class marked `@Service`, `@RestController`, `@Component` or
`@Configuration`, and hands them to whoever asks for them in a constructor.
That is all dependency injection is.

`@EnableScheduling` is what allows `@Scheduled` methods to actually run — without
it the gold price would never refresh.

---

### `model` — the database tables

Each `@Entity` class is one table; each field is one column. These are the only
classes that touch the database schema.

#### `Client.java`
One client. Name, and `currentPureBalance` — the pure gold they owe, stored as
`BigDecimal` with precision 12 scale 3, so three decimal places of a gram.

#### `InventoryState.java`
The locker. **Exactly one row, with id 1.** A whole table for a single number
looks odd, but it means the locker total lives in the database where the
transaction can lock it, rather than in a variable somewhere.

#### `GoldTransaction.java`
One line of the ledger, and the biggest entity. Stores gross weight, purity,
making charge, the effective purity that was applied, the resulting pure gold,
the type, the date, the item type and free-text notes.

`@ManyToOne(fetch = FetchType.LAZY)` on `client`: many transactions belong to
one client, and LAZY means the client row is only fetched if something actually
reads it.

#### `TransactionType.java`
`ISSUE` or `RECEIPT`. Two values, and the direction of every calculation
depends on which one it is.

#### `ItemType.java`
`NECKLACE`, `EARRINGS`, `BANGLES`, `CHAIN`, `RING`, `BRACELET`, `COIN_OR_BAR`,
`OTHER`. Optional — a receipt of loose metal is not an ornament.

Both enums are stored with `@Enumerated(EnumType.STRING)`, so the column holds
the word. The default is `ORDINAL`, which stores `0`, `1`, `2` — and the day
somebody reorders the enum, every historical row silently changes meaning.
Always use STRING.

#### `User.java`
Username, BCrypt password hash, role. Table is `app_users` because `user` is a
reserved word in most databases.

---

### `repository` — reading and writing rows

Four interfaces with no bodies. Extending `JpaRepository<Entity, IdType>` gives
you `save`, `findById`, `findAll`, `count` and `delete` for free.

#### `ClientRepository.java`, `InventoryStateRepository.java`
Empty. The inherited methods are all that is needed.

#### `GoldTransactionRepository.java`
One custom method:

```java
List<GoldTransaction> findByClientIdOrderByTransactionDateDescIdDesc(Long clientId);
```

Spring Data reads the method *name* and writes the SQL. "Find by client id,
newest date first, and newest id first when two share a date." No query written
by hand.

#### `UserRepository.java`
`findByUsername` returns `Optional<User>` — the login flow needs to handle "no
such user" without a null check blowing up.

---

### `dto` — what crosses the network

Entities are never sent to the browser. **Request** DTOs are what Jackson builds
from incoming JSON; **response** DTOs are what it turns back into JSON. Keeping
them separate means renaming a column does not silently change your API.

| File | Direction | Notes |
|---|---|---|
| `CreateClientRequest.java` | in | Name and optional opening balance. Bean Validation annotations on the fields. |
| `ClientSummaryResponse.java` | out | Id, name, current balance. |
| `CreateTransactionRequest.java` | in | The big one — client, type, weight, purity, making charge, date, item type, notes. |
| `CreateTransactionResponse.java` | out | The saved id plus **both** balances after the change, so the page does not have to re-fetch. |
| `TransactionResponse.java` | out | One ledger line. |
| `DashboardResponse.java` | out | Locker, client total, business total, client count. |
| `MonthlyFlowResponse.java` | out | One month, issue total, receipt total. |
| `GoldPriceResponse.java` | out | Price per gram and per 10 g, currency, source, timestamp. |
| `AuthRequest.java` / `AuthResponse.java` | in / out | Username and password in, token out. |
| `RegisterRequest.java` | in | Username, password, optional role. |
| `ApiErrorResponse.java` | out | Timestamp, status, error, message — the shape of every failure. |

Request DTOs need a no-argument constructor and setters, because Jackson creates
the object empty and fills it. Response DTOs only need a constructor and
getters.

---

### `service` — the business logic

Everything that decides anything lives here.

#### `TransactionService.java` — **read this one twice**
The only place gold moves, and the file that matters.

`@Transactional` on `createTransaction` is the important line. The method writes
three rows — the client, the locker, the new ledger entry. Without it, a failure
on the third write would leave the first two committed, and the locker and the
client's balance would disagree permanently, with nothing to tell you. With it,
either all three land or none do.

The issue branch is where the business rule lives:

```java
BigDecimal actualPureGold = calculatePureGold(grossWeight, purityPercent);
// what physically left the locker
inventoryState.setLockerPureGold(locker.subtract(actualPureGold));
// what the client is billed, at purity + making charge
client.setCurrentPureBalance(balance.add(pureGoldEquivalent));
```

Two different numbers, deliberately. The difference is the margin.

`calculatePureGold` uses `BigDecimal`, not `double`. A `double` cannot hold 0.1
exactly; add it ten times and you do not get 1.0. Here that error is missing
gold. `divide` is told to keep 3 decimals with `RoundingMode.HALF_UP` — without
being told, it throws when the division does not terminate.

The rule checks use `compareTo`, not `equals`. `BigDecimal.equals` compares
scale as well as value, so `1.0` and `1.00` are "not equal". `compareTo` compares
the number.

#### `ClientService.java`
Create a client, list clients, read one client's ledger. Plain loops copying
entities into response objects.

#### `DashboardService.java`
Adds up every client balance and returns it with the locker total.
`totalBusinessPureGold` is locker plus client debt — the number that would move
if a transaction ever half-committed.

Note `totalClientPureGold = totalClientPureGold.add(...)` inside the loop:
`BigDecimal` is immutable, so `add` returns a new value and the result must be
assigned back.

#### `ReportService.java`
Monthly issue and receipt totals, in two passes: bucket each transaction into
its `YearMonth`, then add each bucket up. Uses a `TreeMap`, which keeps its keys
sorted, so months come out oldest first with no sorting step.

#### `GoldPriceService.java`
Three ideas worth understanding:

1. **The key never reaches the browser.** If the page called the price provider
   directly, anyone could open dev tools and read your key. So the browser asks
   us and we ask them.
2. **It is cached.** `@Scheduled(fixedDelayString = ...)` refreshes into a field
   every 30 minutes; every web request reads that field. A hundred people
   loading the dashboard costs zero API calls. That is what keeps you inside a
   free tier. `fixedDelay` means "wait this long after the previous run
   finished", so a slow call can never stack up.
3. **It always answers.** No key configured, or the provider is down → it serves
   the configured static price. Somebody cloning this repo without a key still
   gets a working dashboard.

---

### `controller` — the URLs

Thin on purpose. Each method maps a URL to one service call. No business logic.

| File | Endpoint | Does |
|---|---|---|
| `HomeController.java` | `GET /api` | Health check that lists the other endpoints. |
| `DashboardController.java` | `GET /api/dashboard` | The four summary numbers. |
| `ClientController.java` | `GET/POST /api/clients`, `GET /api/clients/{id}/ledger` | Clients and one client's history. |
| `TransactionController.java` | `POST /api/transactions` | The only write that moves gold. |
| `ReportController.java` | `GET /api/reports/monthly-flow` | The monthly report. |
| `GoldPriceController.java` | `GET /api/gold-price` | Public. Returns the cached price — no work happens here. |
| `AuthController.java` | `POST /api/auth/login`, `/register` | Issues a token; hashes a password on register. |

#### `ApiExceptionHandler.java`
`@RestControllerAdvice` means "apply to every controller". It catches
`IllegalArgumentException` (thrown by the services when a rule breaks) and
`MethodArgumentNotValidException` (thrown by Spring when `@Valid` fails) and
turns both into an `ApiErrorResponse` with a 400. Without it the caller gets a
stack trace.

`@Valid` on a controller parameter is what makes the annotations on the request
DTO actually run.

---

### `security` — logging in and staying in

Read this package last. It is the fiddliest and the least interesting.

#### `JwtService.java`
Makes and reads tokens. A JWT is three base64 chunks joined by dots: header,
claims, signature. The signature is the point — made with a secret only this
server knows, so if anyone edits the claims the signature stops matching.

**The token is not encrypted.** Anyone can read the claims. Never put a password
in one.

#### `CustomUserDetailsService.java`
The bridge between our `User` table and Spring Security, which knows nothing
about our entity. It loads a user and hands back Spring's own `UserDetails`.
Roles get a `ROLE_` prefix added here because Spring expects it.

#### `JwtAuthFilter.java`
Runs before every request. If there is a valid `Authorization: Bearer …` header,
it tells Spring Security who the caller is. If not, it does nothing and lets the
request through as anonymous — the rules decide whether that is allowed.
`OncePerRequestFilter` guarantees it runs exactly once even on internal
forwards.

#### `SecurityConfig.java`
All the rules in one place.

- `csrf.disable()` — CSRF protects cookie-based browser form posts. This API has
  no cookies and no sessions.
- `permitAll()` on `/api/auth/**`, `/api/gold-price`, and the static page;
  everything else needs a token.
- `SessionCreationPolicy.STATELESS` — no session is created or looked up. Every
  request carries its own token. That is what "stateless authentication" means,
  and it is why restarting the server does not log anyone out.
- `addFilterBefore(jwtAuthFilter, …)` — our filter runs before the
  username/password one, so a request with a token never reaches the login
  machinery.
- `BCryptPasswordEncoder` — hashes one way, salts each hash so two people with
  the same password get different results, and is deliberately slow to make
  brute forcing expensive.

The lambdas here are the only ones in the project. Spring Security 6 removed the
older chained style, so there is no way to write this file without them.

---

### `resources` — config, seed data, the page

#### `application.properties`
Port 8082, H2 in memory, `ddl-auto=create` (schema rebuilt from the entities on
every start), and the gold price settings. `gold.api.key` is empty by default,
which is what makes the static price kick in.

#### `data.sql`
Seeds one admin user, the single locker row, three clients and four
transactions, so the dashboard has something to show on first run.
`defer-datasource-initialization=true` in the properties is what makes this run
*after* Hibernate has created the tables.

#### `static/index.html`
The whole frontend — one file, no framework. Login, the four summary cards, the
gold price ticker, the locker drawn as 100 g biscuits, the client table, the
monthly report and the two transaction forms. It keeps the token in
`localStorage` and sends it as a `Bearer` header on every call.

The biscuit drawing is pure SVG generated from one number. It never talks to the
server on its own.

---

### `test`

#### `InventoryBillingApplicationTests.java`
One test: does the Spring context load? It looks trivial, but it fails on any
wiring mistake — a missing bean, a bad property, a broken annotation. It is the
cheapest useful test in the project.

**The obvious gap:** there are no tests for `TransactionService`. The two rules
and the making-charge maths are the most important code here and nothing checks
them. If you extend one thing, extend that.

---

## Annotation glossary

| Annotation | Means |
|---|---|
| `@SpringBootApplication` | Start here; scan this package for components. |
| `@RestController` | This class handles HTTP and returns JSON. |
| `@RequestMapping` / `@GetMapping` / `@PostMapping` | Map a URL to a method. |
| `@RequestBody` | Build this object from the JSON body. |
| `@PathVariable` | Take this value out of the URL. |
| `@Valid` | Run the validation annotations on that object. |
| `@Service` | Business logic; make one and inject it where asked. |
| `@Transactional` | All database work in this method commits together or not at all. |
| `@Entity` / `@Table` | This class is a database table. |
| `@Id` / `@GeneratedValue` | Primary key, generated by the database. |
| `@Column` | Column settings — nullable, precision, length. |
| `@Enumerated(EnumType.STRING)` | Store the enum's name, not its position. |
| `@ManyToOne` | Many of these rows belong to one of those. |
| `@Scheduled` | Run this method on a timer. |
| `@Value` | Inject a value from application.properties. |
| `@RestControllerAdvice` | Handle exceptions for every controller. |

---

## Questions you should be able to answer

These are the questions an interviewer will actually ask about this project.

1. **Why `@Transactional` on `createTransaction`?** Three rows change together.
   Without it a partial failure leaves the locker and the client balance
   permanently disagreeing.
2. **What happens if the third save fails?** The whole method rolls back. The
   first two writes never become visible.
3. **Why `BigDecimal` and not `double`?** `double` cannot represent decimal
   fractions exactly. Over many transactions the error is missing gold.
4. **Why `HALF_UP`?** Ordinary commercial rounding, and `divide` throws unless
   you specify a scale and mode.
5. **Why `compareTo` instead of `equals` on `BigDecimal`?** `equals` compares
   scale too, so `1.0` and `1.00` come out unequal.
6. **What are the invariants?** No issue beyond locker stock; no receipt beyond
   what the client owes. Both are checked before anything is written.
7. **What is the making charge doing?** It raises the purity the *client* is
   billed at, without changing what left the locker. The difference is margin.
8. **How does JWT auth work here?** Login returns a signed token; every later
   request carries it; a filter validates the signature and sets the caller
   identity. No session on the server.
9. **Why BCrypt?** One-way, salted per password, and deliberately slow.
10. **Why is the gold price cached on a schedule?** So page loads cost no API
    calls, and so a provider outage cannot take the dashboard down.
11. **Why does the price API key live on the server?** A key in the page is a
    public key. Anyone can read it in dev tools.
12. **What would you improve?** Tests on `TransactionService`; move to
    PostgreSQL so data survives a restart; move the JWT secret out of the source
    into configuration; add optimistic locking on the locker row so two
    simultaneous issues cannot both pass the stock check.

Number 12 is worth rehearsing. Knowing what is weak about your own code is the
thing that separates a junior who wrote something from a junior who copied it.
