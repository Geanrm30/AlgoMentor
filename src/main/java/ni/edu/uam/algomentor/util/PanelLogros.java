package ni.edu.uam.algomentor.util;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import ni.edu.uam.algomentor.service.LogrosService;

import java.util.List;

/** Dibuja las tarjetas de logros (progreso, descripción y estado) dentro de un {@link FlowPane}. */
public final class PanelLogros {

    private PanelLogros() { }

    public static void llenar(FlowPane panel, Label resumen, List<LogrosService.Logro> logros, boolean conHistorial) {
        panel.getChildren().clear();
        long conseguidos = logros.stream().filter(LogrosService.Logro::desbloqueado).count();
        resumen.setText(conseguidos + " de " + logros.size() + " logros desbloqueados."
                + (conHistorial ? "" : " No se pudo leer tu historial de práctica; revisa PostgreSQL."));
        for (LogrosService.Logro logro : logros) {
            VBox tarjeta = new VBox(6);
            tarjeta.getStyleClass().addAll("card", "logro", logro.desbloqueado() ? "logro-ok" : "logro-bloqueado");
            tarjeta.setPrefWidth(250);
            Label titulo = new Label((logro.desbloqueado() ? "★ " : "") + logro.titulo());
            titulo.getStyleClass().add("section-title");
            Label descripcion = new Label(logro.descripcion());
            descripcion.setWrapText(true);
            ProgressBar barra = new ProgressBar(logro.progreso());
            barra.setMaxWidth(Double.MAX_VALUE);
            Label detalle = new Label(logro.desbloqueado() ? "¡Desbloqueado!" : logro.detalle());
            detalle.getStyleClass().add("subtitle");
            tarjeta.getChildren().addAll(titulo, descripcion, barra, detalle);
            panel.getChildren().add(tarjeta);
        }
        if (FondoVidrio.hayCaptura()) {
            Platform.runLater(() -> VidrioEsmerilado.decorar(panel));
        }
        Animaciones.entrarEscalonado(panel.getChildren(), 16);
    }
}
