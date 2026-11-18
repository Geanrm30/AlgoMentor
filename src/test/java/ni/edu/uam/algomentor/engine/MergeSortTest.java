package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MergeSortTest extends ContratoOrdenamiento {
    @Override AlgoritmoOrdenamiento algoritmo() { return new MergeSort(); }

    @Test void divisionMezclaYEscrituraNoSonIntercambios() {
        var pasos = algoritmo().ejecutar(new int[]{4, 1, 3, 2}).pasos();
        for (TipoOperacion tipo : new TipoOperacion[]{TipoOperacion.DIVIDIR, TipoOperacion.MEZCLAR,
                TipoOperacion.ESCRIBIR, TipoOperacion.COMPARAR}) {
            assertTrue(pasos.stream().anyMatch(p -> p.tipo() == tipo));
        }
        assertTrue(pasos.stream().noneMatch(p -> p.tipo() == TipoOperacion.INTERCAMBIAR));
        assertEquals(8, pasos.stream().filter(p -> p.tipo() == TipoOperacion.ESCRIBIR).count());
    }
}
