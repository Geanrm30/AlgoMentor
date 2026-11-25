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
class MapaCalorErroresDAOTest {

    private SessionFactory crearFactory() {
        Configuration cfg = new Configuration();
        cfg.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        cfg.setProperty("hibernate.connection.url", "jdbc:h2:mem:mapacalor_" + UUID.randomUUID()
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
    void sinPerfilesDevuelveListaVacia() {
        try (SessionFactory factory = crearFactory()) {
            assertTrue(new MapaCalorErroresDAO(factory).listarErroresPorPerfil().isEmpty());
        }
    }

    @Test
    void agrupaErroresPorPerfilYConservaPerfilesSinFallos() {
        try (SessionFactory factory = crearFactory()) {
            Perfil ana;
            Perfil bruno;
            Perfil carlos;
            try (Session session = factory.openSession()) {
                var tx = session.beginTransaction();
                Algoritmo gauss = algoritmo(session, "Gauss");
                Ejercicio ej = ejercicio(session, gauss);
                Habilidad hab = habilidad(session, "Signos");

                ana = perfil(session, "Ana");
                bruno = perfil(session, "Bruno");
                carlos = perfil(session, "Carlos");

                Intento iAna = intento(session, ana, ej);
                PasoIntento pAna = paso(session, iAna, 1);
                error(session, pAna, hab, "SIGNO");
                error(session, pAna, hab, "SIGNO");
                error(session, pAna, hab, "FRACCION");

                Intento iBruno = intento(session, bruno, ej);
                PasoIntento pBruno = paso(session, iBruno, 1);
                error(session, pBruno, hab, "ARITMETICO");
                error(session, pBruno, hab, "OPERACION_INVALIDA");
                error(session, pBruno, hab, "OPERACION_INVALIDA");
                error(session, pBruno, hab, "OPERACION_INVALIDA");

                // Carlos tiene intento sin errores registrados.
                intento(session, carlos, ej);
                tx.commit();
            }

            var filas = new MapaCalorErroresDAO(factory).listarErroresPorPerfil();

            assertEquals(List.of(
                    new MapaCalorErroresDAO.ErroresPerfil(bruno.getId(), "Bruno", 0, 0, 1, 3),
                    new MapaCalorErroresDAO.ErroresPerfil(ana.getId(), "Ana", 2, 1, 0, 0),
                    new MapaCalorErroresDAO.ErroresPerfil(carlos.getId(), "Carlos", 0, 0, 0, 0)), filas);
        }
    }

    private Perfil perfil(Session session, String nombre) {
        Perfil p = new Perfil();
        p.setNombre(nombre);
        session.persist(p);
        return p;
    }

    private Algoritmo algoritmo(Session session, String nombre) {
        Algoritmo a = new Algoritmo();
        a.setNombre(nombre);
        a.setCategoria("MATRICIAL");
        session.persist(a);
        return a;
    }

    private Ejercicio ejercicio(Session session, Algoritmo algoritmo) {
        Ejercicio e = new Ejercicio();
        e.setAlgoritmo(algoritmo);
        e.setDatos("1,2,3");
        e.setDificultad(2);
        e.setOrigen("MANUAL");
        session.persist(e);
        return e;
    }

    private Habilidad habilidad(Session session, String nombre) {
        Habilidad h = new Habilidad();
        h.setNombre(nombre);
        session.persist(h);
        return h;
    }

    private Intento intento(Session session, Perfil perfil, Ejercicio ejercicio) {
        Intento i = new Intento();
        i.setPerfil(perfil);
        i.setEjercicio(ejercicio);
        i.setModo("PRACTICA");
        i.setCorrecto(false);
        session.persist(i);
        return i;
    }

    private PasoIntento paso(Session session, Intento intento, int numero) {
        PasoIntento p = new PasoIntento();
        p.setIntento(intento);
        p.setNumeroPaso(numero);
        p.setCorrecto(false);
        session.persist(p);
        return p;
    }

    private void error(Session session, PasoIntento paso, Habilidad habilidad, String tipo) {
        ErrorDetectado e = new ErrorDetectado();
        e.setPasoIntento(paso);
        e.setHabilidad(habilidad);
        e.setTipoError(tipo);
        session.persist(e);
    }
}
