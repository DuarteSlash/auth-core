# Spec 005 — Requisitos no funcionales (atributos de calidad)

| Campo | Valor |
|---|---|
| Estado | Borrador — pendiente de clarificación de volúmenes |
| Creado | 2026-10-14 |
| Aplica a | 001, 002, 003, 004 |
| Investigación | [research.md](research.md) — cómo soportar alto volumen de ingresos |

## Contexto
Los specs 001–004 dicen **qué** hace `auth-core`. Este spec dice **qué tan bien** debe hacerlo. Cada requisito es medible y se verifica con una prueba (carga, seguridad, caos o revisión). Los valores marcados con ⚙ son parámetros por defecto que cada despliegue puede ajustar.

## Perfil de carga de referencia
[NECESITA CLARIFICACIÓN] Volúmenes reales esperados. Mientras tanto se diseña para tres niveles:

| Nivel | Colaboradores registrados | Ingresos/s sostenidos | Ingresos/s en pico (5 min) | Verificaciones de permiso/s |
|---|---|---|---|---|
| S | 10 000 | 20 | 100 | 1 000 |
| M (objetivo v1) | 500 000 | 200 | 1 000 | 20 000 |
| L | 5 000 000 | 1 000 | 5 000 | 100 000 |

> Un pico típico de ingreso ocurre al inicio de la jornada o tras una caída: muchos colaboradores intentan entrar al mismo tiempo.

## 1. Rendimiento
- **RNF-001**: Ingreso (validación de credenciales + emisión de sesión): p95 ≤ 500 ms, p99 ≤ 1 s, en el nivel objetivo.
- **RNF-002**: Verificación de una sesión/token en una aplicación consumidora: p99 ≤ 5 ms y **sin llamada remota** a `auth-core` por cada petición.
- **RNF-003**: Verificación de permiso (`¿tiene recurso:acción?`): p99 ≤ 50 ms (CE-020).
- **RNF-004**: Renovación de sesión: p95 ≤ 200 ms.
- **RNF-005**: El costo de cómputo del hash de contraseña DEBE ser ≤ 1 s y se calibra con benchmark en el hardware de producción.

## 2. Escalabilidad
- **RNF-010**: El servicio DEBE ser **sin estado** en sus instancias; cualquier instancia atiende cualquier petición. Todo estado compartido vive en almacenes externos.
- **RNF-011**: Escalado horizontal automático: añadir instancias DEBE aumentar la capacidad de ingreso de forma aproximadamente lineal hasta el nivel L.
- **RNF-012**: Absorber un pico de 5× la carga sostenida durante 5 min sin incumplir RNF-001 en más del 1 % de peticiones.
- **RNF-013**: El cómputo de hash de contraseñas DEBE ejecutarse en un compartimento con capacidad acotada (bulkhead) para que una avalancha de ingresos no degrade la verificación de tokens ni la administración.
- **RNF-014**: Cuando la capacidad se agote, el sistema DEBE rechazar de forma controlada (HTTP 429/503 con `Retry-After`) en lugar de encolar sin límite o caerse.
- **RNF-015**: Si se usa un proveedor de identidad gestionado, sus cuotas de tasa DEBEN estar dimensionadas para el pico, monitoreadas y con alarmas al 70 % y 85 %.

## 3. Disponibilidad y resiliencia
- **RNF-020**: Disponibilidad mensual del ingreso ≥ 99,9 % ⚙ (≈ 43 min de indisponibilidad/mes).
- **RNF-021**: Despliegue en al menos 2 zonas de disponibilidad; la pérdida de una zona no interrumpe el servicio.
- **RNF-022**: Las sesiones ya emitidas DEBEN seguir siendo verificables aunque `auth-core` esté caído (verificación local con clave pública).
- **RNF-023**: Llamadas a dependencias externas con tiempo máximo de espera, reintentos con retroceso exponencial y cortocircuito (circuit breaker).
- **RNF-024**: RPO ≤ 5 min y RTO ≤ 30 min ⚙ para colaboradores, roles y auditoría.
- **RNF-025**: Despliegues sin interrupción (rolling o blue/green) y rotación de claves de firma sin cerrar sesiones vigentes.

## 4. Seguridad
- **RNF-030**: Cumplimiento verificable de OWASP ASVS nivel 2 en las secciones V2 (autenticación), V3 (sesiones), V4 (control de acceso) y V7 (registros).
- **RNF-031**: Hash de contraseñas con Argon2id (mínimo m=19 MiB, t=2, p=1); PBKDF2-HMAC-SHA256 con 600 000 iteraciones solo si se exige FIPS-140.
- **RNF-032**: Tokens de sesión firmados con algoritmo asimétrico; claves privadas en un gestor de secretos/KMS, nunca en código ni repositorio; rotación ⚙ cada 90 días.
- **RNF-033**: Token de acceso de vida corta ⚙ (≤ 15 min); credencial de renovación rotativa ⚙ (≤ 8 h inactividad, ≤ 12 h absoluta).
- **RNF-034**: TLS 1.2+ en todo tránsito; cifrado en reposo de datos personales y auditoría.
- **RNF-035**: Protección perimetral: firewall de aplicaciones web, limitación de tasa por IP y por cuenta, y detección de relleno de credenciales.
- **RNF-036**: Análisis de seguridad en cada cambio: dependencias vulnerables (SCA), código (SAST) y secretos expuestos; ningún hallazgo crítico se despliega.
- **RNF-037**: Prueba de penetración antes de la primera salida a producción y luego anual.

## 5. Observabilidad
- **RNF-040**: Métricas expuestas: ingresos exitosos/fallidos por segundo, latencias p50/p95/p99, cuentas bloqueadas, rechazos por capacidad, uso de cuotas del proveedor.
- **RNF-041**: Trazas distribuidas con identificador de correlación de punta a punta (el mismo de la auditoría, spec 004).
- **RNF-042**: Logs estructurados (JSON) sin datos sensibles (RF-062).
- **RNF-043**: Alertas por: tasa de fallos anómala, latencia fuera de objetivo, error de proveedor de identidad, consumo de cuota ≥ 70 %.
- **RNF-044**: Tableros de SLO con presupuesto de error.

## 6. Mantenibilidad y calidad de código
- **RNF-050**: Arquitectura hexagonal verificada automáticamente: el dominio no depende de frameworks ni adaptadores (prueba de arquitectura en CI).
- **RNF-051**: Cobertura de pruebas ≥ 80 % en dominio y aplicación; 100 % de escenarios de aceptación de los specs automatizados.
- **RNF-052**: Cambiar de proveedor de identidad DEBE requerir solo un adaptador nuevo y configuración.
- **RNF-053**: Integración continua obligatoria: compilación, pruebas, análisis estático y de seguridad en cada pull request.

## 7. Portabilidad e integración
- **RNF-060**: Empaquetado como contenedor OCI configurable por variables de entorno.
- **RNF-061**: Contratos de API documentados (OpenAPI) y estándares abiertos (OAuth 2.1 / OpenID Connect, JWKS) para que cualquier aplicación, en cualquier lenguaje, se integre.
- **RNF-062**: Infraestructura definida como código.

## 8. Cumplimiento y privacidad
- **RNF-070**: Tratamiento de datos personales conforme a la Ley 1581 de 2012 (Colombia): finalidad, mínimo necesario, y derecho de consulta/rectificación del titular.
- **RNF-071**: Datos personales y auditoría almacenados en región definida ⚙ [NECESITA CLARIFICACIÓN: ¿restricción de residencia de datos?].

## 9. Costo
- **RNF-080**: Costo de infraestructura medible por colaborador activo mensual; el diseño DEBE permitir reducir capacidad fuera de picos.

## Verificación
| Atributo | Cómo se verifica |
|---|---|
| Rendimiento / escalabilidad | Pruebas de carga (Gatling o k6) por nivel S/M/L en cada release |
| Resiliencia | Pruebas de caos: caída de instancia, de zona, del proveedor de identidad y de la caché |
| Seguridad | Lista de chequeo ASVS, SAST/SCA en CI, pentest |
| Arquitectura | Pruebas ArchUnit en CI |
| Observabilidad | Revisión de tableros y simulacro de alerta |

## Preguntas abiertas
1. ¿Qué nivel (S/M/L) corresponde a la primera aplicación que usará `auth-core`?
2. ¿Disponibilidad objetivo: 99,9 % o 99,95 %?
3. ¿Hay restricción de residencia de datos (ej. datos en Colombia o región específica de AWS)?
4. ¿Se requiere cumplimiento FIPS-140?
