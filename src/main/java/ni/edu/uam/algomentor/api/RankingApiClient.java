package ni.edu.uam.algomentor.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class RankingApiClient {

    private static final URI ENDPOINT = URI.create("http://127.0.0.1:8765/api/ranking");
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();

    public List<RankingPerfil> listar() throws IOException, InterruptedException {
        HttpRequest solicitud = HttpRequest.newBuilder(ENDPOINT)
                .timeout(Duration.ofSeconds(15)).GET().build();
        HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
        if (respuesta.statusCode() != 200) {
            throw new IOException("No se pudo consultar el ranking. Revisa PostgreSQL y la API local.");
        }
        return json.readValue(respuesta.body(), new TypeReference<>() { });
    }
}
