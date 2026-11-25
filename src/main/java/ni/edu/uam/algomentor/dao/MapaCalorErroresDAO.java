package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Consulta HQL de solo lectura que cruza todos los perfiles registrados con el conteo de errores
 * detectados por categoría ({@code SIGNO}, {@code FRACCION}, {@code ARITMETICO} y
 * {@code OPERACION_INVALIDA}), conservando los perfiles sin errores.
 */
public class MapaCalorErroresDAO {

    private final SessionFactory factory;

    public MapaCalorErroresDAO() {
        this(HibernateUtil.getSessionFactory());
    }

    public MapaCalorErroresDAO(SessionFactory factory) {
        this.factory = Objects.requireNonNull(factory, "SessionFactory no puede ser nula.");
    }

    public record ErroresPerfil(
            Long perfilId,
            String perfil,
            long signo,
            long fraccion,
            long aritmetico,
            long operacionInvalida
    ) {
        public ErroresPerfil {
            Objects.requireNonNull(perfil, "El nombre del perfil es obligatorio.");
            if (signo < 0 || fraccion < 0 || aritmetico < 0 || operacionInvalida < 0) {
                throw new IllegalArgumentException("Los contadores de errores no pueden ser negativos.");
            }
        }

        public long total() {
            return signo + fraccion + aritmetico + operacionInvalida;
        }

        public long maximoTipo() {
            return Math.max(Math.max(signo, fraccion), Math.max(aritmetico, operacionInvalida));
        }
    }

    public List<ErroresPerfil> listarErroresPorPerfil() {
        try (Session session = factory.openSession()) {
            List<Object[]> filas = session.createQuery("""
                    SELECT p.id, p.nombre,
                           SUM(CASE WHEN UPPER(er.tipoError) = 'SIGNO' THEN 1 ELSE 0 END),
                           SUM(CASE WHEN UPPER(er.tipoError) = 'FRACCION' THEN 1 ELSE 0 END),
                           SUM(CASE WHEN UPPER(er.tipoError) = 'ARITMETICO' THEN 1 ELSE 0 END),
                           SUM(CASE WHEN UPPER(er.tipoError) = 'OPERACION_INVALIDA' THEN 1 ELSE 0 END)
                    FROM Perfil p
                    LEFT JOIN p.intentos i
                    LEFT JOIN i.pasos pi
                    LEFT JOIN pi.errores er
                    GROUP BY p.id, p.nombre
                    """, Object[].class).getResultList();
            return filas.stream()
                    .map(f -> new ErroresPerfil(
                            (Long) f[0],
                            (String) f[1],
                            numero(f[2]),
                            numero(f[3]),
                            numero(f[4]),
                            numero(f[5])
                    ))
                    .sorted(Comparator.comparingLong(ErroresPerfil::total).reversed()
                            .thenComparing(e -> e.perfil().toLowerCase(Locale.ROOT)))
                    .toList();
        }
    }

    private static long numero(Object valor) {
        return valor == null ? 0 : ((Number) valor).longValue();
    }
}
