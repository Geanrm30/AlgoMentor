package ni.edu.uam.algomentor.engine;

import java.util.List;

public record ResultadoVerificacion(boolean correcto, List<ErrorOperacion> errores) {

    public ResultadoVerificacion {
        errores = List.copyOf(errores);
    }
}
