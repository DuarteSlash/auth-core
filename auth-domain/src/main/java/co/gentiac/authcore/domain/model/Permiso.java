package co.gentiac.authcore.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Unidad mínima de autorización, expresada como {@code recurso:acción} (spec 002, RF-021).
 * Admite el comodín de acción {@code recurso:*}.
 */
public record Permiso(String recurso, String accion) {

    public static final String COMODIN = "*";
    private static final Pattern SEGMENTO = Pattern.compile("[a-z][a-z0-9-]{0,62}");

    public Permiso {
        Objects.requireNonNull(recurso, "recurso");
        Objects.requireNonNull(accion, "accion");
        if (!SEGMENTO.matcher(recurso).matches()) {
            throw new IllegalArgumentException("Recurso inválido: " + recurso);
        }
        if (!COMODIN.equals(accion) && !SEGMENTO.matcher(accion).matches()) {
            throw new IllegalArgumentException("Acción inválida: " + accion);
        }
    }

    /** Crea un permiso a partir de su forma textual {@code recurso:acción}. */
    public static Permiso de(String codigo) {
        Objects.requireNonNull(codigo, "codigo");
        int separador = codigo.indexOf(':');
        if (separador <= 0 || separador != codigo.lastIndexOf(':')) {
            throw new IllegalArgumentException("Formato esperado recurso:acción, recibido: " + codigo);
        }
        return new Permiso(codigo.substring(0, separador), codigo.substring(separador + 1));
    }

    /** Indica si este permiso concedido cubre el permiso solicitado. */
    public boolean cubre(Permiso solicitado) {
        return recurso.equals(solicitado.recurso)
                && (COMODIN.equals(accion) || accion.equals(solicitado.accion));
    }

    @Override
    public String toString() {
        return recurso + ":" + accion;
    }
}
