package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CountingSortTest extends ContratoOrdenamiento {
    @Override AlgoritmoOrdenamiento algoritmo() { return new CountingSort(); }

    @Test void cuentaNegativosSinCompararNiIntercambiarEnRangoDenso() {
        var pasos = algoritmo().ejecutar(new int[]{-3, 0, -3, 2}).pasos();
        assertEquals(4, pasos.stream().filter(p -> p.tipo() == TipoOperacion.CONTAR).count());
        assertEquals(4, pasos.stream().filter(p -> p.tipo() == TipoOperacion.ESCRIBIR).count());
        assertTrue(pasos.stream().noneMatch(p -> p.tipo() == TipoOperacion.COMPARAR
                || p.tipo() == TipoOperacion.INTERCAMBIAR));
    }

    @Test void rangoExtremoUsaConteoDispersoYDeclaraSusComparaciones() {
        var r = algoritmo().ejecutar(new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE, 0});
        assertTrue(r.pasos().stream().anyMatch(p -> p.explicacion().contains("4294967296")));
        assertTrue(r.pasos().stream().anyMatch(p -> p.tipo() == TipoOperacion.COMPARAR));
        assertTrue(r.pasos().size() < 100);
    }
}
