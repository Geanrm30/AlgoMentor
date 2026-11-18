package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.service.ComparadorOrdenamientos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Prueba real de JavaFX; se ejecuta explícitamente en un escritorio con JavaFX disponible. */
class InterfazAlgoritmosIT {
    @BeforeAll static void iniciarJavaFx() throws Exception {
        FutureTask<Void> inicio = new FutureTask<>(() -> { Platform.setImplicitExit(false); return null; });
        try { Platform.startup(inicio); }
        catch (IllegalStateException e) { Platform.runLater(inicio); }
        inicio.get(15, TimeUnit.SECONDS);
    }

    @Test void simulaSieteOrdenamientosYRetrocedeHastaElEstadoInicial() throws Exception {
        enJavaFx(() -> {
            FXMLLoader vista = cargar("simulador.fxml");
            ComboBox<String> algoritmos = control(vista, "cmbAlgoritmo");
            assertEquals(9, algoritmos.getItems().size());
            for (var algoritmo : CatalogoAlgoritmos.ordenamientos()) {
                algoritmos.setValue(algoritmo.nombre());
                ((TextField) control(vista, "txtArreglo")).setText("3,-1,0,3");
                boton(vista.getRoot(), "Simular").fire();
                int pasos = recorrer(vista, "btnSiguiente");
                assertEquals(algoritmo.crearOrdenamiento().ejecutar(new int[]{3, -1, 0, 3}).pasos().size(), pasos);
                assertTrue(((Label) control(vista, "lblExplicacion")).getText().contains("[-1, 0, 3, 3]"));
                assertEquals(pasos, recorrer(vista, "btnAnterior"));
                assertEquals("Estado inicial", ((Label) control(vista, "lblPaso")).getText());
            }
        });
    }

    @Test void generaYSimulaEntradasDeLosNueveAlgoritmos() throws Exception {
        enJavaFx(() -> {
            FXMLLoader vista = cargar("simulador.fxml");
            ComboBox<String> algoritmos = control(vista, "cmbAlgoritmo");
            for (String nombre : CatalogoAlgoritmos.nombres()) {
                algoritmos.setValue(nombre);
                boton(vista.getRoot(), "Generar entrada").fire();
                boton(vista.getRoot(), "Simular").fire();
                assertTrue(recorrer(vista, "btnSiguiente") > 0);
                String conclusion = ((Label) control(vista, "lblExplicacion")).getText();
                assertTrue(conclusion.contains(CatalogoAlgoritmos.porNombre(nombre).esOrdenamiento()
                        ? "Resultado:" : "Sistema:"), nombre);
            }
        });
    }

    @Test void comparadorPresentaSieteResultadosYColumnasDeMetricas() throws Exception {
        enJavaFx(() -> {
            FXMLLoader vista = cargar("comparador.fxml");
            ((TextField) control(vista, "txtDatos")).setText("3,-1,0,3");
            boton(vista.getRoot(), "Comparar").fire();
            TableView<ComparadorOrdenamientos.Metrica> tabla = control(vista, "tabla");
            assertEquals(7, tabla.getItems().size());
            assertEquals(9, tabla.getColumns().size());
            assertTrue(tabla.getItems().stream().allMatch(m -> m.resultado().equals("[-1, 0, 3, 3]")));
        });
    }

    @Test void progresoPresentaUnaTablaDeIntegrantesYConservaElDetallePorHabilidad() throws Exception {
        enJavaFx(() -> {
            FXMLLoader vista = cargar("progreso.fxml");
            TableView<?> integrantes = control(vista, "tabla");
            TableView<?> habilidades = control(vista, "tablaHabilidades");
            assertEquals(6, integrantes.getColumns().size());
            assertEquals("Integrante", integrantes.getColumns().getFirst().getText());
            assertEquals(4, habilidades.getColumns().size());
        });
    }

    @Test void practicaCargaConUnaSolaAccionDeVerificacion() throws Exception {
        enJavaFx(() -> {
            FXMLLoader vista = cargar("practica.fxml");
            assertNotNull(control(vista, "btnVerificar"));
            assertTrue(((Parent) vista.getRoot()).lookupAll(".button").stream().filter(Button.class::isInstance)
                    .map(Button.class::cast).noneMatch(b -> "Guardar intento".equals(b.getText())));
        });
    }

    @Test void quickSortResaltaElPivoteYLoQuitaAlVolverAlInicio() throws Exception {
        enJavaFx(() -> {
            FXMLLoader vista = cargar("simulador.fxml");
            ComboBox<String> algoritmos = control(vista, "cmbAlgoritmo");
            algoritmos.setValue("Quick Sort");
            ((TextField) control(vista, "txtArreglo")).setText("3,1,2");
            boton(vista.getRoot(), "Simular").fire();
            ((Button) control(vista, "btnSiguiente")).fire();
            Parent raiz = vista.getRoot();
            assertEquals(1, raiz.lookupAll(".valor-pivote").size());
            ((Button) control(vista, "btnAnterior")).fire();
            assertTrue(raiz.lookupAll(".valor-pivote").isEmpty());
        });
    }

    private int recorrer(FXMLLoader vista, String id) {
        Button boton = control(vista, id);
        int pasos = 0;
        while (!boton.isDisabled()) {
            assertTrue(++pasos < 5_000, "La navegación debe finalizar");
            boton.fire();
        }
        return pasos;
    }

    private FXMLLoader cargar(String nombre) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ni/edu/uam/algomentor/view/" + nombre));
        Parent raiz = loader.load();
        new Scene(raiz, 1100, 720);
        raiz.applyCss();
        raiz.layout();
        return loader;
    }

    private Button boton(Parent raiz, String texto) {
        return raiz.lookupAll(".button").stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> texto.equals(b.getText())).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private <T> T control(FXMLLoader vista, String id) { return (T) vista.getNamespace().get(id); }

    private void enJavaFx(Trabajo trabajo) throws Exception {
        FutureTask<Void> tarea = new FutureTask<>(() -> { trabajo.ejecutar(); return null; });
        Platform.runLater(tarea);
        tarea.get(45, TimeUnit.SECONDS);
    }

    @FunctionalInterface private interface Trabajo { void ejecutar() throws Exception; }
}
