package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Objects;

/** Consulta de solo lectura: cada ejercicio, incluso si aún no tiene práctica evaluada. */
public class DificultadRealDAO {

    private final SessionFactory factory;

    public DificultadRealDAO() {
        this(HibernateUtil.getSessionFactory());
    }

    public DificultadRealDAO(SessionFactory factory) {
        this.factory = Objects.requireNonNull(factory, "SessionFactory no puede ser nula.");
    }

    public record EstadisticaEjercicio(Long id, String algoritmo, int dificultad,
                                       long intentos, long aciertos, long errores) {
        public EstadisticaEjercicio {
            if (dificultad < 1 || dificultad > 5) {
                throw new IllegalArgumentException("La dificultad debe estar entre 1 y 5.");
            }
            if (intentos < 0 || aciertos < 0 || errores < 0
                    || aciertos > intentos || errores != intentos - aciertos) {
                throw new IllegalArgumentException("Los contadores de intentos evaluados no son coherentes.");
            }
        }

        /** Sin intentos devuelve cero; el informe muestra «—» para distinguirlo del 0 % observado. */
        public double porcentajeAciertos() {
            return intentos == 0 ? 0 : aciertos * 100.0 / intentos;
        }
    }

    public List<EstadisticaEjercicio> listarEjercicios() {
        try (Session session = factory.openSession()) {
            // El filtro pertenece al JOIN: un WHERE eliminaría ejercicios sin intentos elegibles.
            List<Object[]> filas = session.createQuery("""
                    SELECT e.id, a.nombre, e.dificultad, COUNT(i.id),
                           SUM(CASE WHEN i.correcto = true THEN 1 ELSE 0 END),
                           SUM(CASE WHEN i.correcto = false THEN 1 ELSE 0 END)
                    FROM Ejercicio e JOIN e.algoritmo a
                    LEFT JOIN e.intentos i ON i.modo = :modo AND i.correcto IS NOT NULL
                    GROUP BY e.id, a.nombre, e.dificultad
                    ORDER BY a.nombre, e.id
                    """, Object[].class)
                    .setParameter("modo", "PRACTICA")
                    .getResultList();
            return filas.stream().map(f -> new EstadisticaEjercicio(
                    (Long) f[0], (String) f[1], ((Number) f[2]).intValue(),
                    numero(f[3]), numero(f[4]), numero(f[5]))).toList();
        }
    }

    private static long numero(Object valor) {
        return valor == null ? 0 : ((Number) valor).longValue();
    }
}
