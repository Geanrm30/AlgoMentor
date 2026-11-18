package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ni.edu.uam.algomentor.engine.Fraccion;
import ni.edu.uam.algomentor.util.EntradaMatrizVisual;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificarSistemaVistaIT {

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        FutureTask<Void> inicio = new FutureTask<>(() -> { Platform.setImplicitExit(false); return null; });
        try { Platform.startup(inicio); }
        catch (IllegalStateException e) { Platform.runLater(inicio); }
        inicio.get(15, TimeUnit.SECONDS);
    }

    @Test
    void muestraTiempoLocalYEvitaMedirServicioCuandoNoAplica() throws Exception {
        FutureTask<Void> prueba = new FutureTask<>(() -> {
            FXMLLoader vista = new FXMLLoader(getClass().getResource("/ni/edu/uam/algomentor/view/verificar-sistema-api.fxml"));
            vista.load();
            VBox entrada = (VBox) vista.getNamespace().get("entradaMatriz");
            ((EntradaMatrizVisual) entrada.getChildren().getFirst()).cargar(new Fraccion[][]{
                    {Fraccion.de(1), Fraccion.de(1), Fraccion.de(2)},
                    {Fraccion.de(2), Fraccion.de(2), Fraccion.de(4)}
            });
            ((Button) vista.getNamespace().get("btnConsultar")).fire();
            assertTrue(((Label) vista.getNamespace().get("lblLocal")).getText().contains("Infinitas soluciones"));
            assertTrue(((Label) vista.getNamespace().get("lblTiempoLocal")).getText()
                    .matches("Tiempo de cálculo de AlgoMentor: [0-9]+\\.[0-9]{3} ms"));
            assertTrue(((Label) vista.getNamespace().get("lblTiempoExterno")).getText().contains("no aplica"));
            return null;
        });
        Platform.runLater(prueba);
        prueba.get(15, TimeUnit.SECONDS);
    }
}
