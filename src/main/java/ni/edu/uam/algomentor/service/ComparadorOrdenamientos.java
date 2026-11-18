package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.*;

import java.util.List;

public final class ComparadorOrdenamientos {

    private ComparadorOrdenamientos() { }

    public record Metrica(String algoritmo, int comparaciones, int intercambios, int escrituras,
                          int escriturasCubeta, int conteos, int distribuciones, int pasos, String resultado) { }

    public static List<Metrica> comparar(int[] datos) {
        if (datos == null || datos.length == 0 || datos.length > 12) {
            throw new IllegalArgumentException("Usa entre 1 y 12 números.");
        }
        return CatalogoAlgoritmos.ordenamientos().stream()
                .map(a -> metrica(a.crearOrdenamiento(), datos)).toList();
    }

    private static Metrica metrica(AlgoritmoOrdenamiento algoritmo, int[] datos) {
        ResultadoEjecucion ejecucion = algoritmo.ejecutar(datos);
        return new Metrica(algoritmo.nombre(), contar(ejecucion, TipoOperacion.COMPARAR),
                contar(ejecucion, TipoOperacion.INTERCAMBIAR), contar(ejecucion, TipoOperacion.ESCRIBIR),
                contar(ejecucion, TipoOperacion.ESCRIBIR_AUXILIAR), contar(ejecucion, TipoOperacion.CONTAR),
                contar(ejecucion, TipoOperacion.DISTRIBUIR),
                ejecucion.pasos().size(), ejecucion.estadoFinal());
    }

    private static int contar(ResultadoEjecucion ejecucion, TipoOperacion tipo) {
        return (int) ejecucion.pasos().stream().filter(p -> p.tipo() == tipo).count();
    }
}
