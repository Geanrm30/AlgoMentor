package ni.edu.uam.algomentor.engine;

import java.util.List;

/** Construye un montículo máximo y extrae repetidamente la raíz hacia el final del arreglo. */
public class HeapSort implements AlgoritmoOrdenamiento {

    @Override public String nombre() { return "Heap Sort"; }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        TrazaOrdenamiento traza = new TrazaOrdenamiento(datos);
        int[] a = traza.datos();
        int n = a.length;
        traza.anotar(TipoOperacion.PASADA, List.of(),
                "Construir el montículo máximo: el padre de la posición i es la (i-1)/2 (desde 0).");
        for (int i = n / 2 - 1; i >= 0; i--) hundir(traza, a, n, i);
        for (int fin = n - 1; fin > 0; fin--) {
            traza.anotar(TipoOperacion.PASADA, List.of(),
                    "La raíz " + a[0] + " es el mayor del montículo: se lleva a la posición " + (fin + 1) + ".");
            traza.intercambiar(0, fin);
            hundir(traza, a, fin, 0);
        }
        return traza.terminar();
    }

    private void hundir(TrazaOrdenamiento traza, int[] a, int tamano, int raiz) {
        while (true) {
            int mayor = raiz;
            int izquierdo = 2 * raiz + 1;
            int derecho = izquierdo + 1;
            if (izquierdo < tamano) {
                boolean cambia = a[izquierdo] > a[mayor];
                traza.anotar(TipoOperacion.COMPARAR, List.of(izquierdo, mayor),
                        "Hijo izquierdo " + a[izquierdo] + (cambia ? " > " : " <= ") + a[mayor] + ".");
                if (cambia) mayor = izquierdo;
            }
            if (derecho < tamano) {
                boolean cambia = a[derecho] > a[mayor];
                traza.anotar(TipoOperacion.COMPARAR, List.of(derecho, mayor),
                        "Hijo derecho " + a[derecho] + (cambia ? " > " : " <= ") + a[mayor] + ".");
                if (cambia) mayor = derecho;
            }
            if (mayor == raiz) return;
            traza.intercambiar(raiz, mayor);
            raiz = mayor;
        }
    }
}
