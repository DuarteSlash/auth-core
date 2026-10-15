# Spec 002 — Autorización por roles y permisos (RBAC)

| Campo | Valor |
|---|---|
| Estado | Borrador — pendiente de clarificación |
| Creado | 2026-10-14 |
| Depende de | 001 (autenticación) |
| Relacionado | 003 (gestión), 004 (auditoría) |

## Contexto
Una vez sabemos quién es el colaborador, cada aplicación necesita decidir qué puede hacer. El modelo debe ser genérico: `auth-core` no sabe qué es una "factura" o un "pedido"; cada aplicación registra sus propios recursos y acciones.

## Modelo conceptual

```
Colaborador ──< asignación >── Rol ──< concesión >── Permiso (recurso:acción)
                                │
                                └─ puede heredar de otro Rol
Colaborador ──── Perfil (datos de presentación y preferencias)
```

- **Permiso**: unidad mínima, expresada como `recurso:acción` (ej. `reportes:leer`, `usuarios:crear`). Admite comodín de acción `recurso:*`.
- **Rol**: conjunto nombrado de permisos (ej. `ADMIN`, `SUPERVISOR`, `OPERADOR`, `CONSULTA`).
- **Perfil**: información del colaborador visible en la aplicación (nombre, cargo, área, foto, idioma, zona horaria). No otorga permisos.
- **Permisos efectivos**: unión de los permisos de todos los roles vigentes del colaborador, incluidos los heredados.

## Historias de usuario

### HU-002.1 · Verificar acceso a una operación (P1)
**Como** aplicación consumidora, **quiero** preguntar si el colaborador autenticado tiene un permiso, **para** permitir o negar la operación.

1. **Dado** un colaborador con el rol `SUPERVISOR` que concede `reportes:leer`, **cuando** solicita leer reportes, **entonces** se permite.
2. **Dado** un colaborador sin ningún rol que conceda `reportes:eliminar`, **cuando** lo intenta, **entonces** se niega y se registra `ACCESO_DENEGADO`.
3. **Dado** un permiso que no existe en el catálogo, **cuando** se consulta, **entonces** se niega (denegar por defecto).
4. **Dado** una solicitud sin sesión válida, **entonces** se responde "no autenticado", distinto de "no autorizado".

### HU-002.2 · Consultar mis permisos y perfil (P1)
**Como** colaborador, **quiero** ver mi perfil, roles y permisos efectivos, **para** que la interfaz muestre solo lo que puedo usar.

1. **Dado** un colaborador autenticado, **cuando** consulta "mi perfil", **entonces** obtiene sus datos de perfil, roles vigentes y permisos efectivos.
2. **Dado** que se le retira un rol, **cuando** vuelve a consultar, **entonces** ya no aparecen esos permisos.

### HU-002.3 · Herencia de roles (P2)
1. **Dado** el rol `SUPERVISOR` que hereda de `OPERADOR`, **entonces** un supervisor tiene todos los permisos del operador más los propios.
2. **Dado** un intento de crear una herencia circular, **entonces** el sistema lo rechaza.

### HU-002.4 · Catálogo de permisos por aplicación (P1)
**Como** equipo que integra `auth-core`, **quiero** registrar los recursos y acciones de mi aplicación, **para** usar permisos propios del negocio sin modificar el componente.

1. **Dado** un catálogo declarado por la aplicación, **cuando** arranca, **entonces** sus permisos quedan disponibles para asignar a roles.
2. **Dado** un permiso retirado del catálogo, **entonces** deja de concederse aunque siga asignado a un rol, y se informa al administrador.

### HU-002.5 · Roles con vigencia (P3)
1. **Dado** un rol asignado con fecha de fin, **cuando** llega esa fecha, **entonces** deja de contar en los permisos efectivos sin intervención manual.

### HU-002.6 · Alcance multi-aplicación / multi-organización (P3)
[NECESITA CLARIFICACIÓN] ¿Un mismo colaborador puede tener roles distintos en distintas aplicaciones u organizaciones (multi-tenant)? Si sí, cada asignación de rol debe llevar un alcance.

## Requisitos funcionales
- **RF-020**: El sistema DEBE negar por defecto cualquier operación que no esté concedida explícitamente.
- **RF-021**: El sistema DEBE expresar permisos como `recurso:acción` y permitir que cada aplicación registre su catálogo.
- **RF-022**: El sistema DEBE permitir que un colaborador tenga múltiples roles; sus permisos efectivos son la unión.
- **RF-023**: El sistema DEBE soportar herencia de roles y rechazar ciclos.
- **RF-024**: El sistema DEBE distinguir entre "no autenticado" y "no autorizado" en sus respuestas.
- **RF-025**: Los cambios de roles o permisos DEBEN reflejarse en las decisiones de acceso en un tiempo máximo configurable (por defecto ≤ 60 s), incluso para sesiones ya abiertas.
- **RF-026**: El sistema DEBE exponer el perfil, roles y permisos efectivos del colaborador autenticado.
- **RF-027**: El sistema DEBE incluir roles base predefinidos: `SUPER_ADMIN` (gestión total de `auth-core`), `ADMIN_SEGURIDAD` (gestión de colaboradores y roles) y `COLABORADOR` (acceso mínimo a su propio perfil).
- **RF-028**: El sistema DEBE impedir que un colaborador se conceda a sí mismo roles o permisos (segregación de funciones).
- **RF-029**: El sistema DEBE impedir que quede cero colaboradores activos con rol `SUPER_ADMIN`.
- **RF-030**: Un administrador solo DEBE poder asignar roles cuyos permisos él mismo posea (no escalar privilegios).

## Entidades clave
- **Permiso**: código `recurso:acción`, descripción, aplicación dueña, estado.
- **Rol**: código único, nombre, descripción, permisos concedidos, rol padre opcional, indicador "requiere MFA", indicador "de sistema" (no eliminable).
- **Asignación de rol**: colaborador, rol, vigente desde/hasta, asignado por, alcance [pendiente HU-002.6].
- **Perfil**: nombre para mostrar, cargo, área, foto, idioma, zona horaria.

## Criterios de éxito
- **CE-020**: El 99 % de las verificaciones de permiso responden en menos de 50 ms.
- **CE-021**: Un rol retirado deja de conceder acceso en ≤ 60 s en todas las sesiones.
- **CE-022**: Una aplicación nueva integra su catálogo de permisos sin cambiar código de `auth-core`.
- **CE-023**: Cero rutas de escalamiento de privilegios en las pruebas de seguridad.

## Preguntas abiertas
1. ¿Multi-tenant (organizaciones/aplicaciones separadas) desde la v1?
2. ¿Se necesitan permisos sobre registros concretos (ej. "solo los reportes de mi área") o basta con permisos por tipo de recurso? Lo primero sería ABAC y amplía el alcance.
3. ¿Qué roles base adicionales esperan tener todas las aplicaciones?
4. ¿Dónde vive la fuente de verdad de roles y perfiles: en `auth-core`, en un directorio/servicio de identidad externo, o mixto (grupos externos mapeados a roles internos)? El spec funciona igual en los tres casos; la decisión se toma en `plan.md`.
