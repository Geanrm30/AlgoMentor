package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GnomeSortTest extends ContratoOrdenamiento {
    @Override AlgoritmoOrdenamiento algoritmo() { return new GnomeSort(); }

    @Test void conArregloOrdenadoSoloComparaCadaParUnaVez() {
        var pasos = algoritmo().ejecutar(new int[]{1, 2, 3, 4}).pasos();
        assertEquals(3, pasos.stream().filter(p -> p.tipo() == TipoOperacion.COMPARAR).count());
        assertTrue(pasos.stream().noneMatch(p -> p.tipo() == TipoOperacion.INTERCAMBIAR));
    }
}
