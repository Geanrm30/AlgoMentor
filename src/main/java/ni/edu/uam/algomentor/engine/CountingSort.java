package ni.edu.uam.algomentor.engine;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Conteo con desplazamiento; los rangos grandes usan frecuencias dispersas en vez de reservar el rango. */
public class CountingSort implements AlgoritmoOrdenamiento {
    static final int MAX_RANGO_DENSO = 65_536;

    @Override public String nombre() { return "Counting Sort"; }

    @Override
    public ResultadoEjecucion ejecutar(int[] datos) {
        TrazaOrdenamiento traza = new TrazaOrdenamiento(datos);
        if (datos.length == 1) return traza.terminar();
        int minimo = datos[0];
        int maximo = datos[0];
        for (int valor : datos) {
            minimo = Math.min(minimo, valor);
            maximo = Math.max(maximo, valor);
        }
        long rango = (long) maximo - minimo + 1;
        if (rango <= MAX_RANGO_DENSO) contarDenso(traza, minimo, (int) rango);
        else contarDisperso(traza, rango);
        return traza.terminar();
    }

    private void contarDenso(TrazaOrdenamiento traza, int minimo, int rango) {
        int[] frecuencias = new int[rango];
        traza.anotar(TipoOperacion.PASADA, List.of(),
                "Conteo denso con " + rango + " casillas. Índice = valor - (" + minimo + ").");
        int[] a = traza.datos();
        for (int i = 0; i < a.length; i++) {
            int indice = (int) ((long) a[i] - minimo);
            frecuencias[indice]++;
            traza.anotar(TipoOperacion.CONTAR, List.of(i),
                    "Contar " + a[i] + ": frecuencia[" + indice + "] = " + frecuencias[indice] + ".");
        }
        int destino = 0;
        for (int indice = 0; indice < rango; indice++) {
            int valor = (int) ((long) minimo + indice);
            for (int copia = 0; copia < frecuencias[indice]; copia++) {
                traza.escribir(destino++, valor,
                        "Reconstruir el arreglo: escribir " + valor + " desde su frecuencia " + frecuencias[indice] + ".");
            }
        }
    }

    private void contarDisperso(TrazaOrdenamiento traza, long rango) {
        traza.anotar(TipoOperacion.PASADA, List.of(),
                "El rango " + rango + " supera " + MAX_RANGO_DENSO
                        + ". Se cuentan solo los valores presentes en un árbol ordenado; sus comparaciones se muestran.");
        Map<Integer, Integer> frecuencias = new TreeMap<>((a, b) -> {
            traza.anotar(TipoOperacion.COMPARAR, List.of(),
                    "Comparar las claves " + a + " y " + b + " en el árbol de frecuencias dispersas.");
            return Integer.compare(a, b);
        });
        int[] a = traza.datos();
        for (int i = 0; i < a.length; i++) {
            int frecuencia = frecuencias.merge(a[i], 1, Integer::sum);
            traza.anotar(TipoOperacion.CONTAR, List.of(i),
                    "Contar " + a[i] + ": frecuencia = " + frecuencia + ".");
        }
        int destino = 0;
        for (var entrada : frecuencias.entrySet()) {
            for (int copia = 0; copia < entrada.getValue(); copia++) {
                traza.escribir(destino++, entrada.getKey(),
                        "Reconstruir en orden desde el conteo disperso: escribir " + entrada.getKey() + ".");
            }
        }
    }
}
