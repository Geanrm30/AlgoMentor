package ni.edu.uam.algomentor.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ni.edu.uam.algomentor.engine.FichaAlgoritmo;
import ni.edu.uam.algomentor.util.Animaciones;

import java.util.function.Function;

/** Tabla comparativa de los ordenamientos y, al elegir uno, la descripción de su mecánica. */
public class GuiaAlgoritmosController {

    @FXML private TableView<FichaAlgoritmo> tabla;
    @FXML private TableColumn<FichaAlgoritmo, String> colAlgoritmo;
    @FXML private TableColumn<FichaAlgoritmo, String> colTecnica;
    @FXML private TableColumn<FichaAlgoritmo, String> colMejor;
    @FXML private TableColumn<FichaAlgoritmo, String> colPromedio;
    @FXML private TableColumn<FichaAlgoritmo, String> colPeor;
    @FXML private TableColumn<FichaAlgoritmo, String> colEspacio;
    @FXML private TableColumn<FichaAlgoritmo, String> colEstable;
    @FXML private TableColumn<FichaAlgoritmo, String> colEnSitio;
    @FXML private Label lblNombre;
    @FXML private Label lblMecanica;
    @FXML private Label lblCuando;

    @FXML
    private void initialize() {
        columna(colAlgoritmo, f -> f.algoritmo().nombre());
        columna(colTecnica, FichaAlgoritmo::tecnica);
        columna(colMejor, FichaAlgoritmo::mejor);
        columna(colPromedio, FichaAlgoritmo::promedio);
        columna(colPeor, FichaAlgoritmo::peor);
        columna(colEspacio, FichaAlgoritmo::espacio);
        columna(colEstable, FichaAlgoritmo::paraEstable);
        columna(colEnSitio, FichaAlgoritmo::paraEnSitio);
        tabla.setItems(FXCollections.observableArrayList(FichaAlgoritmo.todas()));
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, ficha) -> mostrar(ficha));
        tabla.getSelectionModel().selectFirst();
    }

    private void columna(TableColumn<FichaAlgoritmo, String> columna, Function<FichaAlgoritmo, String> valor) {
        columna.setCellValueFactory(c -> new ReadOnlyStringWrapper(valor.apply(c.getValue())));
    }

    private void mostrar(FichaAlgoritmo ficha) {
        if (ficha == null) {
            return;
        }
        lblNombre.setText(ficha.algoritmo().etiqueta());
        lblMecanica.setText(ficha.mecanica());
        lblCuando.setText(ficha.cuandoUsarlo());
        Animaciones.entrar(lblMecanica, 0, 6);
        Animaciones.entrar(lblCuando, 60, 6);
    }
}
