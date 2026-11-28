package ni.edu.uam.algomentor.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.StackPane;

/** Campo de texto con una etiqueta pequeña en la esquina superior, al estilo de los formularios modernos. */
public final class CampoFlotante extends StackPane {

    private final TextInputControl campo;

    public CampoFlotante(String etiqueta, TextInputControl campo, double ancho, double alto) {
        this.campo = campo;
        getStyleClass().add("campo-login");
        Label texto = new Label(etiqueta);
        texto.getStyleClass().add("campo-login-etiqueta");
        texto.setMouseTransparent(true);
        StackPane.setAlignment(texto, Pos.TOP_LEFT);
        StackPane.setMargin(texto, new Insets(9, 0, 0, 16));
        campo.getStyleClass().add("campo-login-texto");
        campo.setMaxWidth(Double.MAX_VALUE);
        campo.setMaxHeight(Double.MAX_VALUE);
        getChildren().addAll(campo, texto);
        setMinSize(ancho, alto);
        setPrefSize(ancho, alto);
        setMaxSize(ancho, alto);
    }

    public TextInputControl campo() {
        return campo;
    }
}
