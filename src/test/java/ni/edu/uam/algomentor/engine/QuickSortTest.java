package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuickSortTest extends ContratoOrdenamiento {
    @Override AlgoritmoOrdenamiento algoritmo() { return new QuickSort(); }

    @Test void muestraPivoteParticionesYComparaciones() {
        var pasos = algoritmo().ejecutar(new int[]{4, 2, 1, 4, 3}).pasos();
        for (TipoOperacion tipo : new TipoOperacion[]{TipoOperacion.PIVOTE, TipoOperacion.PARTICIONAR,
                TipoOperacion.COMPARAR, TipoOperacion.INTERCAMBIAR}) {
            assertTrue(pasos.stream().anyMatch(p -> p.tipo() == tipo));
        }
        assertTrue(pasos.stream().filter(p -> p.tipo() == TipoOperacion.PIVOTE)
                .allMatch(p -> p.indicePivote() >= 0));
    }

    @Test void repetidosSeResuelvenEnUnaSolaParticion() {
        var r = algoritmo().ejecutar(new int[]{5, 5, 5, 5});
        assertEquals(1, r.pasos().stream().filter(p -> p.tipo() == TipoOperacion.PARTICIONAR).count());
        assertTrue(r.pasos().stream().noneMatch(p -> p.tipo() == TipoOperacion.INTERCAMBIAR));
    }

    @Test void laMarcaDelPivoteSigueAlMismoValorDuranteLosIntercambios() {
        int pivote = 0;
        for (Paso paso : algoritmo().ejecutar(new int[]{8, 1, 7, 2, 6, 3, 5, 4}).pasos()) {
            if (paso.indicePivote() < 0) continue;
            String[] valores = paso.estadoPosterior().substring(1, paso.estadoPosterior().length() - 1).split(",");
            int valorMarcado = Integer.parseInt(valores[paso.indicePivote()].strip());
            if (paso.tipo() == TipoOperacion.PIVOTE) pivote = valorMarcado;
            assertEquals(pivote, valorMarcado);
        }
    }
}
