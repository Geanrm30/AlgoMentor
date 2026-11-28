package ni.edu.uam.algomentor.engine;

import java.util.List;

/** Busca el mínimo del tramo sin ordenar y lo coloca al inicio con un único intercambio por pasada. */
public class SelectionSort implements AlgoritmoOrdenamiento {

    @Override public String nombre() { return "Selection Sort"; }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        TrazaOrdenamiento traza = new TrazaOrdenamiento(datos);
        int[] a = traza.datos();
        for (int i = 0; i < a.length - 1; i++) {
            int minimo = i;
            for (int j = i + 1; j < a.length; j++) {
                boolean menor = a[j] < a[minimo];
                traza.anotar(TipoOperacion.COMPARAR, List.of(minimo, j),
                        a[j] + (menor ? " < " : " >= ") + a[minimo] + ": "
                                + (menor ? "nuevo mínimo en la posición " + (j + 1) + "." : "el mínimo no cambia."));
                if (menor) minimo = j;
            }
            traza.intercambiar(i, minimo);
        }
        return traza.terminar();
    }
}
