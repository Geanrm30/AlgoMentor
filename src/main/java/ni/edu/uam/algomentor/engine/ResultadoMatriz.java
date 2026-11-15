package ni.edu.uam.algomentor.engine;

import java.util.List;

/** Resultado de resolver un sistema. 'solucion' solo tiene valores cuando la clasificacion es UNICA. */
public record ResultadoMatriz(ResultadoEjecucion ejecucion, ClasificacionSistema clasificacion,
                              List<Fraccion> solucion) {

    public ResultadoMatriz {
        solucion = List.copyOf(solucion);
    }
}
