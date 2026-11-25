package ni.edu.uam.algomentor.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Deja la consola limpia: los mensajes de registro (avisos de JavaFX, información de Hibernate y errores
 * controlados de la aplicación) van a un archivo en {@code ~/.algomentor/}, en lugar de la salida de error,
 * que muchos entornos pintan en rojo. Solo se guardan avisos y errores; no se registran contraseñas.
 */
public final class ConfiguracionLogs {

    private static final int MAX_BYTES = 1_000_000;
    private static final int ARCHIVOS = 3;

    /** java.util.logging solo guarda referencias débiles a los registradores; se retienen para conservar su nivel. */
    private static Logger hibernate;
    private static Logger raiz;
    private static boolean aplicado;

    private ConfiguracionLogs() { }

    public static synchronized void aplicar() {
        if (aplicado) {
            return;
        }
        aplicado = true;
        raiz = Logger.getLogger("");
        for (Handler handler : raiz.getHandlers()) {
            raiz.removeHandler(handler);
        }
        raiz.setLevel(Level.WARNING);
        hibernate = Logger.getLogger("org.hibernate");
        hibernate.setLevel(Level.SEVERE);
        try {
            Path carpeta = Path.of(System.getProperty("user.home"), ".algomentor");
            Files.createDirectories(carpeta);
            FileHandler archivo = new FileHandler(
                    carpeta.resolve("algomentor-%g.log").toString(), MAX_BYTES, ARCHIVOS, true);
            archivo.setFormatter(new SimpleFormatter());
            archivo.setLevel(Level.WARNING);
            raiz.addHandler(archivo);
        } catch (IOException | SecurityException e) {
            // Sin archivo de registro la aplicación sigue funcionando, solo que sin guardar los avisos.
        }
    }
}
