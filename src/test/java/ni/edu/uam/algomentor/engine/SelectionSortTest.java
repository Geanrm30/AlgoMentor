package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SelectionSortTest extends ContratoOrdenamiento {
    @Override AlgoritmoOrdenamiento algoritmo() { return new SelectionSort(); }

    @Test void haceComoMaximoUnIntercambioPorPasada() {
        var pasos = algoritmo().ejecutar(new int[]{5, 4, 3, 2, 1}).pasos();
        assertTrue(pasos.stream().filter(p -> p.tipo() == TipoOperacion.INTERCAMBIAR).count() <= 4);
    }
}
