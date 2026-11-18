package ni.edu.uam.algomentor.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Construye instantáneas continuas; el estado auxiliar se explica sin fingir cambios en el arreglo. */
final class TrazaOrdenamiento {
    private final int[] datos;
    private final String inicial;
    private final List<Paso> pasos = new ArrayList<>();
    private int pivote = -1;

    TrazaOrdenamiento(int[] entrada) {
        if (entrada == null || entrada.length == 0) {
            throw new IllegalArgumentException("Se necesita al menos un elemento.");
        }
        datos = entrada.clone();
        inicial = estado();
    }

    int[] datos() { return datos; }

    void pivote(int indice) { pivote = indice; }

    void anotar(TipoOperacion tipo, List<Integer> indices, String explicacion) {
        registrar(tipo, indices, estado(), explicacion);
    }

    void intercambiar(int a, int b) {
        if (a == b) return;
        String antes = estado();
        int temporal = datos[a];
        datos[a] = datos[b];
        datos[b] = temporal;
        if (pivote == a) pivote = b;
        else if (pivote == b) pivote = a;
        registrar(TipoOperacion.INTERCAMBIAR, List.of(a, b), antes,
                "Se intercambian las posiciones " + (a + 1) + " y " + (b + 1) + ".");
    }

    void escribir(int indice, int valor, String explicacion) {
        String antes = estado();
        datos[indice] = valor;
        registrar(TipoOperacion.ESCRIBIR, List.of(indice), antes, explicacion);
    }

    ResultadoEjecucion terminar() {
        pivote = -1;
        anotar(TipoOperacion.FINALIZAR, List.of(), "El arreglo está ordenado.");
        return new ResultadoEjecucion(inicial, estado(), pasos);
    }

    private String estado() { return Arrays.toString(datos); }

    private void registrar(TipoOperacion tipo, List<Integer> indices, String antes, String explicacion) {
        pasos.add(new Paso(pasos.size() + 1, tipo, indices, antes, estado(), explicacion, pivote));
    }
}
