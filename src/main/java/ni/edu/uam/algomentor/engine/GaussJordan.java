package ni.edu.uam.algomentor.engine;

import java.util.ArrayList;
import java.util.List;

/** Gauss-Jordan: reduce la matriz a su forma escalonada reducida (pivotes 1 y columnas limpias). */
public class GaussJordan implements AlgoritmoMatricial {

    @Override
    public String nombre() {
        return "Gauss-Jordan";
    }

    @Override
    public ResultadoMatriz ejecutar(Fraccion[][] matrizAmpliada) {
        TableroMatriz tablero = new TableroMatriz(matrizAmpliada);
        int variables = tablero.columnas() - 1;
        List<Integer> columnasPivote = new ArrayList<>();
        int fila = 0;

        for (int c = 0; c < variables && fila < tablero.filas(); c++) {
            int pivote = tablero.buscarPivote(fila, c);
            if (pivote < 0) {
                continue;
            }
            if (pivote != fila) {
                tablero.intercambiarFilas(fila, pivote);
            }
            Fraccion valorPivote = tablero.get(fila, c);
            if (!valorPivote.equals(Fraccion.UNO)) {
                tablero.dividirFila(fila, valorPivote);
            }
            for (int i = 0; i < tablero.filas(); i++) {
                if (i == fila) {
                    continue;
                }
                Fraccion elemento = tablero.get(i, c);
                if (!elemento.esCero()) {
                    tablero.eliminar(i, fila, elemento, c);
                }
            }
            columnasPivote.add(c);
            fila++;
        }
        return Clasificador.concluir(tablero, columnasPivote, true);
    }
}
