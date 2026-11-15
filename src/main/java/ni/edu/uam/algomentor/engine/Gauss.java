package ni.edu.uam.algomentor.engine;

import java.util.ArrayList;
import java.util.List;

/** Eliminacion de Gauss: forma escalonada por eliminacion hacia adelante y sustitucion hacia atras. */
public class Gauss implements AlgoritmoMatricial {

    @Override
    public String nombre() {
        return "Eliminacion de Gauss";
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
            for (int i = fila + 1; i < tablero.filas(); i++) {
                Fraccion elemento = tablero.get(i, c);
                if (!elemento.esCero()) {
                    Fraccion factor = elemento.dividir(tablero.get(fila, c));
                    tablero.eliminar(i, fila, factor, c);
                }
            }
            columnasPivote.add(c);
            fila++;
        }
        return Clasificador.concluir(tablero, columnasPivote, false);
    }
}
