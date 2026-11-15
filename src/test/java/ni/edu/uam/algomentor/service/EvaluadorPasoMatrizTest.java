package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.TipoError;
import ni.edu.uam.algomentor.engine.TipoOperacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EvaluadorPasoMatrizTest {

    @Test
    void conectaEntradaOperacionVerificadorYClasificacion() {
        var resultado = new EvaluadorPasoMatriz().evaluar("1,1,3\n1,-1,1", TipoOperacion.ELIMINAR_FILA,
                "2", "1", "1", "1,1,3\n0,-2,2");
        assertFalse(resultado.correcto());
        assertEquals(TipoError.SIGNO, resultado.errores().getFirst().tipo());
        assertEquals(1, resultado.errores().getFirst().fila());
        assertEquals(2, resultado.errores().getFirst().columna());
        assertEquals("Signos", HabilidadErrorResolver.nombreHabilidad(resultado.errores().getFirst().tipo()));
    }

    @Test
    void aceptaPasoCorrecto() {
        var resultado = new EvaluadorPasoMatriz().evaluar("v1;MATRIZ;2x3;1,1,3|1,-1,1",
                TipoOperacion.ELIMINAR_FILA, "2", "1", "1", "1,1,3\n0,-2,-2");
        assertTrue(resultado.correcto());
    }
}
