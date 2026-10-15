package co.gentiac.authcore.application.port.out;

import co.gentiac.authcore.domain.model.ColaboradorId;
import co.gentiac.authcore.domain.model.Permiso;

import java.util.Set;

/** Puerto de salida: permisos efectivos (unión de roles vigentes, incluida la herencia) — spec 002, RF-022/023. */
public interface RepositorioPermisos {

    Set<Permiso> permisosEfectivos(ColaboradorId colaborador);
}
