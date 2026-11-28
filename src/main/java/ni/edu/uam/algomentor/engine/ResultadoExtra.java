package ni.edu.uam.algomentor.engine;

import java.util.List;

public record ResultadoExtra(List<PasoExtra> pasos, String conclusion) {
    public ResultadoExtra {
        pasos = List.copyOf(pasos);
    }
}
