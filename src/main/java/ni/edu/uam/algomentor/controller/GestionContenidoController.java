package ni.edu.uam.algomentor.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import ni.edu.uam.algomentor.dao.AlgoritmoDAO;
import ni.edu.uam.algomentor.dao.BaseDAO;
import ni.edu.uam.algomentor.dao.EjercicioDAO;
import ni.edu.uam.algomentor.dao.IntentoDAO;
import ni.edu.uam.algomentor.model.Algoritmo;
import ni.edu.uam.algomentor.model.Ejercicio;
import ni.edu.uam.algomentor.model.Intento;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.util.ValidacionesFormulario;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GestionContenidoController {

    private static final Logger LOG = Logger.getLogger(GestionContenidoController.class.getName());

    @FXML private TextField txtNombreAlgoritmo;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtComplejidad;
    @FXML private TextArea txtDescripcionAlgoritmo;
    @FXML private TableView<Algoritmo> tblAlgoritmos;
    @FXML private TableColumn<Algoritmo, String> colNombreAlgoritmo;
    @FXML private TableColumn<Algoritmo, String> colCategoria;
    @FXML private TableColumn<Algoritmo, String> colComplejidad;
    @FXML private TableColumn<Algoritmo, String> colDescripcion;
    @FXML private ComboBox<Algoritmo> cmbAlgoritmoEjercicio;
    @FXML private ComboBox<String> cmbOrigen;
    @FXML private Spinner<Integer> spDificultad;
    @FXML private TextArea txtDatosEjercicio;
    @FXML private TableView<Ejercicio> tblEjercicios;
    @FXML private TableColumn<Ejercicio, String> colAlgoritmoEjercicio;
    @FXML private TableColumn<Ejercicio, String> colDatos;
    @FXML private TableColumn<Ejercicio, String> colDificultad;
    @FXML private TableColumn<Ejercicio, String> colOrigen;
    @FXML private ComboBox<Algoritmo> cmbFiltroAlgoritmo;
    @FXML private ComboBox<Perfil> cmbFiltroPerfil;
    @FXML private TableView<ResultadoConsulta> tblResultados;
    @FXML private TableColumn<ResultadoConsulta, String> colResultadoAlgoritmo;
    @FXML private TableColumn<ResultadoConsulta, String> colResultadoEjercicio;
    @FXML private TableColumn<ResultadoConsulta, String> colResultadoDetalle;
    @FXML private Label lblEstado;

    private AlgoritmoDAO algoritmoDAO;
    private EjercicioDAO ejercicioDAO;
    private IntentoDAO intentoDAO;
    private BaseDAO<Perfil> perfilDAO;

    @FXML
    private void initialize() {
        cmbCategoria.setItems(FXCollections.observableArrayList("ORDENAMIENTO", "MATEMATICO"));
        cmbOrigen.setItems(FXCollections.observableArrayList("ALEATORIO", "MANUAL", "PERSONALIZADO"));
        spDificultad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1));
        StringConverter<Algoritmo> algoritmoConverter = new StringConverter<>() {
            @Override public String toString(Algoritmo a) { return a == null ? "" : a.getNombre(); }
            @Override public Algoritmo fromString(String text) { return null; }
        };
        cmbAlgoritmoEjercicio.setConverter(algoritmoConverter);
        cmbFiltroAlgoritmo.setConverter(algoritmoConverter);
        cmbFiltroPerfil.setConverter(new StringConverter<>() {
            @Override public String toString(Perfil p) { return p == null ? "" : p.getNombre(); }
            @Override public Perfil fromString(String text) { return null; }
        });

        colNombreAlgoritmo.setCellValueFactory(c -> value(c.getValue().getNombre()));
        colCategoria.setCellValueFactory(c -> value(c.getValue().getCategoria()));
        colComplejidad.setCellValueFactory(c -> value(c.getValue().getComplejidad()));
        colDescripcion.setCellValueFactory(c -> value(c.getValue().getDescripcion()));
        colAlgoritmoEjercicio.setCellValueFactory(c -> value(c.getValue().getAlgoritmo().getNombre()));
        colDatos.setCellValueFactory(c -> value(c.getValue().getDatos()));
        colDificultad.setCellValueFactory(c -> value(String.valueOf(c.getValue().getDificultad())));
        colOrigen.setCellValueFactory(c -> value(c.getValue().getOrigen()));
        colResultadoAlgoritmo.setCellValueFactory(c -> value(c.getValue().algoritmo()));
        colResultadoEjercicio.setCellValueFactory(c -> value(c.getValue().ejercicio()));
        colResultadoDetalle.setCellValueFactory(c -> value(c.getValue().detalle()));

        tblAlgoritmos.getSelectionModel().selectedItemProperty().addListener((obs, old, a) -> {
            if (a != null) {
                txtNombreAlgoritmo.setText(a.getNombre());
                cmbCategoria.setValue(a.getCategoria());
                txtComplejidad.setText(a.getComplejidad());
                txtDescripcionAlgoritmo.setText(a.getDescripcion());
            }
        });
        tblEjercicios.getSelectionModel().selectedItemProperty().addListener((obs, old, e) -> {
            if (e != null) {
                cmbAlgoritmoEjercicio.getSelectionModel().select(
                        cmbAlgoritmoEjercicio.getItems().stream()
                                .filter(a -> a.getId().equals(e.getAlgoritmo().getId()))
                                .findFirst().orElse(null));
                cmbOrigen.setValue(e.getOrigen());
                spDificultad.getValueFactory().setValue(e.getDificultad());
                txtDatosEjercicio.setText(e.getDatos());
            }
        });
        try {
            algoritmoDAO = new AlgoritmoDAO();
            ejercicioDAO = new EjercicioDAO();
            intentoDAO = new IntentoDAO();
            perfilDAO = new BaseDAO<>(Perfil.class);
            recargar();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "No se pudo iniciar la persistencia", e);
            lblEstado.setText("No se pudo conectar con PostgreSQL. Revisá db.local.properties y el esquema.");
        }
    }

    private ReadOnlyStringWrapper value(String text) {
        return new ReadOnlyStringWrapper(text == null ? "" : text);
    }

    private void comprobarConexion() {
        if (algoritmoDAO == null) {
            throw new IllegalStateException("La base de datos no está disponible. Revisá la configuración y reiniciá AlgoMentor.");
        }
    }

    private void recargar() {
        comprobarConexion();
        List<Algoritmo> algoritmos = algoritmoDAO.listar();
        tblAlgoritmos.setItems(FXCollections.observableArrayList(algoritmos));
        cmbAlgoritmoEjercicio.setItems(FXCollections.observableArrayList(algoritmos));
        cmbFiltroAlgoritmo.setItems(FXCollections.observableArrayList(algoritmos));
        tblEjercicios.setItems(FXCollections.observableArrayList(ejercicioDAO.listarConAlgoritmo()));
        cmbFiltroPerfil.setItems(FXCollections.observableArrayList(perfilDAO.listar()));
        lblEstado.setText("Datos actualizados");
    }

    @FXML private void guardarAlgoritmo() {
        ejecutar(() -> {
            Algoritmo a = leerAlgoritmo();
            algoritmoDAO.guardar(a);
            recargar();
            limpiarAlgoritmo();
        });
    }

    @FXML private void actualizarAlgoritmo() {
        ejecutar(() -> {
            Algoritmo seleccionado = requerido(tblAlgoritmos.getSelectionModel().getSelectedItem(), "Seleccioná un algoritmo");
            Algoritmo a = leerAlgoritmo();
            a.setId(seleccionado.getId());
            algoritmoDAO.actualizar(a);
            recargar();
            limpiarAlgoritmo();
        });
    }

    @FXML private void eliminarAlgoritmo() {
        ejecutar(() -> {
            Algoritmo a = requerido(tblAlgoritmos.getSelectionModel().getSelectedItem(), "Seleccioná un algoritmo");
            if (confirmarEliminacion(a.getNombre())) {
                algoritmoDAO.eliminar(a.getId());
                recargar();
                limpiarAlgoritmo();
            }
        });
    }

    @FXML private void limpiarAlgoritmo() {
        tblAlgoritmos.getSelectionModel().clearSelection();
        txtNombreAlgoritmo.clear();
        cmbCategoria.setValue(null);
        txtComplejidad.clear();
        txtDescripcionAlgoritmo.clear();
    }

    private Algoritmo leerAlgoritmo() {
        ValidacionesFormulario.validarAlgoritmo(txtNombreAlgoritmo.getText(), cmbCategoria.getValue(),
                txtComplejidad.getText(), txtDescripcionAlgoritmo.getText());
        Algoritmo a = new Algoritmo();
        a.setNombre(txtNombreAlgoritmo.getText().trim());
        a.setCategoria(cmbCategoria.getValue());
        a.setComplejidad(txtComplejidad.getText().trim());
        a.setDescripcion(txtDescripcionAlgoritmo.getText().trim());
        return a;
    }

    @FXML private void guardarEjercicio() {
        ejecutar(() -> {
            ejercicioDAO.guardar(leerEjercicio());
            recargar();
            limpiarEjercicio();
        });
    }

    @FXML private void actualizarEjercicio() {
        ejecutar(() -> {
            Ejercicio seleccionado = requerido(tblEjercicios.getSelectionModel().getSelectedItem(), "Seleccioná un ejercicio");
            Ejercicio e = leerEjercicio();
            e.setId(seleccionado.getId());
            e.setFechaCreacion(seleccionado.getFechaCreacion());
            ejercicioDAO.actualizar(e);
            recargar();
            limpiarEjercicio();
        });
    }

    @FXML private void eliminarEjercicio() {
        ejecutar(() -> {
            Ejercicio e = requerido(tblEjercicios.getSelectionModel().getSelectedItem(), "Seleccioná un ejercicio");
            if (confirmarEliminacion("el ejercicio " + e.getId())) {
                ejercicioDAO.eliminar(e.getId());
                recargar();
                limpiarEjercicio();
            }
        });
    }

    @FXML private void limpiarEjercicio() {
        tblEjercicios.getSelectionModel().clearSelection();
        cmbAlgoritmoEjercicio.setValue(null);
        cmbOrigen.setValue(null);
        spDificultad.getValueFactory().setValue(1);
        txtDatosEjercicio.clear();
    }

    private Ejercicio leerEjercicio() {
        Algoritmo algoritmo = cmbAlgoritmoEjercicio.getValue();
        Integer dificultad = spDificultad.getValue();
        ValidacionesFormulario.validarEjercicio(algoritmo, cmbOrigen.getValue(), dificultad, txtDatosEjercicio.getText());
        Ejercicio e = new Ejercicio();
        e.setAlgoritmo(algoritmo);
        e.setOrigen(cmbOrigen.getValue());
        e.setDificultad(dificultad);
        e.setDatos(txtDatosEjercicio.getText().trim());
        return e;
    }

    @FXML private void buscarEjercicios() {
        ejecutar(() -> {
            Algoritmo a = requerido(cmbFiltroAlgoritmo.getValue(), "Seleccioná un algoritmo");
            tblResultados.setItems(FXCollections.observableArrayList(
                    ejercicioDAO.listarPorAlgoritmo(a.getId()).stream()
                            .map(e -> new ResultadoConsulta(a.getNombre(), e.getDatos(),
                                    "Dificultad " + e.getDificultad() + " · " + e.getOrigen()))
                            .toList()));
            lblEstado.setText("Ejercicios encontrados: " + tblResultados.getItems().size());
        });
    }

    @FXML private void verHistorial() {
        ejecutar(() -> {
            Perfil p = requerido(cmbFiltroPerfil.getValue(), "Seleccioná un perfil");
            List<Intento> intentos = intentoDAO.listarPorPerfil(p.getId());
            tblResultados.setItems(FXCollections.observableArrayList(intentos.stream()
                    .map(i -> new ResultadoConsulta(i.getEjercicio().getAlgoritmo().getNombre(),
                            i.getEjercicio().getDatos(), i.getModo() + " · " + i.getFechaInicio()))
                    .toList()));
            lblEstado.setText("Intentos encontrados: " + intentos.size());
        });
    }

    private <T> T requerido(T valor, String mensaje) {
        if (valor == null) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor;
    }

    private boolean confirmarEliminacion(String nombre) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setHeaderText(null);
        alerta.setTitle("Confirmar eliminación");
        alerta.setContentText("¿Eliminar " + nombre + "?");
        return alerta.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private void ejecutar(Runnable accion) {
        try {
            comprobarConexion();
            accion.run();
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Revisá los datos", e.getMessage());
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Falló una operación de persistencia", e);
            mostrarAlerta(Alert.AlertType.ERROR, "Error de persistencia",
                    "No se pudo completar la operación. Revisá la conexión y las relaciones entre registros.");
        }
    }

    public void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private record ResultadoConsulta(String algoritmo, String ejercicio, String detalle) {
    }
}
