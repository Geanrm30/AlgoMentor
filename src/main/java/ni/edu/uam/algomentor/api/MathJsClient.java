package ni.edu.uam.algomentor.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ni.edu.uam.algomentor.engine.Fraccion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

public class MathJsClient {

    private static final URI ENDPOINT = URI.create("https://api.mathjs.org/v4/");
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();

    public String resolver(Fraccion[][] ampliada) throws IOException, InterruptedException {
        String expresion = expresionSistema(ampliada);
        HttpRequest solicitud = HttpRequest.newBuilder(ENDPOINT)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(
                        json.writeValueAsString(Map.of("expr", expresion))))
                .build();
        HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
        if (respuesta.statusCode() != 200) {
            throw new IOException("math.js respondió con HTTP " + respuesta.statusCode() + ".");
        }
        JsonNode cuerpo = json.readTree(respuesta.body());
        if (cuerpo.hasNonNull("error")) {
            throw new IOException("math.js rechazó el sistema: " + cuerpo.get("error").asText());
        }
        if (!cuerpo.hasNonNull("result") || cuerpo.get("result").asText().isBlank()) {
            throw new IOException("math.js no devolvió una solución.");
        }
        return cuerpo.get("result").asText();
    }

    static String expresionSistema(Fraccion[][] ampliada) {
        if (ampliada == null || ampliada.length == 0) {
            throw new IllegalArgumentException("Ingresa una matriz ampliada cuadrada.");
        }
        StringJoiner coeficientes = new StringJoiner(",", "[", "]");
        StringJoiner resultados = new StringJoiner(",", "[", "]");
        for (Fraccion[] fila : ampliada) {
            if (fila == null || fila.length != ampliada.length + 1) {
                throw new IllegalArgumentException("El sistema debe tener tantas ecuaciones como incógnitas.");
            }
            StringJoiner valores = new StringJoiner(",", "[", "]");
            for (int columna = 0; columna < ampliada.length; columna++) {
                if (fila[columna] == null) {
                    throw new IllegalArgumentException("La matriz contiene una celda vacía.");
                }
                valores.add(fila[columna].toString());
            }
            if (fila[ampliada.length] == null) {
                throw new IllegalArgumentException("La matriz contiene una celda vacía.");
            }
            coeficientes.add(valores.toString());
            resultados.add(fila[ampliada.length].toString());
        }
        return "lusolve(" + coeficientes + "," + resultados + ")";
    }
}
