package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.*;
import ni.edu.uam.algomentor.util.EntradaAprendizaje;

import java.util.Arrays;

public final class EvaluadorPractica {

    private EvaluadorPractica() { }

    public record Resultado(boolean correcto, String esperado) { }

    public static Resultado evaluar(String algoritmo, String datos, String respuesta) {
        if (respuesta == null || respuesta.isBlank()) {
            throw new IllegalArgumentException("Ingresa una respuesta antes de verificar.");
        }
        return switch (algoritmo) {
            case "Bubble Sort", "Insertion Sort" -> evaluarOrdenamiento(algoritmo, datos, respuesta);
            case "Gauss", "Gauss-Jordan" -> evaluarSistema(algoritmo, datos, respuesta);
            default -> throw new IllegalArgumentException("Este algoritmo no tiene evaluación de práctica: " + algoritmo);
        };
    }

    private static Resultado evaluarOrdenamiento(String algoritmo, String datos, String respuesta) {
        int[] entrada = datos != null && datos.startsWith("v1;")
                ? FormatoDatos.leerArreglo(datos) : EntradaAprendizaje.leerArreglo(datos);
        if (entrada.length == 0 || entrada.length > 12) {
            throw new IllegalArgumentException("El ejercicio debe tener entre 1 y 12 elementos.");
        }
        int[] propuesta = EntradaAprendizaje.leerArreglo(respuesta);
        ResultadoEjecucion ejecucion = "Bubble Sort".equals(algoritmo)
                ? new BubbleSort().ejecutar(entrada) : new InsertionSort().ejecutar(entrada);
        int[] esperado = EntradaAprendizaje.leerArreglo(
                ejecucion.estadoFinal().substring(1, ejecucion.estadoFinal().length() - 1));
        return new Resultado(Arrays.equals(propuesta, esperado), Arrays.toString(esperado));
    }

    private static Resultado evaluarSistema(String algoritmo, String datos, String respuesta) {
        Fraccion[][] entrada = datos != null && datos.startsWith("v1;")
                ? FormatoDatos.leerMatriz(datos) : EntradaAprendizaje.leerMatriz(datos);
        ResultadoMatriz resultado = "Gauss".equals(algoritmo)
                ? new Gauss().ejecutar(entrada) : new GaussJordan().ejecutar(entrada);
        if (resultado.clasificacion() != ClasificacionSistema.UNICA) {
            String esperado = resultado.clasificacion().name();
            return new Resultado(esperado.equalsIgnoreCase(respuesta.strip()), esperado);
        }
        String[] partes = respuesta.strip().split(",", -1);
        if (partes.length != resultado.solucion().size()) {
            throw new IllegalArgumentException("Ingresa " + resultado.solucion().size() + " valores separados por coma.");
        }
        Fraccion[] propuesta = new Fraccion[partes.length];
        for (int i = 0; i < partes.length; i++) {
            propuesta[i] = Fraccion.leer(partes[i]);
        }
        return new Resultado(Arrays.asList(propuesta).equals(resultado.solucion()), resultado.solucion().toString());
    }
}
