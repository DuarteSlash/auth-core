# auth-core

Componente reutilizable de **ingreso seguro** para aplicaciones Java: autenticación de colaboradores, autorización por roles y permisos (RBAC), gestión de perfiles y auditoría de seguridad. Agnóstico del dominio: sirve igual para un ERP, un portal de clientes o una app interna.

## Metodología: Spec-Driven Development (SDD)

Este repositorio sigue el flujo de Spec Kit. Los specs son la fuente de verdad; el código se deriva de ellos.

```
constitución → specify → clarify → plan → tasks → implement
     ✅           ✅        ⏳        ⬜      ⬜        ⬜
```

| Fase | Artefacto | Contenido |
|---|---|---|
| Constitución | `.specify/memory/constitution.md` | Principios no negociables |
| Specify | `specs/NNN-*/spec.md` | Qué y por qué (sin tecnología) |
| Clarify | sección "Preguntas abiertas" de cada spec | Decisiones de negocio pendientes |
| Plan | `specs/NNN-*/plan.md` | Cómo: arquitectura, stack, contratos |
| Tasks | `specs/NNN-*/tasks.md` | Tareas ordenadas y verificables |

## Specs funcionales

| # | Spec | Alcance |
|---|---|---|
| 001 | [Autenticación](specs/001-autenticacion/spec.md) | Ingreso, sesiones, bloqueo, recuperación, MFA |
| 002 | [Autorización RBAC](specs/002-autorizacion-rbac/spec.md) | Roles, permisos `recurso:acción`, herencia, perfil |
| 003 | [Gestión de colaboradores](specs/003-gestion-colaboradores/spec.md) | Alta, estados, asignación de roles, administración |
| 004 | [Auditoría de seguridad](specs/004-auditoria-seguridad/spec.md) | Registro inmutable de eventos, consulta, alertas |

## Próximo paso
Resolver las preguntas abiertas (`[NECESITA CLARIFICACIÓN]`) y luego generar el `plan.md` técnico.
