package ni.edu.uam.algomentor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ni.edu.uam.algomentor.controller.LoginController;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.util.HibernateUtil;
import ni.edu.uam.algomentor.util.SesionUsuario;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp extends Application {

    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());
    private Stage stage;

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        Scene scene = new Scene(cargarLogin(), 760, 560);
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

    private Parent cargarLogin() throws IOException {
        URL view = MainApp.class.getResource("/ni/edu/uam/algomentor/view/login.fxml");
        if (view == null) {
            throw new IOException("No se encontró la vista de inicio de sesión");
        }
        FXMLLoader loader = new FXMLLoader(view);
        Parent login = loader.load();
        LoginController controller = loader.getController();
        controller.setAlAutenticar(this::mostrarAplicacion);
        return login;
    }

    private void mostrarAplicacion(Perfil perfil) {
        try {
            SesionUsuario.iniciar(perfil);
            Parent experiencia = cargarVista("experiencia.fxml");
            Parent gestion = cargarVista("gestion-contenido.fxml");
            TabPane navegacion = new TabPane();
            navegacion.getStyleClass().add("content-tabs");
            navegacion.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            navegacion.getTabs().addAll(
                    new Tab("Aprender", experiencia),
                    new Tab("Gestión de contenido", gestion)
            );

            VBox raiz = new VBox();
            raiz.getStyleClass().add("app-root");
            HBox barraTema = new HBox(10);
            barraTema.setAlignment(Pos.CENTER_RIGHT);
            barraTema.getStyleClass().add("app-toolbar");
            Label usuarioActivo = new Label("Sesión: " + perfil.getNombre());
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
            Button cerrarSesion = new Button("Cerrar sesión");
            cerrarSesion.setOnAction(event -> {
                SesionUsuario.cerrar();
                try {
                    stage.getScene().setRoot(cargarLogin());
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "No se pudo volver al inicio de sesión", e);
                    new Alert(Alert.AlertType.ERROR,
                            "No se pudo abrir el inicio de sesión.").showAndWait();
                }
            });
            barraTema.getChildren().addAll(usuarioActivo, etiquetaTema, alternarTema, cerrarSesion);
            raiz.getChildren().addAll(barraTema, navegacion);
            VBox.setVgrow(navegacion, Priority.ALWAYS);

            stage.getScene().setRoot(raiz);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo abrir la aplicación", e);
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir la aplicación.").showAndWait();
        }
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
