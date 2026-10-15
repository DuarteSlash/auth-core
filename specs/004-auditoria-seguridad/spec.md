# Spec 004 — Auditoría de eventos de seguridad

| Campo | Valor |
|---|---|
| Estado | Borrador — pendiente de clarificación |
| Creado | 2026-10-14 |
| Depende de | 001, 002, 003 |

## Contexto
Para investigar incidentes y cumplir normativas (en Colombia, Ley 1581 de 2012 de protección de datos personales), cada evento relevante de seguridad debe quedar registrado de forma confiable.

## Historias de usuario

### HU-004.1 · Registrar eventos de seguridad (P1)
1. **Dado** cualquier evento del catálogo, **cuando** ocurre, **entonces** se registra con: tipo, colaborador afectado, autor (si difiere), fecha y hora en UTC, origen de red, agente/dispositivo, resultado y un identificador de correlación.
2. **Dado** un evento registrado, **cuando** alguien intenta modificarlo o borrarlo, **entonces** el sistema no lo permite.
3. **Dado** un evento con datos sensibles (contraseña, token), **entonces** esos valores nunca se escriben en el registro.

### HU-004.2 · Consultar la auditoría (P2)
1. **Dado** un colaborador con `auditoria:leer`, **cuando** filtra por colaborador, tipo de evento o rango de fechas, **entonces** obtiene los eventos paginados.
2. **Dado** un colaborador, **cuando** consulta su propia actividad reciente, **entonces** ve sus últimos ingresos y desde qué dispositivo.

### HU-004.3 · Alertas ante comportamiento sospechoso (P3)
1. **Dado** un número anormal de fallos sobre muchas cuentas desde un mismo origen, **entonces** se genera una alerta para el equipo de seguridad.
2. **Dado** un ingreso desde un dispositivo nunca visto, **entonces** se notifica al colaborador [NECESITA CLARIFICACIÓN: ¿requerido en v1?].

## Catálogo mínimo de eventos
`LOGIN_EXITOSO`, `LOGIN_FALLIDO`, `CUENTA_BLOQUEADA`, `CUENTA_DESBLOQUEADA`, `LOGOUT`, `SESION_EXPIRADA`, `SESION_RENOVADA`, `REUTILIZACION_SOSPECHOSA`, `CONTRASENA_CAMBIADA`, `CONTRASENA_RESTABLECIDA`, `RECUPERACION_SOLICITADA`, `MFA_ACTIVADO`, `MFA_FALLIDO`, `MFA_RESTABLECIDO`, `ACCESO_DENEGADO`, `COLABORADOR_CREADO`, `COLABORADOR_ACTIVADO`, `COLABORADOR_DESACTIVADO`, `ROL_ASIGNADO`, `ROL_RETIRADO`, `ROL_CREADO`, `ROL_MODIFICADO`, `ROL_ELIMINADO`, `PERFIL_ACTUALIZADO`.

## Requisitos funcionales
- **RF-060**: El sistema DEBE registrar todos los eventos del catálogo mínimo.
- **RF-061**: Los registros DEBEN ser de solo inserción; ninguna función permite modificarlos ni borrarlos.
- **RF-062**: Los registros NO DEBEN contener contraseñas, tokens, secretos ni códigos MFA.
- **RF-063**: Los registros DEBEN conservarse al menos el período configurado (por defecto 1 año) [NECESITA CLARIFICACIÓN: plazo exigido por la normativa aplicable].
- **RF-064**: Una falla al escribir la auditoría NO DEBE dejar pasar silenciosamente una operación administrativa crítica [NECESITA CLARIFICACIÓN: ¿se bloquea la operación o se encola el evento?].
- **RF-065**: El sistema DEBE permitir exportar eventos a un sistema externo de monitoreo (SIEM).

## Criterios de éxito
- **CE-060**: El 100 % de los eventos del catálogo aparecen en la auditoría en las pruebas de aceptación.
- **CE-061**: Una consulta de un mes de eventos de un colaborador responde en menos de 2 s.
- **CE-062**: Cero datos sensibles hallados en auditoría y logs en la revisión de seguridad.
