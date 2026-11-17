package ni.edu.uam.algomentor.util;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ni.edu.uam.algomentor.engine.FormatoDatos;
import ni.edu.uam.algomentor.engine.Fraccion;

public final class EntradaMatrizVisual extends VBox {

    private final Spinner<Integer> filas = new Spinner<>(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, FormatoDatos.MAX_FILAS, 2));
    private final Spinner<Integer> variables = new Spinner<>(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, FormatoDatos.MAX_COLUMNAS - 1, 2));
    private final GridPane cuadricula = new GridPane();
    private TextField[][] celdas = new TextField[0][0];
    private Runnable alCambiar = () -> { };

    public EntradaMatrizVisual() {
        setSpacing(10);
        HBox dimensiones = new HBox(10, new Label("Ecuaciones / filas"), filas,
                new Label("Incógnitas / columnas"), variables);
        dimensiones.setAlignment(Pos.CENTER_LEFT);
        dimensiones.getStyleClass().add("dimensiones-matriz");
        filas.setEditable(false);
        variables.setEditable(false);
        filas.setPrefWidth(68);
        variables.setPrefWidth(68);
        cuadricula.setHgap(7);
        cuadricula.setVgap(7);
        ScrollPane desplazamiento = new ScrollPane(cuadricula);
        desplazamiento.setFitToWidth(false);
        desplazamiento.setFitToHeight(false);
        desplazamiento.setPannable(true);
        desplazamiento.setMaxHeight(265);
        desplazamiento.setMinWidth(0);
        desplazamiento.setPrefWidth(480);
        desplazamiento.setMaxWidth(Double.MAX_VALUE);
        desplazamiento.setPrefViewportHeight(145);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        desplazamiento.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        desplazamiento.getStyleClass().add("desplazamiento-matriz");
        getChildren().addAll(dimensiones, desplazamiento,
                new Label("Escribe enteros o fracciones, por ejemplo 3/4. La última columna es b."));
        filas.valueProperty().addListener((obs, anterior, actual) -> reconstruir());
        variables.valueProperty().addListener((obs, anterior, actual) -> reconstruir());
        reconstruir();
    }

    public void alCambiar(Runnable accion) {
        alCambiar = accion == null ? () -> { } : accion;
    }

    public void fijarDimensiones(boolean fijas) {
        filas.setDisable(fijas);
        variables.setDisable(fijas);
    }

    public void cargar(Fraccion[][] matriz) {
        FormatoDatos.validarMatriz(matriz);
        filas.getValueFactory().setValue(matriz.length);
        variables.getValueFactory().setValue(matriz[0].length - 1);
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                celdas[i][j].setText(matriz[i][j].toString());
            }
        }
        alCambiar.run();
    }

    public void limpiar() {
        for (TextField[] fila : celdas) {
            for (TextField celda : fila) {
                celda.clear();
            }
        }
        alCambiar.run();
    }

    public String texto() {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < celdas.length; i++) {
            if (i > 0) texto.append('\n');
            for (int j = 0; j < celdas[i].length; j++) {
                if (j > 0) texto.append(',');
                texto.append(celdas[i][j].getText().strip());
            }
        }
        return texto.toString();
    }

    public Fraccion[][] leerMatriz() {
        return EntradaAprendizaje.leerMatriz(texto());
    }

    private void reconstruir() {
        TextField[][] anteriores = celdas;
        celdas = new TextField[filas.getValue()][variables.getValue() + 1];
        cuadricula.getChildren().clear();
        cuadricula.add(new Label(""), 0, 0);
        for (int j = 0; j < celdas[0].length; j++) {
            Label cabecera = new Label(j == celdas[0].length - 1 ? "b" : "x" + (j + 1));
            cabecera.getStyleClass().add("cabecera-matriz");
            cuadricula.add(cabecera, j + 1, 0);
        }
        for (int i = 0; i < celdas.length; i++) {
            cuadricula.add(new Label("F" + (i + 1)), 0, i + 1);
            for (int j = 0; j < celdas[i].length; j++) {
                TextField celda = new TextField();
                celda.setPrefWidth(82);
                celda.setMinWidth(82);
                celda.setAlignment(Pos.CENTER);
                celda.setPromptText("0");
                celda.setAccessibleText("Fila " + (i + 1) + ", columna " + (j + 1));
                if (i < anteriores.length) {
                    int indiceAnterior = j == celdas[i].length - 1
                            ? anteriores[i].length - 1 : j;
                    if (j == celdas[i].length - 1 || indiceAnterior < anteriores[i].length - 1) {
                        celda.setText(anteriores[i][indiceAnterior].getText());
                    }
                }
                celda.textProperty().addListener((obs, anterior, actual) -> alCambiar.run());
                celdas[i][j] = celda;
                cuadricula.add(celda, j + 1, i + 1);
            }
        }
        alCambiar.run();
    }
}
