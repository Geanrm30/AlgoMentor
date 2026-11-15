package ni.edu.uam.algomentor.engine;

import java.util.ArrayList;
import java.util.List;

/** Matriz de trabajo que registra un Paso por cada operacion. Uso interno de Gauss y GaussJordan. */
final class TableroMatriz {

    private final Fraccion[][] datos;
    private final String estadoInicial;
    private final List<Paso> pasos = new ArrayList<>();

    TableroMatriz(Fraccion[][] original) {
        FormatoDatos.validarMatriz(original);
        datos = new Fraccion[original.length][];
        for (int i = 0; i < original.length; i++) {
            datos[i] = original[i].clone();
        }
        estadoInicial = estado();
    }

    int filas() {
        return datos.length;
    }

    int columnas() {
        return datos[0].length;
    }

    Fraccion get(int fila, int columna) {
        return datos[fila][columna];
    }

    String estado() {
        return FormatoDatos.matrizAEstado(datos);
    }

    String estadoInicial() {
        return estadoInicial;
    }

    List<Paso> pasos() {
        return pasos;
    }

    /** Primera fila desde 'desdeFila' con elemento distinto de cero en la columna, o -1. */
    int buscarPivote(int desdeFila, int columna) {
        for (int i = desdeFila; i < datos.length; i++) {
            if (!datos[i][columna].esCero()) {
                return i;
            }
        }
        return -1;
    }

    void intercambiarFilas(int a, int b) {
        aplicar(OperacionFila.intercambiar(a, b), List.of(a, b),
                "Se intercambian las filas F" + (a + 1) + " y F" + (b + 1) + " para obtener un pivote distinto de cero.");
    }

    /** Divide la fila entre el pivote para que el pivote valga 1. */
    void dividirFila(int fila, Fraccion pivote) {
        aplicar(OperacionFila.escalar(fila, Fraccion.UNO.dividir(pivote)), List.of(fila),
                "Se divide F" + (fila + 1) + " entre " + pivote + " para que el pivote valga 1.");
    }

    /** F[destino] = F[destino] - factor * F[origen]. */
    void eliminar(int destino, int origen, Fraccion factor, int columna) {
        aplicar(OperacionFila.eliminar(destino, origen, factor), List.of(destino, origen),
                "F" + (destino + 1) + " = F" + (destino + 1) + " - (" + factor + ") * F" + (origen + 1)
                        + " para anular el elemento de la columna " + (columna + 1) + ".");
    }

    void sustituir(int variable, String explicacion) {
        String actual = estado();
        pasos.add(new Paso(pasos.size() + 1, TipoOperacion.SUSTITUIR_ATRAS, List.of(variable),
                actual, actual, explicacion));
    }

    void finalizar(String explicacion) {
        String actual = estado();
        pasos.add(new Paso(pasos.size() + 1, TipoOperacion.FINALIZAR, List.of(), actual, actual, explicacion));
    }

    private void aplicar(OperacionFila operacion, List<Integer> indices, String explicacion) {
        String antes = estado();
        operacion.aplicarEn(datos);
        pasos.add(new Paso(pasos.size() + 1, operacion.tipo(), indices, antes, estado(), explicacion));
    }
}
