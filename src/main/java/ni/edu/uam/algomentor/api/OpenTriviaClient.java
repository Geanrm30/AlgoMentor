package ni.edu.uam.algomentor.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OpenTriviaClient {

    private static final URI ENDPOINT = URI.create("https://opentdb.com/api.php");
    private static final int COMPUTERS_CATEGORY_ID = 18;
    private static final int MAX_QUESTIONS_PER_REQUEST = 50;
    private static final Duration REQUEST_INTERVAL = Duration.ofSeconds(5);

    private final URI endpoint;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration requestInterval;
    private long lastRequestStartedNanos;
    private boolean hasSentRequest;

    public OpenTriviaClient() {
        this(ENDPOINT, REQUEST_INTERVAL);
    }

    OpenTriviaClient(URI endpoint) {
        this(endpoint, REQUEST_INTERVAL);
    }

    OpenTriviaClient(URI endpoint, Duration requestInterval) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.requestInterval = Objects.requireNonNull(requestInterval, "requestInterval");
        if (requestInterval.isNegative()) {
            throw new IllegalArgumentException("El intervalo entre solicitudes no puede ser negativo");
        }
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public OpenTriviaResponse obtenerPreguntasComputacion(int cantidad)
            throws IOException, InterruptedException {
        if (cantidad < 1 || cantidad > MAX_QUESTIONS_PER_REQUEST) {
            throw new IllegalArgumentException(
                    "La cantidad de preguntas debe estar entre 1 y " + MAX_QUESTIONS_PER_REQUEST);
        }

        esperarIntervaloEntreSolicitudes();
        URI requestUri = URI.create(endpoint + "?amount=" + cantidad
                + "&category=" + COMPUTERS_CATEGORY_ID
                + "&type=multiple&encode=url3986");
        HttpRequest request = HttpRequest.newBuilder(requestUri)
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> httpResponse = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
            throw new IOException("Open Trivia DB respondió con HTTP " + httpResponse.statusCode());
        }

        OpenTriviaResponse response;
        try {
            response = objectMapper.readValue(httpResponse.body(), OpenTriviaResponse.class);
        } catch (IOException exception) {
            throw new IOException("Open Trivia DB devolvió una respuesta JSON inválida", exception);
        }

        if (response.responseCode() != 0) {
            throw new IOException(mensajeErrorApi(response.responseCode()));
        }
        return response;
    }

    private synchronized void esperarIntervaloEntreSolicitudes() throws InterruptedException {
        if (hasSentRequest) {
            long elapsedNanos = System.nanoTime() - lastRequestStartedNanos;
            long remainingNanos = requestInterval.toNanos() - elapsedNanos;
            if (remainingNanos > 0) {
                TimeUnit.NANOSECONDS.sleep(remainingNanos);
            }
        }
        lastRequestStartedNanos = System.nanoTime();
        hasSentRequest = true;
    }

    private String mensajeErrorApi(int responseCode) {
        return switch (responseCode) {
            case 1 -> "Open Trivia DB no encontró suficientes preguntas para la solicitud";
            case 2 -> "Open Trivia DB rechazó uno de los parámetros de la solicitud";
            case 3 -> "Open Trivia DB no encontró el token de sesión";
            case 4 -> "Open Trivia DB agotó las preguntas disponibles para el token";
            case 5 -> "Open Trivia DB limitó las solicitudes; espera al menos 5 segundos";
            default -> "Open Trivia DB devolvió el código de respuesta " + responseCode;
        };
    }
}
