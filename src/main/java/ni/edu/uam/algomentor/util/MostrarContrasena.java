package ni.edu.uam.algomentor.util;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.util.Set;

/**
 * Permite mostrar u ocultar uno o varios campos de contraseña con una casilla. Junto a cada
 * {@link PasswordField} se agrega un campo de texto que comparte su contenido y solo uno de los dos
 * está visible; el texto sigue en memoria igual que antes y nunca se guarda.
 */
public final class MostrarContrasena {

    private static final Set<String> CLASES_PROPIAS = Set.of("text-input", "text-field", "password-field");

    private MostrarContrasena() { }

    public static void vincular(CheckBox casilla, PasswordField... campos) {
        for (PasswordField campo : campos) {
            if (!(campo.getParent() instanceof Pane padre)) {
                continue;
            }
            TextField visible = new TextField();
            visible.setPromptText(campo.getPromptText());
            visible.setPrefWidth(campo.getPrefWidth());
            visible.setMaxWidth(campo.getMaxWidth());
            campo.getStyleClass().stream()
                    .filter(clase -> !CLASES_PROPIAS.contains(clase))
                    .forEach(visible.getStyleClass()::add);
            visible.textProperty().bindBidirectional(campo.textProperty());
            visible.setOnAction(evento -> campo.fireEvent(new ActionEvent(campo, campo)));
            visible.visibleProperty().bind(casilla.selectedProperty());
            visible.managedProperty().bind(casilla.selectedProperty());
            campo.visibleProperty().bind(casilla.selectedProperty().not());
            campo.managedProperty().bind(casilla.selectedProperty().not());

            padre.getChildren().add(padre.getChildren().indexOf(campo) + 1, visible);
            casilla.selectedProperty().addListener((obs, antes, ahora) -> {
                if ((antes ? visible : campo).isFocused()) {
                    Platform.runLater((ahora ? visible : campo)::requestFocus);
                }
            });
        }
    }
}
