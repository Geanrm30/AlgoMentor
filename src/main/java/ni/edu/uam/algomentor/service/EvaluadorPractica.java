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
        CatalogoAlgoritmos seleccionado = CatalogoAlgoritmos.porNombre(algoritmo);
        return seleccionado.esOrdenamiento() ? evaluarOrdenamiento(seleccionado, datos, respuesta)
                : evaluarSistema(seleccionado, datos, respuesta);
    }

    private static Resultado evaluarOrdenamiento(CatalogoAlgoritmos algoritmo, String datos, String respuesta) {
        int[] entrada = EntradaAprendizaje.leerArregloGuardado(datos);
        int[] propuesta = EntradaAprendizaje.leerArreglo(respuesta);
        ResultadoEjecucion ejecucion = algoritmo.crearOrdenamiento().ejecutar(entrada);
        int[] esperado = EntradaAprendizaje.leerArreglo(
                ejecucion.estadoFinal().substring(1, ejecucion.estadoFinal().length() - 1));
        return new Resultado(Arrays.equals(propuesta, esperado), Arrays.toString(esperado));
    }

    private static Resultado evaluarSistema(CatalogoAlgoritmos algoritmo, String datos, String respuesta) {
        Fraccion[][] entrada = EntradaAprendizaje.leerMatrizGuardada(datos);
        ResultadoMatriz resultado = algoritmo.crearMatricial().ejecutar(entrada);
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
