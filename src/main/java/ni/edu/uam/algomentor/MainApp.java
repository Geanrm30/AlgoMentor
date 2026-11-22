package ni.edu.uam.algomentor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ni.edu.uam.algomentor.util.HibernateUtil;

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent experiencia = cargarVista("experiencia.fxml");
        Parent gestion = cargarVista("gestion-contenido.fxml");
        TabPane navegacion = new TabPane();
        navegacion.getStyleClass().add("content-tabs");
        navegacion.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        navegacion.getTabs().addAll(new Tab("Aprender", experiencia), new Tab("Gestión de contenido", gestion));

        VBox raiz = new VBox();
        raiz.getStyleClass().add("app-root");
        HBox barraTema = new HBox(10);
        barraTema.setAlignment(Pos.CENTER_RIGHT);
        barraTema.getStyleClass().add("app-toolbar");
        Label etiquetaTema = new Label("Apariencia");
        ToggleButton alternarTema = new ToggleButton("Tema claro");
        alternarTema.setOnAction(event -> {
            boolean temaClaro = alternarTema.isSelected();
            raiz.getStyleClass().remove("light-theme");
            if (temaClaro) {
                raiz.getStyleClass().add("light-theme");
            }
            alternarTema.setText(temaClaro ? "Tema oscuro" : "Tema claro");
        });
        barraTema.getChildren().addAll(etiquetaTema, alternarTema);
        raiz.getChildren().addAll(barraTema, navegacion);
        VBox.setVgrow(navegacion, Priority.ALWAYS);

        Scene scene = new Scene(raiz, 1100, 720);
        URL stylesheet = MainApp.class.getResource("/ni/edu/uam/algomentor/css/algomentor.css");
        if (stylesheet == null) {
            throw new IOException("No se encontró la hoja de estilos de AlgoMentor");
        }
        scene.getStylesheets().add(stylesheet.toExternalForm());

        stage.setTitle("AlgoMentor");
        stage.setMinWidth(760);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> HibernateUtil.shutdown());
        stage.show();
    }

    private Parent cargarVista(String nombre) throws IOException {
        URL view = MainApp.class.getResource("/ni/edu/uam/algomentor/view/" + nombre);
        if (view == null) {
            throw new IOException("No se encontró la vista " + nombre);
        }
        return FXMLLoader.load(view);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
