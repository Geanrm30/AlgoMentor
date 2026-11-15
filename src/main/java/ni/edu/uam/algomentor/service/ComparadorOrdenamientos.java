package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.*;

import java.util.List;

public final class ComparadorOrdenamientos {

    private ComparadorOrdenamientos() { }

    public record Metrica(String algoritmo, int comparaciones, int intercambios, int pasos, String resultado) { }

    public static List<Metrica> comparar(int[] datos) {
        if (datos == null || datos.length == 0 || datos.length > 12) {
            throw new IllegalArgumentException("Usa entre 1 y 12 números.");
        }
        return List.of(metrica(new BubbleSort(), datos), metrica(new InsertionSort(), datos));
    }

    private static Metrica metrica(AlgoritmoOrdenamiento algoritmo, int[] datos) {
        ResultadoEjecucion ejecucion = algoritmo.ejecutar(datos);
        long intercambios = ejecucion.pasos().stream().filter(p -> p.tipo() == TipoOperacion.INTERCAMBIAR).count();
        long comparaciones = ejecucion.pasos().stream().filter(p -> p.tipo() == TipoOperacion.COMPARAR
                || p.tipo() == TipoOperacion.INTERCAMBIAR).count();
        return new Metrica(algoritmo.nombre(), (int) comparaciones, (int) intercambios,
                ejecucion.pasos().size(), ejecucion.estadoFinal());
    }
}
