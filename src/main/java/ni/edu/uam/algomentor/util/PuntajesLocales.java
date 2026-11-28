package ni.edu.uam.algomentor.util;

import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Mejores puntajes de los retos por perfil, guardados en las preferencias del equipo. */
public final class PuntajesLocales {

    private static final Logger LOG = Logger.getLogger(PuntajesLocales.class.getName());

    private PuntajesLocales() { }

    public static int mejorDesafio(long perfilId) {
        return leer("desafio." + perfilId);
    }

    /** @return true si el puntaje supera al anterior mejor. */
    public static boolean registrarDesafio(long perfilId, int puntos) {
        return registrar("desafio." + perfilId, puntos);
    }

    public static int mejorCuestionario(long perfilId) {
        return leer("cuestionario." + perfilId);
    }

    public static boolean registrarCuestionario(long perfilId, int aciertos) {
        return registrar("cuestionario." + perfilId, aciertos);
    }

    private static int leer(String clave) {
        try {
            return Preferences.userNodeForPackage(PuntajesLocales.class).getInt(clave, 0);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo leer el puntaje", e);
            return 0;
        }
    }

    private static boolean registrar(String clave, int valor) {
        try {
            Preferences preferencias = Preferences.userNodeForPackage(PuntajesLocales.class);
            if (valor > preferencias.getInt(clave, 0)) {
                preferencias.putInt(clave, valor);
                return true;
            }
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo guardar el puntaje", e);
        }
        return false;
    }
}
