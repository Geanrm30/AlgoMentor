
package ni.edu.uam.algomentor.service;
import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.engine.TipoError;
import ni.edu.uam.algomentor.model.*;
import ni.edu.uam.algomentor.util.HibernateUtil;
import ni.edu.uam.algomentor.engine.FormatoDatos;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PersistenciaAprendizajeIT {

    private Long perfilId;
    private Long algoritmoId;
    private Long ejercicioId;
    private Long habilidadId;

    @Test
    void debeGuardarYRevertirTransacciones() {

        SessionFactory factory =
                HibernateUtil.getSessionFactory();

        try {
            prepararDatos(factory);

            IntentoService servicio =
                    new IntentoService(factory);

            // Primer intento: debe guardarse correctamente.
            var error = new IntentoService.ErrorRegistrado(
                    habilidadId,
                    TipoError.ARITMETICO,
                    "Calculo incorrecto"
            );

            var pasos = List.of(
                    new IntentoService.PasoRegistrado(
                            1, "3", true, habilidadId, List.of()
                    ),
                    new IntentoService.PasoRegistrado(
                            2, "8", false, habilidadId,
                            List.of(error)
                    )
            );

            Intento intento = servicio.registrarIntento(
                    perfilId,
                    ejercicioId,
                    "PRACTICA",
                    false,
                    60,
                    pasos
            );

            assertNotNull(intento.getId());

            // Comprobar que los datos quedaron persistidos.
            try (Session session = factory.openSession()) {

                Intento guardado = session.find(
                        Intento.class, intento.getId()
                );

                assertNotNull(guardado);
                assertEquals(2, guardado.getPasos().size());

                long errores = session.createQuery(
                                """
                                SELECT COUNT(e)
                                FROM ErrorDetectado e
                                WHERE e.pasoIntento.intento.id = :id
                                """,
                                Long.class
                        )
                        .setParameter("id", intento.getId())
                        .getSingleResult();

                assertEquals(1L, errores);
            }

            // Verificar las estadísticas acumuladas.
            EstadisticasDAO estadisticas =
                    new EstadisticasDAO(factory);

            var progreso =
                    estadisticas.listarProgresoPorHabilidad(perfilId);

            assertEquals(1, progreso.size());
            assertEquals(1, progreso.get(0).aciertos());
            assertEquals(1, progreso.get(0).errores());

            // Registrar otro intento con una habilidad inexistente.
            // Debe fallar después de comenzar la transacción.
            var pasoInvalido = new IntentoService.PasoRegistrado(
                    1,
                    "Respuesta incorrecta",
                    false,
                    Long.MAX_VALUE,
                    List.of()
            );

            assertThrows(IllegalArgumentException.class, () ->
                    servicio.registrarIntento(
                            perfilId,
                            ejercicioId,
                            "PRACTICA",
                            false,
                            30,
                            List.of(pasoInvalido)
                    )
            );

            // Comprobar que el intento fallido no quedó guardado.
            var resumen =
                    estadisticas.obtenerResumenIntentos(perfilId);

            assertEquals(1, resumen.total());

            // El rollback tampoco debe alterar el progreso.
            var progresoFinal =
                    estadisticas.listarProgresoPorHabilidad(perfilId);

            assertEquals(1, progresoFinal.get(0).aciertos());
            assertEquals(1, progresoFinal.get(0).errores());

        } finally {
            limpiarDatos(factory);
        }
    }

    private void prepararDatos(SessionFactory factory) {

        String unico = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);

        try (Session session = factory.openSession()) {

            Transaction tx = session.beginTransaction();

            try {
                Perfil perfil = new Perfil();
                perfil.setNombre("IT_Perfil_" + unico);

                Algoritmo algoritmo = new Algoritmo();
                algoritmo.setNombre("IT_Algoritmo_" + unico);
                algoritmo.setCategoria("ORDENAMIENTO");

                Habilidad habilidad = new Habilidad();
                habilidad.setNombre("IT_Habilidad_" + unico);

                Ejercicio ejercicio = new Ejercicio();
                ejercicio.setAlgoritmo(algoritmo);
                ejercicio.setDatos(
                        FormatoDatos.serializarArreglo(new int[]{3, 1, 2})
                );
                ejercicio.setDificultad(1);
                ejercicio.setOrigen("MANUAL");

                session.persist(perfil);
                session.persist(algoritmo);
                session.persist(habilidad);
                session.persist(ejercicio);

                perfilId = perfil.getId();
                algoritmoId = algoritmo.getId();
                habilidadId = habilidad.getId();
                ejercicioId = ejercicio.getId();

                tx.commit();

            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    private void limpiarDatos(SessionFactory factory) {

        try (Session session = factory.openSession()) {

            Transaction tx = session.beginTransaction();

            try {
                if (perfilId != null) {

                    List<Intento> intentos = session.createQuery(
                                    """
                                    FROM Intento i
                                    WHERE i.perfil.id = :id
                                    """,
                                    Intento.class
                            )
                            .setParameter("id", perfilId)
                            .getResultList();

                    intentos.forEach(session::remove);

                    List<ProgresoHabilidad> progresos =
                            session.createQuery(
                                            """
                                            FROM ProgresoHabilidad p
                                            WHERE p.perfil.id = :id
                                            """,
                                            ProgresoHabilidad.class
                                    )
                                    .setParameter("id", perfilId)
                                    .getResultList();

                    progresos.forEach(session::remove);
                }

                if (ejercicioId != null) {
                    eliminar(session, Ejercicio.class, ejercicioId);
                }

                if (perfilId != null) {
                    eliminar(session, Perfil.class, perfilId);
                }

                if (habilidadId != null) {
                    eliminar(session, Habilidad.class, habilidadId);
                }

                if (algoritmoId != null) {
                    eliminar(session, Algoritmo.class, algoritmoId);
                }

                tx.commit();

            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    private <T> void eliminar(
            Session session,
            Class<T> tipo,
            Long id
    ) {
        T entidad = session.find(tipo, id);

        if (entidad != null) {
            session.remove(entidad);
        }
    }
}
