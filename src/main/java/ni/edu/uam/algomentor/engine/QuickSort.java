package ni.edu.uam.algomentor.engine;

import java.util.ArrayDeque;
import java.util.List;

/** Partición en tres vías: los valores iguales al pivote no vuelven a particionarse. */
public class QuickSort implements AlgoritmoOrdenamiento {
    @Override public String nombre() { return "Quick Sort"; }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        TrazaOrdenamiento traza = new TrazaOrdenamiento(datos);
        ArrayDeque<int[]> pendientes = new ArrayDeque<>();
        pendientes.push(new int[]{0, datos.length - 1});
        while (!pendientes.isEmpty()) {
            int[] rango = pendientes.pop();
            if (rango[0] >= rango[1]) continue;
            int[] iguales = particionar(traza, rango[0], rango[1]);
            pendientes.push(new int[]{iguales[1] + 1, rango[1]});
            pendientes.push(new int[]{rango[0], iguales[0] - 1});
        }
        return traza.terminar();
    }

    private int[] particionar(TrazaOrdenamiento traza, int inicio, int fin) {
        int[] a = traza.datos();
        int medio = inicio + (fin - inicio) / 2;
        int pivote = a[medio];
        traza.pivote(medio);
        traza.anotar(TipoOperacion.PIVOTE, List.of(medio),
                "Pivote " + pivote + " para las posiciones " + (inicio + 1) + " a " + (fin + 1) + ".");
        int menores = inicio;
        int actual = inicio;
        int mayores = fin;
        while (actual <= mayores) {
            traza.anotar(TipoOperacion.COMPARAR, List.of(actual),
                    "¿" + a[actual] + " es menor que el pivote " + pivote + "?");
            if (a[actual] < pivote) {
                traza.intercambiar(menores++, actual++);
            } else {
                traza.anotar(TipoOperacion.COMPARAR, List.of(actual),
                        "¿" + a[actual] + " es mayor que el pivote " + pivote + "?");
                if (a[actual] > pivote) traza.intercambiar(actual, mayores--);
                else actual++;
            }
        }
        traza.anotar(TipoOperacion.PARTICIONAR, List.of(menores, mayores),
                "Partición lista: menores a la izquierda, iguales a " + pivote + " entre las posiciones "
                        + (menores + 1) + " y " + (mayores + 1) + ", mayores a la derecha.");
        traza.pivote(-1);
        return new int[]{menores, mayores};
    }
}
