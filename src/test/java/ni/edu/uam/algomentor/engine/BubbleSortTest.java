package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BubbleSortTest {

    private final BubbleSort bubble = new BubbleSort();

    @Test
    void ordenaUnArregloDesordenado() {
        ResultadoEjecucion r = bubble.ejecutar(new int[]{5, 3, 8, 1, 2});
        assertEquals("[5, 3, 8, 1, 2]", r.estadoInicial());
        assertEquals("[1, 2, 3, 5, 8]", r.estadoFinal());
    }

    @Test
    void arregloYaOrdenadoNoIntercambia() {
        ResultadoEjecucion r = bubble.ejecutar(new int[]{1, 2, 3, 4});
        assertTrue(r.pasos().stream().noneMatch(p -> p.tipo() == TipoOperacion.INTERCAMBIAR));
        assertEquals("[1, 2, 3, 4]", r.estadoFinal());
    }

    @Test
    void unSoloElementoSoloTienePasoFinal() {
        ResultadoEjecucion r = bubble.ejecutar(new int[]{7});
        assertEquals(1, r.pasos().size());
        assertEquals(TipoOperacion.FINALIZAR, r.pasos().get(0).tipo());
    }

    @Test
    void noModificaElArregloOriginal() {
        int[] original = {3, 1, 2};
        bubble.ejecutar(original);
        assertArrayEquals(new int[]{3, 1, 2}, original);
    }

    @Test
    void rechazaArregloVacioONulo() {
        assertThrows(IllegalArgumentException.class, () -> bubble.ejecutar(new int[0]));
        assertThrows(IllegalArgumentException.class, () -> bubble.ejecutar(null));
    }

    @Test
    void pasosNumeradosConsecutivamente() {
        ResultadoEjecucion r = bubble.ejecutar(new int[]{4, 2, 9, 1});
        for (int i = 0; i < r.pasos().size(); i++) {
            assertEquals(i + 1, r.pasos().get(i).numero());
        }
    }

    @Test
    void ultimoPasoEsFinalizar() {
        ResultadoEjecucion r = bubble.ejecutar(new int[]{2, 1});
        assertEquals(TipoOperacion.FINALIZAR, r.pasos().get(r.pasos().size() - 1).tipo());
    }

    @Test
    void formatoDeDatosIdaYVuelta() {
        int[] datos = {5, -3, 8};
        assertArrayEquals(datos, FormatoDatos.leerArreglo(FormatoDatos.serializarArreglo(datos)));
        assertThrows(IllegalArgumentException.class, () -> FormatoDatos.leerArreglo("v1;ARREGLO;1,a"));
        assertThrows(IllegalArgumentException.class, () -> FormatoDatos.leerArreglo("v2;ARREGLO;1"));
    }
}
