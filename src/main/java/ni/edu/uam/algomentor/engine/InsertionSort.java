package ni.edu.uam.algomentor.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InsertionSort implements AlgoritmoOrdenamiento {

    @Override
    public String nombre() {
        return "Insertion Sort";
    }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        if (datos == null || datos.length == 0) {
            throw new IllegalArgumentException("Se necesita al menos un elemento.");
        }
        int[] a = datos.clone();
        String inicial = Arrays.toString(a);
        List<Paso> pasos = new ArrayList<>();

        for (int i = 1; i < a.length; i++) {
            int j = i;
            while (j > 0) {
                String antes = Arrays.toString(a);
                int izquierda = a[j - 1];
                int derecha = a[j];
                if (izquierda > derecha) {
                    pasos.add(new Paso(pasos.size() + 1, TipoOperacion.COMPARAR,
                            List.of(j - 1, j), antes, antes,
                            izquierda + " > " + derecha + ": el elemento debe avanzar hacia la izquierda."));
                    a[j - 1] = derecha;
                    a[j] = izquierda;
                    pasos.add(new Paso(pasos.size() + 1, TipoOperacion.INTERCAMBIAR, List.of(j - 1, j),
                            antes, Arrays.toString(a),
                            izquierda + " > " + derecha + ": " + derecha + " se desplaza una posicion a la izquierda."));
                    j--;
                } else {
                    pasos.add(new Paso(pasos.size() + 1, TipoOperacion.COMPARAR, List.of(j - 1, j),
                            antes, antes,
                            izquierda + " <= " + derecha + ": " + derecha
                                    + " ya esta en su posicion; se pasa al siguiente elemento."));
                    break;
                }
            }
        }

        String finalEstado = Arrays.toString(a);
        pasos.add(new Paso(pasos.size() + 1, TipoOperacion.FINALIZAR, List.of(),
                finalEstado, finalEstado, "El arreglo esta ordenado."));
        return new ResultadoEjecucion(inicial, finalEstado, pasos);
    }
}
