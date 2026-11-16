package ni.edu.uam.algomentor.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class EjerciciosApiClient {

    private static final String BASE = "http://127.0.0.1:8765/api/ejercicios/";
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();

    public byte[] exportar() throws IOException, InterruptedException {
        HttpRequest solicitud = HttpRequest.newBuilder(URI.create(BASE + "exportar"))
                .timeout(Duration.ofSeconds(30)).GET().build();
        HttpResponse<byte[]> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofByteArray());
        comprobar(respuesta.statusCode(), respuesta.body());
        return respuesta.body();
    }

    public ResultadoImportacion importar(byte[] contenido) throws IOException, InterruptedException {
        HttpRequest solicitud = HttpRequest.newBuilder(URI.create(BASE + "importar"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofByteArray(contenido)).build();
        HttpResponse<byte[]> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofByteArray());
        comprobar(respuesta.statusCode(), respuesta.body());
        return json.readValue(respuesta.body(), ResultadoImportacion.class);
    }

    private void comprobar(int estado, byte[] cuerpo) throws IOException {
        if (estado >= 200 && estado < 300) {
            return;
        }
        try {
            JsonNode error = json.readTree(cuerpo);
            if (error.hasNonNull("error")) {
                throw new IOException(error.get("error").asText());
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IOException("La API respondió con HTTP " + estado, e);
        }
        throw new IOException("La API respondió con HTTP " + estado);
    }
}
