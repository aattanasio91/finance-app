# Especificaciones Técnicas - Paso a Paso

## Fase 0: Infraestructura Base

### 0.1 PostgreSQL + Docker Compose
```
docker-compose.yml
init.sql
.env / .env.example
```

### 0.2 Backend - Proyecto Spring Boot
```bash
# Inicializar con Spring Initializr manual o con Maven
java 21
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-validation
flyway-core + flyway-database-postgresql
jjwt-api + jjwt-impl + jjwt-jackson (0.12.x)
lombok
mapstruct o manual mappers
```

**Estructura de paquetes:**
```
com.finance.app
├── FinanceApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   ├── JwtConfig.java
│   ├── WebConfig.java
│   └── OpenApiConfig.java
├── common/
│   ├── entity/BaseEntity.java
│   ├── exception/
│   │   ├── ResourceNotFoundException.java
│   │   ├── BadRequestException.java
│   │   ├── DuplicateResourceException.java
│   │   └── GlobalExceptionHandler.java
│   └── dto/
│       ├── ApiResponse.java
│       └── PagedResponse.java
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── dto/LoginRequest.java
│   ├── dto/LoginResponse.java
│   ├── dto/RegisterRequest.java
│   └── jwt/JwtTokenProvider.java
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserRepository.java
│   ├── User.java
│   └── dto/UserResponse.java
├── account/
│   ├── AccountController.java
│   ├── AccountService.java
│   ├── AccountRepository.java
│   ├── Account.java
│   ├── dto/CreateAccountRequest.java
│   └── dto/AccountResponse.java
├── card/
│   ├── CreditCardController.java
│   ├── CreditCardService.java
│   ├── CreditCardRepository.java
│   ├── CreditCard.java
│   ├── dto/CreateCardRequest.java
│   └── dto/CardResponse.java
├── category/
│   ├── CategoryController.java
│   ├── CategoryService.java
│   ├── CategoryRepository.java
│   ├── Category.java
│   ├── dto/CreateCategoryRequest.java
│   └── dto/CategoryResponse.java
├── merchant/
│   ├── MerchantController.java
│   ├── MerchantService.java
│   ├── MerchantRepository.java
│   ├── Merchant.java
│   └── dto/MerchantResponse.java
├── transaction/
│   ├── TransactionController.java
│   ├── TransactionService.java
│   ├── TransactionRepository.java
│   ├── Transaction.java
│   ├── dto/CreateTransactionRequest.java
│   ├── dto/TransactionResponse.java
│   └── spec/TransactionSpecification.java
├── installment/
│   ├── InstallmentService.java
│   ├── InstallmentRepository.java
│   └── Installment.java
├── budget/
│   ├── BudgetController.java
│   ├── BudgetService.java
│   ├── BudgetRepository.java
│   ├── Budget.java
│   └── dto/CreateBudgetRequest.java
├── recurring/
│   ├── RecurringExpenseController.java
│   ├── RecurringExpenseService.java
│   ├── RecurringExpenseRepository.java
│   └── RecurringExpense.java
├── importation/
│   ├── ImportController.java
│   ├── ImportService.java
│   ├── ImportJobRepository.java
│   ├── TransactionRawRepository.java
│   ├── ImportJob.java
│   ├── TransactionRaw.java
│   ├── parser/
│   │   ├── CsvBankParser.java
│   │   └── CsvCardParser.java
│   ├── normalizer/
│   │   └── MerchantNormalizer.java
│   └── classifier/
│       └── TransactionClassifier.java
├── dashboard/
│   ├── DashboardController.java
│   └── DashboardService.java
├── insight/
│   ├── InsightService.java
│   └── dto/InsightDto.java
└── chat/
    ├── ChatController.java
    └── ChatService.java
```

### 0.3 Base de Datos - Migraciones Flyway

**V1__create_users_table.sql**
```sql
CREATE TABLE users (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    default_currency VARCHAR(3) DEFAULT 'ARS',
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX idx_users_email ON users(email);
```

**V2__create_accounts_table.sql**
```sql
CREATE TABLE accounts (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('CHECKING','SAVINGS','CREDIT_CARD','CASH','INVESTMENT')),
    currency VARCHAR(3) NOT NULL DEFAULT 'ARS',
    balance DECIMAL(15,2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(user_id, name)
);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
```

**V3__create_credit_cards_table.sql**
```sql
CREATE TABLE credit_cards (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    account_id UUID NOT NULL REFERENCES accounts(id),
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(50) NOT NULL CHECK (brand IN ('VISA','MASTERCARD','AMEX','NARANJA','CABAL')),
    closing_day INT NOT NULL CHECK (closing_day BETWEEN 1 AND 31),
    due_day INT NOT NULL CHECK (due_day BETWEEN 1 AND 31),
    credit_limit DECIMAL(15,2),
    color_hex VARCHAR(7),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX idx_credit_cards_user_id ON credit_cards(user_id);
```

**V4__create_categories_table.sql**
```sql
CREATE TABLE categories (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
    icon VARCHAR(50),
    color_hex VARCHAR(7),
    is_system BOOLEAN DEFAULT false,
    UNIQUE(user_id, name)
);
CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_type ON categories(type);
```

**V5__seed_default_categories.sql**
```sql
INSERT INTO categories (name, type, is_system) VALUES
('Sueldo', 'INCOME', true),
('Freelance', 'INCOME', true),
('Inversiones', 'INCOME', true),
('Aguinaldo', 'INCOME', true),
('Bonos', 'INCOME', true),
('Combustible', 'EXPENSE', true),
('Supermercado', 'EXPENSE', true),
('Streaming', 'EXPENSE', true),
('Restaurantes', 'EXPENSE', true),
('Alquiler', 'EXPENSE', true),
('Servicios', 'EXPENSE', true),
('Educación', 'EXPENSE', true),
('Salud', 'EXPENSE', true),
('Transporte', 'EXPENSE', true),
('Indumentaria', 'EXPENSE', true),
('Entretenimiento', 'EXPENSE', true),
('Internet', 'EXPENSE', true),
('Seguros', 'EXPENSE', true),
('Patente', 'EXPENSE', true);
```

**V6__create_merchants_table.sql**
```sql
CREATE TABLE merchants (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(255) NOT NULL UNIQUE,
    category_id UUID REFERENCES categories(id),
    is_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_merchants_normalized_name ON merchants(normalized_name);
CREATE INDEX idx_merchants_category_id ON merchants(category_id);
```

**V7__create_transactions_table.sql**
```sql
CREATE TABLE transactions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    account_id UUID NOT NULL REFERENCES accounts(id),
    category_id UUID REFERENCES categories(id),
    merchant_id UUID REFERENCES merchants(id),
    import_job_id UUID,
    parent_transaction_id UUID REFERENCES transactions(id),
    amount DECIMAL(15,2) NOT NULL,
    original_amount DECIMAL(15,2),
    description VARCHAR(500) NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME','EXPENSE','TRANSFER')),
    currency VARCHAR(3) NOT NULL DEFAULT 'ARS',
    is_manual BOOLEAN DEFAULT false,
    is_recurring BOOLEAN DEFAULT false,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_transactions_date ON transactions(user_id, date);
CREATE INDEX idx_transactions_type ON transactions(type);
```

**V8__create_installments_table.sql**
```sql
CREATE TABLE installments (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    current_installment INT NOT NULL,
    total_installments INT NOT NULL,
    due_date DATE NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    is_paid BOOLEAN DEFAULT false,
    paid_date DATE,
    CHECK (current_installment <= total_installments)
);
CREATE INDEX idx_installments_transaction_id ON installments(transaction_id);
CREATE INDEX idx_installments_due_date ON installments(due_date);
```

**V9__create_recurring_expenses_table.sql**
```sql
CREATE TABLE recurring_expenses (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    category_id UUID REFERENCES categories(id),
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    day_of_month INT NOT NULL CHECK (day_of_month BETWEEN 1 AND 31),
    frequency VARCHAR(20) DEFAULT 'MONTHLY' CHECK (frequency IN ('MONTHLY','BIMONTHLY','QUARTERLY','YEARLY')),
    is_active BOOLEAN DEFAULT true,
    next_date DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_recurring_expenses_user_id ON recurring_expenses(user_id);
```

**V10__create_budgets_table.sql**
```sql
CREATE TABLE budgets (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    category_id UUID NOT NULL REFERENCES categories(id),
    amount DECIMAL(15,2) NOT NULL,
    period VARCHAR(20) NOT NULL DEFAULT 'MONTHLY' CHECK (period IN ('WEEKLY','MONTHLY','YEARLY','CUSTOM')),
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_budgets_category_id ON budgets(category_id);
```

**V11__create_import_jobs_table.sql**
```sql
CREATE TABLE import_jobs (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    source_type VARCHAR(50) NOT NULL CHECK (source_type IN ('CSV_BANK','CSV_CARD','EXCEL_BANK','MANUAL')),
    file_name VARCHAR(255) NOT NULL,
    file_hash VARCHAR(64),
    import_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED','PARTIAL')),
    total_rows INT DEFAULT 0,
    success_rows INT DEFAULT 0,
    error_rows INT DEFAULT 0,
    error_log TEXT
);
CREATE INDEX idx_import_jobs_user_id ON import_jobs(user_id);
```

**V12__create_transaction_raws_table.sql**
```sql
CREATE TABLE transaction_raws (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    import_job_id UUID NOT NULL REFERENCES import_jobs(id),
    original_description VARCHAR(500) NOT NULL,
    original_amount VARCHAR(100),
    original_date VARCHAR(100),
    raw_data JSONB,
    parsed_amount DECIMAL(15,2),
    parsed_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','PARSED','ERROR','DUPLICATE','IMPORTED')),
    error_message TEXT
);
CREATE INDEX idx_transaction_raws_import_job_id ON transaction_raws(import_job_id);
```

---

## Paso 1: Auth + Seguridad

### Backend
**Endpoint:** `/api/v1/auth`

| Método | Ruta | Descripción |
|---|---|---|
| POST | /api/v1/auth/register | Crear cuenta |
| POST | /api/v1/auth/login | Iniciar sesión |
| POST | /api/v1/auth/refresh | Refrescar token |

**Config Security:**
- Filtro JWT que intercepta todas las rutas excepto `/api/v1/auth/**`
- PasswordEncoder con BCrypt
- CORS permitiendo localhost:5173
- Manejo de excepciones: 401 si token inválido/expirado

**JWT Claims:**
```json
{
  "sub": "user-uuid",
  "email": "user@email.com",
  "name": "User Name",
  "iat": 1234567890,
  "exp": 1234567890
}
```

### Frontend
- Página `/login` con formulario email + password
- Página `/register` con formulario name + email + password
- Almacenar token en localStorage / memory
- Zustand store: `authStore` con user, token, login(), logout()
- Axios interceptor para adjuntar Bearer token
- Axios interceptor para redirigir a /login en 401

### Database
- Migration V1 ya creada
- Seed de categorías en V5

---

## Paso 2: CRUD Categorías + Merchants

### Backend
**Endpoint:** `/api/v1/categories`

| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/v1/categories | Listar (filtro ?type=INCOME\|EXPENSE) |
| POST | /api/v1/categories | Crear personalizada |
| PATCH | /api/v1/categories/{id} | Actualizar |
| DELETE | /api/v1/categories/{id} | Eliminar (si no es system) |

**Endpoint:** `/api/v1/merchants`

| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/v1/merchants | Listar |
| POST | /api/v1/merchants | Crear |
| PATCH | /api/v1/merchants/{id} | Asignar categoría |

**Reglas:**
- Seed de categorías del sistema en el registro del usuario
- Categorías system no se pueden eliminar ni modificar
- Merchant tiene normalized_name único global

### Frontend
- Página `/categories` con lista + modal crear/editar
- Página `/settings/categories` integrada

### Database
- V4, V5, V6

---

## Paso 3: CRUD Accounts + CreditCards

### Backend
**Endpoint:** `/api/v1/accounts`

| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/v1/accounts | Listar cuentas del usuario |
| POST | /api/v1/accounts | Crear cuenta |
| GET | /api/v1/accounts/{id} | Detalle |
| PATCH | /api/v1/accounts/{id} | Actualizar |
| DELETE | /api/v1/accounts/{id} | Eliminar (lógico) |

**Endpoint:** `/api/v1/cards`

| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/v1/cards | Listar tarjetas |
| POST | /api/v1/cards | Crear tarjeta |
| GET | /api/v1/cards/{id} | Detalle |
| PATCH | /api/v1/cards/{id} | Actualizar |
| DELETE | /api/v1/cards/{id} | Eliminar |

**Reglas:**
- Al registrarse se crea cuenta "Efectivo" por defecto
- No eliminar cuenta con transacciones
- closing_day y due_day entre 1-31

### Frontend
- Página `/accounts` con lista + modal crear/editar
- Página `/cards` con lista + modal crear/editar
- Indicador visual de saldo por cuenta

### Database
- V2, V3

---

## Paso 4: CRUD Transactions (Manual)

### Backend
**Endpoint:** `/api/v1/transactions`

| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/v1/transactions | Listar con filtros |
| POST | /api/v1/transactions | Crear manual |
| GET | /api/v1/transactions/{id} | Detalle |
| PATCH | /api/v1/transactions/{id} | Recategorizar |
| DELETE | /api/v1/transactions/{id} | Eliminar |

**Filtros GET:** `account_id`, `category_id`, `type`, `merchant_id`, `from`, `to`, `is_manual`, `is_recurring`, `page`, `size`, `sort`

**Reglas:**
- INCOME: amount positivo, incrementa saldo cuenta
- EXPENSE: amount negativo, decrementa saldo cuenta
- TRANSFER: dos transacciones vinculadas por parent_transaction_id
- No modificar monto, cuenta, fecha ni tipo después de creada
- Detección de duplicados por amount + description + date + account_id

### Frontend
- Página `/transactions` con tabla paginada + filtros
- Filtros: rango de fechas, tipo, cuenta, categoría
- Modal crear transacción manual
- Acción de recategorizar (editar categoría inline)
- Badge de color por tipo (verde INCOME, rojo EXPENSE, azul TRANSFER)

### Database
- V7

---

## Paso 5: Importación CSV

### Backend
**Endpoint:** `/api/v1/imports`

| Método | Ruta | Descripción |
|---|---|---|
| POST | /api/v1/imports/upload | Subir archivo |
| GET | /api/v1/imports | Historial |
| GET | /api/v1/imports/{id} | Detalle |
| GET | /api/v1/imports/{id}/errors | Errores |

**Pipeline:**
```
Upload (MultipartFile)
  → Crear ImportJob (PENDING)
  → Calcular SHA-256 (rechazar si duplicado)
  → Parsear CSV fila por fila → TransactionRaw
  → Normalizar merchant (buscar/crear Merchant)
  → Clasificar (merchant.category_id)
  → Crear Transaction (con merchant asignado)
  → Detectar cuotas (si aplica)
  → Detectar recurrentes (si aplica)
  → Actualizar ImportJob status
```

**Normalizer:**
```java
// Reglas de normalización
"YPF SAN MARTIN" -> "YPF"
"YPF 1234" -> "YPF"
"EST YPF" -> "YPF"
"COTO SAN JUSTO" -> "COTO"
// lookup en tabla merchants, si no existe crear
// usar similaridad o reglas fijas
```

**Classifier:**
```java
// Reglas merchant -> categoría
YPF -> Combustible
SHELL -> Combustible
COTO -> Supermercado
NETFLIX -> Streaming
// lookup en merchants.category_id
// si no hay categoría, queda null (pendiente)
```

### Frontend
- Página `/imports` con historial
- Página `/imports/new` con drag & drop (react-dropzone)
- Progreso de importación
- Detalle con filas exitosas/fallidas
- Botón para descargar errores

### Database
- V11, V12

---

## Paso 6: Dashboard + Gráficos

### Backend
**Endpoint:** `/api/v1/dashboard`

| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/v1/dashboard | Resumen general |
| GET | /api/v1/dashboard/expenses-by-category | Gastos por categoría |
| GET | /api/v1/dashboard/expenses-by-merchant | Gastos por comercio |
| GET | /api/v1/dashboard/monthly-evolution | Evolución mensual |
| GET | /api/v1/dashboard/projection | Proyección |
| GET | /api/v1/dashboard/upcoming-installments | Próximas cuotas |

**Dashboard Response:**
```json
{
  "current_balance": 450000.00,
  "month_income": 500000.00,
  "month_expenses": 320000.00,
  "balance_by_account": [
    { "account_id": "uuid", "name": "Santander", "balance": 350000 }
  ],
  "expenses_by_category": [
    { "category": "Combustible", "amount": 45000, "percentage": 14.06 }
  ],
  "monthly_evolution": [
    { "month": "2026-01", "income": 500000, "expenses": 320000 }
  ],
  "projection": {
    "estimated_end_balance": 380000,
    "status": "POSITIVE" | "NEGATIVE" | "CAUTION"
  }
}
```

### Frontend
- Página `/dashboard` como home post-login
- Widgets:
  - Saldo actual (grande, arriba)
  - Ingresos vs Gastos del mes (donut chart)
  - Gastos por categoría (bar chart horizontal)
  - Evolución mensual (line chart)
  - Próximas cuotas (lista)
  - Proyección fin de mes (alerta color)
- Recharts para gráficos

---

## Paso 7: Presupuestos

### Backend
**Endpoint:** `/api/v1/budgets`

| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/v1/budgets | Listar |
| POST | /api/v1/budgets | Crear |
| PATCH | /api/v1/budgets/{id} | Actualizar |
| DELETE | /api/v1/budgets/{id} | Eliminar |
| GET | /api/v1/budgets/summary | Consumo vs presupuesto |

**Budget Summary:**
```json
[
  {
    "category": "Combustible",
    "budget_amount": 50000.00,
    "spent": 35000.00,
    "percentage": 70,
    "status": "OK" | "WARNING" | "EXCEEDED"
  }
]
```

### Frontend
- Página `/budgets`
- Lista de presupuestos con barra de progreso
- Alerta visual al 80% (amarillo) y 100% (rojo)
- Modal crear/editar con selector de categoría

### Database
- V10

---

## Paso 8: Gastos Recurrentes + Cuotas

### Backend
**Endpoint:** `/api/v1/recurring-expenses`

| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/v1/recurring-expenses | Listar |
| POST | /api/v1/recurring-expenses | Crear |
| PATCH | /api/v1/recurring-expenses/{id} | Actualizar |
| DELETE | /api/v1/recurring-expenses/{id} | Eliminar |

**Endpoint:** `/api/v1/cards/{id}/installments`

| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/v1/cards/{id}/installments | Cuotas pendientes |

**Reglas:**
- Al importar CSV de tarjeta, detectar cuotas por columna "cuota" (ej: "3/6")
- Generar N filas de cuota al crear transacción con cuotas
- Cálculo: monto_total / total_cuotas, diferencia en primera cuota

### Frontend
- Página `/recurring` con lista de gastos recurrentes
- Página `/cards/:id/installments` con tabla de cuotas pendientes

### Database
- V8, V9

---

## Paso 9: Insights Automáticos (Fase 1 IA)

### Backend
**Service:** `InsightService.java`

**Reglas de insight ya definidas en reglas_de_negocio.md (RN-039 a RN-043):**

| Condición | Mensaje |
|---|---|
| Gasto categoría > mes anterior * 1.10 | "Gastaste X% más que el mes pasado en [categoría]" |
| Gasto categoría < mes anterior * 0.90 | "Gastaste X% menos que el mes pasado en [categoría]" |
| Gasto categoría > promedio 3 meses * 1.20 | "Tu gasto en [categoría] está un X% por encima del promedio" |
| Consumo > presupuesto | "Superaste el presupuesto de [categoría] por $X" |
| Proyección negativa | "Si mantenés este ritmo, cerrarás con saldo negativo de $X" |
| Gasto individual > 30% del total mensual | "Este gasto representa el X% de tus gastos del mes" |

**Endpoint:** `/api/v1/dashboard/insights`

### Frontend
- Sección de insights en el Dashboard
- Cards con iconos por tipo (alerta, warning, info, success)
- Animación de aparición

---

## Paso 10: Chat Financiero (Fase 2 IA - Futuro)

### Backend
**Endpoint:** `/api/v1/chat`

**Flujo:**
1. User envía pregunta
2. Backend calcula contexto financiero relevante
3. Construye prompt con contexto estructurado
4. Envía a LLM (OpenAI / Anthropic / local)
5. Devuelve respuesta al usuario

**Contexto incluido en cada request:**
```json
{
  "user_context": {
    "current_balance": 450000,
    "month_income": 500000,
    "month_expenses": 320000,
    "top_categories": [...],
    "recent_transactions": [...],
    "upcoming_bills": [...]
  },
  "question": "¿Por qué gasté más este mes?"
}
```

### Frontend
- Componente Chat flotante en el dashboard
- Input de texto + historial de mensajes
- Loading state mientras responde

---

## Paso 11: Frontend - Estructura Completa

### Rutas
```
/                          → redirect a /dashboard
/login                     → LoginPage
/register                  → RegisterPage
/dashboard                 → DashboardPage (protegida)
/transactions              → TransactionsPage
/transactions/new          → TransactionFormPage
/accounts                  → AccountsPage
/accounts/new              → AccountFormPage
/cards                     → CardsPage
/cards/new                 → CardFormPage
/cards/:id/installments    → InstallmentsPage
/budgets                   → BudgetsPage
/budgets/new               → BudgetFormPage
/recurring                 → RecurringPage
/imports                   → ImportsPage
/imports/new               → ImportUploadPage
/imports/:id               → ImportDetailPage
/categories                → CategoriesPage
/settings                  → SettingsPage
```

### Componentes Compartidos
```
src/components/
├── Layout/
│   ├── AppLayout.tsx        (AppBar + Sidebar + Outlet)
│   ├── Sidebar.tsx
│   └── MobileNav.tsx
├── ProtectedRoute.tsx
├── LoadingScreen.tsx
├── ErrorBoundary.tsx
├── PageHeader.tsx
├── ConfirmDialog.tsx
├── EmptyState.tsx
├── filters/
│   ├── DateRangeFilter.tsx
│   ├── TypeFilter.tsx
│   ├── AccountFilter.tsx
│   └── CategoryFilter.tsx
├── charts/
│   ├── DonutChart.tsx
│   ├── BarChart.tsx
│   └── LineChart.tsx
└── forms/
    ├── AccountForm.tsx
    ├── CardForm.tsx
    ├── CategoryForm.tsx
    ├── TransactionForm.tsx
    ├── BudgetForm.tsx
    └── RecurringForm.tsx
```

### Hooks
```
src/hooks/
├── useAuth.ts
├── useAccounts.ts
├── useCards.ts
├── useTransactions.ts
├── useCategories.ts
├── useMerchants.ts
├── useBudgets.ts
├── useRecurring.ts
├── useImports.ts
└── useDashboard.ts
```

### Servicios API
```
src/services/
├── api.ts                  (axios instance + interceptors)
├── authService.ts
├── accountService.ts
├── cardService.ts
├── categoryService.ts
├── merchantService.ts
├── transactionService.ts
├── budgetService.ts
├── recurringService.ts
├── importService.ts
└── dashboardService.ts
```

### Tipos
```
src/types/
├── user.ts
├── account.ts
├── card.ts
├── category.ts
├── merchant.ts
├── transaction.ts
├── installment.ts
├── budget.ts
├── recurring.ts
├── import.ts
├── dashboard.ts
└── api.ts                  (ApiResponse<T>, PagedResponse<T>)
```

### Tema / Estilos
- `theme.ts` con paleta clara y oscura
- Modo oscuro por sistema con toggle manual
- Colores: verde para ingresos, rojo para gastos, azul para transferencias

---

## Orden de Implementación Resumido

| Paso | Dependencias | Backend | Frontend |
|---|---|---|---|
| 0. Infra | - | Proyecto Spring Boot + Flyway | Proyecto React + Vite |
| 1. Auth | 0 | JWT, register, login, refresh | Login/Register pages |
| 2. Cat/Merchant | 1 | CRUD categorías + merchants | Categories page |
| 3. Accounts/Cards | 1 | CRUD cuentas + tarjetas | Accounts + Cards pages |
| 4. Transactions | 1,2,3 | CRUD transacciones manuales | Transactions page |
| 5. Import | 1,2,3,4 | Subida CSV + pipeline | Import pages |
| 6. Dashboard | 4,5 | Endpoints dashboard + insights | Dashboard page + charts |
| 7. Budgets | 2,4 | CRUD presupuestos + summary | Budgets page |
| 8. Recurring | 4 | CRUD recurrentes + cuotas | Recurring + Installments pages |
| 9. Insights | 6 | Servicio de insights automáticos | Insights en dashboard |
| 10. Chat | 6 | Chat con IA (futuro) | Chat flotante |
