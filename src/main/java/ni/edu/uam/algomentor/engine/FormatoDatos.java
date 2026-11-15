package ni.edu.uam.algomentor.engine;

public final class FormatoDatos {

    public static final String VERSION = "v1";
    public static final int MAX_ELEMENTOS = 30;

    private FormatoDatos() { }

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
        String[] partes = texto.strip().split(";");
        if (partes.length != 3 || !VERSION.equals(partes[0]) || !"ARREGLO".equals(partes[1])) {
            throw new IllegalArgumentException("Formato no reconocido. Se esperaba: v1;ARREGLO;n1,n2,...");
        }
        String[] numeros = partes[2].split(",");
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
}