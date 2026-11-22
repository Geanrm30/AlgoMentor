package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import ni.edu.uam.algomentor.engine.BubbleSort;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SimuladorDesplazamientoIT {

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        FutureTask<Void> inicio = new FutureTask<>(() -> { Platform.setImplicitExit(false); return null; });
        try { Platform.startup(inicio); }
        catch (IllegalStateException e) { Platform.runLater(inicio); }
        inicio.get(15, TimeUnit.SECONDS);
    }

    @Test
    void sigueLaCeldaActivaFueraDeLaVistaEnUnArregloDeTreintaYCinco() throws Exception {
        AtomicReference<ScrollPane> desplazamiento = new AtomicReference<>();
        FutureTask<Void> preparar = new FutureTask<>(() -> {
            FXMLLoader vista = new FXMLLoader(getClass().getResource("/ni/edu/uam/algomentor/view/simulador.fxml"));
            Parent raiz = vista.load();
            new Scene(raiz, 760, 720);
            raiz.applyCss();
            raiz.layout();
            int[] valores = new int[35];
            for (int i = 0; i < valores.length; i++) valores[i] = valores.length - i;
            ((TextField) vista.getNamespace().get("txtArreglo")).setText(
                    java.util.Arrays.stream(valores).mapToObj(Integer::toString)
                            .collect(java.util.stream.Collectors.joining(",")));
            ((Button) raiz.lookup(".primary-button")).fire();
            int paso = 0;
            var pasos = new BubbleSort().ejecutar(valores).pasos();
            while (!pasos.get(paso).indices().contains(34)) paso++;
            Button siguiente = (Button) vista.getNamespace().get("btnSiguiente");
            for (int i = 0; i <= paso; i++) siguiente.fire();
            desplazamiento.set((ScrollPane) vista.getNamespace().get("scrollArreglo"));
            return null;
        });
        Platform.runLater(preparar);
        preparar.get(15, TimeUnit.SECONDS);
        FutureTask<Void> verificar = new FutureTask<>(() -> {
            assertTrue(desplazamiento.get().getHvalue() > 0, "La celda 35 debe entrar en la zona visible");
            return null;
        });
        Platform.runLater(verificar);
        verificar.get(15, TimeUnit.SECONDS);
    }
}
