package ni.edu.uam.algomentor.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ni.edu.uam.algomentor.engine.FichaAlgoritmo;

import java.util.function.Function;

/** Tabla comparativa de los ordenamientos y gráfica de crecimiento de las complejidades. */
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
    @FXML private LineChart<Number, Number> graficoComplejidad;

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
        cargarGrafico();
    }

    /** Curvas de crecimiento de las complejidades más comunes, para n de 1 a 20. */
    private void cargarGrafico() {
        String[] nombres = {"O(log n)", "O(n)", "O(n log n)", "O(n²)"};
        java.util.List<XYChart.Series<Number, Number>> series = new java.util.ArrayList<>();
        for (String nombre : nombres) {
            XYChart.Series<Number, Number> serie = new XYChart.Series<>();
            serie.setName(nombre);
            for (int n = 1; n <= 20; n++) {
                serie.getData().add(new XYChart.Data<>(n, operaciones(nombre, n)));
            }
            series.add(serie);
        }
        graficoComplejidad.getData().addAll(series);
    }

    static double operaciones(String complejidad, int n) {
        return switch (complejidad) {
            case "O(log n)" -> Math.log(n) / Math.log(2);
            case "O(n)" -> n;
            case "O(n log n)" -> n * Math.log(n) / Math.log(2);
            case "O(n²)" -> (double) n * n;
            default -> throw new IllegalArgumentException(complejidad);
        };
    }

    private void columna(TableColumn<FichaAlgoritmo, String> columna, Function<FichaAlgoritmo, String> valor) {
        columna.setCellValueFactory(c -> new ReadOnlyStringWrapper(valor.apply(c.getValue())));
    }
}
