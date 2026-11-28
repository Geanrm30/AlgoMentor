package ni.edu.uam.algomentor.engine;

import java.util.List;

/** Inserción con saltos decrecientes (n/2, n/4, ... 1): mueve los elementos lejanos más rápido. */
public class ShellSort implements AlgoritmoOrdenamiento {

    @Override public String nombre() { return "Shell Sort"; }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        TrazaOrdenamiento traza = new TrazaOrdenamiento(datos);
        int[] a = traza.datos();
        for (int salto = a.length / 2; salto >= 1; salto /= 2) {
            traza.anotar(TipoOperacion.PASADA, List.of(),
                    "Pasada con salto " + salto + ": se ordenan por inserción los elementos separados " + salto + " posiciones.");
            for (int i = salto; i < a.length; i++) {
                int j = i;
                while (j >= salto) {
                    boolean fuera = a[j - salto] > a[j];
                    traza.anotar(TipoOperacion.COMPARAR, List.of(j - salto, j),
                            a[j - salto] + (fuera ? " > " : " <= ") + a[j] + ": "
                                    + (fuera ? "están desordenados, se intercambian." : "ya están en orden."));
                    if (!fuera) break;
                    traza.intercambiar(j - salto, j);
                    j -= salto;
                }
            }
        }
        return traza.terminar();
    }
}
