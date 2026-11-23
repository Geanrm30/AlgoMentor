package ni.edu.uam.algomentor.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioRecordadoTest {

    private Preferences nodo;
    private UsuarioRecordado recordado;

    @BeforeEach
    void preparar() {
        nodo = Preferences.userRoot().node("algomentor-prueba-usuario-recordado");
        recordado = new UsuarioRecordado(nodo);
        recordado.olvidar();
    }

    @AfterEach
    void limpiar() throws BackingStoreException {
        nodo.removeNode();
    }

    @Test
    void guardaElUsuarioNormalizadoYLoDevuelve() {
        recordado.guardar("  Gean ");
        assertEquals("gean", recordado.leer().orElseThrow());
    }

    @Test
    void sinUsuarioGuardadoNoDevuelveNada() {
        assertTrue(recordado.leer().isEmpty());
    }

    @Test
    void olvidarBorraElUsuario() {
        recordado.guardar("ana_01");
        recordado.olvidar();
        assertTrue(recordado.leer().isEmpty());
    }

    @Test
    void unUsuarioInvalidoNoSeGuarda() {
        recordado.guardar("gean");
        recordado.guardar("x");
        assertTrue(recordado.leer().isEmpty());
    }
}
