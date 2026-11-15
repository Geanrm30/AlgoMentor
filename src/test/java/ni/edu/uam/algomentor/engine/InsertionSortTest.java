package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsertionSortTest {

    private final InsertionSort insertion = new InsertionSort();

    @Test
    void ordenaUnArregloDesordenado() {
        ResultadoEjecucion r = insertion.ejecutar(new int[]{5, 3, 8, 1, 2});
        assertEquals("[5, 3, 8, 1, 2]", r.estadoInicial());
        assertEquals("[1, 2, 3, 5, 8]", r.estadoFinal());
    }

    @Test
    void ordenaArregloEnOrdenInverso() {
        ResultadoEjecucion r = insertion.ejecutar(new int[]{4, 3, 2, 1});
        assertEquals("[1, 2, 3, 4]", r.estadoFinal());
    }

    @Test
    void manejaElementosRepetidosYNegativos() {
        ResultadoEjecucion r = insertion.ejecutar(new int[]{2, -1, 2, 0, -1});
        assertEquals("[-1, -1, 0, 2, 2]", r.estadoFinal());
    }

    @Test
    void arregloYaOrdenadoNoIntercambia() {
        ResultadoEjecucion r = insertion.ejecutar(new int[]{1, 2, 3});
        assertTrue(r.pasos().stream().noneMatch(p -> p.tipo() == TipoOperacion.INTERCAMBIAR));
    }

    @Test
    void unSoloElementoSoloTienePasoFinal() {
        ResultadoEjecucion r = insertion.ejecutar(new int[]{9});
        assertEquals(1, r.pasos().size());
        assertEquals(TipoOperacion.FINALIZAR, r.pasos().get(0).tipo());
    }

    @Test
    void noModificaElArregloOriginal() {
        int[] original = {3, 1, 2};
        insertion.ejecutar(original);
        assertArrayEquals(new int[]{3, 1, 2}, original);
    }

    @Test
    void rechazaArregloVacioONulo() {
        assertThrows(IllegalArgumentException.class, () -> insertion.ejecutar(new int[0]));
        assertThrows(IllegalArgumentException.class, () -> insertion.ejecutar(null));
    }

    @Test
    void cadaPasoEncadenaConElSiguiente() {
        ResultadoEjecucion r = insertion.ejecutar(new int[]{4, 2, 9, 1});
        for (int i = 1; i < r.pasos().size(); i++) {
            assertEquals(r.pasos().get(i - 1).estadoPosterior(), r.pasos().get(i).estadoAnterior());
            assertEquals(i + 1, r.pasos().get(i).numero());
        }
        assertEquals(r.estadoInicial(), r.pasos().get(0).estadoAnterior());
    }
}
