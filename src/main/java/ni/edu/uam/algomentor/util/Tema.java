package ni.edu.uam.algomentor.util;

import javafx.scene.control.Dialog;

import java.net.URL;

public final class Tema {

    public static final String HOJA_ESTILOS = "/ni/edu/uam/algomentor/css/algomentor.css";

    private Tema() {
    }

    public static URL hojaEstilos() {
        return Tema.class.getResource(HOJA_ESTILOS);
    }

    public static void estilizar(Dialog<?> dialogo) {
        URL hoja = hojaEstilos();
        if (hoja != null) {
            dialogo.getDialogPane().getStylesheets().add(hoja.toExternalForm());
        }
    }
}
