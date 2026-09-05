# Reading guide

Same project, same behaviour, written in plain Java. Nothing clever, nothing
implicit. Read the files in this order and each one only uses things you have
already seen.

## 1. `InventoryBillingApplication.java`
Twelve lines. `main` starts Spring Boot, which scans this package, finds every
class marked `@Service`, `@RestController`, `@Component` or `@Configuration`,
creates one of each, and passes them to whoever asks for them in a constructor.
That is all dependency injection is.

## 2. `model/` — the four tables
`Client`, `InventoryState`, `GoldTransaction`, `User`. Each `@Entity` class is
one database table and each field is one column. `InventoryState` has exactly
one row, id 1, holding the locker total.

## 3. `repository/` — getting rows in and out
Four interfaces with no code in them. Extending `JpaRepository` gives you
`save`, `findById`, `findAll` and `count` for free. The two custom methods are
built from their names alone:
`findByClientIdOrderByTransactionDateDescIdDesc` becomes a SQL query without
you writing one.

## 4. `dto/` — what crosses the network
Entities are never sent to the browser directly. Request classes are what
Jackson builds from incoming JSON; response classes are what it turns back into
JSON. Keeping them separate means changing a column does not change your API.

## 5. `controller/` — the URLs
Thin on purpose. Each method maps a URL to one service call and returns the
result. No business logic lives here. `ApiExceptionHandler` catches exceptions
from anywhere and formats them as JSON.

## 6. `service/TransactionService.java` — read this one twice
The only place gold moves, and the only file that really matters. Work through
`createTransaction` line by line. The two things worth understanding:

- **Why `@Transactional`** — the method writes three rows. Without it, a
  failure on the third write would leave the first two committed, and the
  locker and the client's balance would disagree permanently. With it, either
  all three land or none do.
- **Why two different numbers on an issue** — `actualPureGold` leaves the
  locker at real purity; `pureGoldEquivalent` is what the client is billed, at
  purity plus making charge. The client owes more than left the locker, and
  that difference is the margin. This is the business rule the whole system
  exists for.

## 7. `service/` — the rest
`ClientService`, `DashboardService`, `ReportService`. Straight loops. The
dashboard's `totalBusinessPureGold` is the number that would move if a
transaction ever half-committed.

## 8. `security/` — save it for last
It is the least interesting part and the most fiddly. Order: `JwtService`
(makes and reads tokens), `CustomUserDetailsService` (loads a user from our
table into Spring's own type), `JwtAuthFilter` (runs before every request and
authenticates it if the token is good), `SecurityConfig` (the rules).

---

## What changed from the original

Behaviour is identical. Only the style differs.

| Was | Now |
|---|---|
| `record` DTOs | plain classes with a constructor and getters |
| `.stream().map(...).toList()` | `for` loops building an `ArrayList` |
| `Collectors.groupingBy` | a `TreeMap` filled by a loop |
| `.reduce(BigDecimal.ZERO, BigDecimal::add)` | a running total in a loop |
| `orElseThrow(() -> new ...)` | `Optional` + `isEmpty()` + `get()` |
| `Function<Claims, T>` and `Claims::getSubject` | direct methods, no generics |
| ternaries | `if` / `else` |

The lambdas in `SecurityConfig` stay. Spring Security 6 removed the older
style, so there is no way to write that file without them.

## Running it

```bash
./mvnw spring-boot:run
```

Both of these pass:

```bash
./mvnw compile
./mvnw test
```
