
package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.model.ProgresoHabilidad;
import ni.edu.uam.algomentor.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

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

    /** Un intento del historial; {@code correcto} es null cuando no se calificó (por ejemplo, una simulación). */
    public record IntentoReciente(Long id, LocalDateTime fecha, String algoritmo, String modo,
                                  Boolean correcto, Integer duracionSegundos) { }

    /** Resultados de un perfil con un algoritmo. */
    public record RendimientoAlgoritmo(String algoritmo, long intentos, long correctos, long incorrectos) {

        public double porcentajeAciertos() {
            long calificados = correctos + incorrectos;
            return calificados == 0 ? 0.0 : (correctos * 100.0) / calificados;
        }
    }

    /** Cantidad de errores detectados de un mismo tipo (SIGNO, FRACCION, ARITMETICO u OPERACION_INVALIDA). */
    public record ErrorPorTipo(String tipo, long cantidad) { }

    /**
     * Aciertos seguidos más recientes. Recibe los intentos del más nuevo al más antiguo, ignora los
     * que no tienen calificación y se detiene en el primer error.
     */
    public static int rachaActual(List<IntentoReciente> recientes) {
        int racha = 0;
        for (IntentoReciente intento : recientes) {
            if (intento.correcto() == null) {
                continue;
            }
            if (!intento.correcto()) {
                break;
            }
            racha++;
        }
        return racha;
    }

    /**
     * Porcentaje de acierto acumulado después de cada intento calificado, del más antiguo al más
     * reciente. Recibe los intentos del más nuevo al más antiguo, como los devuelve la consulta.
     */
    public static List<Double> precisionAcumulada(List<IntentoReciente> recientes) {
        List<Double> serie = new ArrayList<>();
        int calificados = 0;
        int correctos = 0;
        for (int i = recientes.size() - 1; i >= 0; i--) {
            Boolean correcto = recientes.get(i).correcto();
            if (correcto == null) {
                continue;
            }
            calificados++;
            if (correcto) {
                correctos++;
            }
            serie.add((correctos * 100.0) / calificados);
        }
        return serie;
    }

    public List<IntentoReciente> listarIntentosRecientes(Long perfilId, int limite) {
        validarPerfilId(perfilId);
        if (limite < 1 || limite > 200) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 200.");
        }
        try (Session session = factory.openSession()) {
            List<Object[]> filas = session.createQuery("""
                    SELECT i.id, i.fechaInicio, a.nombre, i.modo, i.correcto, i.duracionSegundos
                    FROM Intento i JOIN i.ejercicio e JOIN e.algoritmo a
                    WHERE i.perfil.id = :perfilId
                    ORDER BY i.fechaInicio DESC, i.id DESC
                    """, Object[].class)
                    .setParameter("perfilId", perfilId)
                    .setMaxResults(limite)
                    .getResultList();
            return filas.stream().map(fila -> new IntentoReciente(
                    (Long) fila[0], (LocalDateTime) fila[1], (String) fila[2], (String) fila[3],
                    (Boolean) fila[4], (Integer) fila[5])).toList();
        }
    }

    public List<RendimientoAlgoritmo> listarRendimientoPorAlgoritmo(Long perfilId) {
        validarPerfilId(perfilId);
        try (Session session = factory.openSession()) {
            List<Object[]> filas = session.createQuery("""
                    SELECT a.nombre, COUNT(i),
                           SUM(CASE WHEN i.correcto = true THEN 1 ELSE 0 END),
                           SUM(CASE WHEN i.correcto = false THEN 1 ELSE 0 END)
                    FROM Intento i JOIN i.ejercicio e JOIN e.algoritmo a
                    WHERE i.perfil.id = :perfilId
                    GROUP BY a.nombre
                    ORDER BY a.nombre
                    """, Object[].class)
                    .setParameter("perfilId", perfilId)
                    .getResultList();
            return filas.stream().map(fila -> new RendimientoAlgoritmo(
                    (String) fila[0], ((Number) fila[1]).longValue(),
                    ((Number) fila[2]).longValue(), ((Number) fila[3]).longValue())).toList();
        }
    }

    public List<ErrorPorTipo> listarErroresPorTipo(Long perfilId) {
        validarPerfilId(perfilId);
        try (Session session = factory.openSession()) {
            List<Object[]> filas = session.createQuery("""
                    SELECT er.tipoError, COUNT(er)
                    FROM ErrorDetectado er
                    WHERE er.pasoIntento.intento.perfil.id = :perfilId
                    GROUP BY er.tipoError
                    ORDER BY COUNT(er) DESC, er.tipoError
                    """, Object[].class)
                    .setParameter("perfilId", perfilId)
                    .getResultList();
            return filas.stream().map(fila -> new ErrorPorTipo(
                    (String) fila[0], ((Number) fila[1]).longValue())).toList();
        }
    }

    /** Duración promedio de los intentos que registraron tiempo; vacío si ninguno lo hizo. */
    public OptionalDouble obtenerDuracionPromedioSegundos(Long perfilId) {
        validarPerfilId(perfilId);
        try (Session session = factory.openSession()) {
            List<Double> resultado = session.createQuery("""
                    SELECT AVG(i.duracionSegundos)
                    FROM Intento i
                    WHERE i.perfil.id = :perfilId AND i.duracionSegundos IS NOT NULL
                    """, Double.class)
                    .setParameter("perfilId", perfilId)
                    .getResultList();
            Double promedio = resultado.isEmpty() ? null : resultado.get(0);
            return promedio == null ? OptionalDouble.empty() : OptionalDouble.of(promedio);
        }
    }

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
