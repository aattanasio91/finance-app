---
name: arquitectura
description: >
  Use when designing system architecture, discussing design patterns, making
  technical decisions, or reviewing architectural changes. Covers SOLID,
  clean architecture, design patterns, coupling, cohesion, and trade-offs.
---

# Arquitectura

## Principios SOLID

| Principio | Descripción |
|-----------|-------------|
| **S**RP | Una clase/módulo debe tener una sola razón para cambiar |
| **O**CP | Abierto a extensión, cerrado a modificación |
| **L**SP | Subtipos deben ser sustituibles por sus tipos base |
| **I**SP | Interfaces específicas > interfaz general |
| **D**IP | Dependé de abstracciones, no de implementaciones concretas |

## Acoplamiento y cohesión

- **Alta cohesión**: un módulo hace una cosa bien (SRP).
- **Bajo acoplamiento**: módulos se comunican por interfaces/abstracciones.
- Señales de warning:
  - Cambiar un módulo requiere cambiar 3+ módulos → mucho acoplamiento.
  - Un módulo hace demasiadas cosas → baja cohesión.

## Patrones comunes

### Creacionales
- **Factory**: crear objetos sin exponer la lógica de creación.
- **Builder**: construir objetos complejos paso a paso.
- **Singleton**: una única instancia (usar con cuidado).

### Estructurales
- **Adapter**: hacer que interfaces incompatibles funcionen juntas.
- **Decorator**: agregar comportamiento a objetos dinámicamente.
- **Facade**: interfaz simplificada para un subsistema complejo.

### De comportamiento
- **Strategy**: intercambiar algoritmos en runtime.
- **Observer**: notificar cambios a múltiples suscriptores.
- **Command**: encapsular requests como objetos.

## Clean Architecture / Hexagonal

```
+------------------+
|    Presentation  |  (UI, API, CLI)
+--------+---------+
         |
+--------+---------+
|   Application    |  (use cases, DTOs, ports)
+--------+---------+
         |
+--------+---------+
|     Domain       |  (entities, value objects, business logic)
+--------+---------+
         |
+--------+---------+
|   Infrastructure |  (DB, external APIs, file system)
+------------------+
```

- **Domain**: nunca depende de nada externo. Contiene la lógica de negocio pura.
- **Application**: orquesta use cases. Depende solo del Domain.
- **Infrastructure**: implementa interfaces definidas en Application/Domain.
- **Presentation**: capa más externa. Se comunica con Application.

## Decisiones técnicas

Al tomar decisiones de arquitectura, documentá:

- **Contexto**: ¿qué problema estamos resolviendo?
- **Opción A y B**: ¿qué alternativas consideramos?
- **Decisión**: ¿cuál elegimos y por qué?
- **Trade-offs**: ¿qué perdemos con esta decisión?
- **Consecuencias**: ¿qué impacto tiene a futuro?

Usá [ADR (Architecture Decision Records)](skills/documentacion/SKILL.md) para
documentar decisiones importantes.

## Anti-patrones comunes

| Anti-patrón | Problema | Solución |
|-------------|----------|----------|
| God Object | Una clase lo hace todo | SRP, dividir responsabilidades |
| Spaghetti Code | Sin estructura clara | Modularizar, patrones |
| Golden Hammer | Usar la misma solución para todo | Evaluar contexto |
| Copy-Paste Programming | Código duplicado | DRY, abstraer |
| Premature Optimization | Optimizar sin medir | Primero correcto, luego rápido |

## Estrategias de evolución

- **Monolito primero**: no empieces con microservicios. Un monolith bien modularizado se puede dividir después.
- **Feature flags**: desplegá features apagados, activalos gradualmente.
- **Strangler fig**: reemplazá partes del sistema gradualmente sin migración big bang.
- **Event-driven**: desacoplá componentes con eventos para escalar independientemente.
