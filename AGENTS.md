# AGENTS.md

## Project structure
- Monorepo: `backend/` (Spring Boot 3.4.1 + Java 21) only; no frontend yet
- Entrypoint: `com.finance.app.FinanceApplication`

## Run the server
```sh
mvn spring-boot:run -q        # from backend/, uses dev profile (H2 in-memory)
```
Dev profile: H2 on `jdbc:h2:mem:finance_db;MODE=PostgreSQL`, `ddl-auto: update`, Flyway **disabled**.
Prod profile: PostgreSQL + Flyway migrations (`db/migration/V*`). Start DB: `docker compose up -d` then `mvn spring-boot:run -q -Dspring.profiles.active=prod`.

## API conventions
- Base path: `/api/v1/`
- Auth: Register → get JWT → send as `Authorization: Bearer <token>`
- Public endpoints: `/api/v1/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`, `/h2-console/**`
- Response envelope: `{ "success": true, "message": null, "data": ... }`
- User ID injected via `@AuthenticationPrincipal UUID userId` (JWT filter extracts it)
- Page serialization: `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` — do not break this

## Key conventions
- **DTOs are Java records** (CreateXRequest, UpdateXRequest, XResponse)
- **Lombok**: `@RequiredArgsConstructor` + `final` fields for DI
- **Soft delete**: `is_active` boolean on Accounts, CreditCards — filter `where is_active = true`
- **Transactions**: amount/account/date/type write-once after creation; only category, merchant, description, notes mutable via PATCH
- **Categories**: 19 seeded by `DataSeeder` on startup (5 INCOME, 14 EXPENSE), `is_system = true` prevents deletion
- **Merchants**: `normalizeName()` = uppercase + strip non-alphanumeric + trim; unique `normalized_name` globally
- **Accounts**: balance auto-updated on transaction create/delete

## Testing
- No tests exist yet. Test framework: `spring-boot-starter-test` + `spring-security-test`
- All modules have `Service`, `Controller`, `Repository` layers — test via `@WebMvcTest` + `@DataJpaTest`
- H2 in-memory for tests matches dev profile

## Swagger
- UI: `http://localhost:8080/swagger-ui/index.html`
- Spec: `http://localhost:8080/v3/api-docs`

## Key env quirks
- Docker unavailable in dev env (snap confinement, no sudo) — do not rely on it
- `JWT_SECRET` default: `default-secret-change-in-production` — change for deploy
- `.env` is gitignored; copy from `.env.example` for local

## Active modules
| Package | Endpoints | Status |
|---------|-----------|--------|
| `auth/` | register, login | done |
| `user/` | GET/PATCH /users/me | done |
| `category/` | CRUD /categories | done |
| `merchant/` | CRUD /merchants | done |
| `account/` | CRUD /accounts (soft delete) | done |
| `card/` | CRUD /cards (credit, soft delete) | done |
| `transaction/` | CRUD /transactions (filters, balance tracking) | done |
| `common/` | BaseEntity, ApiResponse, exceptions, DataSeeder | done |

Packages not yet implemented: transfer, import (CSV), budget, recurring_expense, installment, dashboard.
