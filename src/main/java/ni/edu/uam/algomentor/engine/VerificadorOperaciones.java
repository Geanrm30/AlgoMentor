package ni.edu.uam.algomentor.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Comprueba la respuesta de un estudiante: dada la matriz anterior y la operacion que eligio,
 * compara la matriz que escribio contra la que resulta de aplicar la operacion.
 */
public class VerificadorOperaciones {

    public ResultadoVerificacion verificar(Fraccion[][] anterior, OperacionFila operacion, Fraccion[][] propuesta) {
        FormatoDatos.validarMatriz(anterior);
        if (operacion == null) {
            return invalido("No se indico ninguna operacion.");
        }
        if (!mismasDimensiones(anterior, propuesta)) {
            return invalido("La matriz escrita no tiene las mismas dimensiones que la anterior.");
        }
        Optional<String> problema = operacion.validar(anterior.length);
        if (problema.isPresent()) {
            return invalido(problema.get());
        }

        Fraccion[][] esperada = new Fraccion[anterior.length][];
        for (int i = 0; i < anterior.length; i++) {
            esperada[i] = anterior[i].clone();
        }
        operacion.aplicarEn(esperada);

        Set<Integer> afectadas = operacion.tipo() == TipoOperacion.INTERCAMBIAR_FILAS
                ? Set.of(operacion.filaDestino(), operacion.filaOrigen())
                : Set.of(operacion.filaDestino());

        List<ErrorOperacion> errores = new ArrayList<>();
        for (int i = 0; i < esperada.length; i++) {
            for (int j = 0; j < esperada[i].length; j++) {
                if (esperada[i][j].equals(propuesta[i][j])) {
                    continue;
                }
                if (!afectadas.contains(i)) {
                    errores.add(new ErrorOperacion(TipoError.OPERACION_INVALIDA, i, j,
                            "Se modifico la fila F" + (i + 1) + ", que no participa en la operacion."));
                } else {
                    errores.add(clasificar(i, j, esperada[i][j], propuesta[i][j]));
                }
            }
        }
        return new ResultadoVerificacion(errores.isEmpty(), errores);
    }

    private ErrorOperacion clasificar(int fila, int columna, Fraccion esperada, Fraccion escrita) {
        String lugar = "F" + (fila + 1) + ", columna " + (columna + 1);
        if (!esperada.esCero() && escrita.equals(esperada.negar())) {
            return new ErrorOperacion(TipoError.SIGNO, fila, columna,
                    "En " + lugar + " el signo esta invertido: debia ser " + esperada + " y escribiste " + escrita + ".");
        }
        if (!esperada.esCero() && !escrita.esCero() && esErrorDeFraccion(esperada, escrita)) {
            return new ErrorOperacion(TipoError.FRACCION, fila, columna,
                    "En " + lugar + " hay un error al operar con fracciones: debia ser " + esperada
                            + " y escribiste " + escrita + ".");
        }
        return new ErrorOperacion(TipoError.ARITMETICO, fila, columna,
                "En " + lugar + " el resultado es incorrecto: debia ser " + esperada
                        + " y escribiste " + escrita + ".");
    }

    private boolean esErrorDeFraccion(Fraccion esperada, Fraccion escrita) {
        boolean reciproco = escrita.equals(esperada.reciproco());
        boolean mismoNumeradorOtroDenominador = escrita.numerador().equals(esperada.numerador())
                && !escrita.denominador().equals(esperada.denominador());
        return reciproco || mismoNumeradorOtroDenominador;
    }

    private boolean mismasDimensiones(Fraccion[][] anterior, Fraccion[][] propuesta) {
        if (propuesta == null || propuesta.length != anterior.length) {
            return false;
        }
        for (int i = 0; i < propuesta.length; i++) {
            if (propuesta[i] == null || propuesta[i].length != anterior[i].length) {
                return false;
            }
            for (Fraccion celda : propuesta[i]) {
                if (celda == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private ResultadoVerificacion invalido(String motivo) {
        return new ResultadoVerificacion(false,
                List.of(new ErrorOperacion(TipoError.OPERACION_INVALIDA, -1, -1, motivo)));
    }
}
