
package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.TipoError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalculoProgresoTest {

    private IntentoService.PasoRegistrado crearPaso(
            int numero,
            boolean correcto,
            Long habilidadId,
            IntentoService.ErrorRegistrado... errores
    ) {
        return new IntentoService.PasoRegistrado(
                numero,
                correcto ? "Correcto" : "Incorrecto",
                correcto,
                habilidadId,
                List.of(errores)
        );
    }

    @Test
    void debeSumarAciertoPorPasoCorrecto() {

        var paso = crearPaso(1, true, 1L);

        var resultado = IntentoService.calcularResultados(
                "PRACTICA",
                List.of(paso)
        );

        assertEquals(
                new ProgresoHabilidadService.Resultado(1, 0),
                resultado.get(1L)
        );
    }

    @Test
    void debeSumarErrorPorPasoIncorrecto() {

        var paso = crearPaso(1, false, 1L);

        var resultado = IntentoService.calcularResultados(
                "PRACTICA",
                List.of(paso)
        );

        assertEquals(
                new ProgresoHabilidadService.Resultado(0, 1),
                resultado.get(1L)
        );
    }

    @Test
    void noDebeContarDosVecesLaMismaHabilidad() {

        var error = new IntentoService.ErrorRegistrado(
                1L,
                TipoError.ARITMETICO,
                "Calculo incorrecto"
        );

        var paso = crearPaso(
                1, false, 1L, error
        );

        var resultado = IntentoService.calcularResultados(
                "PRACTICA",
                List.of(paso)
        );

        assertEquals(
                new ProgresoHabilidadService.Resultado(0, 1),
                resultado.get(1L)
        );
    }

    @Test
    void debeRegistrarErroresDeDiferentesHabilidades() {

        var error = new IntentoService.ErrorRegistrado(
                2L,
                TipoError.SIGNO,
                "Error de signos"
        );

        var paso = crearPaso(
                1, false, 1L, error
        );

        var resultado = IntentoService.calcularResultados(
                "PRACTICA",
                List.of(paso)
        );

        assertEquals(2, resultado.size());

        assertEquals(
                new ProgresoHabilidadService.Resultado(0, 1),
                resultado.get(1L)
        );

        assertEquals(
                new ProgresoHabilidadService.Resultado(0, 1),
                resultado.get(2L)
        );
    }

    @Test
    void debeContabilizarErrorSinHabilidadEvaluada() {

        var error = new IntentoService.ErrorRegistrado(
                3L,
                TipoError.FRACCION,
                "Division incorrecta"
        );

        var paso = crearPaso(
                1, false, null, error
        );

        var resultado = IntentoService.calcularResultados(
                "PRACTICA",
                List.of(paso)
        );

        assertEquals(
                new ProgresoHabilidadService.Resultado(0, 1),
                resultado.get(3L)
        );
    }

    @Test
    void simulacionNoDebeModificarProgreso() {

        var paso = crearPaso(1, true, 1L);

        var resultado = IntentoService.calcularResultados(
                "SIMULACION",
                List.of(paso)
        );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void pasoSinHabilidadNoDebeGenerarEstadisticas() {

        var paso = crearPaso(
                1, true, null
        );

        var resultado = IntentoService.calcularResultados(
                "PRACTICA",
                List.of(paso)
        );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeAcumularResultadosDeVariosPasos() {

        var pasos = List.of(
                crearPaso(1, true, 1L),
                crearPaso(2, true, 1L),
                crearPaso(3, false, 1L),
                crearPaso(4, true, 2L)
        );

        Map<Long, ProgresoHabilidadService.Resultado> resultado =
                IntentoService.calcularResultados(
                        "PRACTICA",
                        pasos
                );

        assertEquals(
                new ProgresoHabilidadService.Resultado(2, 1),
                resultado.get(1L)
        );

        assertEquals(
                new ProgresoHabilidadService.Resultado(1, 0),
                resultado.get(2L)
        );
    }

    @Test
    void debeRechazarModoDesconocido() {

        var paso = crearPaso(1, true, 1L);

        assertThrows(IllegalArgumentException.class, () ->
                IntentoService.calcularResultados(
                        "DESCONOCIDO",
                        List.of(paso)
                )
        );
    }
}
