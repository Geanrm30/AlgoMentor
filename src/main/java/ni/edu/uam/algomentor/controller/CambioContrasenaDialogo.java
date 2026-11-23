package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import ni.edu.uam.algomentor.service.AutenticacionService;
import ni.edu.uam.algomentor.util.Tema;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Ventana para cambiar la contraseña de la cuenta con la sesión iniciada. */
public final class CambioContrasenaDialogo {

    private static final Logger LOG = Logger.getLogger(CambioContrasenaDialogo.class.getName());

    private CambioContrasenaDialogo() { }

    public static void mostrar(Window propietario, Long perfilId) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        Tema.estilizar(dialogo);
        if (propietario != null) {
            dialogo.initOwner(propietario);
        }
        dialogo.setTitle("Cambiar contraseña");
        dialogo.setHeaderText(null);

        PasswordField actual = new PasswordField();
        actual.setPromptText("Contraseña actual");
        PasswordField nueva = new PasswordField();
        nueva.setPromptText("Nueva contraseña (mínimo 8 caracteres)");
        PasswordField confirmar = new PasswordField();
        confirmar.setPromptText("Confirmar la nueva contraseña");
        Label estado = new Label("Elige una contraseña que no uses en otros sitios.");
        estado.setWrapText(true);
        VBox contenido = new VBox(10, actual, nueva, confirmar, estado);
        contenido.getStyleClass().add("tab-content");
        contenido.setPrefWidth(360);
        dialogo.getDialogPane().setContent(contenido);

        ButtonType cambiar = new ButtonType("Cambiar contraseña", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(cambiar, ButtonType.CANCEL);
        Button botonCambiar = (Button) dialogo.getDialogPane().lookupButton(cambiar);
        botonCambiar.addEventFilter(ActionEvent.ACTION, evento -> {
            evento.consume();
            char[] contrasenaActual = actual.getText().toCharArray();
            char[] contrasenaNueva = nueva.getText().toCharArray();
            char[] repetida = confirmar.getText().toCharArray();
            if (!Arrays.equals(contrasenaNueva, repetida)) {
                estado.setText("Las contraseñas nuevas no coinciden.");
                limpiar(contrasenaActual, contrasenaNueva, repetida);
                return;
            }
            botonCambiar.setDisable(true);
            estado.setText("Actualizando…");
            Thread hilo = new Thread(() -> {
                try {
                    new AutenticacionService().cambiarContrasena(perfilId, contrasenaActual, contrasenaNueva);
                    Platform.runLater(() -> {
                        dialogo.close();
                        Alert aviso = new Alert(Alert.AlertType.INFORMATION,
                                "Tu contraseña se actualizó. Úsala la próxima vez que inicies sesión.");
                        Tema.estilizar(aviso);
                        aviso.setHeaderText(null);
                        aviso.showAndWait();
                    });
                } catch (IllegalArgumentException e) {
                    Platform.runLater(() -> {
                        estado.setText(e.getMessage());
                        botonCambiar.setDisable(false);
                    });
                } catch (RuntimeException e) {
                    LOG.log(Level.SEVERE, "No se pudo cambiar la contraseña", e);
                    Platform.runLater(() -> {
                        estado.setText("No se pudo cambiar la contraseña. Revisa la conexión con PostgreSQL.");
                        botonCambiar.setDisable(false);
                    });
                } finally {
                    limpiar(contrasenaActual, contrasenaNueva, repetida);
                }
            }, "algomentor-cambio-contrasena");
            hilo.setDaemon(true);
            hilo.start();
        });
        dialogo.showAndWait();
    }

    private static void limpiar(char[]... claves) {
        for (char[] clave : claves) {
            Arrays.fill(clave, '\0');
        }
    }
}
