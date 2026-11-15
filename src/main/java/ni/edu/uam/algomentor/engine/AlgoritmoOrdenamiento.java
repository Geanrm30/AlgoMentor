package ni.edu.uam.algomentor.engine;

public interface AlgoritmoOrdenamiento {

    String nombre();

    /** No modifica el arreglo recibido. Lanza IllegalArgumentException si los datos no son validos. */
    ResultadoEjecucion ejecutar(int[] datos);
}