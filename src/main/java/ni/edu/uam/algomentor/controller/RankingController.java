package ni.edu.uam.algomentor.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import ni.edu.uam.algomentor.api.RankingApiClient;
import ni.edu.uam.algomentor.api.RankingPerfil;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RankingController {

    private static final Logger LOG = Logger.getLogger(RankingController.class.getName());

    @FXML private Button btnActualizar;
    @FXML private Label lblEstado;
    @FXML private TableView<RankingPerfil> tabla;
    @FXML private TableColumn<RankingPerfil, Integer> colPosicion;
    @FXML private TableColumn<RankingPerfil, String> colPerfil;
    @FXML private TableColumn<RankingPerfil, Long> colAciertos;
    @FXML private TableColumn<RankingPerfil, String> colPorcentaje;

    private final RankingApiClient cliente = new RankingApiClient();

    @FXML
    private void initialize() {
        colPosicion.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().posicion()));
        colPerfil.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().nombre()));
        colAciertos.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().aciertos()));
        colPorcentaje.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                String.format(Locale.forLanguageTag("es"), "%.1f%%", c.getValue().porcentaje())));
        actualizar();
    }

    @FXML
    private void actualizar() {
        btnActualizar.setDisable(true);
        lblEstado.setText("Cargando ranking…");
        Task<List<RankingPerfil>> tarea = new Task<>() {
            @Override
            protected List<RankingPerfil> call() throws Exception {
                return cliente.listar();
            }
        };
        tarea.setOnSucceeded(event -> {
            List<RankingPerfil> perfiles = tarea.getValue();
            tabla.setItems(FXCollections.observableArrayList(perfiles));
            lblEstado.setText(perfiles.isEmpty() ? "Todavía no hay perfiles."
                    : "Ordenado por aciertos y porcentaje de intentos evaluados.");
            btnActualizar.setDisable(false);
        });
        tarea.setOnFailed(event -> {
            LOG.log(Level.WARNING, "No se pudo consultar el ranking", tarea.getException());
            tabla.getItems().clear();
            lblEstado.setText("No se pudo consultar el ranking. Revisa PostgreSQL y la API local.");
            btnActualizar.setDisable(false);
        });
        Thread hilo = new Thread(tarea, "algomentor-ranking");
        hilo.setDaemon(true);
        hilo.start();
    }
}
