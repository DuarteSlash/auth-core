# auth-core

Componente reutilizable de **ingreso seguro** para aplicaciones Java: autenticación de colaboradores, autorización por roles y permisos (RBAC), gestión de perfiles y auditoría de seguridad. Agnóstico del dominio: sirve igual para un ERP, un portal de clientes o una app interna.

## Metodología: Spec-Driven Development (SDD)

Este repositorio sigue el flujo de Spec Kit. Los specs son la fuente de verdad; el código se deriva de ellos.

```
constitución → specify → clarify → plan → tasks → implement
     ✅           ✅        ⏳        ⬜      ⬜        🧱 esqueleto
```

| Fase | Artefacto | Contenido |
|---|---|---|
| Constitución | `.specify/memory/constitution.md` | Principios no negociables (incluye arquitectura hexagonal) |
| Specify | `specs/NNN-*/spec.md` | Qué y por qué (sin tecnología) |
| Clarify | sección "Preguntas abiertas" de cada spec | Decisiones de negocio pendientes |
| Research | `specs/005-*/research.md` | Investigación técnica: escalar el login, Cognito vs. LDAP vs. propio |
| Plan | `specs/NNN-*/plan.md` | Cómo: arquitectura, stack, contratos |
| Tasks | `specs/NNN-*/tasks.md` | Tareas ordenadas y verificables |

## Specs

| # | Spec | Alcance |
|---|---|---|
| 001 | [Autenticación](specs/001-autenticacion/spec.md) | Ingreso, sesiones, bloqueo, recuperación, MFA |
| 002 | [Autorización RBAC](specs/002-autorizacion-rbac/spec.md) | Roles, permisos `recurso:acción`, herencia, perfil |
| 003 | [Gestión de colaboradores](specs/003-gestion-colaboradores/spec.md) | Alta, estados, asignación de roles, administración |
| 004 | [Auditoría de seguridad](specs/004-auditoria-seguridad/spec.md) | Registro inmutable de eventos, consulta, alertas |
| 005 | [Requisitos no funcionales](specs/005-requisitos-no-funcionales/spec.md) | Rendimiento, escalabilidad, disponibilidad, seguridad, observabilidad · [investigación](specs/005-requisitos-no-funcionales/research.md) |

## Arquitectura hexagonal

```
auth-bootstrap ──► auth-infrastructure ──► auth-application ──► auth-domain
 (Spring Boot)       (adaptadores)          (casos de uso +        (Java puro)
                                             puertos)
```

| Módulo | Contiene | Dependencias permitidas |
|---|---|---|
| `auth-domain` | `Permiso`, `ColaboradorId`, `EventoSeguridad` | Ninguna |
| `auth-application` | Puertos de entrada (`VerificarPermisoUseCase`) y salida (`VerificadorCredenciales`, `RepositorioPermisos`, `PublicadorAuditoria`), servicios | `auth-domain` |
| `auth-infrastructure` | Adaptadores: REST, Cognito, LDAP, local Argon2id, PostgreSQL, Redis, auditoría | `auth-application` + librerías del proveedor |
| `auth-bootstrap` | Aplicación Spring Boot, configuración, prueba de arquitectura | Todo |

La regla de dependencias se verifica en cada build con ArchUnit (`ArquitecturaHexagonalTest`).

## Estado del código
Esqueleto de arquitectura. Lo implementado se limita a lo que exige la constitución (permiso `recurso:acción`, denegar por defecto) con sus pruebas. Los casos de uso de ingreso, sesiones y adaptadores se escriben al aprobar `plan.md` y `tasks.md`.

## Requisitos y ejecución
- Java 21, Maven 3.9+
- Stack: Spring Boot 4.1

```bash
mvn verify                                   # compila, pruebas unitarias y de arquitectura
mvn -pl auth-bootstrap -am spring-boot:run   # levanta el servicio; salud en /actuator/health
```

## Próximo paso
Resolver las preguntas abiertas (`[NECESITA CLARIFICACIÓN]`) y generar el `plan.md` técnico.
