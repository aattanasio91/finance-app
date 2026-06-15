---
name: code-review
description: >
  Use when reviewing PRs, preparing code for review, or discussing code review
  best practices. Covers how to give and receive constructive feedback,
  what to look for in a review, and how to structure review comments.
---

# Code Review

## Principios generales

- Revisá el código, no a la persona. Los comentarios son sobre la implementación, no sobre quien la escribió.
- Automatizá lo automatable: linting, formato, type checking, tests. La revisión humana es para lo que las máquinas no pueden detectar.
- equilibrio: no hagas micro-management de estilo si ya hay una tool que lo cubre.

## Qué revisar (en orden de importancia)

1. **Corrección**: ¿La solución resuelve el problema? ¿Hay edge cases sin cubrir?
2. **Seguridad**: ¿Hay inyección de SQL, XSS, exposición de datos sensibles, CSRF?
3. **Performance**: ¿Hay queries N+1, bucles innecesarios, memory leaks?
4. **Mantenibilidad**: ¿El código es legible? ¿Las names/abstracciones son adecuadas?
5. **Testing**: ¿Hay tests para los cambios? ¿Cubren casos exitosos y de error?
6. **Consistencia**: ¿Sigue las conventions del proyecto?

## Cómo escribir comentarios

- Usá ["conventional comments"](https://conventionalcomments.org/):
  - `**sugerencia (nitpick):**` para preferencias menores
  - `**sugerencia (issue):**` para problemas reales que bloquearían el merge
  - `**pregunta:**` si no entendés algo
  - `**elogio:**` cuando algo está bien hecho
- Explicá el *por qué* detrás de cada comentario, no solo el *qué*.
- Ofrecé alternativas concretas en vez de solo señalar problemas.

## Cómo recibir reviews

- No te lo tomes personal. Cada comentario es una oportunidad de mejora.
- Si no estás de acuerdo, explicá tu reasoning con respeto.
- Agradecé el feedback, incluso si no lo aplicás.
- Respondé a cada comentario (resolved o con una respuesta).

## Checklist pre-merge

- [ ] Código libre de warnings/errores
- [ ] Tests pasan
- [ ] Nuevos tests incluidos si aplica
- [ ] Documentación actualizada (si corresponde)
- [ ] Sin secrets/credentials hardcodeados
- [ ] Sin código comentado/dead code
