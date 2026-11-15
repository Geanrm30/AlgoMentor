package ni.edu.uam.algomentor.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Cierra la resolucion: detecta si el sistema es incompatible, indeterminado o de solucion unica. */
final class Clasificador {

    private Clasificador() { }

    /**
     * @param columnasPivote columnas de coeficientes en las que se encontro pivote
     * @param reducida       true en Gauss-Jordan (la solucion se lee directo de la ultima columna);
     *                       false en Gauss (se agrega la sustitucion hacia atras)
     */
    static ResultadoMatriz concluir(TableroMatriz tablero, List<Integer> columnasPivote, boolean reducida) {
        int variables = tablero.columnas() - 1;

        for (int i = 0; i < tablero.filas(); i++) {
            if (filaDeCeros(tablero, i, variables) && !tablero.get(i, variables).esCero()) {
                tablero.finalizar("Sistema sin solucion: la fila F" + (i + 1) + " queda como 0 = "
                        + tablero.get(i, variables) + ".");
                return resultado(tablero, ClasificacionSistema.SIN_SOLUCION, List.of());
            }
        }

        if (columnasPivote.size() < variables) {
            List<String> libres = new ArrayList<>();
            for (int c = 0; c < variables; c++) {
                if (!columnasPivote.contains(c)) {
                    libres.add("x" + (c + 1));
                }
            }
            tablero.finalizar("Sistema con infinitas soluciones: las variables libres son "
                    + String.join(", ", libres) + ".");
            return resultado(tablero, ClasificacionSistema.INFINITAS, List.of());
        }

        List<Fraccion> x = new ArrayList<>(Collections.nCopies(variables, Fraccion.CERO));
        if (reducida) {
            for (int i = 0; i < variables; i++) {
                x.set(i, tablero.get(i, variables));
            }
        } else {
            for (int i = variables - 1; i >= 0; i--) {
                Fraccion suma = Fraccion.CERO;
                for (int j = i + 1; j < variables; j++) {
                    suma = suma.sumar(tablero.get(i, j).multiplicar(x.get(j)));
                }
                Fraccion valor = tablero.get(i, variables).restar(suma).dividir(tablero.get(i, i));
                x.set(i, valor);
                tablero.sustituir(i, "x" + (i + 1) + " = " + valor + " (se despeja de la fila F" + (i + 1) + ").");
            }
        }
        tablero.finalizar("Sistema con solucion unica: " + describir(x) + ".");
        return resultado(tablero, ClasificacionSistema.UNICA, x);
    }

    private static boolean filaDeCeros(TableroMatriz tablero, int fila, int variables) {
        for (int j = 0; j < variables; j++) {
            if (!tablero.get(fila, j).esCero()) {
                return false;
            }
        }
        return true;
    }

    private static String describir(List<Fraccion> x) {
        List<String> partes = new ArrayList<>();
        for (int i = 0; i < x.size(); i++) {
            partes.add("x" + (i + 1) + " = " + x.get(i));
        }
        return String.join(", ", partes);
    }

    private static ResultadoMatriz resultado(TableroMatriz tablero, ClasificacionSistema clasificacion,
                                             List<Fraccion> solucion) {
        ResultadoEjecucion ejecucion = new ResultadoEjecucion(tablero.estadoInicial(), tablero.estado(),
                tablero.pasos());
        return new ResultadoMatriz(ejecucion, clasificacion, solucion);
    }
}
