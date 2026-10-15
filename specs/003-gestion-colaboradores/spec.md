# Spec 003 — Gestión de colaboradores, roles y permisos

| Campo | Valor |
|---|---|
| Estado | Borrador — pendiente de clarificación |
| Creado | 2026-10-14 |
| Depende de | 001, 002 |
| Relacionado | 004 (auditoría) |

## Contexto
Los administradores necesitan dar de alta colaboradores, asignarles roles y mantener el catálogo de roles sin intervención técnica.

## Historias de usuario

### HU-003.1 · Alta de colaborador (P1)
**Como** administrador de seguridad, **quiero** registrar un colaborador nuevo, **para** que pueda ingresar a la aplicación.

1. **Dado** un administrador con `colaboradores:crear`, **cuando** registra un colaborador con datos válidos, **entonces** se crea en estado `PENDIENTE_ACTIVACION`, con el rol `COLABORADOR` y se le envía una invitación de un solo uso para definir su contraseña.
2. **Dado** un identificador ya registrado, **cuando** se intenta crear otro igual, **entonces** se rechaza.
3. **Dado** una invitación vencida, **entonces** el administrador puede reenviarla.

### HU-003.2 · Activar, desactivar y desbloquear (P1)
1. **Dado** un colaborador activo, **cuando** el administrador lo desactiva, **entonces** se cierran todas sus sesiones de inmediato y no puede ingresar.
2. **Dado** un colaborador bloqueado por intentos fallidos, **cuando** el administrador lo desbloquea, **entonces** puede volver a ingresar y su contador vuelve a cero.
3. **Dado** un colaborador desactivado, **cuando** se reactiva, **entonces** conserva su historial y sus roles quedan como estaban [NECESITA CLARIFICACIÓN: ¿o se le retiran los roles al desactivar?].

### HU-003.3 · Asignar y retirar roles (P1)
1. **Dado** un administrador con `roles:asignar`, **cuando** asigna un rol a un colaborador, **entonces** sus permisos efectivos se actualizan y se registra `ROL_ASIGNADO` con quién lo hizo.
2. **Dado** que el administrador intenta asignarse un rol a sí mismo, **entonces** se rechaza (RF-028).
3. **Dado** que se intenta retirar el último `SUPER_ADMIN` activo, **entonces** se rechaza (RF-029).

### HU-003.4 · Administrar roles (P2)
1. **Dado** un administrador con `roles:crear`, **cuando** crea un rol con un conjunto de permisos del catálogo, **entonces** queda disponible para asignar.
2. **Dado** un rol de sistema, **cuando** se intenta eliminar, **entonces** se rechaza.
3. **Dado** un rol con colaboradores asignados, **cuando** se intenta eliminar, **entonces** se exige reasignarlos o confirmar el retiro.

### HU-003.5 · Consultar colaboradores (P2)
1. **Dado** un administrador con `colaboradores:leer`, **cuando** busca por nombre, identificador, rol o estado, **entonces** obtiene resultados paginados sin datos sensibles (nunca hashes ni secretos de MFA).

### HU-003.6 · Editar mi perfil (P2)
1. **Dado** un colaborador autenticado, **cuando** edita su nombre para mostrar, foto, idioma o zona horaria, **entonces** se guardan los cambios.
2. **Dado** que intenta editar sus roles, estado o identificador desde su perfil, **entonces** se rechaza.

### HU-003.7 · Forzar acciones de seguridad (P3)
1. **Dado** un administrador, **cuando** fuerza el cierre de todas las sesiones de un colaborador, **entonces** se invalidan de inmediato.
2. **Dado** un administrador, **cuando** marca "cambio de contraseña obligatorio", **entonces** el colaborador debe cambiarla en su próximo ingreso.
3. **Dado** un administrador, **cuando** restablece el MFA de un colaborador, **entonces** este debe configurarlo de nuevo.

## Requisitos funcionales
- **RF-040**: Solo colaboradores con los permisos administrativos correspondientes DEBEN poder gestionar colaboradores, roles y asignaciones.
- **RF-041**: Los estados de un colaborador DEBEN ser: `PENDIENTE_ACTIVACION`, `ACTIVO`, `BLOQUEADO`, `INACTIVO`.
- **RF-042**: El alta DEBE generar una invitación de un solo uso con vigencia configurable (por defecto 72 h); el administrador nunca define ni conoce la contraseña.
- **RF-043**: Desactivar un colaborador DEBE invalidar todas sus sesiones.
- **RF-044**: Ninguna consulta DEBE exponer contraseñas, hashes, secretos de MFA ni tokens.
- **RF-045**: El sistema NO DEBE eliminar físicamente colaboradores con historial; se desactivan (preservar la auditoría).
- **RF-046**: El sistema DEBE permitir la carga inicial (semilla) de un primer `SUPER_ADMIN` de forma segura al instalarse.

## Entidades clave
- **Colaborador**: identificador único, correo, estado, fechas de creación/último ingreso, intentos fallidos, indicador de cambio obligatorio, MFA activo.
- **Invitación / token de un solo uso**: propósito (activación, recuperación), vencimiento, usado sí/no.

## Criterios de éxito
- **CE-040**: Un administrador da de alta y asigna rol a un colaborador en menos de 2 minutos.
- **CE-041**: Un colaborador desactivado pierde acceso en menos de 5 segundos.
- **CE-042**: El 100 % de las acciones administrativas quedan auditadas con su autor.

## Preguntas abiertas
1. ¿Al desactivar se conservan o se retiran los roles?
2. ¿Se requiere importación masiva de colaboradores (CSV) en la v1?
3. ¿Hay aprobación en dos pasos (maker-checker) para asignar roles críticos?
