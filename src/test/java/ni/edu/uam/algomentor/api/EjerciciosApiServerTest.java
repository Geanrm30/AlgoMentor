package ni.edu.uam.algomentor.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EjerciciosApiServerTest {

    private HttpServer servidor;
    private URI base;
    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void iniciar() throws IOException {
        servidor = EjerciciosApiServer.crear(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), 0),
                new IntercambioEjerciciosService() {
                    @Override
                    public PaqueteEjercicios exportar() {
                        return new PaqueteEjercicios(1,
                                List.of(new EjercicioIntercambio("Bubble Sort", "3,1", 2, "MANUAL")));
                    }

                    @Override
                    public ResultadoImportacion importar(PaqueteEjercicios paquete) {
                        if (paquete.version() != 1) {
                            throw new IllegalArgumentException("Versión inválida.");
                        }
                        return new ResultadoImportacion(paquete.ejercicios().size(), 0);
                    }
                });
        base = URI.create("http://127.0.0.1:" + servidor.getAddress().getPort()
                + "/api/ejercicios/");
    }

    @AfterEach
    void detener() {
        servidor.stop(0);
    }

    @Test
    void exportaJsonPortableSinIdsDeBaseDeDatos() throws Exception {
        HttpResponse<String> respuesta = cliente.send(
                HttpRequest.newBuilder(base.resolve("exportar")).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, respuesta.statusCode());
        assertEquals("Bubble Sort", json.readTree(respuesta.body())
                .get("ejercicios").get(0).get("algoritmo").asText());
        assertTrue(!respuesta.body().contains("algoritmo_id"));
    }

    @Test
    void importaUnLoteJsonYDevuelveElResumen() throws Exception {
        String cuerpo = """
                {"version":1,"ejercicios":[{"algoritmo":"Bubble Sort","datos":"3,1", "dificultad":2,"origen":"MANUAL"}]}
                """;
        HttpResponse<String> respuesta = cliente.send(
                HttpRequest.newBuilder(base.resolve("importar"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(cuerpo)).build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, respuesta.statusCode());
        assertEquals(1, json.readTree(respuesta.body()).get("importados").asInt());
    }

    @Test
    void rechazaJsonInvalidoYMetodoIncorrecto() throws Exception {
        HttpResponse<String> invalido = cliente.send(
                HttpRequest.newBuilder(base.resolve("importar"))
                        .POST(HttpRequest.BodyPublishers.ofString("{"))
                        .build(), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> metodo = cliente.send(
                HttpRequest.newBuilder(base.resolve("exportar"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(), HttpResponse.BodyHandlers.ofString());

        assertEquals(400, invalido.statusCode());
        assertEquals(405, metodo.statusCode());
        assertEquals("GET", metodo.headers().firstValue("Allow").orElseThrow());
    }
}
