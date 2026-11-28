package ni.edu.uam.algomentor.engine;

import java.util.List;

/** Burbuja bidireccional: una pasada sube el mayor al final y la siguiente baja el menor al inicio. */
public class CocktailSort implements AlgoritmoOrdenamiento {

    @Override public String nombre() { return "Cocktail Sort"; }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        TrazaOrdenamiento traza = new TrazaOrdenamiento(datos);
        int[] a = traza.datos();
        int inicio = 0;
        int fin = a.length - 1;
        boolean cambio = true;
        while (cambio && inicio < fin) {
            cambio = false;
            traza.anotar(TipoOperacion.PASADA, List.of(), "Pasada hacia la derecha: el mayor viaja hasta la posición " + (fin + 1) + ".");
            for (int i = inicio; i < fin; i++) cambio |= compararEIntercambiar(traza, a, i);
            fin--;
            if (!cambio) break;
            cambio = false;
            traza.anotar(TipoOperacion.PASADA, List.of(), "Pasada hacia la izquierda: el menor viaja hasta la posición " + (inicio + 1) + ".");
            for (int i = fin; i > inicio; i--) cambio |= compararEIntercambiar(traza, a, i - 1);
            inicio++;
        }
        return traza.terminar();
    }

    private boolean compararEIntercambiar(TrazaOrdenamiento traza, int[] a, int i) {
        boolean fuera = a[i] > a[i + 1];
        traza.anotar(TipoOperacion.COMPARAR, List.of(i, i + 1),
                a[i] + (fuera ? " > " : " <= ") + a[i + 1] + ": " + (fuera ? "se intercambian." : "ya están en orden."));
        if (fuera) traza.intercambiar(i, i + 1);
        return fuera;
    }
}
