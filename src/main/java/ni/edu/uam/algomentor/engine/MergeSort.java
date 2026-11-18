package ni.edu.uam.algomentor.engine;

import java.util.Arrays;
import java.util.List;

/** Mezcla estable: ante igualdad se conserva primero el elemento del subarreglo izquierdo. */
public class MergeSort implements AlgoritmoOrdenamiento {
    @Override public String nombre() { return "Merge Sort"; }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        TrazaOrdenamiento traza = new TrazaOrdenamiento(datos);
        dividir(traza, new int[datos.length], 0, datos.length - 1);
        return traza.terminar();
    }

    private void dividir(TrazaOrdenamiento traza, int[] auxiliar, int inicio, int fin) {
        if (inicio >= fin) return;
        int medio = inicio + (fin - inicio) / 2;
        traza.anotar(TipoOperacion.DIVIDIR, List.of(inicio, medio, fin),
                "Dividir las posiciones " + (inicio + 1) + " a " + (fin + 1)
                        + " en dos subarreglos, después de la posición " + (medio + 1) + ".");
        dividir(traza, auxiliar, inicio, medio);
        dividir(traza, auxiliar, medio + 1, fin);
        mezclar(traza, auxiliar, inicio, medio, fin);
    }

    private void mezclar(TrazaOrdenamiento traza, int[] auxiliar, int inicio, int medio, int fin) {
        int[] a = traza.datos();
        System.arraycopy(a, inicio, auxiliar, inicio, fin - inicio + 1);
        traza.anotar(TipoOperacion.MEZCLAR, List.of(inicio, fin),
                "Mezclar " + Arrays.toString(Arrays.copyOfRange(auxiliar, inicio, medio + 1))
                        + " con " + Arrays.toString(Arrays.copyOfRange(auxiliar, medio + 1, fin + 1)) + ".");
        int izquierda = inicio;
        int derecha = medio + 1;
        for (int destino = inicio; destino <= fin; destino++) {
            int valor;
            if (izquierda > medio) valor = auxiliar[derecha++];
            else if (derecha > fin) valor = auxiliar[izquierda++];
            else {
                traza.anotar(TipoOperacion.COMPARAR, List.of(izquierda, derecha),
                        "Comparar " + auxiliar[izquierda] + " y " + auxiliar[derecha]
                                + " en los subarreglos auxiliares; en empate se elige el izquierdo.");
                valor = auxiliar[izquierda] <= auxiliar[derecha]
                        ? auxiliar[izquierda++] : auxiliar[derecha++];
            }
            traza.escribir(destino, valor, "Escribir " + valor + " en la posición " + (destino + 1)
                    + " durante la mezcla.");
        }
    }
}
