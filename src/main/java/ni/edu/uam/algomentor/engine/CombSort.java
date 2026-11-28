package ni.edu.uam.algomentor.engine;

import java.util.List;

/** Burbuja con brecha que se reduce por un factor de 1.3 hasta llegar a 1; elimina las "tortugas". */
public class CombSort implements AlgoritmoOrdenamiento {

    @Override public String nombre() { return "Comb Sort"; }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        TrazaOrdenamiento traza = new TrazaOrdenamiento(datos);
        int[] a = traza.datos();
        int brecha = a.length;
        boolean cambio = true;
        while (brecha > 1 || cambio) {
            brecha = Math.max(1, (int) (brecha / 1.3));
            cambio = false;
            traza.anotar(TipoOperacion.PASADA, List.of(), "Pasada con brecha " + brecha + ".");
            for (int i = 0; i + brecha < a.length; i++) {
                boolean fuera = a[i] > a[i + brecha];
                traza.anotar(TipoOperacion.COMPARAR, List.of(i, i + brecha),
                        a[i] + (fuera ? " > " : " <= ") + a[i + brecha] + ": "
                                + (fuera ? "se intercambian." : "ya están en orden."));
                if (fuera) {
                    traza.intercambiar(i, i + brecha);
                    cambio = true;
                }
            }
        }
        return traza.terminar();
    }
}
