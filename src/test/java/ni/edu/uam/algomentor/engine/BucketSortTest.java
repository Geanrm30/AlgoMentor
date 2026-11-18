package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BucketSortTest extends ContratoOrdenamiento {
    @Override AlgoritmoOrdenamiento algoritmo() { return new BucketSort(); }

    @Test void distribuyeOrdenaDentroDeCubetasYConcatenaSinIntercambiosFicticios() {
        var pasos = algoritmo().ejecutar(new int[]{9, 8, 1, 0, -1, 8, 7, 6, 5}).pasos();
        assertEquals(9, pasos.stream().filter(p -> p.tipo() == TipoOperacion.DISTRIBUIR).count());
        assertEquals(9, pasos.stream().filter(p -> p.tipo() == TipoOperacion.ESCRIBIR).count());
        assertTrue(pasos.stream().anyMatch(p -> p.tipo() == TipoOperacion.ORDENAR_CUBETA));
        assertTrue(pasos.stream().anyMatch(p -> p.tipo() == TipoOperacion.ESCRIBIR_AUXILIAR));
        assertTrue(pasos.stream().noneMatch(p -> p.tipo() == TipoOperacion.INTERCAMBIAR));
    }
}
