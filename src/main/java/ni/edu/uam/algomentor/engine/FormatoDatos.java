package ni.edu.uam.algomentor.engine;

/**
 * Formato de texto versionado para guardar datos en Ejercicio.datos.
 * Arreglo: v1;ARREGLO;5,3,8,1
 * Matriz:  v1;MATRIZ;2x3;2,1,5|1,-1,1   (filas separadas por |, celdas enteras o fracciones a/b)
 */
public final class FormatoDatos {

    public static final String VERSION = "v1";
    public static final int MAX_ELEMENTOS = 30;
    public static final int MAX_FILAS = 6;
    public static final int MAX_COLUMNAS = 7;

    private FormatoDatos() { }

    // ---------- Arreglos ----------

    public static String serializarArreglo(int[] datos) {
        StringBuilder sb = new StringBuilder(VERSION).append(";ARREGLO;");
        for (int i = 0; i < datos.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(datos[i]);
        }
        return sb.toString();
    }

    public static int[] leerArreglo(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Los datos del ejercicio estan vacios.");
        }
        String[] partes = texto.strip().split(";", -1);
        if (partes.length != 3 || !VERSION.equals(partes[0]) || !"ARREGLO".equals(partes[1])) {
            throw new IllegalArgumentException("Formato no reconocido. Se esperaba: v1;ARREGLO;n1,n2,...");
        }
        String[] numeros = partes[2].split(",", -1);
        if (numeros.length > MAX_ELEMENTOS) {
            throw new IllegalArgumentException("Maximo " + MAX_ELEMENTOS + " elementos.");
        }
        int[] resultado = new int[numeros.length];
        try {
            for (int i = 0; i < numeros.length; i++) {
                resultado[i] = Integer.parseInt(numeros[i].strip());
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El arreglo solo admite numeros enteros.", e);
        }
        return resultado;
    }

    // ---------- Matrices ----------

    /** Comprueba forma rectangular, limites y celdas no nulas. La ultima columna es el termino independiente. */
    public static void validarMatriz(Fraccion[][] matriz) {
        if (matriz == null || matriz.length < 1 || matriz.length > MAX_FILAS) {
            throw new IllegalArgumentException("La matriz debe tener entre 1 y " + MAX_FILAS + " filas.");
        }
        int columnas = matriz[0] == null ? 0 : matriz[0].length;
        if (columnas < 2 || columnas > MAX_COLUMNAS) {
            throw new IllegalArgumentException("La matriz debe tener entre 2 y " + MAX_COLUMNAS
                    + " columnas (coeficientes mas el termino independiente).");
        }
        for (Fraccion[] fila : matriz) {
            if (fila == null || fila.length != columnas) {
                throw new IllegalArgumentException("Todas las filas deben tener el mismo numero de columnas.");
            }
            for (Fraccion celda : fila) {
                if (celda == null) {
                    throw new IllegalArgumentException("La matriz tiene celdas vacias.");
                }
            }
        }
    }

    public static String serializarMatriz(Fraccion[][] matriz) {
        validarMatriz(matriz);
        return VERSION + ";MATRIZ;" + matriz.length + "x" + matriz[0].length + ";" + matrizAEstado(matriz);
    }

    /** Solo el cuerpo ("2,1,5|1,-1,1"). Es el formato de estadoAnterior/estadoPosterior de los pasos. */
    public static String matrizAEstado(Fraccion[][] matriz) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matriz.length; i++) {
            if (i > 0) {
                sb.append('|');
            }
            for (int j = 0; j < matriz[i].length; j++) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append(matriz[i][j]);
            }
        }
        return sb.toString();
    }

    public static Fraccion[][] leerMatriz(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Los datos del ejercicio estan vacios.");
        }
        String[] partes = texto.strip().split(";", -1);
        if (partes.length != 4 || !VERSION.equals(partes[0]) || !"MATRIZ".equals(partes[1])) {
            throw new IllegalArgumentException("Formato no reconocido. Se esperaba: v1;MATRIZ;FxC;fila|fila");
        }
        String[] dimensiones = partes[2].split("x", -1);
        if (dimensiones.length != 2) {
            throw new IllegalArgumentException("Las dimensiones deben tener la forma FxC, por ejemplo 2x3.");
        }
        int filas;
        int columnas;
        try {
            filas = Integer.parseInt(dimensiones[0].strip());
            columnas = Integer.parseInt(dimensiones[1].strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Las dimensiones deben ser numeros enteros.", e);
        }
        Fraccion[][] matriz = leerEstadoMatriz(partes[3]);
        if (matriz.length != filas || matriz[0].length != columnas) {
            throw new IllegalArgumentException("Las dimensiones no coinciden con el contenido de la matriz.");
        }
        validarMatriz(matriz);
        return matriz;
    }

    /** Lee un estado de paso ("2,1,5|1,-1,1") y lo convierte en matriz. */
    public static Fraccion[][] leerEstadoMatriz(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado de la matriz esta vacio.");
        }
        String[] filas = estado.strip().split("\\|", -1);
        Fraccion[][] matriz = new Fraccion[filas.length][];
        for (int i = 0; i < filas.length; i++) {
            String[] celdas = filas[i].split(",", -1);
            if (i > 0 && celdas.length != matriz[0].length) {
                throw new IllegalArgumentException("Todas las filas deben tener el mismo numero de columnas.");
            }
            matriz[i] = new Fraccion[celdas.length];
            for (int j = 0; j < celdas.length; j++) {
                matriz[i][j] = Fraccion.leer(celdas[j]);
            }
        }
        return matriz;
    }
}
