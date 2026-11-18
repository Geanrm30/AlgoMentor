package ni.edu.uam.algomentor.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ni.edu.uam.algomentor.engine.ClasificacionSistema;
import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.engine.FormatoDatos;
import ni.edu.uam.algomentor.engine.Fraccion;
import ni.edu.uam.algomentor.engine.TipoOperacion;
import ni.edu.uam.algomentor.model.Ejercicio;

import java.util.Arrays;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.List;
import java.util.StringJoiner;

public final class TextoInterfaz {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private TextoInterfaz() { }

    public static String categoria(String valor) {
        return switch (valor) {
            case "ORDENAMIENTO" -> "Ordenamiento";
            case "MATEMATICO" -> "Matemáticas";
            default -> valor;
        };
    }

    public static String origen(String valor) {
        return switch (valor) {
            case "ALEATORIO" -> "Aleatorio";
            case "MANUAL" -> "Manual";
            case "PERSONALIZADO" -> "Personalizado";
            default -> valor;
        };
    }

    public static String algoritmo(String valor) {
        return CatalogoAlgoritmos.admite(valor) ? CatalogoAlgoritmos.porNombre(valor).etiqueta() : valor;
    }

    public static String modo(String valor) {
        return switch (valor) {
            case "PRACTICA" -> "Práctica";
            case "SIMULACION" -> "Simulación";
            default -> valor;
        };
    }

    public static String fecha(LocalDateTime valor) {
        return valor == null ? "Sin fecha" : FECHA.format(valor);
    }

    public static String solucion(List<Fraccion> valores) {
        return IntStream.range(0, valores.size())
                .mapToObj(i -> "x" + (i + 1) + " = " + valores.get(i))
                .collect(Collectors.joining(" · "));
    }

    public static String solucionDecimal(List<Fraccion> valores) {
        return IntStream.range(0, valores.size())
                .mapToObj(i -> "x" + (i + 1) + " = " + decimal(new BigDecimal(valores.get(i).numerador())
                        .divide(new BigDecimal(valores.get(i).denominador()), 6, RoundingMode.HALF_UP)))
                .collect(Collectors.joining(" · "));
    }

    public static String solucionExterna(String respuesta) {
        try {
            JsonNode valores = new ObjectMapper().readTree(respuesta);
            if (!valores.isArray() || valores.isEmpty()) {
                return respuesta;
            }
            StringJoiner texto = new StringJoiner(" · ");
            for (int i = 0; i < valores.size(); i++) {
                JsonNode fila = valores.get(i);
                if (!fila.isArray() || fila.size() != 1 || !fila.get(0).isNumber()) {
                    return respuesta;
                }
                texto.add("x" + (i + 1) + " = " + decimal(new BigDecimal(fila.get(0).asText())));
            }
            return texto.toString();
        } catch (IOException e) {
            return respuesta;
        }
    }

    private static String decimal(BigDecimal valor) {
        BigDecimal redondeado = valor.setScale(6, RoundingMode.HALF_UP).stripTrailingZeros();
        return redondeado.signum() == 0 ? "0" : redondeado.toPlainString();
    }

    public static String clasificacion(ClasificacionSistema valor) {
        return switch (valor) {
            case UNICA -> "Una solución";
            case INFINITAS -> "Infinitas soluciones";
            case SIN_SOLUCION -> "Sin solución";
        };
    }

    public static String operacion(TipoOperacion valor) {
        return switch (valor) {
            case COMPARAR -> "Comparar elementos";
            case INTERCAMBIAR -> "Intercambiar elementos";
            case PIVOTE -> "Seleccionar pivote";
            case PARTICIONAR -> "Particionar";
            case DIVIDIR -> "Dividir subarreglo";
            case MEZCLAR -> "Mezclar subarreglos";
            case ESCRIBIR -> "Escribir elemento";
            case ESCRIBIR_AUXILIAR -> "Escribir en cubeta auxiliar";
            case CONTAR -> "Contar frecuencia";
            case DISTRIBUIR -> "Distribuir elemento";
            case PASADA -> "Procesar posición de dígito";
            case ORDENAR_CUBETA -> "Ordenar cubeta";
            case FINALIZAR -> "Finalizar";
            case INTERCAMBIAR_FILAS -> "Intercambiar filas";
            case ESCALAR_FILA -> "Multiplicar una fila";
            case ELIMINAR_FILA -> "Eliminar un valor de una fila";
            case SUSTITUIR_ATRAS -> "Sustituir hacia atrás";
        };
    }

    public static String datos(Ejercicio ejercicio) {
        String datos = ejercicio.getDatos();
        if (datos == null || datos.isBlank()) {
            return "Sin datos";
        }
        try {
            if (datos.startsWith(FormatoDatos.VERSION + ";ARREGLO;")) {
                return "Números: " + Arrays.stream(FormatoDatos.leerArreglo(datos))
                        .mapToObj(Integer::toString).collect(Collectors.joining(", "));
            }
            if (datos.startsWith(FormatoDatos.VERSION + ";MATRIZ;")) {
                return matriz(FormatoDatos.leerMatriz(datos));
            }
            if (ejercicio.getAlgoritmo().getNombre().startsWith("Gauss")) {
                return matriz(EntradaAprendizaje.leerMatriz(datos));
            }
            return "Números: " + Arrays.stream(datos.split(",", -1))
                    .map(String::strip).collect(Collectors.joining(", "));
        } catch (IllegalArgumentException e) {
            return "Datos inválidos";
        }
    }

    private static String matriz(Fraccion[][] valores) {
        return IntStream.range(0, valores.length)
                .mapToObj(fila -> "F" + (fila + 1) + ": " + Arrays.stream(valores[fila])
                        .map(Fraccion::toString).collect(Collectors.joining(", ")))
                .collect(Collectors.joining(" · "));
    }
}
