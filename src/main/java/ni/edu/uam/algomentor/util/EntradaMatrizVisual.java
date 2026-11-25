package ni.edu.uam.algomentor.util;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ni.edu.uam.algomentor.engine.FormatoDatos;
import ni.edu.uam.algomentor.engine.Fraccion;

import java.util.List;

public final class EntradaMatrizVisual extends VBox {

    private final Spinner<Integer> filas = new Spinner<>(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, FormatoDatos.MAX_FILAS, 2));
    private final Spinner<Integer> variables = new Spinner<>(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, FormatoDatos.MAX_COLUMNAS - 1, 2));
    private final GridPane cuadricula = new GridPane();
    private final HBox dimensiones;
    private String[][] referencia;
    private TextField[][] celdas = new TextField[0][0];
    private Runnable alCambiar = () -> { };

    public EntradaMatrizVisual() {
        setSpacing(10);
        dimensiones = new HBox(10, new Label("Ecuaciones / filas"), filas,
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
        // El recuadro se ajusta a la matriz en vez de ocupar todo el ancho de la tarjeta.
        desplazamiento.prefWidthProperty().bind(cuadricula.widthProperty().add(34));
        desplazamiento.setMaxWidth(Region.USE_PREF_SIZE);
        desplazamiento.prefViewportHeightProperty().bind(cuadricula.heightProperty().add(4));
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

    /** Con dimensiones fijas se ocultan los selectores: la forma la dicta el ejercicio. */
    public void fijarDimensiones(boolean fijas) {
        filas.setDisable(fijas);
        variables.setDisable(fijas);
        dimensiones.setVisible(!fijas);
        dimensiones.setManaged(!fijas);
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
        referencia = new String[matriz.length][matriz[0].length];
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                referencia[i][j] = matriz[i][j].toString();
            }
        }
        actualizarMarcas();
        alCambiar.run();
    }

    /** Vuelve a escribir la matriz inicial cargada con {@link #cargar}, si la hay. */
    public void restablecer() {
        if (referencia == null || celdas.length != referencia.length
                || celdas[0].length != referencia[0].length) {
            return;
        }
        for (int i = 0; i < celdas.length; i++) {
            for (int j = 0; j < celdas[i].length; j++) {
                celdas[i][j].setText(referencia[i][j]);
            }
        }
        alCambiar.run();
    }

    /**
     * Señala el resultado de una verificación: en verde las celdas que el estudiante cambió si acertó, o en rojo
     * las celdas con error (pares fila y columna, base 0). Las marcas se quitan al editar cualquier celda.
     */
    public void marcarResultado(boolean correcto, List<int[]> celdasConError) {
        quitarMarcasDeResultado();
        if (correcto) {
            for (int i = 0; i < celdas.length; i++) {
                for (int j = 0; j < celdas[i].length; j++) {
                    if (estaModificada(i, j)) {
                        celdas[i][j].getStyleClass().add("celda-correcta");
                    }
                }
            }
            return;
        }
        for (int[] par : celdasConError) {
            if (par[0] >= 0 && par[0] < celdas.length && par[1] >= 0 && par[1] < celdas[par[0]].length) {
                celdas[par[0]][par[1]].getStyleClass().add("celda-error");
            }
        }
    }

    private boolean estaModificada(int i, int j) {
        return referencia != null && i < referencia.length && j < referencia[i].length
                && !celdas[i][j].getText().strip().equals(referencia[i][j]);
    }

    private void actualizarMarcas() {
        for (int i = 0; i < celdas.length; i++) {
            for (int j = 0; j < celdas[i].length; j++) {
                actualizarMarca(i, j);
            }
        }
    }

    private void actualizarMarca(int i, int j) {
        var clases = celdas[i][j].getStyleClass();
        boolean modificada = estaModificada(i, j);
        if (modificada && !clases.contains("celda-modificada")) {
            clases.add("celda-modificada");
        } else if (!modificada) {
            clases.remove("celda-modificada");
        }
    }

    private void quitarMarcasDeResultado() {
        for (TextField[] fila : celdas) {
            for (TextField celda : fila) {
                celda.getStyleClass().removeAll("celda-correcta", "celda-error");
            }
        }
    }

    public void limpiar() {
        for (TextField[] fila : celdas) {
            for (TextField celda : fila) {
                celda.clear();
            }
        }
        referencia = null;
        actualizarMarcas();
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
                int fila = i;
                int columna = j;
                celda.textProperty().addListener((obs, anterior, actual) -> {
                    quitarMarcasDeResultado();
                    actualizarMarca(fila, columna);
                    alCambiar.run();
                });
                celdas[i][j] = celda;
                cuadricula.add(celda, j + 1, i + 1);
            }
        }
        alCambiar.run();
    }
}
