# Plataforma de Inteligencia Financiera Personal

## Objetivo

Construir una aplicación web con proyección a aplicación móvil, enfocada en la importación automática de movimientos financieros, análisis de gastos e ingresos, y asistencia financiera mediante IA.

---

## Visión General

```text
Banco / Tarjeta / Mercado Pago
            │
            ▼
      Importador
            │
            ▼
 Normalización de datos
            │
            ▼
 Clasificación automática
            │
            ▼
 Motor financiero
            │
      ┌─────┴─────┐
      ▼           ▼
 Dashboard      IA
```

La carga manual existirá como alternativa, pero el foco principal será la importación automática de información financiera.

---

## Arquitectura Técnica

### Frontend Web

- React
- TypeScript
- Material UI

### Backend

- Java 21
- Spring Boot
- Spring Security
- JWT
- JPA / Hibernate
- Flyway

### Base de Datos

- PostgreSQL

### Infraestructura

- Docker

### Mobile (Futuro)

- React Native

---

## Arquitectura General

```text
┌─────────────────┐
│ React Web       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Spring Boot API │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ PostgreSQL      │
└─────────────────┘
```

Futuro:

```text
┌─────────────────┐
│ React Web       │
└────────┬────────┘
         │
┌────────▼────────┐
│ Spring Boot API │
└────────┬────────┘
         │
 ┌───────┴────────┐
 │                │
 ▼                ▼
React Native   PostgreSQL
```

---

## Organización del Backend

```text
finance-app
│
├── auth
├── users
├── accounts
├── cards
├── transactions
├── budgets
├── reports
├── imports
└── notifications
```

Monolito modular.

---

## Capas de Negocio

### 1. Ingesta

Fuentes iniciales:

- CSV bancarios
- Excel bancarios
- CSV de tarjetas

Fuentes futuras:

- PDF
- Emails
- APIs bancarias
- Mercado Pago

Interfaz:

```java
public interface ImportProvider {
    List<TransactionDTO> importFile(File file);
}
```

---

### 2. Normalización

Ejemplos:

```text
YPF SAN MARTIN
YPF 1234
EST YPF
```

Resultado:

```json
{
  "merchant": "YPF"
}
```

---

### 3. Clasificación

Reglas automáticas:

```text
YPF -> Combustible
SHELL -> Combustible

COTO -> Supermercado
JUMBO -> Supermercado

NETFLIX -> Streaming
```

Objetivo: lograr alta precisión sin IA.

---

### 4. Motor Financiero

Servicios:

```text
ExpenseAnalysisService

IncomeAnalysisService

BudgetService

ProjectionService
```

Responsabilidades:

- Saldo actual
- Gastos por categoría
- Gastos por comercio
- Evolución mensual
- Cuotas pendientes
- Proyección financiera

---

## Modelo de Datos Inicial

### User

```text
id
name
email
password_hash
created_at
```

### Account

```text
id
user_id
name
type
balance
```

Ejemplos:

- Santander
- Mercado Pago
- Efectivo

### CreditCard

```text
id
user_id
name
closing_day
due_day
```

### Category

```text
id
user_id
name
type
```

Tipos:

- INCOME
- EXPENSE

### Transaction

```text
id
account_id
category_id
amount
description
date
type
```

Tipos:

- INCOME
- EXPENSE
- TRANSFER

---

## Entidades Adicionales

### Merchant

```text
id
name
normalized_name
```

### ImportJob

```text
id
user_id
source_type
file_name
import_date
status
```

### TransactionRaw

```text
id
import_job_id
original_description
original_amount
original_date
```

### Installment

```text
id
transaction_id
current_installment
total_installments
```

### RecurringExpense

Representa gastos recurrentes:

- Alquiler
- Colegio
- Internet
- Seguro
- Patente
- Prepaga
- Cuota de vehículo

---

## API Inicial

```http
POST /auth/login

POST /transactions

GET /transactions

GET /dashboard

POST /accounts

POST /budgets
```

---

## Seguridad

JWT simple.

```text
Frontend
    ↓
JWT
    ↓
Backend
```

Sin OAuth en la primera versión.

---

## IA - Fase 1

Insights automáticos sin LLM.

Ejemplos:

- Gastaste 18% más en combustible que el promedio de los últimos tres meses.
- Tu gasto en restaurantes aumentó $43.000 respecto al mes anterior.
- Si mantienes este ritmo de gasto, cerrarás el mes con un saldo estimado de $380.000.

---

## IA - Fase 2

Chat financiero.

Proceso:

1. El motor financiero calcula métricas.
2. Se construye un contexto estructurado.
3. El LLM genera explicaciones.

Ejemplo:

Pregunta:

> ¿Por qué gasté más este mes?

Contexto:

```json
{
  "gasto_mes_actual": 1200000,
  "gasto_mes_anterior": 980000,
  "mayores_variaciones": [
    {
      "categoria": "Combustible",
      "incremento": 80000
    },
    {
      "categoria": "Restaurantes",
      "incremento": 60000
    }
  ]
}
```

El modelo responde utilizando únicamente información validada.

---

## Roadmap

### V1

- Login
- Registro
- PostgreSQL
- Docker
- Importación CSV
- Clasificación automática
- Dashboard
- Gráficos

### V2

- Presupuestos
- Cuotas
- Gastos recurrentes

### V3

- Insights automáticos

### V4

- Chat financiero con IA

### V5

- Aplicación móvil React Native

---

## Diferencial para Argentina

Soporte nativo para:

- Cuotas
- Tarjetas de crédito
- Aguinaldo
- Bonos salariales
- Impuesto a las ganancias
- Alquiler
- Educación
- Gastos recurrentes

Objetivo: ofrecer una herramienta adaptada a la realidad financiera argentina.
