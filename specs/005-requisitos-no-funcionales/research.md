# Investigación — Soportar muchas peticiones de ingreso

Fecha: 2026-10-14 · Insumo para `plan.md`

## 1. Dónde está realmente el cuello de botella

Un sistema de login tiene dos tipos de tráfico muy distintos:

| Operación | Frecuencia | Costo | Estrategia |
|---|---|---|---|
| **Ingreso** (validar contraseña) | Baja: una vez por jornada por colaborador | **Alto**: el hash de contraseña es lento *a propósito* (CPU + memoria) | Aislar, limitar y escalar horizontalmente |
| **Verificar sesión/permiso** | Muy alta: cada petición a la app | Debe ser casi gratis | Token firmado verificado **localmente**, sin llamar a `auth-core` |

Conclusión: no se trata de hacer el login "rápido"; se trata de que **el login caro no contamine** el tráfico barato y de que el tráfico barato **nunca toque** el servicio de autenticación.

## 2. Arquitectura recomendada

```
                 ┌──────────── Borde ────────────┐
Colaboradores ─► │ CDN / WAF  →  Límite de tasa   │  (IP, cuenta, relleno de credenciales)
                 └──────────────┬────────────────┘
                                ▼
                 ┌─────── auth-core (N instancias sin estado, ≥ 2 zonas) ───────┐
                 │  Adaptador entrada REST                                       │
                 │      │                                                        │
                 │  Casos de uso ──► Puerto VerificadorCredenciales              │
                 │      │               ├─ Adaptador Cognito (hash en AWS)       │
                 │      │               ├─ Adaptador LDAP / Directory Service    │
                 │      │               └─ Adaptador local (Argon2id en          │
                 │      │                  pool acotado = bulkhead)              │
                 │      ├─► Puerto EmisorTokens (JWT firmado, clave en KMS)       │
                 │      ├─► Puerto AlmacenSesiones ──► Redis (renovación,       │
                 │      │                               revocación, contadores)  │
                 │      ├─► Puerto RepositorioRoles ──► PostgreSQL (+ réplicas)  │
                 │      └─► Puerto Auditoria ──► outbox → cola (SQS/Kafka)       │
                 └──────────────────────────────────────────────────────────────┘
                                ▲ JWKS (clave pública, en caché)
Apps consumidoras ──────────────┘  verifican el token localmente en cada petición
```

### Decisiones clave
1. **Tokens de acceso cortos, firmados con clave asimétrica** (ES256/RS256). Cada aplicación verifica la firma con la clave pública publicada en JWKS. Así 100 000 verificaciones/s no generan ni una llamada a `auth-core` (RNF-002, RNF-022).
2. **Revocación rápida sin perder lo anterior**: el token de acceso dura ≤ 15 min; para cumplir RF-025 y CE-041 (≤ 60 s / ≤ 5 s) se mantiene en Redis un "número de versión de seguridad" por colaborador que las apps consultan con caché corta, o una lista de revocación de vida igual a la del token.
3. **Bulkhead para el hash**: los hilos virtuales de Java 21 ayudan con E/S, **no** con trabajo de CPU. El hash Argon2id corre en un pool de tamaño = núcleos disponibles, con cola acotada; si se llena, respuesta 503 + `Retry-After` (RNF-013, RNF-014).
4. **Límite de tasa distribuido** en Redis (token bucket) por IP y por cuenta, además del WAF. Esto también reduce el trabajo de hash desperdiciado en ataques.
5. **Auditoría asíncrona con outbox**: el evento se escribe en la misma transacción y se publica a una cola, para no sumar latencia al login. Las operaciones administrativas críticas sí esperan confirmación (RF-064).
6. **Lecturas de roles desde caché** con invalidación por evento; réplicas de lectura en PostgreSQL.
7. **Autoescalado** por CPU y por profundidad de la cola del pool de hash, con capacidad mínima precalentada antes de horarios pico conocidos.

## 3. Opción gestionada: Amazon Cognito

Cognito elimina el costo de CPU del hash en nuestras instancias, pero **traslada el límite a las cuotas de AWS**. Datos de la documentación oficial:

- La categoría **UserAuthentication** (InitiateAuth, renovación de tokens, login gestionado) tiene por defecto **120 peticiones/s**, ajustable, y la cuota es **por cuenta y por región, compartida entre todos los user pools**.
- Las respuestas a desafíos (por ejemplo, el código MFA) tienen una cuota aparte de 3× ese valor.
- El login gestionado (hosted UI) tiene topes **no ajustables**: 300 peticiones/s por IP de origen, 300 por app client y 500 por dominio.
- Recuperación y cambio de contraseña: 30 peticiones/s, **no ajustable**.
- Hasta 10 000 grupos por user pool y 100 grupos por usuario (no ajustables) — suficiente para mapear roles, pero los **permisos finos `recurso:acción` deben seguir en `auth-core`**.
- Desde julio de 2026 existen **límites aprovisionados**: se pueden subir las cuotas de autenticación al momento desde la consola o la API, pagando solo la capacidad por encima del valor por defecto. AWS recomienda alarmas al 70 % y 85 % y subir la capacidad antes de picos esperados.

**Implicación para el nivel M (pico 1 000 ingresos/s):** con Cognito hay que aprovisionar ~1 000 RPS de UserAuthentication (costo adicional) y **evitar el login gestionado** (tope de 500/dominio), usando el flujo por API desde nuestro adaptador. Con renovaciones de token también contando en la misma categoría, conviene que las renovaciones sean poco frecuentes o hacerlas en `auth-core`.

### Opción propia (Argon2id en nuestras instancias) — estimación a validar
Con parámetros OWASP (19 MiB, t=2) un hash suele tomar del orden de decenas de ms por núcleo. Como orden de magnitud, ~20–40 ingresos/s por vCPU dedicada, es decir decenas de vCPU para 1 000 ingresos/s en pico. **Este número debe confirmarse con un benchmark** en el hardware real antes de dimensionar (RNF-005).

## 4. Recomendación
| Criterio | Cognito | Directorio LDAP | Propio (Argon2id) |
|---|---|---|---|
| Esfuerzo inicial | Bajo | Medio | Alto |
| Escala del login | Limitada por cuotas (ajustables, con costo) | Limitada por el directorio | Lineal con instancias |
| MFA / recuperación | Incluido | Parcial | A construir |
| Dependencia de proveedor | Alta (mitigada por hexagonal) | Media | Ninguna |

**Recomendación v1:** adaptador **Cognito** para validar credenciales y MFA, con **roles y permisos en `auth-core`** (grupos de Cognito → roles internos), tokens propios verificados localmente y el adaptador local como alternativa lista para el día en que las cuotas o el costo lo justifiquen. La arquitectura hexagonal hace que ese cambio sea solo un adaptador nuevo.

## Fuentes
- [Quotas in Amazon Cognito — AWS](https://docs.aws.amazon.com/cognito/latest/developerguide/limits.html)
- [Amazon Cognito launches provisioned limits — AWS Security Blog](https://aws.amazon.com/blogs/security/from-2-weeks-to-2-minutes-amazon-cognito-launches-provisioned-limits-for-self-service-rate-limit-management/)
- [Password Storage Cheat Sheet — OWASP](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
