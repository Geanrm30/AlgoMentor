package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

/** Contrato compartido que se ejecuta íntegramente para cada implementación nueva. */
abstract class ContratoOrdenamiento {
    abstract AlgoritmoOrdenamiento algoritmo();

    @Test void ordenaCasosRepresentativos() {
        for (int[] datos : new int[][]{{5, 3, 8, 1, 2}, {1, 2, 3}, {3, 2, 1},
                {4, 4, 1, 4, 1}, {-5, 0, -1, 3, -5}, {7}, {0, 0, 0},
                {Integer.MAX_VALUE, 0, Integer.MIN_VALUE, -1, Integer.MAX_VALUE},
                {Integer.MIN_VALUE}, {Integer.MAX_VALUE, Integer.MIN_VALUE}}) {
            verificar(datos);
        }
    }

    @Test void coincideConArraysSortEnEntradasAleatorias() {
        Random azar = new Random(812);
        for (int prueba = 0; prueba < 120; prueba++) {
            int[] datos = new int[1 + azar.nextInt(30)];
            for (int i = 0; i < datos.length; i++) {
                datos[i] = prueba % 2 == 0 ? azar.nextInt(41) - 20 : azar.nextInt();
            }
            verificar(datos);
        }
    }

    @Test void rechazaEntradasInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> algoritmo().ejecutar(null));
        assertThrows(IllegalArgumentException.class, () -> algoritmo().ejecutar(new int[0]));
    }

    private void verificar(int[] datos) {
        int[] original = datos.clone();
        int[] esperado = datos.clone();
        Arrays.sort(esperado);
        ResultadoEjecucion r = algoritmo().ejecutar(datos);
        assertArrayEquals(original, datos, "No debe modificar la entrada");
        assertEquals(Arrays.toString(original), r.estadoInicial());
        assertEquals(Arrays.toString(esperado), r.estadoFinal());
        assertFalse(r.pasos().isEmpty());
        String anterior = r.estadoInicial();
        for (int i = 0; i < r.pasos().size(); i++) {
            Paso paso = r.pasos().get(i);
            assertEquals(i + 1, paso.numero());
            assertEquals(anterior, paso.estadoAnterior(), "La traza debe ser continua");
            assertNotNull(paso.tipo());
            assertFalse(paso.explicacion().isBlank());
            assertTrue(paso.indices().stream().allMatch(indice -> indice >= 0 && indice < datos.length));
            assertTrue(paso.indicePivote() >= -1 && paso.indicePivote() < datos.length);
            if (paso.tipo() != TipoOperacion.ESCRIBIR && paso.tipo() != TipoOperacion.INTERCAMBIAR) {
                assertEquals(paso.estadoAnterior(), paso.estadoPosterior());
            }
            anterior = paso.estadoPosterior();
        }
        assertEquals(r.estadoFinal(), anterior);
        assertEquals(TipoOperacion.FINALIZAR, r.pasos().getLast().tipo());
    }
}
