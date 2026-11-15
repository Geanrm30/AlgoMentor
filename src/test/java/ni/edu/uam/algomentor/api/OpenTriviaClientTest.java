package ni.edu.uam.algomentor.api;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenTriviaClientTest {

    private HttpServer server;
    private OpenTriviaClient client;
    private String responseBody;
    private int statusCode;
    private String requestUri;

    @BeforeEach
    void iniciarServidor() throws IOException {
        responseBody = """
                {"response_code":0,"results":[]}
                """;
        statusCode = 200;
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api.php", exchange -> {
            requestUri = exchange.getRequestURI().toString();
            byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();
        client = new OpenTriviaClient(URI.create(
                "http://localhost:" + server.getAddress().getPort() + "/api.php"));
    }

    @AfterEach
    void detenerServidor() {
        server.stop(0);
    }

    @Test
    void solicitaPreguntasDeComputacionConParametrosEsperados() throws Exception {
        OpenTriviaResponse response = client.obtenerPreguntasComputacion(3);

        assertEquals(0, response.responseCode());
        assertTrue(requestUri.contains("amount=3"));
        assertTrue(requestUri.contains("category=18"));
        assertTrue(requestUri.contains("type=multiple"));
        assertTrue(requestUri.contains("encode=url3986"));
    }

    @Test
    void informaLosCodigosDeErrorDeOpenTriviaDb() {
        responseBody = """
                {"response_code":5,"results":[]}
                """;

        IOException exception = assertThrows(IOException.class,
                () -> client.obtenerPreguntasComputacion(1));

        assertTrue(exception.getMessage().contains("5 segundos"));
    }

    @Test
    void rechazaRespuestasHttpFallidas() {
        statusCode = 503;

        IOException exception = assertThrows(IOException.class,
                () -> client.obtenerPreguntasComputacion(1));

        assertTrue(exception.getMessage().contains("HTTP 503"));
    }

    @Test
    void validaCantidadAntesDeEnviarLaSolicitud() {
        assertThrows(IllegalArgumentException.class,
                () -> client.obtenerPreguntasComputacion(0));
    }

    @Test
    void esperaElIntervaloConfiguradoEntreSolicitudes() throws Exception {
        OpenTriviaClient clienteConIntervalo = new OpenTriviaClient(
                URI.create("http://localhost:" + server.getAddress().getPort() + "/api.php"),
                Duration.ofMillis(100));

        clienteConIntervalo.obtenerPreguntasComputacion(1);
        long inicioSegundaSolicitud = System.nanoTime();
        clienteConIntervalo.obtenerPreguntasComputacion(1);
        long transcurridoMillis = Duration.ofNanos(
                System.nanoTime() - inicioSegundaSolicitud).toMillis();

        assertTrue(transcurridoMillis >= 80,
                "La segunda solicitud debe esperar el intervalo configurado");
    }
}
