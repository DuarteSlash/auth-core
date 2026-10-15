package co.gentiac.authcore.application.port.in;

import co.gentiac.authcore.domain.model.ColaboradorId;
import co.gentiac.authcore.domain.model.Permiso;

/** Puerto de entrada: ¿el colaborador puede ejecutar {@code recurso:acción}? (spec 002, HU-002.1). */
public interface VerificarPermisoUseCase {

    boolean tienePermiso(ColaboradorId colaborador, Permiso solicitado);
}
