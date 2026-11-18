
package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.model.ProgresoHabilidad;
import ni.edu.uam.algomentor.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Objects;

public class EstadisticasDAO {

    private final SessionFactory factory;

    public EstadisticasDAO() {
        this(HibernateUtil.getSessionFactory());
    }

    public EstadisticasDAO(SessionFactory factory) {
        this.factory = Objects.requireNonNull(
                factory,
                "SessionFactory no puede ser nula."
        );
    }

    // Estadisticas de una habilidad del estudiante.
    public record EstadisticaHabilidad(
            Long habilidadId,
            String nombre,
            int aciertos,
            int errores
    ) {

        public EstadisticaHabilidad {
            if (habilidadId == null || habilidadId <= 0) {
                throw new IllegalArgumentException(
                        "El ID de la habilidad debe ser positivo."
                );
            }

            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException(
                        "El nombre de la habilidad es obligatorio."
                );
            }

            if (aciertos < 0 || errores < 0) {
                throw new IllegalArgumentException(
                        "Los contadores no pueden ser negativos."
                );
            }
        }

        public long totalEvaluaciones() {
            return (long) aciertos + errores;
        }

        public double porcentajeAciertos() {
            long total = totalEvaluaciones();

            if (total == 0) {
                return 0.0;
            }

            return (aciertos * 100.0) / total;
        }
    }

    // Resumen general de intentos.
    public record ResumenIntentos(
            long total,
            long correctos,
            long incorrectos
    ) {

        public ResumenIntentos {
            if (total < 0 || correctos < 0
                    || incorrectos < 0
                    || correctos > total
                    || incorrectos > total - correctos) {
                throw new IllegalArgumentException(
                        "Los contadores de intentos no son validos."
                );
            }
        }

        // Intentos sin resultado definitivo.
        public long sinCalificar() {
            return total - correctos - incorrectos;
        }

        public double porcentajeCorrectos() {
            if (total == 0) {
                return 0.0;
            }

            return (correctos * 100.0) / total;
        }
    }

    public record ResumenPerfil(Long id, String nombre, ResumenIntentos intentos) { }

    public List<ResumenPerfil> listarResumenesPerfiles() {
        try (Session session = factory.openSession()) {
            List<Object[]> filas = session.createQuery("""
                    SELECT p.id, p.nombre, COUNT(i),
                           SUM(CASE WHEN i.correcto = true THEN 1 ELSE 0 END),
                           SUM(CASE WHEN i.correcto = false THEN 1 ELSE 0 END)
                    FROM Perfil p LEFT JOIN p.intentos i
                    GROUP BY p.id, p.nombre
                    ORDER BY p.nombre
                    """, Object[].class).getResultList();
            return filas.stream().map(fila -> new ResumenPerfil(
                    (Long) fila[0], (String) fila[1],
                    new ResumenIntentos(((Number) fila[2]).longValue(),
                            ((Number) fila[3]).longValue(), ((Number) fila[4]).longValue())))
                    .toList();
        }
    }

    // Validar el identificador antes de consultar Hibernate.
    public static void validarPerfilId(Long perfilId) {
        if (perfilId == null || perfilId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del perfil debe ser positivo."
            );
        }
    }

    // Consulta HQL de progreso agrupado por habilidad.
    // Lee los acumulados de ProgresoHabilidad.
    public List<EstadisticaHabilidad> listarProgresoPorHabilidad(
            Long perfilId
    ) {

        validarPerfilId(perfilId);

        try (Session session = factory.openSession()) {

            String hql = """
                    SELECT p
                    FROM ProgresoHabilidad p
                    JOIN FETCH p.habilidad h
                    WHERE p.perfil.id = :perfilId
                    ORDER BY h.nombre
                    """;

            List<ProgresoHabilidad> progresos =
                    session.createQuery(
                                    hql,
                                    ProgresoHabilidad.class
                            )
                            .setParameter("perfilId", perfilId)
                            .getResultList();

            return progresos.stream()
                    .map(p -> new EstadisticaHabilidad(
                            p.getHabilidad().getId(),
                            p.getHabilidad().getNombre(),
                            p.getAciertos(),
                            p.getErrores()
                    ))
                    .toList();
        }
    }

    // Consulta HQL de resumen de intentos.
    // Se cuentan resultados correctos, incorrectos
    // y aquellos sin calificacion definitiva.
    public ResumenIntentos obtenerResumenIntentos(
            Long perfilId
    ) {

        validarPerfilId(perfilId);

        try (Session session = factory.openSession()) {

            String hqlTotal = """
                    SELECT COUNT(i)
                    FROM Intento i
                    WHERE i.perfil.id = :perfilId
                    """;

            String hqlCorrectos = """
                    SELECT COUNT(i)
                    FROM Intento i
                    WHERE i.perfil.id = :perfilId
                    AND i.correcto = true
                    """;

            String hqlIncorrectos = """
                    SELECT COUNT(i)
                    FROM Intento i
                    WHERE i.perfil.id = :perfilId
                    AND i.correcto = false
                    """;

            Long total = session.createQuery(
                            hqlTotal, Long.class
                    )
                    .setParameter("perfilId", perfilId)
                    .getSingleResult();

            Long correctos = session.createQuery(
                            hqlCorrectos, Long.class
                    )
                    .setParameter("perfilId", perfilId)
                    .getSingleResult();

            Long incorrectos = session.createQuery(
                            hqlIncorrectos, Long.class
                    )
                    .setParameter("perfilId", perfilId)
                    .getSingleResult();

            return new ResumenIntentos(
                    total,
                    correctos,
                    incorrectos
            );
        }
    }
}
