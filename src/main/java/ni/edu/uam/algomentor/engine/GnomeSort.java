package ni.edu.uam.algomentor.engine;

import java.util.List;

/** El "gnomo" avanza si el par está en orden y retrocede intercambiando cuando no lo está. */
public class GnomeSort implements AlgoritmoOrdenamiento {

    @Override public String nombre() { return "Gnome Sort"; }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        TrazaOrdenamiento traza = new TrazaOrdenamiento(datos);
        int[] a = traza.datos();
        int i = 1;
        while (i < a.length) {
            boolean fuera = a[i - 1] > a[i];
            traza.anotar(TipoOperacion.COMPARAR, List.of(i - 1, i),
                    a[i - 1] + (fuera ? " > " : " <= ") + a[i] + ": "
                            + (fuera ? "se intercambian y el gnomo retrocede." : "en orden, el gnomo avanza."));
            if (fuera) {
                traza.intercambiar(i - 1, i);
                i = Math.max(1, i - 1);
            } else {
                i++;
            }
        }
        return traza.terminar();
    }
}
