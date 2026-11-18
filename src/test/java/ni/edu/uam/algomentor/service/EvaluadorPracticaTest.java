package ni.edu.uam.algomentor.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EvaluadorPracticaTest {

    @org.junit.jupiter.api.Test void evaluaLosSieteOrdenamientosConDatosSimplesYVersionados() {
        for (var algoritmo : ni.edu.uam.algomentor.engine.CatalogoAlgoritmos.ordenamientos()) {
            for (String datos : new String[]{"-3,0,2,-3", "v1;ARREGLO;-3,0,2,-3"}) {
                assertTrue(EvaluadorPractica.evaluar(algoritmo.nombre(), datos, "-3,-3,0,2").correcto());
                assertFalse(EvaluadorPractica.evaluar(algoritmo.nombre(), datos, "2,0,-3,-3").correcto());
            }
            assertTrue(EvaluadorPractica.evaluar(algoritmo.nombre(), "2147483647,-2147483648,0",
                    "-2147483648,0,2147483647").correcto());
        }
    }

    @Test
    void evaluaOrdenamientoSinDependerDelFormatoVisual() {
        assertTrue(EvaluadorPractica.evaluar("Bubble Sort", "v1;ARREGLO;3,1,2", "1, 2, 3").correcto());
        assertFalse(EvaluadorPractica.evaluar("Insertion Sort", "v1;ARREGLO;3,1,2", "3, 2, 1").correcto());
        assertTrue(EvaluadorPractica.evaluar("Bubble Sort", "3,1,2", "1,2,3").correcto());
    }

    @Test
    void evaluaSolucionConFraccionesExactas() {
        var resultado = EvaluadorPractica.evaluar("Gauss-Jordan", "v1;MATRIZ;2x3;1,1,3|1,-1,1", "2, 1");
        assertTrue(resultado.correcto());
        assertThrows(IllegalArgumentException.class,
                () -> EvaluadorPractica.evaluar("Gauss", "v1;MATRIZ;2x3;1,1,3|1,-1,1", "2"));
    }
}
