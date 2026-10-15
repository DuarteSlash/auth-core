package co.gentiac.authcore.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermisoTest {

    @Test
    void parseaFormatoRecursoAccion() {
        Permiso p = Permiso.de("reportes:leer");
        assertEquals("reportes", p.recurso());
        assertEquals("leer", p.accion());
        assertEquals("reportes:leer", p.toString());
    }

    @Test
    void rechazaFormatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> Permiso.de("reportes"));
        assertThrows(IllegalArgumentException.class, () -> Permiso.de(":leer"));
        assertThrows(IllegalArgumentException.class, () -> Permiso.de("a:b:c"));
        assertThrows(IllegalArgumentException.class, () -> Permiso.de("Reportes:leer"));
        assertThrows(IllegalArgumentException.class, () -> Permiso.de("*:leer"));
    }

    @Test
    void comodinCubreCualquierAccionDelMismoRecurso() {
        Permiso todo = Permiso.de("reportes:*");
        assertTrue(todo.cubre(Permiso.de("reportes:eliminar")));
        assertFalse(todo.cubre(Permiso.de("usuarios:leer")));
    }

    @Test
    void permisoExactoSoloCubreSuAccion() {
        Permiso leer = Permiso.de("reportes:leer");
        assertTrue(leer.cubre(Permiso.de("reportes:leer")));
        assertFalse(leer.cubre(Permiso.de("reportes:eliminar")));
    }
}
