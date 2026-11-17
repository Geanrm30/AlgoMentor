package ni.edu.uam.algomentor.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ni.edu.uam.algomentor.dao.RankingDAO;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

public final class EjerciciosApiServer {

    private static final int PUERTO = 8765;
    private static final int MAX_BYTES = 1_048_576;
    private static HttpServer servidor;

    private EjerciciosApiServer() { }

    public static synchronized void iniciar() throws IOException {
        if (servidor != null) {
            return;
        }
        servidor = crear(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), PUERTO),
                new IntercambioEjerciciosService());
    }

    public static synchronized void detener() {
        if (servidor != null) {
            servidor.stop(0);
            servidor = null;
        }
    }

    static HttpServer crear(InetSocketAddress direccion, IntercambioEjerciciosService servicio) throws IOException {
        HttpServer nuevo = HttpServer.create(direccion, 0);
        nuevo.createContext("/api/ejercicios/exportar", intercambio -> atender(intercambio, true, servicio));
        nuevo.createContext("/api/ejercicios/importar", intercambio -> atender(intercambio, false, servicio));
        nuevo.createContext("/api/ranking", EjerciciosApiServer::atenderRanking);
        nuevo.start();
        return nuevo;
    }

    private static void atenderRanking(HttpExchange intercambio) throws IOException {
        try (intercambio) {
            ObjectMapper json = new ObjectMapper();
            if (!"/api/ranking".equals(intercambio.getRequestURI().getPath())) {
                responder(intercambio, json, 404, Map.of("error", "Ruta no encontrada."));
            } else if (!"GET".equals(intercambio.getRequestMethod())) {
                intercambio.getResponseHeaders().set("Allow", "GET");
                responder(intercambio, json, 405, Map.of("error", "Método no permitido."));
            } else {
                try {
                    responder(intercambio, json, 200, new RankingDAO().listar());
                } catch (RuntimeException e) {
                    responder(intercambio, json, 503,
                            Map.of("error", "No se pudo consultar el ranking. Revisa PostgreSQL."));
                }
            }
        }
    }

    private static void atender(HttpExchange intercambio, boolean exportar, IntercambioEjerciciosService servicio)
            throws IOException {
        try (intercambio) {
            ObjectMapper json = new ObjectMapper();
            String ruta = exportar ? "/api/ejercicios/exportar" : "/api/ejercicios/importar";
            String metodo = exportar ? "GET" : "POST";
            if (!ruta.equals(intercambio.getRequestURI().getPath())) {
                responder(intercambio, json, 404, Map.of("error", "Ruta no encontrada."));
                return;
            }
            if (!metodo.equals(intercambio.getRequestMethod())) {
                intercambio.getResponseHeaders().set("Allow", metodo);
                responder(intercambio, json, 405, Map.of("error", "Método no permitido."));
                return;
            }
            try {
                if (exportar) {
                    responder(intercambio, json, 200, servicio.exportar());
                } else {
                    byte[] cuerpo = intercambio.getRequestBody().readNBytes(MAX_BYTES + 1);
                    if (cuerpo.length > MAX_BYTES) {
                        responder(intercambio, json, 413, Map.of("error", "El lote supera 1 MB."));
                        return;
                    }
                    PaqueteEjercicios paquete = json.readValue(cuerpo, PaqueteEjercicios.class);
                    responder(intercambio, json, 200, servicio.importar(paquete));
                }
            } catch (JsonProcessingException | IllegalArgumentException e) {
                responder(intercambio, json, 400,
                        Map.of("error", e.getMessage() == null ? "Solicitud inválida." : e.getMessage()));
            } catch (RuntimeException e) {
                responder(intercambio, json, 503,
                        Map.of("error", "No se pudo acceder a PostgreSQL. Revisa la conexión y schema.sql."));
            }
        }
    }

    private static void responder(HttpExchange intercambio, ObjectMapper json, int estado, Object contenido)
            throws IOException {
        byte[] cuerpo = json.writeValueAsBytes(contenido);
        intercambio.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        intercambio.sendResponseHeaders(estado, cuerpo.length);
        intercambio.getResponseBody().write(cuerpo);
    }
}
