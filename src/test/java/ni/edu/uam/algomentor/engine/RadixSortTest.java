package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RadixSortTest extends ContratoOrdenamiento {
    @Override AlgoritmoOrdenamiento algoritmo() { return new RadixSort(); }

    @Test void cadaPasadaEsEstableYNoHayIntercambiosNiComparaciones() {
        int[] entrada = {12, -8, 2, -18, 12, 11};
        var pasos = algoritmo().ejecutar(entrada).pasos();
        assertTrue(pasos.stream().noneMatch(p -> p.tipo() == TipoOperacion.COMPARAR
                || p.tipo() == TipoOperacion.INTERCAMBIAR));
        List<Integer> previo = Arrays.stream(entrada).boxed().toList();
        List<Integer> salida = new ArrayList<>();
        long divisor = 1;
        int pasadas = 0;
        for (Paso paso : pasos) {
            if (paso.tipo() != TipoOperacion.ESCRIBIR) continue;
            int[] estado = leer(paso.estadoPosterior());
            salida.add(estado[paso.indices().getFirst()]);
            if (salida.size() == entrada.length) {
                long d = divisor;
                // List.sort es estable y aquí solo sirve como oráculo de una pasada de dígito.
                List<Integer> esperado = new ArrayList<>(previo);
                esperado.sort(java.util.Comparator.comparingLong(v -> ((long) v - Integer.MIN_VALUE) / d % 10));
                assertEquals(esperado, salida);
                previo = List.copyOf(salida);
                salida.clear();
                divisor *= 10;
                pasadas++;
            }
        }
        assertEquals(10, pasadas);
    }

    private int[] leer(String estado) {
        return Arrays.stream(estado.substring(1, estado.length() - 1).split(","))
                .map(String::strip).mapToInt(Integer::parseInt).toArray();
    }
}
