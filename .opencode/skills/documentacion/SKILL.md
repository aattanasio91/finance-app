---
name: documentacion
description: >
  Use when writing or updating documentation, creating README files, adding
  JSDoc/TSDoc comments, or discussing documentation strategy. Covers
  documentation standards, API docs, README structure, and inline comments.
---

# Documentación

## Principios

- Documentá el *qué* y el *por qué*, no el *cómo* (el código ya dice cómo).
- La documentación debe ser mantenible: si no podés mantenerla, no la escribas.
- Una buena documentación reduce el tiempo de onboarding y el número de bugs.

## README

Estructura recomendada:

```markdown
# Nombre del proyecto

Breve descripción (1-2 párrafos).

## Requisitos

- Node.js 18+
- PostgreSQL 15+

## Instalación

```bash
git clone ...
npm install
cp .env.example .env
npm run dev
```

## Uso

Ejemplos básicos de uso.

## API (opcional)

Endpoints principales, formato request/response.

## Scripts

| Comando | Descripción |
|---------|-------------|
| dev     | Inicia en desarrollo |
| build   | Compila para producción |
| test    | Ejecuta tests |

## Contribuir

Guía breve: branches, PRs, coding standards.

## Licencia

MIT
```

## Comentarios en código

### Cuándo comentar

- **API pública**: JSDoc/TSDoc para funciones exportadas.
- **Lógica no obvia**: explicá *por qué* se hace algo inusual.
- **Trade-offs**: documentá decisiones y alternativas consideradas.
- **TODO, FIXME, HACK**: marcá deuda técnica con formato consistente.

### Cuándo NO comentar

- Código auto-explicativo: `// suma a + b` al lado de `return a + b`.
- Comentarios comentados (`// const x = ...`): borralos.

```ts
// Bueno
/** Calcula el interés compuesto. */
function calcularInteres(principal: number, tasa: number, periodos: number): number {
  return principal * Math.pow(1 + tasa, periodos);
}

// Malo (obvio)
// multiplica principal por (1 + tasa) elevado a periodos
```

### TSDoc/JSDoc

```ts
/**
 * Crea un usuario en el sistema.
 *
 * @param email - Email único del usuario
 * @param role - Rol asignado (default: 'user')
 * @returns El usuario creado con ID generado
 * @throws {DuplicateEmailError} si el email ya existe
 */
async function createUser(email: string, role?: Role): Promise<User>
```

## Documentación de decisiones (ADRs)

Para decisiones importantes de arquitectura:

```markdown
# ADR-001: Usar PostgreSQL como base de datos

## Contexto
Necesitamos una DB relacional con soporte geoespacial.

## Decisión
Usamos PostgreSQL + PostGIS.

## Consecuencias
- Consultas geoespaciales performantes
- Mayor costo operativo que SQLite
- ORM: Prisma
```

## Changelog

Mantené un `CHANGELOG.md` con formato [Keep a Changelog](https://keepachangelog.com/):

```
## [1.2.0] - 2024-03-15

### Added
- Login con Google OAuth

### Fixed
- Error 500 en validación de email vacío

### Changed
- Actualizado a React 19
```
