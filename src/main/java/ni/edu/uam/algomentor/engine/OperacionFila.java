package ni.edu.uam.algomentor.engine;

import java.util.Objects;
import java.util.Optional;

/**
 * Operacion elemental de fila. Los indices son base 0.
 * INTERCAMBIAR_FILAS: F[destino] <-> F[origen]
 * ESCALAR_FILA:       F[destino] = factor * F[destino]
 * ELIMINAR_FILA:      F[destino] = F[destino] - factor * F[origen]
 */
public record OperacionFila(TipoOperacion tipo, int filaDestino, int filaOrigen, Fraccion factor) {

    public OperacionFila {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(factor, "factor");
    }

    public static OperacionFila intercambiar(int filaA, int filaB) {
        return new OperacionFila(TipoOperacion.INTERCAMBIAR_FILAS, filaA, filaB, Fraccion.UNO);
    }

    public static OperacionFila escalar(int fila, Fraccion factor) {
        return new OperacionFila(TipoOperacion.ESCALAR_FILA, fila, fila, factor);
    }

    public static OperacionFila eliminar(int destino, int origen, Fraccion factor) {
        return new OperacionFila(TipoOperacion.ELIMINAR_FILA, destino, origen, factor);
    }

    /** Devuelve el motivo si la operacion no es valida para una matriz con esa cantidad de filas. */
    public Optional<String> validar(int filas) {
        if (tipo != TipoOperacion.INTERCAMBIAR_FILAS && tipo != TipoOperacion.ESCALAR_FILA
                && tipo != TipoOperacion.ELIMINAR_FILA) {
            return Optional.of("Operacion no permitida: " + tipo + ".");
        }
        if (filaDestino < 0 || filaDestino >= filas) {
            return Optional.of("La fila F" + (filaDestino + 1) + " no existe.");
        }
        if (filaOrigen < 0 || filaOrigen >= filas) {
            return Optional.of("La fila F" + (filaOrigen + 1) + " no existe.");
        }
        if (tipo == TipoOperacion.INTERCAMBIAR_FILAS && filaDestino == filaOrigen) {
            return Optional.of("Intercambiar una fila consigo misma no hace nada.");
        }
        if (tipo == TipoOperacion.ESCALAR_FILA && factor.esCero()) {
            return Optional.of("No se puede multiplicar una fila por cero.");
        }
        if (tipo == TipoOperacion.ELIMINAR_FILA && filaDestino == filaOrigen) {
            return Optional.of("Una fila no puede restarse a si misma.");
        }
        return Optional.empty();
    }

    /** Aplica la operacion sobre la matriz recibida (la modifica). Lanza IllegalArgumentException si no es valida. */
    public void aplicarEn(Fraccion[][] matriz) {
        Optional<String> problema = validar(matriz.length);
        if (problema.isPresent()) {
            throw new IllegalArgumentException(problema.get());
        }
        switch (tipo) {
            case INTERCAMBIAR_FILAS -> {
                Fraccion[] temporal = matriz[filaDestino];
                matriz[filaDestino] = matriz[filaOrigen];
                matriz[filaOrigen] = temporal;
            }
            case ESCALAR_FILA -> {
                for (int j = 0; j < matriz[filaDestino].length; j++) {
                    matriz[filaDestino][j] = matriz[filaDestino][j].multiplicar(factor);
                }
            }
            case ELIMINAR_FILA -> {
                for (int j = 0; j < matriz[filaDestino].length; j++) {
                    matriz[filaDestino][j] = matriz[filaDestino][j]
                            .restar(factor.multiplicar(matriz[filaOrigen][j]));
                }
            }
            default -> throw new IllegalStateException("Operacion no soportada: " + tipo);
        }
    }
}
