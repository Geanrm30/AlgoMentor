package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.*;
import ni.edu.uam.algomentor.util.EntradaAprendizaje;
import ni.edu.uam.algomentor.util.TextoInterfaz;

import java.util.Arrays;

public final class ComandosTerminal {

    private ComandosTerminal() { }

    public static String ejecutar(String comando) {
        if (comando == null || comando.isBlank()) {
            throw new IllegalArgumentException("Escribe un comando. Usa ayuda para ver las opciones.");
        }
        String[] partes = comando.strip().split("\\s+", 3);
        if (partes[0].equalsIgnoreCase("ayuda") && partes.length == 1) {
            return "Ordenamientos: " + CatalogoAlgoritmos.ordenamientos().stream()
                    .map(CatalogoAlgoritmos::comando).collect(java.util.stream.Collectors.joining(", "))
                    + ". Sistemas: gauss y jordan.\n"
                    + "Ejemplos:\nordenar bubble 4,2,1\nordenar insertion 4,2,1\n"
                    + "resolver gauss 1,1,3;1,-1,1\nresolver jordan 1,1,3;1,-1,1\nlimpiar";
        }
        if (partes[0].equalsIgnoreCase("limpiar") && partes.length == 1) {
            return "";
        }
        if (partes.length != 3) {
            throw new IllegalArgumentException("Comando incompleto. Usa ayuda para ver la sintaxis.");
        }
        if (partes[0].equalsIgnoreCase("ordenar")) {
            int[] datos = EntradaAprendizaje.leerArreglo(partes[2]);
            ResultadoEjecucion resultado = CatalogoAlgoritmos.porComando(partes[1])
                    .crearOrdenamiento().ejecutar(datos);
            return "Entrada: " + Arrays.toString(datos) + "\n" + describir(resultado);
        }
        if (partes[0].equalsIgnoreCase("resolver")) {
            Fraccion[][] datos = EntradaAprendizaje.leerMatriz(partes[2].replace(';', '\n'));
            ResultadoMatriz resultado = CatalogoAlgoritmos.porComando(partes[1]).crearMatricial().ejecutar(datos);
            return describir(resultado.ejecucion()) + "Sistema: " + TextoInterfaz.clasificacion(resultado.clasificacion())
                    + (resultado.solucion().isEmpty() ? "" : " · " + TextoInterfaz.solucion(resultado.solucion()));
        }
        throw new IllegalArgumentException("Comando desconocido. Usa ayuda para ver las opciones.");
    }

    private static String describir(ResultadoEjecucion resultado) {
        StringBuilder texto = new StringBuilder();
        for (Paso paso : resultado.pasos()) {
            texto.append(paso.numero()).append(". ").append(paso.explicacion())
                    .append(" → ").append(paso.estadoPosterior()).append('\n');
        }
        return texto.toString();
    }
}
