package ni.edu.uam.algomentor.util;

import ni.edu.uam.algomentor.dao.EstadisticasDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exporta el historial de intentos a CSV (UTF-8 con BOM, para que Excel respete las tildes). Las celdas que
 * empiezan con =, +, - o @ se neutralizan para que una hoja de cálculo no las interprete como fórmulas.
 */
public final class CsvHistorial {

    private static final String SEPARADOR_LINEA = "\r\n";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CsvHistorial() { }

    public static String generar(List<EstadisticasDAO.IntentoReciente> intentos) {
        StringBuilder csv = new StringBuilder("Fecha,Algoritmo,Modo,Resultado,Duración (segundos)")
                .append(SEPARADOR_LINEA);
        for (EstadisticasDAO.IntentoReciente intento : intentos) {
            csv.append(celda(intento.fecha() == null ? "" : intento.fecha().format(FECHA))).append(',')
                    .append(celda(TextoInterfaz.algoritmo(intento.algoritmo()))).append(',')
                    .append(celda(TextoInterfaz.modo(intento.modo()))).append(',')
                    .append(celda(TextoInterfaz.resultado(intento.correcto()))).append(',')
                    .append(celda(intento.duracionSegundos() == null ? "" : intento.duracionSegundos().toString()))
                    .append(SEPARADOR_LINEA);
        }
        return csv.toString();
    }

    public static Path escribir(Path destino, List<EstadisticasDAO.IntentoReciente> intentos) throws IOException {
        byte[] contenido = ("﻿" + generar(intentos)).getBytes(StandardCharsets.UTF_8);
        return Files.write(destino, contenido);
    }

    static String celda(String valor) {
        String texto = valor == null ? "" : valor;
        if (!texto.isEmpty() && "=+-@\t\r".indexOf(texto.charAt(0)) >= 0) {
            texto = "'" + texto;
        }
        if (texto.contains(",") || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            texto = "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }
}
