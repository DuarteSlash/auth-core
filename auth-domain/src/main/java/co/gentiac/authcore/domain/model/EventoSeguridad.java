package co.gentiac.authcore.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Evento de seguridad auditable (spec 004). Nunca contiene contraseñas, tokens ni secretos (RF-062).
 *
 * @param tipo           código del catálogo, p. ej. {@code LOGIN_EXITOSO}
 * @param afectado       colaborador sobre el que recae el evento, si se conoce
 * @param autor          quien ejecuta la acción cuando difiere del afectado
 * @param instante       momento en UTC
 * @param correlacion    identificador de correlación de punta a punta (RNF-041)
 * @param resultado      resultado de la operación
 */
public record EventoSeguridad(
        String tipo,
        Optional<ColaboradorId> afectado,
        Optional<ColaboradorId> autor,
        Instant instante,
        String correlacion,
        Resultado resultado) {

    public enum Resultado { EXITO, FALLO, DENEGADO }

    public EventoSeguridad {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(afectado, "afectado");
        Objects.requireNonNull(autor, "autor");
        Objects.requireNonNull(instante, "instante");
        Objects.requireNonNull(correlacion, "correlacion");
        Objects.requireNonNull(resultado, "resultado");
    }
}
