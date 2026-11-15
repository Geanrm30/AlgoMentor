
package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.TipoError;
import ni.edu.uam.algomentor.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.Objects;

/**
 * Relaciona los errores del motor con las habilidades
 * registradas en PostgreSQL.
 *
 * Los nombres corresponden a los datos iniciales
 * definidos en schema.sql.
 */
public class HabilidadErrorResolver {

    private final SessionFactory factory;

    public HabilidadErrorResolver() {
        this(HibernateUtil.getSessionFactory());
    }

    public HabilidadErrorResolver(SessionFactory factory) {
        this.factory = Objects.requireNonNull(
                factory,
                "SessionFactory no puede ser nula."
        );
    }

    /**
     * Devuelve el nombre de la habilidad asociada
     * al tipo de error.
     *
     * No necesita conexion a PostgreSQL.
     */
    public static String nombreHabilidad(TipoError tipo) {

        Objects.requireNonNull(
                tipo,
                "El tipo de error no puede ser nulo."
        );

        return switch (tipo) {
            case SIGNO -> "Signos";
            case FRACCION -> "Fracciones";
            case ARITMETICO -> "Aritmetica";
            case OPERACION_INVALIDA -> "Procedimiento";
        };
    }

    /**
     * Busca en PostgreSQL el identificador real
     * de la habilidad correspondiente.
     */
    public Long resolverId(TipoError tipo) {

        String nombre = nombreHabilidad(tipo);

        try (Session session = factory.openSession()) {

            String hql = """
                    SELECT h.id
                    FROM Habilidad h
                    WHERE h.nombre = :nombre
                    """;

            return session.createQuery(hql, Long.class)
                    .setParameter("nombre", nombre)
                    .uniqueResultOptional()
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "No existe la habilidad: "
                                            + nombre
                                            + ". Verifique los datos "
                                            + "iniciales de PostgreSQL."
                            )
                    );
        }
    }

    /**
     * Permite crear un error con la habilidad
     * resuelta automaticamente.
     *
     * Puede utilizarse desde la futura interfaz
     * o desde el servicio de evaluacion.
     */
    public IntentoService.ErrorRegistrado crearError(
            TipoError tipo,
            String descripcion
    ) {

        Long habilidadId = resolverId(tipo);

        return new IntentoService.ErrorRegistrado(
                habilidadId,
                tipo,
                descripcion
        );
    }
}
