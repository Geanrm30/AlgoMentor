package ni.edu.uam.algomentor.util;

import java.util.Arrays;

public final class MovimientoCeldas {

    private MovimientoCeldas() {
    }

    /**
     * Para cada posición del estado nuevo devuelve la posición que ocupaba esa celda en el estado anterior,
     * o -1 si no se puede rastrear. Las celdas que no cambiaron conservan su posición.
     */
    public static int[] origenes(String[] anterior, String[] nuevo) {
        int[] origen = new int[nuevo.length];
        Arrays.fill(origen, -1);
        if (anterior == null || anterior.length != nuevo.length) {
            return origen;
        }
        boolean[] usada = new boolean[nuevo.length];
        for (int i = 0; i < nuevo.length; i++) {
            if (anterior[i].equals(nuevo[i])) {
                origen[i] = i;
                usada[i] = true;
            }
        }
        for (int i = 0; i < nuevo.length; i++) {
            if (origen[i] == -1) {
                int cercana = posicionMasCercanaLibre(anterior, usada, nuevo[i], i);
                if (cercana != -1) {
                    origen[i] = cercana;
                    usada[cercana] = true;
                }
            }
        }
        return origen;
    }

    private static int posicionMasCercanaLibre(String[] anterior, boolean[] usada, String valor, int destino) {
        int mejor = -1;
        for (int j = 0; j < anterior.length; j++) {
            boolean mejora = mejor == -1 || Math.abs(j - destino) < Math.abs(mejor - destino);
            if (!usada[j] && anterior[j].equals(valor) && mejora) {
                mejor = j;
            }
        }
        return mejor;
    }
}
