
package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.model.Intento;
import ni.edu.uam.algomentor.util.HibernateUtil;

import org.hibernate.Session;

import java.util.List;

public class IntentoDAO extends BaseDAO<Intento> {

    public IntentoDAO() {
        super(Intento.class);
    }

    public List<Intento> listarPorPerfil(Long perfilId) {

        if (perfilId == null || perfilId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del perfil debe ser válido"
            );
        }

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            String hql = """
                SELECT i
                FROM Intento i
                JOIN FETCH i.perfil p
                JOIN FETCH i.ejercicio e
                JOIN FETCH e.algoritmo
                WHERE p.id = :perfilId
                ORDER BY i.fechaInicio DESC
                """;

            return session.createQuery(hql, Intento.class)
                    .setParameter("perfilId", perfilId)
                    .getResultList();
        }
    }
}
