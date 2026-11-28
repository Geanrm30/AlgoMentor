package ni.edu.uam.algomentor.engine;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Lo que la interfaz dibuja en un paso de los algoritmos que no son de ordenamiento ni matriciales. */
public sealed interface EstadoVisual {

    /** Una fila de celdas: activas (cian), encontradas (verde) y descartadas (apagadas). */
    record Celdas(List<String> valores, Set<Integer> activos, Set<Integer> encontrados,
                  Set<Integer> descartados) implements EstadoVisual { }

    /** Grafo de ejemplo: estado de cada nodo ("actual", "visitado" o "frontera"), etiquetas y aristas resaltadas. */
    record Grafo(Map<String, String> estados, Map<String, String> etiquetas, Set<String> aristasActivas,
                 String cola) implements EstadoVisual { }

    /** Torres de Hanoi: cada lista es una varilla con los discos de abajo hacia arriba. */
    record Torres(List<List<Integer>> varillas, int discos) implements EstadoVisual { }
}
