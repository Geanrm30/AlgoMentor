package ni.edu.uam.algomentor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.algomentor.util.HibernateUtil;

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL view = MainApp.class.getResource("/ni/edu/uam/algomentor/view/gestion-contenido.fxml");
        if (view == null) {
            throw new IOException("No se encontró la vista de gestión de contenido");
        }

        Parent root = FXMLLoader.load(view);
        Scene scene = new Scene(root, 1000, 680);
        URL stylesheet = MainApp.class.getResource("/ni/edu/uam/algomentor/css/algomentor.css");
        if (stylesheet == null) {
            throw new IOException("No se encontró la hoja de estilos de AlgoMentor");
        }
        scene.getStylesheets().add(stylesheet.toExternalForm());

        stage.setTitle("AlgoMentor · Gestión de contenido");
        stage.setMinWidth(760);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> HibernateUtil.shutdown());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
