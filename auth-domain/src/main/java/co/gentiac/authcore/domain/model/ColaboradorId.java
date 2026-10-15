package co.gentiac.authcore.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identificador opaco e inmutable de un colaborador. */
public record ColaboradorId(UUID valor) {

    public ColaboradorId {
        Objects.requireNonNull(valor, "valor");
    }

    public static ColaboradorId nuevo() {
        return new ColaboradorId(UUID.randomUUID());
    }
}
