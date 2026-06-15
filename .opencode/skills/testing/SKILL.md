---
name: testing
description: >
  Use when writing tests, designing test strategies, debugging failing tests,
  or discussing testing approaches. Covers unit tests, integration tests,
  TDD/BDD, mocks, fixtures, and test best practices.
---

# Testing

## Pirámide de testing

```
        /\
       /  \         E2E (pocos, lentos, costosos)
      /    \
     /------\       Integration (varios)
    /        \
   /----------\     Unit (muchos, rápidos, baratos)
```

- **Unit tests**: probá lógica aislada. Mockeá dependencias externas.
- **Integration tests**: probá interacción entre módulos (DB, API, servicios).
- **E2E tests**: probá flujos completos del usuario (los más valiosos pero más lentos).

## Qué testear

- **Casos felices**: el camino principal funciona.
- **Edge cases**: valores límite, empty state, null/undefined, tipos inesperados.
- **Errores**: validaciones, excepciones, timeouts, respuestas HTTP no exitosas.
- **Regresión**: bugs que ya se fixearon (no vuelven a aparecer).

## Estructura de un test (AAA)

```ts
// Arrange
const input = { name: "test", age: 25 };
const sut = new MyService();

// Act
const result = sut.process(input);

// Assert
expect(result).toEqual({ valid: true });
```

## Naming

- Nombre del test debe describir: `[método] debería [comportamiento] cuando [condición]`
- Ej: `createUser debería lanzar error cuando el email ya existe`

## Mocks y stubs

- Usá mocks solo para fronteras del sistema (IO, APIs, DB).
- No mockees lo que no te pertenece (librerías de terceros → wrappeá).
- Preferí fakes sobre mocks cuando sea posible.
- Verificá interacciones solo cuando sea necesario (no sobrescribas).

## Coverage

- No persigas 100% de coverage. Es métrica engañosa.
- Enfocate en cubrir lógica crítica y paths de error.
- Usá coverage como guía, no como objetivo.

## TDD (opcional pero recomendado)

1. Escribí el test que falla (Red)
2. Escribí el mínimo código para que pase (Green)
3. Refactor (Refactor)

## Estrategia por capa

| Capa         | Tipo de test         | Mockear             |
|------------- |----------------------|----------------------|
| UI/Component | Unit + Integration   | API calls, store     |
| Service/UseCase | Unit              | Repositorios, APIs   |
| Repository   | Integration (DB)     | Base de datos real/testcontainer |
| API/Endpoint | Integration + E2E    | Auth, servicios externos |
