
package ni.edu.uam.algomentor.service;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProgresoHabilidadServiceTest {

    // ========================================
    // VALIDACION DE RESULTADOS
    // ========================================

    @Test
    void debeAceptarResultadoValido() {
        var resultado =
                new ProgresoHabilidadService.Resultado(3, 2);

        assertEquals(3, resultado.aciertos());
        assertEquals(2, resultado.errores());
    }

    @Test
    void debeAceptarSoloAciertos() {
        assertDoesNotThrow(() ->
                new ProgresoHabilidadService.Resultado(5, 0)
        );
    }

    @Test
    void debeAceptarSoloErrores() {
        assertDoesNotThrow(() ->
                new ProgresoHabilidadService.Resultado(0, 3)
        );
    }

    @Test
    void debeRechazarAciertosNegativos() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProgresoHabilidadService.Resultado(-1, 2)
        );
    }

    @Test
    void debeRechazarErroresNegativos() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProgresoHabilidadService.Resultado(2, -1)
        );
    }

    @Test
    void debeRechazarResultadoSinEvaluaciones() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProgresoHabilidadService.Resultado(0, 0)
        );
    }

    // ========================================
    // VALIDACION DE HABILIDADES
    // ========================================

    @Test
    void debeAceptarResultadosMultiples() {
        Map<Long, ProgresoHabilidadService.Resultado> datos =
                Map.of(
                        1L,
                        new ProgresoHabilidadService.Resultado(5, 2),
                        2L,
                        new ProgresoHabilidadService.Resultado(3, 1)
                );

        assertDoesNotThrow(() ->
                ProgresoHabilidadService.validarResultados(datos)
        );
    }

    @Test
    void debeAceptarMapaVacio() {
        assertDoesNotThrow(() ->
                ProgresoHabilidadService.validarResultados(
                        Map.of()
                )
        );
    }

    @Test
    void debeRechazarMapaNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                ProgresoHabilidadService.validarResultados(null)
        );
    }

    @Test
    void debeRechazarIdHabilidadNulo() {
        Map<Long, ProgresoHabilidadService.Resultado> datos =
                new HashMap<>();

        datos.put(
                null,
                new ProgresoHabilidadService.Resultado(2, 1)
        );

        assertThrows(IllegalArgumentException.class, () ->
                ProgresoHabilidadService.validarResultados(datos)
        );
    }

    @Test
    void debeRechazarIdHabilidadInvalido() {
        var datos = Map.of(
                -1L,
                new ProgresoHabilidadService.Resultado(2, 1)
        );

        assertThrows(IllegalArgumentException.class, () ->
                ProgresoHabilidadService.validarResultados(datos)
        );
    }

    @Test
    void debeRechazarResultadoNulo() {
        Map<Long, ProgresoHabilidadService.Resultado> datos =
                new HashMap<>();

        datos.put(1L, null);

        assertThrows(IllegalArgumentException.class, () ->
                ProgresoHabilidadService.validarResultados(datos)
        );
    }
}
