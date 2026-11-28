package ni.edu.uam.algomentor.util;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Necesita pantalla (o Xvfb): comprueba que el fondo morado se dibuja y que los paneles lo muestran desenfocado. */
class FondoVidrioIT {

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        FutureTask<Void> inicio = new FutureTask<>(() -> { Platform.setImplicitExit(false); return null; });
        try { Platform.startup(inicio); }
        catch (IllegalStateException e) { Platform.runLater(inicio); }
        inicio.get(15, TimeUnit.SECONDS);
    }

    private static <T> T enHiloFx(Callable<T> tarea) throws Exception {
        FutureTask<T> trabajo = new FutureTask<>(tarea);
        Platform.runLater(trabajo);
        return trabajo.get(30, TimeUnit.SECONDS);
    }

    /** Diferencia máxima de luminosidad entre las muestras de la imagen: una imagen lisa daría casi cero. */
    private static double variacion(Image imagen) {
        var lector = imagen.getPixelReader();
        double minimo = 1;
        double maximo = 0;
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                double l = lector.getColor((int) ((i + 0.5) * imagen.getWidth() / 12), (int) ((j + 0.5) * imagen.getHeight() / 12)).getBrightness();
                minimo = Math.min(minimo, l);
                maximo = Math.max(maximo, l);
            }
        }
        return maximo - minimo;
    }

    @Test void seDibujanElFondoYSuCopiaDesenfocadaParaCadaTema() throws Exception {
        for (boolean claro : new boolean[]{false, true}) {
            enHiloFx(() -> {
                FondoVidrio.preparar(claro);
                assertTrue(FondoVidrio.hayCaptura());
                FondoVidrio.activar(true);
                assertTrue(FondoVidrio.activo());
                Image borrosa = FondoVidrio.borrosaProperty().get();
                assertNotNull(borrosa);
                assertEquals(480, borrosa.getWidth(), 0.5);
                assertEquals(270, borrosa.getHeight(), 0.5);
                assertTrue(variacion(borrosa) > 0.02, "El fondo no debe ser liso (claro=" + claro + ")");
                return null;
            });
        }
        enHiloFx(() -> {
            FondoVidrio.activar(false);
            assertFalse(FondoVidrio.activo());
            return null;
        });
    }

    @Test void elFondoSeInstalaDetrasDeTodoYSeReemplazaSinDuplicarse() throws Exception {
        enHiloFx(() -> {
            FondoVidrio.preparar(false);
            StackPane contenedor = new StackPane(new VBox());
            new Scene(contenedor, 800, 600);
            FondoVidrio.instalar(contenedor);
            FondoVidrio.instalar(contenedor);
            long fondos = contenedor.getChildren().stream().filter(n -> n.getStyleClass().contains(FondoVidrio.CLASE)).count();
            assertEquals(1, fondos);
            assertTrue(contenedor.getChildren().get(0) instanceof ImageView, "El fondo va al fondo de todo");
            ImageView fondo = (ImageView) contenedor.getChildren().get(0);
            assertNotNull(fondo.getImage());
            assertEquals(1600, fondo.getImage().getWidth(), 0.5);
            return null;
        });
    }

    @Test void unPanelRecibeUnaCopiaDesenfocadaQueSoloSeVeConElVidrioActivo() throws Exception {
        enHiloFx(() -> {
            FondoVidrio.preparar(false);
            FondoVidrio.activar(true);
            VBox panel = new VBox();
            panel.getStyleClass().add("card");
            StackPane raiz = new StackPane(panel);
            Scene escena = new Scene(raiz, 800, 600);
            raiz.applyCss();
            raiz.layout();
            VidrioEsmerilado.decorar(raiz);
            VidrioEsmerilado.decorar(raiz);
            long copias = panel.getChildren().stream().filter(n -> n instanceof ImageView).count();
            assertEquals(1, copias, "Decorar dos veces no debe duplicar la copia");
            ImageView copia = (ImageView) panel.getChildren().get(0);
            assertTrue(copia.isVisible());
            assertEquals(escena.getWidth(), copia.getFitWidth(), 0.5, "La copia tiene el tamaño de la ventana");
            FondoVidrio.activar(false);
            assertFalse(copia.isVisible(), "Sin vidrio, la copia se oculta");
            return null;
        });
    }
}
