
package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.service.PerfilService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PerfilServiceTest {

    @Test
    void debeNormalizarNombre() {
        String resultado =
                PerfilService.validarNombre("  Anthony  ");

        assertEquals("Anthony", resultado);
    }

    @Test
    void debeRechazarNombreVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> PerfilService.validarNombre("   "));
    }

    @Test
    void debeRechazarNombreNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> PerfilService.validarNombre(null));
    }

    @Test
    void debeRechazarNombreMuyLargo() {
        String nombre = "A".repeat(81);

        assertThrows(IllegalArgumentException.class,
                () -> PerfilService.validarNombre(nombre));
    }

    @Test
    void debeRechazarCaracteresDeControl() {
        assertThrows(IllegalArgumentException.class,
                () -> PerfilService.validarNombre("Ana\nMaria"));
    }

    @Test
    void debeAceptarNombreDe80Caracteres() {
        String nombre = "A".repeat(80);

        assertEquals(nombre,
                PerfilService.validarNombre(nombre));
    }
}
