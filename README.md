# Inventory Billing Backend

This is the Spring Boot backend we are building for your resume.

## Lesson 1 structure

- `InventoryBillingApplication`: starts Spring Boot
- `controller`: receives HTTP requests
- `service`: contains business logic
- `repository`: talks to the database
- `model`: represents database tables

## Current endpoints

- `GET /` opens the simple HTML dashboard
- `GET /api` checks if the API is running
- `GET /api/dashboard` returns locker gold and total client balance
- `GET /api/clients` returns all clients
- `POST /api/clients` creates a client
- `GET /api/clients/{clientId}/ledger` returns one client's ledger
- `POST /api/transactions` creates an issue or receipt transaction and updates balances
- `GET /api/reports/monthly-flow` returns monthly issue and receipt totals
