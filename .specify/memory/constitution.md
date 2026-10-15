# Constitución del proyecto — auth-core

> Principios no negociables que gobiernan todos los specs, planes y tareas de este repositorio.
> Versión: 1.0.0 · Ratificada: 2026-10-14

## Propósito

`auth-core` es un componente reutilizable de **ingreso seguro** para cualquier aplicación, sin importar su dominio de negocio. Resuelve tres preguntas y nada más:

1. **¿Quién eres?** (autenticación)
2. **¿Qué puedes hacer?** (autorización por roles y permisos)
3. **¿Quién hizo qué y cuándo?** (auditoría de seguridad)

La lógica de negocio de cada aplicación consumidora queda fuera de su alcance.

## Principios

### I. Spec primero
Ningún código se escribe sin un spec aprobado. El flujo es: `constitución → specify → clarify → plan → tasks → implement`. Un spec describe el **qué** y el **por qué**; el **cómo** (tecnología) vive solo en `plan.md`.

### II. Agnóstico del dominio
El componente no conoce el negocio de la aplicación que lo usa. Los permisos se expresan como `recurso:acción` y cada aplicación registra su propio catálogo. Ningún requisito puede mencionar entidades de negocio concretas.

### III. Seguro por defecto
- **Denegar por defecto**: sin permiso explícito, no hay acceso.
- **Mínimo privilegio**: un colaborador nuevo nace sin roles administrativos.
- Las credenciales nunca se almacenan ni se registran en texto plano.
- Los mensajes de error no revelan si un usuario existe.
- Línea base de cumplimiento: OWASP ASVS nivel 2.

### IV. Auditable
Todo evento de seguridad (ingreso, fallo, bloqueo, cambio de permisos) deja rastro inmutable con quién, qué, cuándo y desde dónde.

### V. Configurable, no modificable
Las políticas (longitud de contraseña, intentos permitidos, duración de sesión, obligatoriedad de MFA) son parámetros. Una aplicación consumidora ajusta la configuración, no el código.

### VI. Probado contra el spec
Cada escenario de aceptación (Dado/Cuando/Entonces) se convierte en una prueba automatizada antes de dar la funcionalidad por terminada.

### VII. Arquitectura hexagonal (puertos y adaptadores)
- El **dominio** (colaborador, rol, permiso, sesión, política) no depende de ningún framework, base de datos ni proveedor de nube.
- Toda dependencia externa entra por un **puerto** definido por el dominio: proveedor de identidad, repositorio de roles y permisos, emisor de tokens, notificaciones, auditoría.
- Cada proveedor concreto (directorio corporativo, servicio de identidad en la nube, base de datos local) es un **adaptador** intercambiable. Cambiar de proveedor no toca el dominio ni los specs.
- Las dependencias apuntan siempre hacia adentro: `adaptadores → aplicación → dominio`.

## Gobierno
Esta constitución prevalece sobre cualquier otro documento. Cambiarla requiere justificación escrita, aumento de versión y revisión de los specs afectados.
