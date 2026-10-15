/**
 * Adaptadores de la arquitectura hexagonal.
 *
 * <ul>
 *   <li>{@code adapter.in.rest} — API REST / OpenID Connect para las aplicaciones consumidoras.</li>
 *   <li>{@code adapter.out.cognito} — {@code VerificadorCredenciales} sobre Amazon Cognito.</li>
 *   <li>{@code adapter.out.ldap} — {@code VerificadorCredenciales} sobre AWS Directory Service (LDAP).</li>
 *   <li>{@code adapter.out.local} — {@code VerificadorCredenciales} con Argon2id en pool acotado (bulkhead).</li>
 *   <li>{@code adapter.out.persistence} — {@code RepositorioPermisos} sobre PostgreSQL.</li>
 *   <li>{@code adapter.out.redis} — sesiones, revocación y límite de tasa.</li>
 *   <li>{@code adapter.out.audit} — {@code PublicadorAuditoria} con outbox hacia cola.</li>
 * </ul>
 *
 * Cada adaptador se implementa cuando su spec llegue a la fase tasks/implement.
 */
package co.gentiac.authcore.infrastructure;
