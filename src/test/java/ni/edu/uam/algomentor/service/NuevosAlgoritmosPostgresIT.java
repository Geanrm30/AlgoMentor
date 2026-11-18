package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.api.*;
import ni.edu.uam.algomentor.dao.EjercicioDAO;
import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.model.*;
import ni.edu.uam.algomentor.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Solo PostgreSQL real. Requiere schema.sql o la migración; elimina exclusivamente sus datos temporales. */
class NuevosAlgoritmosPostgresIT {
    private static final List<String> NOMBRES = List.of("Quick Sort", "Merge Sort", "Counting Sort", "Radix Sort", "Bucket Sort");

    @Test void migracionEsIdempotenteDentroDeUnaTransaccionRevertida() throws Exception {
        SessionFactory factory = HibernateUtil.getSessionFactory();
        String sql;
        try (var archivo = getClass().getResourceAsStream("/ni/edu/uam/algomentor/db/actualizar-nuevos-algoritmos.sql")) {
            assertNotNull(archivo);
            sql = new String(archivo.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("(?m)^(BEGIN|COMMIT);\\s*$", "");
        }
        try (Session session = factory.openSession()) {
            var tx = session.beginTransaction();
            try {
                String script = sql;
                session.doWork(conexion -> {
                    try (var sentencia = conexion.createStatement()) {
                        sentencia.execute(script);
                        sentencia.execute(script);
                    }
                });
                assertEquals(5L, session.createQuery("SELECT COUNT(a) FROM Algoritmo a WHERE a.nombre IN :nombres", Long.class)
                        .setParameter("nombres", NOMBRES).getSingleResult());
            } finally { tx.rollback(); }
        }
    }

    @Test void importaRecuperaEvaluaYRegistraIntentosDeLosCincoAlgoritmos() {
        SessionFactory factory = HibernateUtil.getSessionFactory();
        UUID identificador = UUID.randomUUID();
        int[] valores = {(int) (identificador.getMostSignificantBits() >>> 32),
                (int) identificador.getMostSignificantBits(), (int) (identificador.getLeastSignificantBits() >>> 32),
                (int) identificador.getLeastSignificantBits(), Integer.MIN_VALUE, 0, Integer.MAX_VALUE};
        String datos = Arrays.stream(valores).mapToObj(Integer::toString).collect(java.util.stream.Collectors.joining(","));
        Long perfilId = null;
        try {
            Perfil perfil = new PerfilService().crearPerfil("IT_Nuevos_" + identificador);
            perfilId = perfil.getId();
            IntercambioEjerciciosService intercambio = new IntercambioEjerciciosService();
            PaqueteEjercicios paquete = new PaqueteEjercicios(1, NOMBRES.stream()
                    .map(n -> new EjercicioIntercambio(n, datos, 3, "MANUAL")).toList());
            assertEquals(5, intercambio.importar(paquete).importados());
            assertEquals(5, intercambio.importar(paquete).existentes());
            List<Ejercicio> ejercicios = new EjercicioDAO().listarConAlgoritmo().stream()
                    .filter(e -> datos.equals(e.getDatos()) && NOMBRES.contains(e.getAlgoritmo().getNombre())).toList();
            assertEquals(5, ejercicios.size());
            assertEquals(5, intercambio.exportar().ejercicios().stream()
                    .filter(e -> datos.equals(e.datos()) && NOMBRES.contains(e.algoritmo())).count());
            for (Ejercicio ejercicio : ejercicios) {
                String salida = CatalogoAlgoritmos.porNombre(ejercicio.getAlgoritmo().getNombre())
                        .crearOrdenamiento().ejecutar(valores).estadoFinal();
                String respuesta = salida.substring(1, salida.length() - 1);
                assertTrue(EvaluadorPractica.evaluar(ejercicio.getAlgoritmo().getNombre(), datos, respuesta).correcto());
                new RegistroPracticaService().registrar(perfilId, ejercicio.getId(), respuesta, true, 1, List.of());
                new RegistroPracticaService().registrar(perfilId, ejercicio.getId(), "0", false, 1, List.of());
            }
            EstadisticasDAO estadisticas = new EstadisticasDAO(factory);
            assertEquals(10, estadisticas.obtenerResumenIntentos(perfilId).total());
            assertEquals(5, estadisticas.obtenerResumenIntentos(perfilId).correctos());
            var progreso = estadisticas.listarProgresoPorHabilidad(perfilId);
            assertEquals(1, progreso.size());
            assertEquals("Procedimiento", progreso.getFirst().nombre());
            assertEquals(5, progreso.getFirst().aciertos());
            assertEquals(5, progreso.getFirst().errores());
        } finally {
            limpiar(factory, perfilId, datos);
        }
    }

    private void limpiar(SessionFactory factory, Long perfilId, String datos) {
        try (Session session = factory.openSession()) {
            var tx = session.beginTransaction();
            try {
                if (perfilId != null) {
                    session.createQuery("FROM Intento i WHERE i.perfil.id = :id", Intento.class)
                            .setParameter("id", perfilId).getResultList().forEach(session::remove);
                    session.createQuery("FROM ProgresoHabilidad p WHERE p.perfil.id = :id", ProgresoHabilidad.class)
                            .setParameter("id", perfilId).getResultList().forEach(session::remove);
                    session.flush();
                }
                session.createQuery("FROM Ejercicio e WHERE e.datos = :datos AND e.algoritmo.nombre IN :nombres", Ejercicio.class)
                        .setParameter("datos", datos).setParameter("nombres", NOMBRES)
                        .getResultList().forEach(session::remove);
                session.flush();
                if (perfilId != null) session.remove(session.find(Perfil.class, perfilId));
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }
}
