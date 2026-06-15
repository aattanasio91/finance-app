---
name: performance
description: >
  Use when optimizing code, profiling applications, investigating slow
  responses, or discussing performance best practices. Covers frontend
  and backend performance optimization, profiling tools, and patterns.
---

# Performance

## Principios

1. **Medí antes de optimizar**: sin datos, toda optimización es conjetura.
2. El cuello de botella suele ser I/O (red, disco, DB), no CPU.
3. La optimización prematura es la raíz de todos los males (Knuth).
4. Preferí algoritmos correctos y legibles primero → optimizá después.

## Backend

### Base de datos

- **N+1 queries**: usá eager loading (`JOIN`, `INCLUDE`, `SELECT * FROM X WHERE id IN (...)`).
- **Indexación**: agregá índices a columnas usadas en `WHERE`, `JOIN`, `ORDER BY`.
- **Paginación**: usá cursor-based en vez de offset para grandes datasets.
- **Connection pooling**: no abras/cierres conexiones por request.
- **EXPLAIN ANALYZE**: analizá queries lentas.

### API

- **Caching**: respuestas cacheables (HTTP cache headers, Redis, CDN).
- **Compresión**: habilitá gzip/brotli.
- **Payload**: devolvé solo los campos necesarios (GraphQL, sparse fieldsets).
- **Batch**: agrupá operaciones (ej: bulk inserts, batch API calls).
- **Rate limiting**: protegé recursos compartidos.

## Frontend

### Bundle

- **Tree shaking**: importá solo lo que usás.
- **Code splitting**: dividí el bundle por rutas (`React.lazy`, `dynamic import`).
- **Compresión de assets**: imágenes WebP/AVIF, SVGs optimizados.

### Rendering

- **Virtual list**: para listas largas (react-window, tanstack-virtual).
- **Memoization**: `useMemo`, `useCallback`, `React.memo` (solo si hay rerenders medibles).
- **Debounce/Throttle**: para inputs de búsqueda, resize, scroll.
- **Infinite scroll vs pagination**: dependé del caso de uso.

### Network

- **Prefetch**: cargá recursos anticipadamente (`<link rel="preload">`, `prefetch`).
- **Lazy loading**: imágenes y componentes fuera de la viewport.
- **Service Workers**: caching offline, estrategias de red.

## Profiling

| Contexto    | Herramienta                  |
|------------|------------------------------|
| Node.js    | `clinic`, `0x`, `--cpu-prof` |
| Python     | `cProfile`, `py-spy`         |
| Browser    | DevTools > Performance, Lighthouse |
| DB         | `EXPLAIN ANALYZE`, `pg_stat_statements` |
| React      | React DevTools > Profiler    |

## Memory

- **Memory leaks**: event listeners no removidos, closures, referencias globales, `setInterval` sin cleanup.
- **Garbage collection**: evitá crear objetos temporales en loops.
- **Streams**: procesá datos grandes en streams en vez de cargar todo en memoria.

## Checklist de optimización

- [ ] ¿Hay N+1 queries?
- [ ] ¿Hay índices faltantes?
- [ ] ¿El bundle está optimizado?
- [ ] ¿Hay caching donde corresponde?
- [ ] ¿Las imágenes están comprimidas?
- [ ] ¿Hay memory leaks?
- [ ] ¿Las queries lentas tienen `EXPLAIN ANALYZE`?
- [ ] ¿Hay redirecciones innecesarias?
