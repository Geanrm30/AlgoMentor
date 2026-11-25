package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ejecuta la HQL real con Hibernate y H2 en memoria; nunca usa la configuración de PostgreSQL. */
class DificultadRealDAOTest {

    private SessionFactory crearFactory() {
        Configuration cfg = new Configuration();
        cfg.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        cfg.setProperty("hibernate.connection.url", "jdbc:h2:mem:dificultad_" + UUID.randomUUID()
                + ";MODE=PostgreSQL");
        cfg.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        cfg.setProperty("hibernate.show_sql", "false");
        for (Class<?> entidad : List.of(Algoritmo.class, Ejercicio.class, Intento.class, Perfil.class,
                PasoIntento.class, ErrorDetectado.class, Habilidad.class, ProgresoHabilidad.class)) {
            cfg.addAnnotatedClass(entidad);
        }
        return cfg.buildSessionFactory();
    }

    @Test
    void unCatalogoVacioNoInventaEjercicios() {
        try (SessionFactory factory = crearFactory()) {
            assertTrue(new DificultadRealDAO(factory).listarEjercicios().isEmpty());
        }
    }

    @Test
    void cuentaSoloPracticaEvaluadaYConservaEjerciciosSinIntentosElegibles() {
        try (SessionFactory factory = crearFactory()) {
            Ejercicio mixto;
            Ejercicio sinIntentos;
            Ejercicio sinEvaluados;
            Ejercicio otro;
            try (Session session = factory.openSession()) {
                var tx = session.beginTransaction();
                Perfil perfil = new Perfil();
                perfil.setNombre("Prueba");
                session.persist(perfil);
                Algoritmo gauss = algoritmo(session, "Gauss");
                Algoritmo bubble = algoritmo(session, "Bubble Sort");
                mixto = ejercicio(session, gauss, 3);
                sinIntentos = ejercicio(session, gauss, 1);
                sinEvaluados = ejercicio(session, gauss, 5);
                otro = ejercicio(session, bubble, 2);
                intento(session, perfil, mixto, "PRACTICA", true);
                intento(session, perfil, mixto, "PRACTICA", true);
                intento(session, perfil, mixto, "PRACTICA", false);
                intento(session, perfil, mixto, "PRACTICA", null);
                intento(session, perfil, mixto, "SIMULACION", true);
                intento(session, perfil, mixto, "SIMULACION", false);
                intento(session, perfil, mixto, "SIMULACION", null);
                intento(session, perfil, sinEvaluados, "PRACTICA", null);
                intento(session, perfil, sinEvaluados, "SIMULACION", true);
                intento(session, perfil, otro, "PRACTICA", false);
                tx.commit();
            }

            var filas = new DificultadRealDAO(factory).listarEjercicios();

            assertEquals(List.of(
                    new DificultadRealDAO.EstadisticaEjercicio(otro.getId(), "Bubble Sort", 2, 1, 0, 1),
                    new DificultadRealDAO.EstadisticaEjercicio(mixto.getId(), "Gauss", 3, 3, 2, 1),
                    new DificultadRealDAO.EstadisticaEjercicio(sinIntentos.getId(), "Gauss", 1, 0, 0, 0),
                    new DificultadRealDAO.EstadisticaEjercicio(sinEvaluados.getId(), "Gauss", 5, 0, 0, 0)), filas);
            assertEquals(filas, new DificultadRealDAO(factory).listarEjercicios());
            try (Session session = factory.openSession()) {
                assertEquals(3, session.find(Ejercicio.class, mixto.getId()).getDificultad());
                assertEquals(5, session.find(Ejercicio.class, sinEvaluados.getId()).getDificultad());
            }
        }
    }

    private Algoritmo algoritmo(Session session, String nombre) {
        Algoritmo algoritmo = new Algoritmo();
        algoritmo.setNombre(nombre);
        algoritmo.setCategoria("ORDENAMIENTO");
        session.persist(algoritmo);
        return algoritmo;
    }

    private Ejercicio ejercicio(Session session, Algoritmo algoritmo, int nivel) {
        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setAlgoritmo(algoritmo);
        ejercicio.setDatos("3,2,1");
        ejercicio.setDificultad(nivel);
        ejercicio.setOrigen("MANUAL");
        session.persist(ejercicio);
        return ejercicio;
    }

    private void intento(Session session, Perfil perfil, Ejercicio ejercicio, String modo, Boolean correcto) {
        Intento intento = new Intento();
        intento.setPerfil(perfil);
        intento.setEjercicio(ejercicio);
        intento.setModo(modo);
        intento.setCorrecto(correcto);
        session.persist(intento);
    }
}
