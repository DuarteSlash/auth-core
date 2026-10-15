# Spec 001 — Autenticación de colaboradores

| Campo | Valor |
|---|---|
| Estado | Borrador — pendiente de clarificación |
| Creado | 2026-10-14 |
| Depende de | — |
| Relacionado | 002 (autorización), 004 (auditoría) |

## Contexto
Toda aplicación que adopte `auth-core` necesita verificar que quien ingresa es realmente el colaborador que dice ser, mantener su sesión de forma segura y cerrarla cuando corresponda.

## Historias de usuario

### HU-001.1 · Iniciar sesión (Prioridad P1)
**Como** colaborador registrado, **quiero** ingresar con mi identificador y contraseña, **para** acceder a las funciones que me corresponden.

**Prueba independiente:** con un colaborador activo, el ingreso con credenciales válidas devuelve una sesión que identifica al colaborador y sus roles.

**Escenarios de aceptación**
1. **Dado** un colaborador activo, **cuando** ingresa con credenciales válidas, **entonces** obtiene una sesión activa y se registra el evento `LOGIN_EXITOSO`.
2. **Dado** un colaborador activo, **cuando** ingresa con contraseña incorrecta, **entonces** recibe el mensaje genérico "Credenciales inválidas" y se incrementa su contador de intentos fallidos.
3. **Dado** un identificador inexistente, **cuando** se intenta ingresar, **entonces** se recibe el mismo mensaje genérico y en un tiempo de respuesta equivalente al caso anterior.
4. **Dado** un colaborador inactivo o bloqueado, **cuando** intenta ingresar con credenciales válidas, **entonces** se le niega el acceso sin crear sesión.

### HU-001.2 · Bloqueo por intentos fallidos (P1)
**Como** responsable de seguridad, **quiero** que una cuenta se bloquee tras varios intentos fallidos, **para** frenar ataques de fuerza bruta.

1. **Dado** un colaborador con N−1 intentos fallidos consecutivos, **cuando** falla de nuevo, **entonces** la cuenta queda bloqueada temporalmente y se registra `CUENTA_BLOQUEADA`.
2. **Dado** una cuenta bloqueada temporalmente, **cuando** vence el tiempo de bloqueo, **entonces** puede volver a intentar el ingreso.
3. **Dado** un ingreso exitoso, **entonces** el contador de intentos fallidos vuelve a cero.

### HU-001.3 · Cerrar sesión (P1)
1. **Dado** un colaborador con sesión activa, **cuando** cierra sesión, **entonces** la sesión queda invalidada de inmediato y ya no autoriza ninguna operación.
2. **Dado** un colaborador, **cuando** elige "cerrar todas mis sesiones", **entonces** se invalidan las sesiones en todos sus dispositivos.

### HU-001.4 · Mantener y expirar la sesión (P1)
1. **Dado** una sesión sin actividad durante el tiempo de inactividad configurado, **entonces** expira y el colaborador debe ingresar de nuevo.
2. **Dado** una sesión activa, **cuando** alcanza su duración máxima absoluta, **entonces** expira aunque haya actividad.
3. **Dado** una sesión vigente, **cuando** el colaborador la renueva antes de expirar, **entonces** obtiene continuidad sin volver a escribir su contraseña; la credencial de renovación anterior deja de servir.
4. **Dado** que se reutiliza una credencial de renovación ya usada, **entonces** se invalida toda la cadena de sesión y se registra `REUTILIZACION_SOSPECHOSA`.

### HU-001.5 · Recuperar contraseña (P2)
1. **Dado** un colaborador que olvidó su contraseña, **cuando** solicita recuperación con su identificador, **entonces** recibe por su canal registrado un enlace de un solo uso con vigencia limitada; la respuesta en pantalla es la misma exista o no la cuenta.
2. **Dado** un enlace válido, **cuando** define una contraseña que cumple la política, **entonces** se actualiza, se cierran todas sus sesiones activas y se registra `CONTRASENA_RESTABLECIDA`.
3. **Dado** un enlace usado o vencido, **entonces** se rechaza.

### HU-001.6 · Cambiar contraseña (P2)
1. **Dado** un colaborador autenticado, **cuando** cambia su contraseña indicando la actual correctamente, **entonces** se actualiza y se cierran sus demás sesiones.
2. **Dado** una nueva contraseña igual a alguna de las últimas N, **entonces** se rechaza.
3. **Dado** un colaborador marcado con "cambio obligatorio", **cuando** ingresa, **entonces** solo puede cambiar su contraseña antes de usar cualquier otra función.

### HU-001.7 · Segundo factor de autenticación (P3)
1. **Dado** un colaborador con MFA activo, **cuando** sus credenciales son válidas, **entonces** se le pide un código de segundo factor antes de crear la sesión.
2. **Dado** un rol marcado como "requiere MFA", **cuando** un colaborador con ese rol ingresa sin MFA configurado, **entonces** debe configurarlo antes de continuar.
3. **Dado** que perdió su dispositivo, **entonces** puede usar un código de respaldo de un solo uso.

## Requisitos funcionales
- **RF-001**: El sistema DEBE autenticar colaboradores mediante identificador (correo o nombre de usuario) y contraseña.
- **RF-002**: El sistema DEBE almacenar contraseñas únicamente con un algoritmo de hash adaptativo con sal; nunca en texto plano ni reversible.
- **RF-003**: El sistema DEBE aplicar una política de contraseñas configurable (longitud mínima por defecto 12, verificación contra contraseñas comprometidas conocidas, historial de N=5).
- **RF-004**: El sistema DEBE responder con un mensaje genérico e indistinguible ante usuario inexistente, contraseña incorrecta o cuenta inactiva.
- **RF-005**: El sistema DEBE bloquear temporalmente la cuenta tras N intentos fallidos consecutivos (por defecto 5 intentos, 15 minutos).
- **RF-006**: El sistema DEBE limitar la tasa de intentos por origen de red, además del bloqueo por cuenta.
- **RF-007**: El sistema DEBE emitir sesiones con vencimiento por inactividad y vencimiento absoluto, ambos configurables.
- **RF-008**: El sistema DEBE permitir renovar la sesión con rotación de la credencial de renovación y detección de reutilización.
- **RF-009**: El sistema DEBE permitir invalidar inmediatamente una sesión o todas las de un colaborador.
- **RF-010**: El sistema DEBE ofrecer recuperación de contraseña con enlace de un solo uso y vigencia configurable (por defecto 30 minutos).
- **RF-011**: El sistema DEBE soportar segundo factor (TOTP) con códigos de respaldo, activable por colaborador u obligatorio por rol.
- **RF-012**: La sesión emitida DEBE identificar al colaborador y permitir conocer sus roles y permisos vigentes (ver spec 002).
- **RF-013**: El sistema DEBE permitir que la verificación de credenciales la haga un proveedor de identidad intercambiable (almacén propio, directorio corporativo o servicio de identidad en la nube) sin cambiar el comportamiento descrito en este spec. Las políticas de bloqueo, sesión y auditoría se aplican igual sea cual sea el proveedor.

## Fuera de alcance
- Registro abierto (autoregistro) de usuarios: los colaboradores los da de alta un administrador (spec 003).
- Ingreso sin contraseña (passkeys, enlaces mágicos) — candidato a versión futura.

## Criterios de éxito
- **CE-001**: Un colaborador completa el ingreso en menos de 10 segundos (sin MFA) y menos de 30 (con MFA).
- **CE-002**: El 95 % de las validaciones de ingreso responden en menos de 500 ms bajo la carga esperada.
- **CE-003**: Cero contraseñas recuperables desde el almacenamiento o los registros.
- **CE-004**: Una sesión cerrada deja de autorizar operaciones en menos de 5 segundos.
- **CE-005**: Un ataque de fuerza bruta contra una cuenta no supera N intentos por ventana de bloqueo.

## Preguntas abiertas
1. ¿Identificador de ingreso: correo, nombre de usuario o ambos?
2. ¿Canal de recuperación: solo correo, o también SMS?
3. ¿Se permiten sesiones concurrentes ilimitadas por colaborador o hay un tope?
4. ¿SSO corporativo en la v1? (RF-013)
