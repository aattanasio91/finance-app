# Guía de Desarrollo - Plataforma de Inteligencia Financiera Personal

## Stack Tecnológico

### Backend
- Java 21 + Spring Boot 3.x
- Spring Security + JWT
- JPA / Hibernate + Flyway
- PostgreSQL 16

### Frontend Web
- React 18+ + TypeScript
- Material UI 5+
- React Router v6
- Zustand (estado global)
- Recharts o Chart.js (gráficos)

### Mobile (futuro)
- React Native

### Infraestructura
- Docker Compose (dev)
- Docker (prod)

---

## Modelo de Datos

### User

| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| name | VARCHAR(255) | NOT NULL |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| default_currency | VARCHAR(3) | DEFAULT 'ARS' |
| enabled | BOOLEAN | DEFAULT true |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | |

### Account

| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| user_id | UUID | FK -> User, NOT NULL |
| name | VARCHAR(255) | NOT NULL |
| type | VARCHAR(50) | NOT NULL |
| currency | VARCHAR(3) | NOT NULL, DEFAULT 'ARS' |
| balance | DECIMAL(15,2) | DEFAULT 0 |
| is_active | BOOLEAN | DEFAULT true |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | |

**Tipos de cuenta:** `CHECKING`, `SAVINGS`, `CREDIT_CARD`, `CASH`, `INVESTMENT`

**Ejemplos:** Santander (CHECKING), Mercado Pago (SAVINGS), Efectivo (CASH)

### CreditCard

| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| user_id | UUID | FK -> User, NOT NULL |
| account_id | UUID | FK -> Account, NOT NULL |
| name | VARCHAR(255) | NOT NULL |
| brand | VARCHAR(50) | NOT NULL |
| closing_day | INT | NOT NULL (1-31) |
| due_day | INT | NOT NULL (1-31) |
| limit | DECIMAL(15,2) | |
| color_hex | VARCHAR(7) | |
| is_active | BOOLEAN | DEFAULT true |

**Brands:** `VISA`, `MASTERCARD`, `AMEX`, `NARANJA`, `CABAL`

### Category

| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| user_id | UUID | FK -> User, NULLABLE (null = categoría global) |
| name | VARCHAR(100) | NOT NULL |
| type | VARCHAR(20) | NOT NULL |
| icon | VARCHAR(50) | |
| color_hex | VARCHAR(7) | |
| is_system | BOOLEAN | DEFAULT false |

**Tipos:** `INCOME`, `EXPENSE`

**Categorías semilla del sistema:**
- INGRESOS: Sueldo, Freelance, Inversiones, Aguinaldo, Bonos, Otros
- EGRESOS: Combustible, Supermercado, Streaming, Restaurantes, Alquiler, Servicios, Educación, Salud, Transporte, Indumentaria, Entretenimiento, Internet, Seguros, Patente, Otros

### Merchant

| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| name | VARCHAR(255) | NOT NULL |
| normalized_name | VARCHAR(255) | NOT NULL |
| category_id | UUID | FK -> Category, NULLABLE |
| is_verified | BOOLEAN | DEFAULT false |
| created_at | TIMESTAMP | NOT NULL |

### Transaction

| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| user_id | UUID | FK -> User, NOT NULL |
| account_id | UUID | FK -> Account, NOT NULL |
| category_id | UUID | FK -> Category, NULLABLE |
| merchant_id | UUID | FK -> Merchant, NULLABLE |
| import_job_id | UUID | FK -> ImportJob, NULLABLE |
| parent_transaction_id | UUID | FK -> Transaction (para contracaras), NULLABLE |
| amount | DECIMAL(15,2) | NOT NULL |
| original_amount | DECIMAL(15,2) | |
| description | VARCHAR(500) | NOT NULL |
| date | DATE | NOT NULL |
| type | VARCHAR(20) | NOT NULL |
| currency | VARCHAR(3) | NOT NULL, DEFAULT 'ARS' |
| is_manual | BOOLEAN | DEFAULT false |
| is_recurring | BOOLEAN | DEFAULT false |
| notes | TEXT | |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | |

**Tipos:** `INCOME`, `EXPENSE`, `TRANSFER`

### Installment

| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| transaction_id | UUID | FK -> Transaction, NOT NULL |
| current_installment | INT | NOT NULL |
| total_installments | INT | NOT NULL |
| due_date | DATE | NOT NULL |
| amount | DECIMAL(15,2) | NOT NULL |
| is_paid | BOOLEAN | DEFAULT false |
| paid_date | DATE | |

### RecurringExpense

| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| user_id | UUID | FK -> User, NOT NULL |
| category_id | UUID | FK -> Category |
| name | VARCHAR(255) | NOT NULL |
| amount | DECIMAL(15,2) | NOT NULL |
| day_of_month | INT | NOT NULL (1-31) |
| frequency | VARCHAR(20) | DEFAULT 'MONTHLY' |
| is_active | BOOLEAN | DEFAULT true |
| next_date | DATE | |
| notes | TEXT | |

**Frecuencias:** `MONTHLY`, `BIMONTHLY`, `QUARTERLY`, `YEARLY`

### Budget

| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| user_id | UUID | FK -> User, NOT NULL |
| category_id | UUID | FK -> Category, NOT NULL |
| amount | DECIMAL(15,2) | NOT NULL |
| period | VARCHAR(20) | NOT NULL, DEFAULT 'MONTHLY' |
| start_date | DATE | NOT NULL |
| end_date | DATE | |
| is_active | BOOLEAN | DEFAULT true |

**Periodos:** `WEEKLY`, `MONTHLY`, `YEARLY`, `CUSTOM`

### ImportJob

| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| user_id | UUID | FK -> User, NOT NULL |
| source_type | VARCHAR(50) | NOT NULL |
| file_name | VARCHAR(255) | NOT NULL |
| file_hash | VARCHAR(64) | (SHA-256 para evitar duplicados) |
| import_date | TIMESTAMP | NOT NULL |
| status | VARCHAR(20) | NOT NULL |
| total_rows | INT | |
| success_rows | INT | |
| error_rows | INT | |
| error_log | TEXT | |

**Status:** `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`, `PARTIAL`

**Source types:** `CSV_BANK`, `CSV_CARD`, `EXCEL_BANK`, `MANUAL`

### TransactionRaw

| Campo | Tipo | Restricciones |
|---|---|---|
| id | UUID | PK |
| import_job_id | UUID | FK -> ImportJob, NOT NULL |
| original_description | VARCHAR(500) | NOT NULL |
| original_amount | VARCHAR(100) | (texto, sin parsear aún) |
| original_date | VARCHAR(100) | (texto, sin parsear aún) |
| raw_data | JSONB | (datos completos del CSV) |
| parsed_amount | DECIMAL(15,2) | |
| parsed_date | DATE | |
| status | VARCHAR(20) | DEFAULT 'PENDING' |
| error_message | TEXT | |

**Status:** `PENDING`, `PARSED`, `ERROR`, `DUPLICATE`, `IMPORTED`

---

## Relaciones Clave

```
User 1──N Account
User 1──N CreditCard
User 1──N Category
User 1──N Transaction
User 1──N RecurringExpense
User 1──N Budget
User 1──N ImportJob

Account 1──N Transaction
CreditCard N──1 Account
Category 1──N Transaction
Category 1──N Merchant
Merchant 1──N Transaction

ImportJob 1──N TransactionRaw
Transaction 1──N Installment
Transaction N──1 Transaction (parent)
```

---

## API Completa

### Auth

```
POST   /api/v1/auth/register        # Crear cuenta
POST   /api/v1/auth/login           # Iniciar sesión
POST   /api/v1/auth/refresh         # Refrescar JWT
POST   /api/v1/auth/logout          # Invalidar token (opcional)
```

**POST /auth/register**
```json
{
  "name": "string",
  "email": "string",
  "password": "string"
}
// → 201 { "id", "name", "email", "created_at" }
```

**POST /auth/login**
```json
{
  "email": "string",
  "password": "string"
}
// → 200 { "access_token", "refresh_token", "expires_in", "user": { ... } }
```

### Users

```
GET    /api/v1/users/me              # Perfil propio
PATCH  /api/v1/users/me              # Actualizar perfil
```

### Accounts

```
GET    /api/v1/accounts              # Listar cuentas
POST   /api/v1/accounts              # Crear cuenta
GET    /api/v1/accounts/{id}         # Detalle cuenta
PATCH  /api/v1/accounts/{id}         # Actualizar cuenta
DELETE /api/v1/accounts/{id}         # Eliminar cuenta
```

**POST /accounts**
```json
{
  "name": "Santander",
  "type": "CHECKING",
  "currency": "ARS",
  "balance": 0
}
```

### Credit Cards

```
GET    /api/v1/cards                 # Listar tarjetas
POST   /api/v1/cards                 # Crear tarjeta
GET    /api/v1/cards/{id}            # Detalle tarjeta
PATCH  /api/v1/cards/{id}            # Actualizar tarjeta
DELETE /api/v1/cards/{id}            # Eliminar tarjeta

GET    /api/v1/cards/{id}/installments   # Cuotas pendientes
```

### Categories

```
GET    /api/v1/categories            # Listar categorías (filtro: ?type=EXPENSE)
POST   /api/v1/categories            # Crear categoría personalizada
PATCH  /api/v1/categories/{id}       # Actualizar
DELETE /api/v1/categories/{id}       # Eliminar
```

### Merchants

```
GET    /api/v1/merchants             # Listar comercios
POST   /api/v1/merchants             # Crear comercio
PATCH  /api/v1/merchants/{id}        # Actualizar (ej: reasignar categoría)
```

### Transactions

```
GET    /api/v1/transactions          # Listar (filtros: ?account_id, ?category_id, ?type, ?from, ?to, ?page, ?size)
POST   /api/v1/transactions          # Crear manual
GET    /api/v1/transactions/{id}     # Detalle
PATCH  /api/v1/transactions/{id}     # Actualizar (recategorizar)
DELETE /api/v1/transactions/{id}     # Eliminar
```

**GET /transamples?**
```
?type=EXPENSE
&account_id=<uuid>
&category_id=<uuid>
&from=2025-01-01
&to=2025-12-31
&merchant_id=<uuid>
&is_manual=true
&is_recurring=true
&page=0
&size=50
&sort=date,desc
```

**POST /transactions** (manual)
```json
{
  "account_id": "uuid",
  "category_id": "uuid",
  "merchant_id": "uuid|null",
  "amount": -15000.00,
  "description": "Cena en Mostaza",
  "date": "2025-01-15",
  "type": "EXPENSE"
}
```

### Budgets

```
GET    /api/v1/budgets               # Listar presupuestos
POST   /api/v1/budgets               # Crear presupuesto
PATCH  /api/v1/budgets/{id}          # Actualizar
DELETE /api/v1/budgets/{id}          # Eliminar

GET    /api/v1/budgets/summary       # Resumen con consumo vs presupuesto
```

**POST /budgets**
```json
{
  "category_id": "uuid",
  "amount": 50000.00,
  "period": "MONTHLY",
  "start_date": "2025-01-01"
}
```

### Recurring Expenses

```
GET    /api/v1/recurring-expenses    # Listar
POST   /api/v1/recurring-expenses    # Crear
PATCH  /api/v1/recurring-expenses/{id}
DELETE /api/v1/recurring-expenses/{id}
```

### Imports

```
POST   /api/v1/imports/upload        # Subir archivo CSV/Excel
GET    /api/v1/imports               # Historial de importaciones
GET    /api/v1/imports/{id}          # Detalle de una importación
GET    /api/v1/imports/{id}/errors   # Filas con error
```

**POST /imports/upload**
```
Multipart form:
  file: File (CSV o Excel)
  source_type: "CSV_BANK" | "CSV_CARD" | "EXCEL_BANK"

→ 202 { "import_job_id", "status": "PENDING", "total_rows": 150 }
```

### Dashboard

```
GET    /api/v1/dashboard             # Resumen general
GET    /api/v1/dashboard/expenses-by-category   # Gastos por categoría (?from, ?to)
GET    /api/v1/dashboard/expenses-by-merchant   # Gastos por comercio (?from, ?to)
GET    /api/v1/dashboard/monthly-evolution      # Evolución mensual (?months=6)
GET    /api/v1/dashboard/projection             # Proyección financiera
GET    /api/v1/dashboard/upcoming-installments  # Próximas cuotas
GET    /api/v1/dashboard/insights               # Insights automáticos (Fase 1)
```

### Chat IA (Fase 2)

```
POST   /api/v1/chat                  # Pregunta al asistente financiero
```

---

## Pipeline de Importación

### Formato CSV Bancario Esperado
```csv
fecha,descripcion,importe,tipo
01/01/2025,YPF SAN MARTIN,-15000.00,DEBITO
02/01/2025,COBRO DE HABERES,450000.00,CREDITO
```

### Formato CSV Tarjeta
```csv
fecha,comercio,cuota,monto_total,monto_cuota
05/01/2025,FALABELLA,3/6,120000.00,20000.00
```

### Flujo

```
1. Upload → ImportJob (PENDING)
2. Parse → TransactionRaw (PARSED / ERROR)
3. Normalize → Match Merchant (por normalized_name)
4. Classify → Asignar categoría vía merchant->category
5. Import → Crear Transaction
6. Post-process → Detectar cuotas, gastos recurrentes
7. Complete → ImportJob (COMPLETED / PARTIAL / FAILED)
```

### Manejo de duplicados
- Se calcula SHA-256 del archivo en el upload
- Si el hash ya existe para el mismo usuario, se rechaza con 409

---

## Reglas de Clasificación (sin IA)

### Normalización de comercios
```text
Input                    →  Normalized
YPF SAN MARTIN           →  YPF
YPF 1234                 →  YPF
ESTACION DE SERVICIO YPF →  YPF
SHELL                          →  SHELL
COTO SAN JUSTO                →  COTO
JUMBO                        →  JUMBO
NETFLIX.COM                   →  NETFLIX
DISNEY+                       →  DISNEY_PLUS
MERCADO PAGO / MERCADOPAGO*  →  MERCADO_PAGO
```

### Reglas merchant → categoría
```text
YPF, SHELL, AXION → Combustible
COTO, JUMBO, DISCO, DIA, CHANGOMAS → Supermercado
NETFLIX, DISNEY_PLUS, SPOTIFY, HBO, AMAZON_PRIME → Streaming
MOSTAZA, MCDONALDS, BURGER_KING → Comida rápida
FALABELLA, FRÁVEGA, MUSIMUNDO → Indumentaria
```

### Reglas por descripción (keywords)
```text
"ALQUILER", "expensa" → Alquiler
"COLEGIO", "CUOTA COLEGIO" → Educación
"OSDE", "SWISS MEDICAL", "PREPAGA" → Salud
"SEGURO", "SEGUROS" → Seguros
"PATENTE", "RENTAS" → Patente
"PAGO DE HABERES", "SUELDO", "SALARIO" → Sueldo (ingreso)
"AGUINALDO" → Aguinaldo (ingreso)
```

---

## Frontend - Estructura de Rutas

```text
/login
/register

/dashboard                  → Resumen general + gráficos
  /transactions              → Listado de movimientos
  /accounts                  → Mis cuentas
  /cards                     → Mis tarjetas
    /cards/:id/installments  → Cuotas de una tarjeta
  /budgets                   → Presupuestos
  /recurring                 → Gastos recurrentes
  /imports                   → Importaciones
    /imports/new             → Subir archivo
    /imports/:id             → Detalle importación
  /categories                → Categorías
  /settings                  → Configuración
```

### Componentes globales
- `Layout` (AppBar + Sidebar/ BottomNav)
- `ProtectedRoute` (redirige a /login si no hay token)
- `LoadingScreen`, `ErrorBoundary`

### Estado global (Zustand)
```typescript
interface AuthStore {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

interface UiStore {
  sidebarOpen: boolean;
  theme: 'light' | 'dark';
  currency: 'ARS' | 'USD';
}
```

### Librerías externas
- `@mui/material` + `@mui/icons-material` + `@emotion/react`
- `react-router-dom` v6
- `zustand`
- `recharts` (gráficos)
- `@tanstack/react-query` (data fetching / cache)
- `axios` (HTTP)
- `date-fns` (fechas)
- `react-dropzone` (upload)

---

## Seguridad

### JWT
- Access token: 24h de expiración
- Refresh token: 7 días, almacenado en `httpOnly` cookie o DB
- Claims: `sub` (user_id), `email`, `name`, `iat`, `exp`

### CORS
- Desarrollo: permitir `http://localhost:5173` (Vite)
- Producción: dominio del frontend

### Rate Limiting
- Login: 5 intentos por minuto por IP
- API general: 100 requests por minuto por usuario

### Perfiles
- `dev`: PostgreSQL local, logging DEBUG, CORS abierto
- `prod`: PostgreSQL en Docker, logging INFO, CORS restringido

---

## Infraestructura

### Docker Compose (desarrollo)
```yaml
services:
  postgres:
    image: postgres:16-alpine
    ports: ["5432:5432"]
    volumes: [postgres_data, ./init.sql:/docker-entrypoint-initdb.d/init.sql]

  backend:
    build: ./backend
    ports: ["8080:8080"]
    depends_on: [postgres]
    environment:
      SPRING_PROFILES_ACTIVE: dev

  frontend:
    build: ./frontend
    ports: ["5173:80"]
    depends_on: [backend]
```

### Scripts útiles
```bash
# Desarrollo backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Desarrollo frontend
npm run dev

# Todo junto
docker compose up --build
```

---

## Testing

### Backend
- **Unitarios:** JUnit 5 + Mockito (servicios)
- **Integración:** Spring Boot Test + Testcontainers (repositorios, controladores)
- Cobertura mínima: 80% en servicios

### Frontend
- **Unitarios:** Vitest + React Testing Library
- **E2E:** Playwright (futuro)

---

## V1 - Alcance concreto

### Backend (V1)
- [x] Auth: register, login, JWT
- [x] CRUD Accounts
- [x] CRUD CreditCards
- [x] CRUD Categories (seed globales)
- [x] CRUD Transactions (manual)
- [ ] Importación CSV bancario
- [ ] Importación CSV tarjeta
- [ ] Normalización de comercios
- [ ] Clasificación automática
- [ ] Dashboard endpoints
- [ ] Flyway migrations

### Frontend (V1)
- [ ] Login / Register
- [ ] Dashboard con gráficos
- [ ] Listado de transacciones con filtros
- [ ] ABM de cuentas
- [ ] ABM de tarjetas
- [ ] Subida de archivos CSV
- [ ] Historial de importaciones
- [ ] Modo oscuro / claro

---

## Convenciones

### Backend
- Paquete base: `com.finance.app`
- Arquitectura: Controller → Service → Repository
- DTOs para request/response (no exponer entidades)
- Mappers manuales o MapStruct
- Excepciones: `ResourceNotFoundException`, `BadRequestException`, `DuplicateResourceException`
- Manejo centralizado de errores con `@RestControllerAdvice`
- IDs UUID v4

### Frontend
- Naming: camelCase variables, PascalCase componentes
- Each page in `src/pages/{nombre}/`
- Each component in `src/components/{nombre}/`
- Hooks personalizados en `src/hooks/`
- Servicios API en `src/services/`
- Tipos compartidos en `src/types/`

---

## Proyecto en disco

```text
agente/
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/finance/app/
│       ├── FinanceApplication.java
│       ├── config/          (Security, CORS, JWT, etc.)
│       ├── auth/            (AuthController, AuthService, JwtTokenProvider)
│       ├── user/            (UserController, UserService, UserRepository)
│       ├── account/         (AccountController, AccountService, AccountRepository)
│       ├── card/            (CreditCardController, CardService, CardRepository)
│       ├── category/        (CategoryController, CategoryService, CategoryRepository)
│       ├── merchant/        (MerchantController, MerchantService, MerchantRepository)
│       ├── transaction/     (TransactionController, TransactionService, TransactionRepository)
│       ├── installment/     (InstallmentService, InstallmentRepository)
│       ├── budget/          (BudgetController, BudgetService, BudgetRepository)
│       ├── recurring/       (RecurringService, RecurringRepository)
│       ├── importation/     (ImportController, ImportService, parsers, normalizer, classifier)
│       ├── dashboard/       (DashboardController, DashboardService)
│       ├── insight/         (InsightService - Fase 1)
│       ├── chat/            (ChatController, ChatService - Fase 2)
│       ├── common/
│       │   ├── exception/
│       │   ├── dto/
│       │   └── entity/      (BaseEntity)
│       └── infrastructure/
│           └── persistence/ (Audit, etc.)
│
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── Dockerfile
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       ├── routes.tsx
│       ├── theme.ts
│       ├── store/           (authStore, uiStore)
│       ├── services/        (api.ts, authService, etc.)
│       ├── types/            (user.ts, account.ts, transaction.ts, etc.)
│       ├── pages/
│       │   ├── LoginPage.tsx
│       │   ├── RegisterPage.tsx
│       │   ├── DashboardPage.tsx
│       │   ├── TransactionsPage.tsx
│       │   ├── AccountsPage.tsx
│       │   ├── CardsPage.tsx
│       │   ├── BudgetsPage.tsx
│       │   ├── RecurringPage.tsx
│       │   ├── ImportsPage.tsx
│       │   ├── CategoriesPage.tsx
│       │   └── SettingsPage.tsx
│       ├── components/      (Layout, Chart, Table, Filters, etc.)
│       └── hooks/           (useTransactions, useAccounts, etc.)
│
├── docker-compose.yml
├── .env
├── .env.example
├── init.sql
├── .gitignore
└── Documentacion/
    ├── arquitectura_plataforma_financiera.md
    └── desarrollo.md
```
