package ni.edu.uam.algomentor.engine;

import java.util.List;

/**
 * Un paso verificable de un algoritmo. JavaFX solo reproduce estos pasos,
 * y la persistencia los guarda tal cual en PasoIntento.
 */
public record Paso(
        int numero,
        TipoOperacion tipo,
        List<Integer> indices,
        String estadoAnterior,
        String estadoPosterior,
        String explicacion,
        int indicePivote) {

    public Paso(int numero, TipoOperacion tipo, List<Integer> indices, String estadoAnterior,
                String estadoPosterior, String explicacion) {
        this(numero, tipo, indices, estadoAnterior, estadoPosterior, explicacion, -1);
    }

    public Paso {
        indices = List.copyOf(indices);
    }
}
