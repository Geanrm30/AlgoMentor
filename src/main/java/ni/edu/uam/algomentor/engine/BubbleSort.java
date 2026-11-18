package ni.edu.uam.algomentor.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BubbleSort implements AlgoritmoOrdenamiento {

    @Override
    public String nombre() {
        return "Bubble Sort";
    }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        if (datos == null || datos.length == 0) {
            throw new IllegalArgumentException("Se necesita al menos un elemento.");
        }
        int[] a = datos.clone();
        String inicial = Arrays.toString(a);
        List<Paso> pasos = new ArrayList<>();
        int n = a.length;

        for (int i = 0; i < n - 1; i++) {
            boolean huboCambio = false;
            for (int j = 0; j < n - 1 - i; j++) {
                String antes = Arrays.toString(a);
                int izquierda = a[j];
                int derecha = a[j + 1];
                if (izquierda > derecha) {
                    pasos.add(new Paso(pasos.size() + 1, TipoOperacion.COMPARAR,
                            List.of(j, j + 1), antes, antes,
                            izquierda + " > " + derecha + ": se necesita intercambiar los elementos."));
                    a[j] = derecha;
                    a[j + 1] = izquierda;
                    huboCambio = true;
                    pasos.add(new Paso(pasos.size() + 1, TipoOperacion.INTERCAMBIAR,
                            List.of(j, j + 1), antes, Arrays.toString(a),
                            izquierda + " > " + derecha + ": se intercambian las posiciones " + j + " y " + (j + 1) + "."));
                } else {
                    pasos.add(new Paso(pasos.size() + 1, TipoOperacion.COMPARAR,
                            List.of(j, j + 1), antes, antes,
                            izquierda + " <= " + derecha + ": ya estan en orden, no se intercambian."));
                }
            }
            if (!huboCambio) {
                break;
            }
        }

        String finalEstado = Arrays.toString(a);
        pasos.add(new Paso(pasos.size() + 1, TipoOperacion.FINALIZAR, List.of(),
                finalEstado, finalEstado, "El arreglo esta ordenado."));
        return new ResultadoEjecucion(inicial, finalEstado, pasos);
    }
}
