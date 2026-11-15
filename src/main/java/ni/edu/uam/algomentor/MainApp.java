package ni.edu.uam.algomentor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
        Scene scene = new Scene(navegacion, 1100, 720);
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
