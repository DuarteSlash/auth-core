package co.gentiac.authcore.application.service;

import co.gentiac.authcore.domain.model.ColaboradorId;
import co.gentiac.authcore.domain.model.Permiso;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Escenarios de aceptación de HU-002.1 (spec 002). */
class VerificarPermisoServiceTest {

    private final ColaboradorId supervisor = ColaboradorId.nuevo();

    private final VerificarPermisoService servicio = new VerificarPermisoService(
            id -> id.equals(supervisor) ? Set.of(Permiso.de("reportes:leer")) : Set.of());

    @Test
    void escenario1_permiteSiUnRolConcedeElPermiso() {
        assertTrue(servicio.tienePermiso(supervisor, Permiso.de("reportes:leer")));
    }

    @Test
    void escenario2_niegaSiNingunRolConcedeElPermiso() {
        assertFalse(servicio.tienePermiso(supervisor, Permiso.de("reportes:eliminar")));
    }

    @Test
    void escenario3_niegaPorDefectoAColaboradorSinPermisos() {
        assertFalse(servicio.tienePermiso(ColaboradorId.nuevo(), Permiso.de("reportes:leer")));
    }

    @Test
    void niegaAnteEntradasNulas() {
        assertFalse(servicio.tienePermiso(null, Permiso.de("reportes:leer")));
        assertFalse(servicio.tienePermiso(supervisor, null));
    }
}
