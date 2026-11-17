package ni.edu.uam.algomentor.dao;

import ni.edu.uam.algomentor.api.RankingPerfil;
import ni.edu.uam.algomentor.util.HibernateUtil;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RankingDAO {

    public List<RankingPerfil> listar() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> filas = session.createQuery("""
                    SELECT p.id, p.nombre,
                           SUM(CASE WHEN i.correcto = true THEN 1 ELSE 0 END),
                           SUM(CASE WHEN i.correcto IS NOT NULL THEN 1 ELSE 0 END)
                    FROM Perfil p LEFT JOIN p.intentos i
                    GROUP BY p.id, p.nombre
                    """, Object[].class).getResultList();

            List<RankingPerfil> ordenados = filas.stream()
                    .map(fila -> {
                        long aciertos = ((Number) fila[2]).longValue();
                        long evaluados = ((Number) fila[3]).longValue();
                        double porcentaje = evaluados == 0 ? 0 : aciertos * 100.0 / evaluados;
                        return new RankingPerfil(0, (Long) fila[0], (String) fila[1],
                                aciertos, evaluados, porcentaje);
                    })
                    .sorted(Comparator.comparingLong(RankingPerfil::aciertos).reversed()
                            .thenComparing(Comparator.comparingDouble(RankingPerfil::porcentaje).reversed())
                            .thenComparing(RankingPerfil::nombre, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(RankingPerfil::perfilId))
                    .toList();

            List<RankingPerfil> ranking = new ArrayList<>(ordenados.size());
            for (int indice = 0; indice < ordenados.size(); indice++) {
                RankingPerfil perfil = ordenados.get(indice);
                ranking.add(new RankingPerfil(indice + 1, perfil.perfilId(), perfil.nombre(),
                        perfil.aciertos(), perfil.evaluados(), perfil.porcentaje()));
            }
            return ranking;
        }
    }
}
