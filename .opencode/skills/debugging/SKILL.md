---
name: debugging
description: >
  Use when troubleshooting bugs, investigating errors, or optimizing debugging
  workflows. Covers systematic debugging methodology, tools, logging, and
  common debugging patterns.
---

# Debugging

## Metodología sistemática

1. **Reproducí el bug**: pasos concretos y consistentes para reproducirlo.
2. **Aislá la causa**: dividí el problema en partes, encontrá el punto exacto de falla.
3. **Formulá una hipótesis**: "esto falla porque X". Hacé una predicción.
4. **Probá la hipótesis**: cambiá X y verificá si el bug persiste.
5. **Si funciona**: documentá y commiteá el fix. **Si no**: volvé al paso 3.

## Técnicas

### Binary search (bisección)

- Comentá/deshabilitá la mitad del código sospechoso.
- Si el bug persiste → está en la otra mitad.
- Repetí hasta encontrar la línea exacta.
- También aplica a commits: `git bisect`.

### Rubber duck debugging

- Explicále el problema a un objeto inanimado (o a un colega).
- Verbalizar el problema suele revelar la solución.

### Divide and conquer

- Simplificá el input mínimo que reproduce el bug.
- Reducí el escenario al mínimo posible.

## Herramientas

| Entorno    | Herramienta                          |
|-----------|--------------------------------------|
| Node.js   | `node --inspect`, `node --watch`     |
| Python    | `pdb`, `ipdb`, `breakpoint()`        |
| React     | React DevTools, `console.log` estricto |
| DB        | `EXPLAIN ANALYZE`, `pg_stat_activity` |
| Network   | DevTools > Network, `curl -v`, `wireshark` |

## Logging estratégico

En vez de `console.log` al azar:

```ts
// Bueno
console.log("[AuthService] validateToken iniciado", { tokenLength: token.length });

// Malo
console.log(token);
```

- Incluí contexto suficiente para reconstruir el estado.
- Usá niveles: `debug`, `info`, `warn`, `error`.
- Structured logging > strings concatenados.
- Nunca loggees secrets/tokens/passwords.

## Patrones comunes de bugs

| Síntoma                      | Posible causa                          |
|-----------------------------|----------------------------------------|
| "Funciona en mi máquina"    | Environment mismatch, config diferencias |
| Bug intermitente            | Race condition, timer, estado global   |
| Cambio en A rompe B         | Efecto secundario no deseado, acoplamiento |
| Error en producción solo    | Dato específico, carga, red, permiso   |
| Performance degradada       | Memory leak, N+1 query, bloqueo        |

## Tips

- Leé el mensaje de error completo (incluyendo stack trace).
- Googleá el mensaje de error exacto (entre comillas).
- Verificá assumptions: "estoy seguro de que X vale Y" → comprobálo.
- Descansá si estás stuck. El cerebro procesa en background.
- Si llevás >30min sin avance, pedí ayuda y explicá el contexto.
