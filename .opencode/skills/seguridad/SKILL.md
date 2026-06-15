---
name: seguridad
description: >
  Use when reviewing security aspects of code, implementing authentication,
  handling sensitive data, or discussing secure development practices. Covers
  OWASP Top 10, secure coding, auth, encryption, and compliance.
---

# Seguridad

## Principios fundamentales

- **Defense in depth**: múltiples capas de seguridad. Si una falla, la siguiente contiene.
- **Least privilege**: cada componente tiene solo los permisos que necesita.
- **Never trust user input**: toda entrada del usuario es potencialmente maliciosa.
- **Security by design**: la seguridad se diseña desde el inicio, no se agrega después.

## OWASP Top 10 (resumen)

| # | Vulnerabilidad | Prevención |
|---|---------------|------------|
| 1 | Broken Access Control | Validá permisos en cada request |
| 2 | Cryptographic Failures | Usá HTTPS, no inventes crypto |
| 3 | Injection (SQL, XSS, Command) | Sanitizá input, usá ORM/prepared statements |
| 4 | Insecure Design | Threat modeling, rate limiting |
| 5 | Security Misconfiguration | Default secrets off, hardening |
| 6 | Vulnerable Components | Dependencias actualizadas, SCA |
| 7 | Auth Failures | MFA, rate limiting, pw hashing |
| 8 | Data Integrity Failures | Firmas, checksums |
| 9 | Logging & Monitoring | Audit logs, alerting |
| 10 | SSRF | Validá URLs, whitelist de destinos |

## Autenticación

- Passwords: nunca en texto plano. Usá **bcrypt** (cost >= 12) o **argon2**.
- JWTs: firmá con algoritmo asimétrico (RS256/ES256). Seteá `exp`, `iat`, `aud`.
- Sesiones: usá HTTP-only, Secure, SameSite cookies.
- MFA: ofrecé TOTP o WebAuthn.
- Rate limiting en login: prevení brute force.

## APIs

- **Headers de seguridad**:
  ```
  Content-Security-Policy: default-src 'self'
  Strict-Transport-Security: max-age=31536000
  X-Content-Type-Options: nosniff
  X-Frame-Options: DENY
  ```
- **CORS**: no uses `*` en producción. Whitelist de orígenes específicos.
- **Input validation**: validá tipo, longitud, formato, rango en el servidor.
- **Output encoding**: escapá output para prevenir XSS según el contexto (HTML, JS, CSS, URL).

## Dependencias

- `npm audit`, `pip audit`, `cargo audit` en CI.
- Usá lockfiles (`package-lock.json`, `Cargo.lock`).
- Dependabot / Renovate para actualizaciones automáticas.
- No instales paquetes innecesarios.

## Datos sensibles

- **In transit**: siempre HTTPS/TLS.
- **At rest**: encriptá datos sensibles en DB (AES-256).
- **Secrets**: nunca en el código. Usá variables de entorno o secret manager.
- **Logs**: no loggees passwords, tokens, API keys, PII.
- **.env** en `.gitignore`.

## Checklist de seguridad pre-deploy

- [ ] HTTPS habilitado y redirección HTTP → HTTPS
- [ ] Headers de seguridad configurados
- [ ] Autenticación y autorización en todos los endpoints protegidos
- [ ] Input validation en server-side
- [ ] Dependencias auditadas sin vulnerabilidades críticas
- [ ] Secrets manejados con environment/secret manager
- [ ] Logs sin datos sensibles
- [ ] CORS configurado correctamente
- [ ] Rate limiting implementado
