---
name: git-workflow
description: >
  Use when committing, branching, reviewing PRs, resolving conflicts, or
  discussing Git best practices. Covers conventional commits, branching
  strategies, rebase vs merge, PR hygiene, and conflict resolution.
---

# Git Workflow

## Conventional Commits

Formato: `tipo(alcance opcional): descripción imperativa`

```
feat: agregar login con Google
fix(auth): corregir validación de token expirado
docs: actualizar README con ejemplos de uso
refactor: extraer lógica de validación a helper
test: agregar tests para CreateUser
chore: actualizar dependencias
perf: optimizar consulta de usuarios
style: aplicar formateo de prettier
```

- Usá el cuerpo del commit para explicar *qué* y *por qué* (no *cómo*).
- Referenciá issues: `fix(#123): corregir crash en null pointer`

## Branching

Estrategia recomendada: **trunk-based** o **GitHub Flow**.

```
main ← feature/login
main ← fix/auth-token
main ← chore/update-deps
```

- Nombres de branch: `tipo/descripción-corta` (ej: `feat/login`, `fix/auth-error`)
- Mantené branches cortas (1-3 días máximo).
- Siempre actualizate con `main` antes de abrir PR.

## PRs

- Un PR = un cambio atómico (no mezcles refactors con features).
- Descripción clara: qué cambia, por qué, cómo testear.
- Mantené PRs pequeños (< 400 líneas idealmente).
- Si un PR crece mucho → dividilo en varios.

## Rebase vs Merge

Preferí **rebase** para mantener historia lineal:

```bash
git checkout feature/login
git rebase main
# resolver conflictos si hay
git push --force-with-lease
```

Usá merge commits solo cuando:
- Un PR ya fue aprobado y querés preservar el merge explícito.
- Estás integrando una rama compartida por múltiples personas.

## Conflictos

1. `git rebase main` (o `git merge main`)
2. Resolvé conflictos con tu editor.
3. `git add . && git rebase --continue`
4. Si te arrepentís: `git rebase --abort`

## Comandos útiles

```bash
git log --oneline --graph -20        # historia visual
git commit --amend --no-edit          # arreglar último commit sin cambiar msg
git stash push -m "wip: ..."          # guardar cambios temporales
git stash pop                         # recuperar cambios
git diff main...HEAD                  # diff de tu branch contra main
git bisect start                      # encontrar el commit que introdujo un bug
```

## Buenas prácticas

- No commitees secrets, `.env`, `node_modules/`, `dist/`, `build/`.
- Usá `.gitignore` desde el inicio del proyecto.
- Commitee seguido (cambios pequeños y lógicos).
- Escribí mensajes de commit en inglés o español (consistente en el proyecto).
