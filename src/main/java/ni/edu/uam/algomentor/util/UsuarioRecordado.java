package ni.edu.uam.algomentor.util;

import ni.edu.uam.algomentor.service.AutenticacionService;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/** Recuerda solo el nombre de usuario del último inicio de sesión; la contraseña nunca se guarda. */
public final class UsuarioRecordado {

    private static final Logger LOG = Logger.getLogger(UsuarioRecordado.class.getName());
    private static final String CLAVE = "login.usuario";

    private final Preferences preferencias;

    public UsuarioRecordado() {
        this(Preferences.userNodeForPackage(UsuarioRecordado.class));
    }

    public UsuarioRecordado(Preferences preferencias) {
        this.preferencias = Objects.requireNonNull(preferencias);
    }

    public Optional<String> leer() {
        try {
            String valor = preferencias.get(CLAVE, null);
            if (valor == null) {
                return Optional.empty();
            }
            return Optional.of(AutenticacionService.validarUsuario(valor));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** Un valor con formato de usuario inválido se ignora en lugar de guardarse. */
    public void guardar(String usuario) {
        try {
            preferencias.put(CLAVE, AutenticacionService.validarUsuario(usuario));
        } catch (IllegalArgumentException e) {
            olvidar();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo guardar el usuario recordado", e);
        }
    }

    public void olvidar() {
        try {
            preferencias.remove(CLAVE);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo olvidar el usuario recordado", e);
        }
    }
}
