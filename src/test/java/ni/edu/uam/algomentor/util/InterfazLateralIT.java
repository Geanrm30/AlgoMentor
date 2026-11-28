package ni.edu.uam.algomentor.util;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Necesita pantalla (o Xvfb): comprueba la barra lateral y la decoración de botones con JavaFX real. */
class InterfazLateralIT {

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        FutureTask<Void> inicio = new FutureTask<>(() -> { Platform.setImplicitExit(false); return null; });
        try { Platform.startup(inicio); }
        catch (IllegalStateException e) { Platform.runLater(inicio); }
        inicio.get(15, TimeUnit.SECONDS);
    }

    private static <T> T enHiloFx(java.util.concurrent.Callable<T> tarea) throws Exception {
        FutureTask<T> trabajo = new FutureTask<>(tarea);
        Platform.runLater(trabajo);
        return trabajo.get(20, TimeUnit.SECONDS);
    }

    private static BarraLateral crear(AtomicInteger acciones, boolean[] contraida) {
        Runnable accion = acciones::incrementAndGet;
        return new BarraLateral(List.of(
                new BarraLateral.Grupo("Uno", List.of(
                        new BarraLateral.Elemento("a", "Alfa", "play", "Ctrl+1", accion),
                        new BarraLateral.Elemento("b", "Beta", "lapiz", null, accion))),
                new BarraLateral.Grupo("Dos", List.of(
                        new BarraLateral.Elemento("c", "Gamma", "libro", null, accion)))),
                false, c -> contraida[0] = c);
    }

    @Test void seleccionarMarcaElBotonYColocaElIndicadorSobreEl() throws Exception {
        enHiloFx(() -> {
            AtomicInteger acciones = new AtomicInteger();
            BarraLateral barra = crear(acciones, new boolean[1]);
            Scene escena = new Scene(new StackPane(barra), 800, 600);
            barra.applyCss();
            barra.layout();
            assertNull(barra.seleccion());
            barra.seleccionar("b");
            assertEquals("b", barra.seleccion());
            assertTrue(barra.boton("b").getStyleClass().contains("item-nav-activo"));
            assertFalse(barra.boton("a").getStyleClass().contains("item-nav-activo"));
            barra.seleccionar("c");
            assertFalse(barra.boton("b").getStyleClass().contains("item-nav-activo"));
            assertTrue(barra.boton("c").getStyleClass().contains("item-nav-activo"));
            barra.seleccionar("no-existe");
            assertNull(barra.seleccion());
            assertTrue(barra.getStyleClass().contains("lateral"));
            assertNotNull(escena);
            return null;
        });
    }

    @Test void pulsarUnElementoLoSeleccionaYEjecutaSuAccion() throws Exception {
        enHiloFx(() -> {
            AtomicInteger acciones = new AtomicInteger();
            BarraLateral barra = crear(acciones, new boolean[1]);
            new Scene(new StackPane(barra), 800, 600);
            barra.boton("c").fire();
            assertEquals(1, acciones.get());
            assertEquals("c", barra.seleccion());
            assertTrue(barra.contiene("a") && !barra.contiene("z"));
            assertTrue(barra.boton("a").getTooltip().getText().contains("Ctrl+1"));
            return null;
        });
    }

    @Test void contraerOcultaLosTextosYAvisaAlCambio() throws Exception {
        enHiloFx(() -> {
            boolean[] avisado = new boolean[1];
            BarraLateral barra = crear(new AtomicInteger(), avisado);
            new Scene(new StackPane(barra), 800, 600);
            assertFalse(barra.contraida());
            assertEquals(ContentDisplay.LEFT, barra.boton("a").getContentDisplay());
            barra.alternarContraida();
            assertTrue(barra.contraida());
            assertTrue(avisado[0]);
            assertEquals(ContentDisplay.GRAPHIC_ONLY, barra.boton("a").getContentDisplay());
            barra.alternarContraida();
            assertFalse(avisado[0]);
            assertEquals(ContentDisplay.LEFT, barra.boton("a").getContentDisplay());
            return null;
        });
    }

    @Test void decorarPoneIconoSoloALosBotonesQueLoNecesitan() throws Exception {
        enHiloFx(() -> {
            Button eliminar = new Button("Eliminar");
            Button actualizar = new Button("Actualizar");
            Button siguiente = new Button("Siguiente");
            Button conIcono = new Button("Guardar", Iconos.crear("check"));
            Button navegacion = new Button("Guardar");
            navegacion.getStyleClass().add("item-nav");
            HBox raiz = new HBox(eliminar, actualizar, siguiente, conIcono, navegacion);
            new Scene(raiz, 800, 200);
            EfectosBoton.decorar(raiz);
            assertNotNull(eliminar.getGraphic());
            assertNotNull(eliminar.getGraphic().getProperties().get(Iconos.CLAVE_TAPA));
            assertNotNull(actualizar.getGraphic());
            assertNull(siguiente.getGraphic());
            assertTrue(conIcono.getGraphic().getStyleClass().contains("ico-check"));
            assertNull(navegacion.getGraphic());
            var graficoActualizar = actualizar.getGraphic();
            EfectosBoton.decorar(raiz);
            assertSame(graficoActualizar, actualizar.getGraphic(), "Decorar dos veces no debe duplicar el ícono");
            return null;
        });
    }

    @Test void guardarYVerificarSeRestauranTrasLaAnimacion() throws Exception {
        Button guardar = enHiloFx(() -> {
            Button b = new Button("Guardar");
            HBox raiz = new HBox(b);
            new Scene(raiz, 400, 100);
            EfectosBoton.decorar(raiz);
            EfectosBoton.exito(b);
            assertEquals("Guardado", b.getText());
            assertTrue(b.getStyleClass().contains("boton-exito"));
            return b;
        });
        Thread.sleep(2500);
        enHiloFx(() -> {
            assertEquals("Guardar", guardar.getText());
            assertFalse(guardar.getStyleClass().contains("boton-exito"));
            return null;
        });
    }
}
