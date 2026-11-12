
package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.model.Ejercicio;
import ni.edu.uam.algomentor.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class EjercicioDAO extends BaseDAO<Ejercicio> {

    public EjercicioDAO() {
        super(Ejercicio.class);
    }

    public List<Ejercicio> listarPorAlgoritmo(Long algoritmoId) {

        if (algoritmoId == null || algoritmoId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del algoritmo debe ser válido"
            );
        }

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            String hql = """
                SELECT e
                FROM Ejercicio e
                JOIN FETCH e.algoritmo a
                WHERE a.id = :algoritmoId
                ORDER BY e.id
                """;

            return session.createQuery(hql, Ejercicio.class)
                    .setParameter("algoritmoId", algoritmoId)
                    .getResultList();
        }
    }
}
