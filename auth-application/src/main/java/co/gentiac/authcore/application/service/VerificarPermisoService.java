package co.gentiac.authcore.application.service;

import co.gentiac.authcore.application.port.in.VerificarPermisoUseCase;
import co.gentiac.authcore.application.port.out.RepositorioPermisos;
import co.gentiac.authcore.domain.model.ColaboradorId;
import co.gentiac.authcore.domain.model.Permiso;

import java.util.Objects;
import java.util.Set;

/**
 * Denegar por defecto (constitución III, RF-020): solo se permite si algún permiso efectivo
 * cubre explícitamente el solicitado.
 */
public final class VerificarPermisoService implements VerificarPermisoUseCase {

    private final RepositorioPermisos repositorio;

    public VerificarPermisoService(RepositorioPermisos repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
    }

    @Override
    public boolean tienePermiso(ColaboradorId colaborador, Permiso solicitado) {
        if (colaborador == null || solicitado == null) {
            return false;
        }
        Set<Permiso> efectivos = repositorio.permisosEfectivos(colaborador);
        return efectivos != null && efectivos.stream().anyMatch(p -> p.cubre(solicitado));
    }
}
