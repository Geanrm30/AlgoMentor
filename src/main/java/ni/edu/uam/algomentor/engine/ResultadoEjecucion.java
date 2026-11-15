package ni.edu.uam.algomentor.engine;

import java.util.List;

public record ResultadoEjecucion(String estadoInicial, String estadoFinal, List<Paso> pasos) {

    public ResultadoEjecucion {
        pasos = List.copyOf(pasos);
    }
}