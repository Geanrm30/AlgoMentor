
package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.service.IntentoService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntentoServiceTest {

    private List<IntentoService.PasoRegistrado> pasosValidos() {
        return List.of(
                new IntentoService.PasoRegistrado(1, "5", true),
                new IntentoService.PasoRegistrado(2, "10", true)
        );
    }

    @Test
    void debeAceptarIntentoDePractica() {
        assertDoesNotThrow(() ->
                IntentoService.validarDatos(
                        1L, 2L, "PRACTICA", 120, pasosValidos()
                )
        );
    }

    @Test
    void debeAceptarIntentoDeSimulacion() {
        assertDoesNotThrow(() ->
                IntentoService.validarDatos(
                        1L, 2L, "SIMULACION", 0, pasosValidos()
                )
        );
    }

    @Test
    void debeRechazarPerfilInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                IntentoService.validarDatos(
                        -1L, 2L, "PRACTICA", 60, pasosValidos()
                )
        );
    }

    @Test
    void debeRechazarEjercicioInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                IntentoService.validarDatos(
                        1L, 0L, "PRACTICA", 60, pasosValidos()
                )
        );
    }

    @Test
    void debeRechazarModoDesconocido() {
        assertThrows(IllegalArgumentException.class, () ->
                IntentoService.validarDatos(
                        1L, 2L, "EXAMEN", 60, pasosValidos()
                )
        );
    }

    @Test
    void debeRechazarDuracionNegativa() {
        assertThrows(IllegalArgumentException.class, () ->
                IntentoService.validarDatos(
                        1L, 2L, "PRACTICA", -10, pasosValidos()
                )
        );
    }

    @Test
    void debeRechazarListaDePasosVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                IntentoService.validarDatos(
                        1L, 2L, "PRACTICA", 60, List.of()
                )
        );
    }

    @Test
    void debeRechazarPasosDuplicados() {
        var pasos = List.of(
                new IntentoService.PasoRegistrado(1, "5", true),
                new IntentoService.PasoRegistrado(1, "8", false)
        );

        assertThrows(IllegalArgumentException.class, () ->
                IntentoService.validarDatos(
                        1L, 2L, "PRACTICA", 60, pasos
                )
        );
    }

    @Test
    void debeRechazarNumeroDePasoInvalido() {
        var pasos = List.of(
                new IntentoService.PasoRegistrado(0, "5", true)
        );

        assertThrows(IllegalArgumentException.class, () ->
                IntentoService.validarDatos(
                        1L, 2L, "PRACTICA", 60, pasos
                )
        );
    }

    @Test
    void debeRechazarPasoNulo() {
        var pasos = Arrays.asList(
                new IntentoService.PasoRegistrado(1, "5", true),
                null
        );

        assertThrows(IllegalArgumentException.class, () ->
                IntentoService.validarDatos(
                        1L, 2L, "PRACTICA", 60, pasos
                )
        );
    }
}
