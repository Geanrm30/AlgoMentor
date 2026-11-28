package ni.edu.uam.algomentor.engine;

import java.util.ArrayList;
import java.util.List;

/** Grafo no dirigido y ponderado que usan BFS, DFS y Dijkstra. */
public final class GrafoEjemplo {

    public record Arista(String a, String b, int peso) {
        public String clave() {
            return a.compareTo(b) <= 0 ? a + "-" + b : b + "-" + a;
        }
    }

    public static final List<String> NODOS = List.of("A", "B", "C", "D", "E", "F");

    /** Posición relativa (0 a 1) de cada nodo, en el mismo orden que {@link #NODOS}. */
    public static final double[][] POSICIONES = {
            {0.08, 0.50}, {0.34, 0.14}, {0.34, 0.86}, {0.66, 0.14}, {0.66, 0.86}, {0.92, 0.50}};

    public static final List<Arista> ARISTAS = List.of(
            new Arista("A", "B", 4), new Arista("A", "C", 2), new Arista("B", "C", 1),
            new Arista("B", "D", 5), new Arista("C", "E", 8), new Arista("D", "E", 2),
            new Arista("D", "F", 6), new Arista("E", "F", 3));

    private GrafoEjemplo() { }

    /** Vecinos en orden alfabético, para que el recorrido sea determinista. */
    public static List<Arista> vecinos(String nodo) {
        List<Arista> resultado = new ArrayList<>();
        for (Arista arista : ARISTAS) {
            if (arista.a().equals(nodo)) resultado.add(arista);
            else if (arista.b().equals(nodo)) resultado.add(new Arista(nodo, arista.a(), arista.peso()));
        }
        resultado.sort((x, y) -> x.b().compareTo(y.b()));
        return resultado;
    }
}
