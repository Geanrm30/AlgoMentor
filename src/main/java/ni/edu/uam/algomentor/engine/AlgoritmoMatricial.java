package ni.edu.uam.algomentor.engine;

public interface AlgoritmoMatricial {

    String nombre();

    /**
     * Resuelve el sistema representado por la matriz ampliada (ultima columna = termino independiente).
     * No modifica la matriz recibida. Lanza IllegalArgumentException si la forma no es valida.
     */
    ResultadoMatriz ejecutar(Fraccion[][] matrizAmpliada);
}
