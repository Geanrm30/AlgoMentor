package ni.edu.uam.algomentor.controller;

import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.util.Tema;

import java.util.List;
import java.util.Optional;

/** Ventana para que un administrador elija de qué perfil quiere el informe. */
public final class ElegirPerfilDialogo {

    private ElegirPerfilDialogo() { }

    public static Optional<Perfil> mostrar(Window propietario, List<Perfil> perfiles, Perfil porDefecto) {
        Dialog<Perfil> dialogo = new Dialog<>();
        Tema.estilizar(dialogo);
        if (propietario != null) {
            dialogo.initOwner(propietario);
        }
        dialogo.setTitle("Exportar informe de progreso");
        dialogo.setHeaderText(null);

        ComboBox<Perfil> selector = new ComboBox<>();
        selector.getItems().setAll(perfiles);
        selector.setConverter(new StringConverter<>() {
            @Override
            public String toString(Perfil perfil) {
                return perfil == null ? "" : perfil.getNombre() + " (ID " + perfil.getId() + ")";
            }

            @Override
            public Perfil fromString(String texto) {
                return null;
            }
        });
        perfiles.stream().filter(p -> p.getId().equals(porDefecto.getId())).findFirst()
                .ifPresentOrElse(selector.getSelectionModel()::select, selector.getSelectionModel()::selectFirst);
        selector.setMaxWidth(Double.MAX_VALUE);

        VBox contenido = new VBox(10, new Label("Elige el perfil del que quieres el informe:"), selector);
        contenido.getStyleClass().add("tab-content");
        contenido.setPrefWidth(360);
        dialogo.getDialogPane().setContent(contenido);
        ButtonType continuar = new ButtonType("Continuar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(continuar, ButtonType.CANCEL);
        dialogo.getDialogPane().lookupButton(continuar).disableProperty()
                .bind(selector.getSelectionModel().selectedItemProperty().isNull());
        dialogo.setResultConverter(boton -> boton == continuar ? selector.getValue() : null);
        return dialogo.showAndWait();
    }
}
