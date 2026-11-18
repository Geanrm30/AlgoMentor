package ni.edu.uam.algomentor.engine;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class CatalogoSqlTest {
    @Test void esquemaInicialContieneElCatalogoCompleto() throws IOException {
        String sql = leer("schema.sql");
        for (var algoritmo : CatalogoAlgoritmos.values()) {
            assertTrue(sql.contains("('" + algoritmo.nombre() + "'"), algoritmo.nombre());
        }
    }

    @Test void actualizacionSoloInsertaNuevosNombresSinBorrarDatos() throws IOException {
        String sql = leer("actualizar-nuevos-algoritmos.sql");
        assertTrue(sql.contains("ON CONFLICT (nombre) DO NOTHING"));
        assertFalse(sql.matches("(?is).*\\b(DROP|TRUNCATE|DELETE|UPDATE)\\s+.*"));
        for (var algoritmo : new CatalogoAlgoritmos[]{CatalogoAlgoritmos.QUICK, CatalogoAlgoritmos.MERGE,
                CatalogoAlgoritmos.COUNTING, CatalogoAlgoritmos.RADIX, CatalogoAlgoritmos.BUCKET}) {
            assertTrue(sql.contains("('" + algoritmo.nombre() + "'"));
        }
    }

    private String leer(String nombre) throws IOException {
        try (var recurso = getClass().getResourceAsStream("/ni/edu/uam/algomentor/db/" + nombre)) {
            assertNotNull(recurso);
            return new String(recurso.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
