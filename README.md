# Jewellery Ledger & Inventory System

[![CI](https://github.com/rahulmaity0/Inventory-Management/actions/workflows/ci.yml/badge.svg)](https://github.com/rahulmaity0/Inventory-Management/actions/workflows/ci.yml)

A Spring Boot backend for a gold manufacturing business. It tracks the pure gold in the locker, what each client owes, and every issue and receipt between them.

## The problem

Gold goes out to clients as ornaments and comes back as metal. Each movement has to update two numbers at once: the locker's stock and the client's balance. If one update lands and the other doesn't, the books never reconcile again.

## How it works

- **One transaction per movement.** `TransactionService.createTransaction` updates the client, the locker and the ledger entry inside a single `@Transactional` method. Any failure rolls back all three.
- **Making charge as a purity uplift.** On an issue, the client is billed at *purity + making charge*, but the locker is debited only the metal that physically left at its real purity. The difference is the business's margin. Receipts carry no making charge.
- **Two invariants, checked before anything is written:**
  - an issue can never take more gold than the locker holds;
  - a receipt can never exceed what the client owes.
- **Exact arithmetic.** All weights and purities use `BigDecimal` with explicit `HALF_UP` rounding (3 decimals for weights, 2 for percentages). There is no `double` anywhere in the money path.
- **Typed inputs.** Transaction and ornament types are enums, so an invalid value is rejected at the request boundary.
- **Stateless JWT auth** with Spring Security and BCrypt, built on a feature branch and merged by pull request.
- **Gold price ticker.** It refreshes on a schedule from a configurable live provider and falls back to a static rate when no API key is set or the provider fails.

## API

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Create a user |
| POST | `/api/auth/login` | Get a JWT |
| GET | `/api/dashboard` | Locker gold and total client balance |
| GET | `/api/clients` | All clients with balances |
| POST | `/api/clients` | Create a client |
| GET | `/api/clients/{clientId}/ledger` | One client's transaction history |
| POST | `/api/transactions` | Record an issue or a receipt |
| GET | `/api/reports/monthly-flow` | Monthly issue and receipt totals |
| GET | `/api/gold-price` | Current gold rate (public) |

Every route except auth, the gold price and the HTML dashboard page needs `Authorization: Bearer <token>`.

## Tech stack

Java 17, Spring Boot 3.4, Spring Security, Spring Data JPA / Hibernate, JJWT, H2, Maven

## Running locally

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8082` with an in-memory H2 database seeded with sample clients and transactions. Open `/` for the HTML dashboard, and log in with the seeded development account `admin` / `password`.
