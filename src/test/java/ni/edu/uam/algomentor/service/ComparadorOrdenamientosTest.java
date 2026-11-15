package ni.edu.uam.algomentor.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComparadorOrdenamientosTest {

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
