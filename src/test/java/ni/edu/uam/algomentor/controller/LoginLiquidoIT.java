package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import ni.edu.uam.algomentor.util.FormularioLiquido;
import ni.edu.uam.algomentor.util.Tema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Necesita pantalla (o Xvfb): recorre el inicio de sesión animado sin tocar la base de datos. */
class LoginLiquidoIT {

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
        return trabajo.get(20, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private static <T> T campo(Object objeto, String nombre) throws Exception {
        Field f = objeto.getClass().getDeclaredField(nombre);
        f.setAccessible(true);
        return (T) f.get(objeto);
    }

    private record Vista(Parent raiz, LoginController controlador) { }

    private static Vista cargar() throws Exception {
        return enHiloFx(() -> {
            FXMLLoader cargador = new FXMLLoader(LoginLiquidoIT.class.getResource("/ni/edu/uam/algomentor/view/login.fxml"));
            Parent raiz = cargador.load();
            Scene escena = new Scene(raiz, 1200, 800);
            escena.getStylesheets().add(Tema.hojaEstilos().toExternalForm());
            raiz.applyCss();
            raiz.layout();
            return new Vista(raiz, cargador.getController());
        });
    }

    @Test void arrancaSoloConLaPastillaYSeAbreAlPulsarla() throws Exception {
        Vista vista = cargar();
        enHiloFx(() -> {
            Button pastilla = campo(vista.controlador(), "pastilla");
            FormularioLiquido formulario = campo(vista.controlador(), "formulario");
            assertTrue(pastilla.getText().equals("Iniciar sesión") || pastilla.getText().startsWith("Continuar como "),
                    pastilla.getText());
            assertFalse(formulario.abierto());
            assertEquals(FormularioLiquido.ALTO_PASTILLA, formulario.getPrefHeight(), 0.001);
            pastilla.fire();
            assertTrue(formulario.abierto());
            assertEquals("Entrar", pastilla.getText());
            return null;
        });
        Thread.sleep(1500);
        enHiloFx(() -> {
            FormularioLiquido formulario = campo(vista.controlador(), "formulario");
            Button pastilla = campo(vista.controlador(), "pastilla");
            assertTrue(formulario.getPrefHeight() > 200, "El formulario debe haberse abierto: " + formulario.getPrefHeight());
            assertEquals(formulario.getPrefHeight() - FormularioLiquido.ALTO_PASTILLA, pastilla.getLayoutY(), 1.0);
            return null;
        });
    }

    @Test void laParteSuperiorDeLaTarjetaNoSeMueveAlAbrirElFormulario() throws Exception {
        Vista vista = cargar();
        Thread.sleep(1500);   // deja terminar la animación de entrada de la tarjeta antes de medir
        double[] antes = new double[1];
        enHiloFx(() -> {
            vista.raiz().applyCss();
            vista.raiz().layout();
            javafx.scene.layout.Region tarjeta = campo(vista.controlador(), "tarjeta");
            antes[0] = tarjeta.localToScene(0, 0).getY();
            ((Button) campo(vista.controlador(), "pastilla")).fire();
            return null;
        });
        Thread.sleep(1600);
        enHiloFx(() -> {
            vista.raiz().applyCss();
            vista.raiz().layout();
            javafx.scene.layout.Region tarjeta = campo(vista.controlador(), "tarjeta");
            assertEquals(antes[0], tarjeta.localToScene(0, 0).getY(), 2.0,
                    "La tarjeta no debe saltar: su parte superior queda fija mientras crece hacia abajo");
            assertTrue(tarjeta.getHeight() > 450, "La tarjeta sí debe haber crecido: " + tarjeta.getHeight());
            return null;
        });
    }

    @Test void cambiarDeModoMuestraElRegistroYDejaLaTarjetaLibre() throws Exception {
        Vista vista = cargar();
        enHiloFx(() -> {
            Button modo = campo(vista.controlador(), "btnModo");
            Label titulo = campo(vista.controlador(), "lblTitulo");
            assertEquals("Bienvenido de nuevo", titulo.getText());
            modo.fire();
            return null;
        });
        Thread.sleep(6000);
        enHiloFx(() -> {
            Label titulo = campo(vista.controlador(), "lblTitulo");
            Button pastilla = campo(vista.controlador(), "pastilla");
            FormularioLiquido formulario = campo(vista.controlador(), "formulario");
            javafx.scene.layout.Region tarjeta = campo(vista.controlador(), "tarjeta");
            assertEquals("Crea tu cuenta", titulo.getText());
            assertEquals("Crear cuenta", pastilla.getText());
            assertTrue(formulario.abierto(), "Tras expandirse, el formulario se abre solo");
            assertEquals(420, tarjeta.getWidth(), 4.0);
            assertEquals(javafx.scene.layout.Region.USE_COMPUTED_SIZE, tarjeta.getMinHeight(), "La tarjeta debe quedar libre");
            return null;
        });
    }
}
