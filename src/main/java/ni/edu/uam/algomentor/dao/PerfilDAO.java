
package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.util.HibernateUtil;
import org.hibernate.Session;

import java.util.Locale;
import java.util.Optional;

public class PerfilDAO extends BaseDAO<Perfil> {

    public PerfilDAO() {
        super(Perfil.class);
    }

    public Optional<Perfil> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del perfil es obligatorio."
            );
        }

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            String hql = """
                FROM Perfil p
                WHERE lower(p.nombre) = :nombre
                """;

            return session.createQuery(hql, Perfil.class)
                    .setParameter(
                            "nombre",
                            nombre.strip().toLowerCase(Locale.ROOT)
                    )
                    .uniqueResultOptional();
        }
    }
}
