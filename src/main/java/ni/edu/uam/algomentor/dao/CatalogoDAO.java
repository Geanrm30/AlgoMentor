package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Objects;

/** Consultas de solo lectura sobre el contenido: algoritmos, ejercicios y su uso en intentos. */
public class CatalogoDAO {

    public static final int MAX_EJERCICIOS_USADOS = 50;

    private final SessionFactory factory;

    public CatalogoDAO() {
        this(HibernateUtil.getSessionFactory());
    }

    public CatalogoDAO(SessionFactory factory) {
        this.factory = Objects.requireNonNull(factory, "SessionFactory no puede ser nula.");
    }

    public record AlgoritmoCatalogo(String nombre, String categoria, String complejidad,
                                    long ejercicios, long intentos, long correctos) {
        public double porcentajeAciertos() {
            return intentos == 0 ? 0 : correctos * 100.0 / intentos;
        }
    }

    public record ConteoClave(String clave, long cantidad) { }

    public record EjercicioUsado(Long id, String algoritmo, int dificultad, String origen, long intentos) { }

    public List<AlgoritmoCatalogo> listarAlgoritmos() {
        try (Session session = factory.openSession()) {
            List<Object[]> filas = session.createQuery("""
                    SELECT a.nombre, a.categoria, a.complejidad, COUNT(DISTINCT e.id), COUNT(i.id),
                           SUM(CASE WHEN i.correcto = true THEN 1 ELSE 0 END)
                    FROM Algoritmo a LEFT JOIN a.ejercicios e LEFT JOIN e.intentos i
                    GROUP BY a.id, a.nombre, a.categoria, a.complejidad
                    ORDER BY a.categoria, a.nombre
                    """, Object[].class).getResultList();
            return filas.stream().map(f -> new AlgoritmoCatalogo(
                    (String) f[0], (String) f[1], (String) f[2],
                    numero(f[3]), numero(f[4]), numero(f[5]))).toList();
        }
    }

    public List<ConteoClave> listarEjerciciosPorDificultad() {
        try (Session session = factory.openSession()) {
            List<Object[]> filas = session.createQuery("""
                    SELECT e.dificultad, COUNT(e)
                    FROM Ejercicio e
                    GROUP BY e.dificultad
                    ORDER BY e.dificultad
                    """, Object[].class).getResultList();
            return filas.stream().map(f -> new ConteoClave(String.valueOf(f[0]), numero(f[1]))).toList();
        }
    }

    public List<ConteoClave> listarEjerciciosPorOrigen() {
        try (Session session = factory.openSession()) {
            List<Object[]> filas = session.createQuery("""
                    SELECT e.origen, COUNT(e)
                    FROM Ejercicio e
                    GROUP BY e.origen
                    ORDER BY COUNT(e) DESC, e.origen
                    """, Object[].class).getResultList();
            return filas.stream().map(f -> new ConteoClave((String) f[0], numero(f[1]))).toList();
        }
    }

    public List<EjercicioUsado> listarEjerciciosMasUsados(int limite) {
        if (limite < 1 || limite > MAX_EJERCICIOS_USADOS) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y " + MAX_EJERCICIOS_USADOS + ".");
        }
        try (Session session = factory.openSession()) {
            List<Object[]> filas = session.createQuery("""
                    SELECT e.id, a.nombre, e.dificultad, e.origen, COUNT(i.id)
                    FROM Ejercicio e JOIN e.algoritmo a LEFT JOIN e.intentos i
                    GROUP BY e.id, a.nombre, e.dificultad, e.origen
                    ORDER BY COUNT(i.id) DESC, e.id
                    """, Object[].class)
                    .setMaxResults(limite)
                    .getResultList();
            return filas.stream().map(f -> new EjercicioUsado(
                    (Long) f[0], (String) f[1], ((Number) f[2]).intValue(), (String) f[3],
                    numero(f[4]))).toList();
        }
    }

    private static long numero(Object valor) {
        return valor == null ? 0 : ((Number) valor).longValue();
    }
}
