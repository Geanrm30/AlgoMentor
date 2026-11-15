package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.FormatoDatos;
import ni.edu.uam.algomentor.engine.Fraccion;
import ni.edu.uam.algomentor.engine.OperacionFila;
import ni.edu.uam.algomentor.engine.ResultadoVerificacion;
import ni.edu.uam.algomentor.engine.TipoOperacion;
import ni.edu.uam.algomentor.engine.VerificadorOperaciones;
import ni.edu.uam.algomentor.util.EntradaAprendizaje;

public class EvaluadorPasoMatriz {

    private final VerificadorOperaciones verificador = new VerificadorOperaciones();

    public ResultadoVerificacion evaluar(String datos, TipoOperacion tipo, String destino,
                                         String origen, String factor, String matrizEscrita) {
        Fraccion[][] anterior = datos != null && datos.startsWith("v1;")
                ? FormatoDatos.leerMatriz(datos) : EntradaAprendizaje.leerMatriz(datos);
        OperacionFila operacion = leerOperacion(tipo, destino, origen, factor);
        Fraccion[][] propuesta = EntradaAprendizaje.leerMatriz(matrizEscrita);
        return verificador.verificar(anterior, operacion, propuesta);
    }

    private OperacionFila leerOperacion(TipoOperacion tipo, String destino, String origen, String factor) {
        if (tipo == null) {
            throw new IllegalArgumentException("Selecciona una operación de filas.");
        }
        int filaDestino = leerFila(destino, "destino");
        return switch (tipo) {
            case INTERCAMBIAR_FILAS -> OperacionFila.intercambiar(filaDestino, leerFila(origen, "origen"));
            case ESCALAR_FILA -> OperacionFila.escalar(filaDestino, Fraccion.leer(factor));
            case ELIMINAR_FILA -> OperacionFila.eliminar(filaDestino, leerFila(origen, "origen"), Fraccion.leer(factor));
            default -> throw new IllegalArgumentException("Operación de filas no permitida.");
        };
    }

    private int leerFila(String texto, String nombre) {
        try {
            int fila = Integer.parseInt(texto.strip());
            if (fila < 1) {
                throw new NumberFormatException();
            }
            return fila - 1;
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalArgumentException("La fila " + nombre + " debe ser un entero positivo.", e);
        }
    }
}
