package ni.edu.uam.algomentor.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import ni.edu.uam.algomentor.service.ComparadorOrdenamientos;
import ni.edu.uam.algomentor.util.EntradaAprendizaje;
import ni.edu.uam.algomentor.util.Tema;

public class ComparadorController {

    @FXML private TextField txtDatos;
    @FXML private TableView<ComparadorOrdenamientos.Metrica> tabla;
    @FXML private TableColumn<ComparadorOrdenamientos.Metrica, String> colAlgoritmo;
    @FXML private TableColumn<ComparadorOrdenamientos.Metrica, String> colComparaciones;
    @FXML private TableColumn<ComparadorOrdenamientos.Metrica, String> colIntercambios;
    @FXML private TableColumn<ComparadorOrdenamientos.Metrica, String> colPasos;
    @FXML private TableColumn<ComparadorOrdenamientos.Metrica, String> colResultado;

    @FXML
    private void initialize() {
        colAlgoritmo.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().algoritmo()));
        colComparaciones.setCellValueFactory(c -> new ReadOnlyStringWrapper(Integer.toString(c.getValue().comparaciones())));
        colIntercambios.setCellValueFactory(c -> new ReadOnlyStringWrapper(Integer.toString(c.getValue().intercambios())));
        colPasos.setCellValueFactory(c -> new ReadOnlyStringWrapper(Integer.toString(c.getValue().pasos())));
        colResultado.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().resultado()));
    }

    @FXML
    private void comparar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(
                    ComparadorOrdenamientos.comparar(EntradaAprendizaje.leerArreglo(txtDatos.getText()))));
        } catch (IllegalArgumentException e) {
            tabla.getItems().clear();
            mostrarAlerta(Alert.AlertType.WARNING, "Entrada inválida", e.getMessage());
        }
    }

    public void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        Tema.estilizar(alerta);
        alerta.setHeaderText(null);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
