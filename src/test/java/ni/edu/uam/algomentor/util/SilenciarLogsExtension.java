package ni.edu.uam.algomentor.util;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Se detecta sola (ver junit-platform.properties) y deja la consola de las pruebas sin mensajes en rojo. */
public class SilenciarLogsExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext contexto) {
        ConfiguracionLogs.aplicar();
    }
}
