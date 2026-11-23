package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.service.AutenticacionService;
import ni.edu.uam.algomentor.util.MostrarContrasena;
import ni.edu.uam.algomentor.util.UsuarioRecordado;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {

    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private TextField txtNuevoUsuario;
    @FXML private PasswordField txtNuevaContrasena;
    @FXML private PasswordField txtConfirmarContrasena;
    @FXML private Label lblEstado;
    @FXML private CheckBox chkRecordarUsuario;
    @FXML private CheckBox chkMostrarContrasena;
    @FXML private CheckBox chkMostrarNuevaContrasena;
    @FXML private Button btnIniciarSesion;
    @FXML private Button btnRegistrar;

    private final UsuarioRecordado usuarioRecordado = new UsuarioRecordado();
    private Consumer<Perfil> alAutenticar = perfil -> { };

    @FXML
    private void initialize() {
        MostrarContrasena.vincular(chkMostrarContrasena, txtContrasena);
        MostrarContrasena.vincular(chkMostrarNuevaContrasena, txtNuevaContrasena, txtConfirmarContrasena);
        usuarioRecordado.leer().ifPresent(usuario -> {
            txtUsuario.setText(usuario);
            chkRecordarUsuario.setSelected(true);
            Platform.runLater(txtContrasena::requestFocus);
        });
    }

    public void setAlAutenticar(Consumer<Perfil> accion) {
        alAutenticar = accion;
    }

    @FXML
    private void iniciarSesion() {
        String usuario = txtUsuario.getText();
        boolean recordar = chkRecordarUsuario.isSelected();
        char[] contrasena = txtContrasena.getText().toCharArray();
        ejecutarAutenticacion("Verificando credenciales…", () -> {
            try {
                Optional<Perfil> perfil = new AutenticacionService()
                        .iniciarSesion(usuario, contrasena);
                if (perfil.isEmpty()) {
                    throw new CredencialesInvalidasException();
                }
                if (recordar) {
                    usuarioRecordado.guardar(usuario);
                } else {
                    usuarioRecordado.olvidar();
                }
                return perfil.get();
            } finally {
                Arrays.fill(contrasena, '\0');
            }
        });
        txtContrasena.clear();
    }

    @FXML
    private void registrar() {
        String usuario = txtNuevoUsuario.getText();
        char[] contrasena = txtNuevaContrasena.getText().toCharArray();
        char[] confirmacion = txtConfirmarContrasena.getText().toCharArray();
        if (!Arrays.equals(contrasena, confirmacion)) {
            lblEstado.setText("Las contraseñas no coinciden.");
            Arrays.fill(contrasena, '\0');
            Arrays.fill(confirmacion, '\0');
            return;
        }

        ejecutarAutenticacion("Creando cuenta…", () -> {
            try {
                return new AutenticacionService().registrar(usuario, contrasena);
            } finally {
                Arrays.fill(contrasena, '\0');
                Arrays.fill(confirmacion, '\0');
            }
        });
        txtNuevaContrasena.clear();
        txtConfirmarContrasena.clear();
    }

    private void ejecutarAutenticacion(
            String mensajeInicial,
            AccionAutenticacion accion
    ) {
        btnIniciarSesion.setDisable(true);
        btnRegistrar.setDisable(true);
        lblEstado.setText(mensajeInicial);
        Thread hilo = new Thread(() -> {
            try {
                Perfil perfil = accion.ejecutar();
                Platform.runLater(() -> {
                    lblEstado.setText("Bienvenido, " + perfil.getNombre() + ".");
                    alAutenticar.accept(perfil);
                });
            } catch (CredencialesInvalidasException e) {
                Platform.runLater(() -> lblEstado.setText("Usuario o contraseña incorrectos."));
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> lblEstado.setText(e.getMessage()));
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "No se pudo completar la autenticación", e);
                Platform.runLater(() -> lblEstado.setText(
                        "No se pudo acceder a las cuentas. Revisa PostgreSQL y ejecuta schema.sql."
                ));
            } finally {
                Platform.runLater(() -> {
                    btnIniciarSesion.setDisable(false);
                    btnRegistrar.setDisable(false);
                });
            }
        }, "algomentor-autenticacion");
        hilo.setDaemon(true);
        hilo.start();
    }

    @FunctionalInterface
    private interface AccionAutenticacion {
        Perfil ejecutar();
    }

    private static final class CredencialesInvalidasException extends RuntimeException { }
}
