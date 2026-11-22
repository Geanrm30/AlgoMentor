package ni.edu.uam.algomentor.service;

import ni.edu.uam.algomentor.dao.CuentaDAO;
import ni.edu.uam.algomentor.model.Cuenta;
import ni.edu.uam.algomentor.model.Perfil;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class AutenticacionService {

    private static final int ITERACIONES = 210_000;
    private static final int TAMANO_SAL = 16;
    private static final int TAMANO_CLAVE_BITS = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final Pattern FORMATO_USUARIO =
            Pattern.compile("[\\p{L}\\p{N}._-]{3,40}");

    private static final int MAXIMO_FALLOS = 5;
    private static final Duration DURACION_BLOQUEO = Duration.ofSeconds(30);
    /** Compartido: el controlador crea un servicio nuevo en cada intento. */
    private static final LimitadorIntentos LIMITADOR_GLOBAL =
            new LimitadorIntentos(MAXIMO_FALLOS, DURACION_BLOQUEO);

    private final CuentaDAO cuentaDAO;
    private final LimitadorIntentos limitador;

    public AutenticacionService() {
        this(new CuentaDAO());
    }

    public AutenticacionService(CuentaDAO cuentaDAO) {
        this(cuentaDAO, LIMITADOR_GLOBAL);
    }

    AutenticacionService(CuentaDAO cuentaDAO, LimitadorIntentos limitador) {
        this.cuentaDAO = Objects.requireNonNull(cuentaDAO);
        this.limitador = Objects.requireNonNull(limitador);
    }

    public Optional<Perfil> iniciarSesion(String usuario, char[] contrasena) {
        String usuarioNormalizado = validarUsuario(usuario);
        validarContrasena(contrasena);
        long espera = limitador.segundosRestantes(usuarioNormalizado);
        if (espera > 0) {
            throw new IllegalArgumentException(
                    "Demasiados intentos fallidos. Espera " + espera + " segundos e inténtalo de nuevo."
            );
        }
        Optional<Cuenta> cuenta = cuentaDAO.buscarPorUsuario(usuarioNormalizado);

        if (cuenta.isEmpty()
                || !verificarContrasena(contrasena, cuenta.get().getContrasenaHash())) {
            limitador.registrarFallo(usuarioNormalizado);
            return Optional.empty();
        }
        limitador.registrarExito(usuarioNormalizado);
        return Optional.of(cuenta.get().getPerfil());
    }

    public Perfil registrar(String usuario, char[] contrasena) {
        String usuarioNormalizado = validarUsuario(usuario);
        validarContrasena(contrasena);
        return cuentaDAO.registrar(usuarioNormalizado, hashContrasena(contrasena));
    }

    public static String validarUsuario(String usuario) {
        if (usuario == null || !FORMATO_USUARIO.matcher(usuario.strip()).matches()) {
            throw new IllegalArgumentException(
                    "El usuario debe tener entre 3 y 40 caracteres: letras, números, punto, guion o guion bajo."
            );
        }
        return usuario.strip().toLowerCase(Locale.ROOT);
    }

    public static void validarContrasena(char[] contrasena) {
        if (contrasena == null || contrasena.length < 8 || contrasena.length > 128) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener entre 8 y 128 caracteres."
            );
        }
    }

    static String hashContrasena(char[] contrasena) {
        byte[] sal = new byte[TAMANO_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] derivada = derivarClave(contrasena, sal, ITERACIONES);
        return ITERACIONES + "$" + Base64.getEncoder().encodeToString(sal)
                + "$" + Base64.getEncoder().encodeToString(derivada);
    }

    static boolean verificarContrasena(char[] contrasena, String hashAlmacenado) {
        if (hashAlmacenado == null) {
            return false;
        }
        try {
            String[] partes = hashAlmacenado.split("\\$", -1);
            if (partes.length != 3) {
                return false;
            }
            int iteraciones = Integer.parseInt(partes[0]);
            if (iteraciones < 1 || iteraciones > 1_000_000) {
                return false;
            }
            byte[] sal = Base64.getDecoder().decode(partes[1]);
            byte[] esperada = Base64.getDecoder().decode(partes[2]);
            byte[] obtenida = derivarClave(contrasena, sal, iteraciones);
            return java.security.MessageDigest.isEqual(esperada, obtenida);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivarClave(char[] contrasena, byte[] sal, int iteraciones) {
        PBEKeySpec especificacion = new PBEKeySpec(
                contrasena, sal, iteraciones, TAMANO_CLAVE_BITS
        );
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion)
                    .getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo proteger la contraseña.", e);
        } finally {
            especificacion.clearPassword();
        }
    }
}
