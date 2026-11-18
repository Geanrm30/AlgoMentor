package ni.edu.uam.algomentor.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComparadorOrdenamientosTest {

    @Test void comparaSieteAlgoritmosConMetricasPropias() {
        var metricas = ComparadorOrdenamientos.comparar(new int[]{4, -1, 0, 4, -3});
        assertEquals(7, metricas.size());
        assertTrue(metricas.stream().allMatch(m -> m.resultado().equals("[-3, -1, 0, 4, 4]")));
        var counting = metricas.stream().filter(m -> m.algoritmo().equals("Counting Sort")).findFirst().orElseThrow();
        assertEquals(0, counting.comparaciones());
        assertEquals(0, counting.intercambios());
        assertEquals(5, counting.escrituras());
        assertEquals(5, counting.conteos());
        var radix = metricas.stream().filter(m -> m.algoritmo().equals("Radix Sort")).findFirst().orElseThrow();
        assertEquals(0, radix.comparaciones());
        assertEquals(0, radix.intercambios());
        assertEquals(50, radix.escrituras());
        assertEquals(50, radix.distribuciones());
        var merge = metricas.stream().filter(m -> m.algoritmo().equals("Merge Sort")).findFirst().orElseThrow();
        assertEquals(0, merge.intercambios());
        assertTrue(merge.escrituras() > 0);
    }

    @Test void aceptaExtremosYRechazaDatosFueraDelLimiteVisual() {
        assertTrue(ComparadorOrdenamientos.comparar(new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE})
                .stream().allMatch(m -> m.resultado().equals("[-2147483648, 2147483647]")));
        assertThrows(IllegalArgumentException.class, () -> ComparadorOrdenamientos.comparar(null));
        assertThrows(IllegalArgumentException.class, () -> ComparadorOrdenamientos.comparar(new int[0]));
        assertThrows(IllegalArgumentException.class, () -> ComparadorOrdenamientos.comparar(new int[13]));
    }

    @Test
    void comparaMismaEntradaSinMutarla() {
        int[] datos = {3, 2, 1};
        var metricas = ComparadorOrdenamientos.comparar(datos);
        assertArrayEquals(new int[]{3, 2, 1}, datos);
        assertEquals(metricas.get(0).resultado(), metricas.get(1).resultado());
        assertEquals(3, metricas.get(0).comparaciones());
        assertEquals(3, metricas.get(1).comparaciones());
    }
}
