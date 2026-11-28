package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.model.Algoritmo;
import ni.edu.uam.algomentor.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public final class CatalogoPersistidoService {

    private static final List<Definicion> DEFINICIONES = List.of(
            new Definicion(CatalogoAlgoritmos.BUBBLE, "O(n^2)", "Compara pares adyacentes y los intercambia"),
            new Definicion(CatalogoAlgoritmos.INSERTION, "O(n^2)", "Inserta cada elemento en su posicion"),
            new Definicion(CatalogoAlgoritmos.QUICK, "O(n log n); peor O(n^2)", "Particiones en tres vias con pivote y manejo de repetidos"),
            new Definicion(CatalogoAlgoritmos.MERGE, "O(n log n)", "Divide subarreglos y los mezcla de forma estable"),
            new Definicion(CatalogoAlgoritmos.COUNTING, "O(n+k); disperso O(n log u)", "Cuenta frecuencias con desplazamiento; usa conteo disperso para rangos grandes"),
            new Definicion(CatalogoAlgoritmos.RADIX, "O(d(n+b))", "Diez pasadas decimales estables sobre claves long desplazadas"),
            new Definicion(CatalogoAlgoritmos.BUCKET, "Prom. O(n+k); peor O(n^2)", "Distribuye en cubetas, ordena por insercion y concatena"),
            new Definicion(CatalogoAlgoritmos.SELECTION, "O(n^2)", "Busca el minimo y lo coloca al inicio del tramo sin ordenar"),
            new Definicion(CatalogoAlgoritmos.SHELL, "O(n log n) a O(n^2)", "Insercion con saltos decrecientes"),
            new Definicion(CatalogoAlgoritmos.HEAP, "O(n log n)", "Monticulo maximo; extrae la raiz hacia el final"),
            new Definicion(CatalogoAlgoritmos.COCKTAIL, "O(n^2)", "Burbuja bidireccional"),
            new Definicion(CatalogoAlgoritmos.COMB, "O(n^2); promedio mejor", "Burbuja con brecha decreciente (factor 1.3)"),
            new Definicion(CatalogoAlgoritmos.GNOME, "O(n^2)", "Avanza si el par esta en orden; si no, intercambia y retrocede"),
            new Definicion(CatalogoAlgoritmos.GAUSS, "O(n^3)", "Eliminacion hacia adelante y sustitucion"),
            new Definicion(CatalogoAlgoritmos.JORDAN, "O(n^3)", "Reduce la matriz a la identidad")
    );

    private CatalogoPersistidoService() { }

    public static void completarCatalogo() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                for (Definicion definicion : DEFINICIONES) {
                    session.createNativeMutationQuery("""
                                    INSERT INTO algoritmo (nombre, categoria, complejidad, descripcion)
                                    VALUES (:nombre, :categoria, :complejidad, :descripcion)
                                    ON CONFLICT (nombre) DO NOTHING
                                    """)
                            .setParameter("nombre", definicion.algoritmo().nombre())
                            .setParameter("categoria", definicion.algoritmo().esOrdenamiento() ? "ORDENAMIENTO" : "MATEMATICO")
                            .setParameter("complejidad", definicion.complejidad())
                            .setParameter("descripcion", definicion.descripcion())
                            .executeUpdate();
                }
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    private record Definicion(CatalogoAlgoritmos algoritmo, String complejidad, String descripcion) { }
}
