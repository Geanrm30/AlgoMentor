package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import ni.edu.uam.algomentor.engine.ErrorOperacion;
import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.engine.ResultadoVerificacion;
import ni.edu.uam.algomentor.engine.TipoOperacion;
import ni.edu.uam.algomentor.engine.Fraccion;
import ni.edu.uam.algomentor.dao.EjercicioDAO;
import ni.edu.uam.algomentor.model.Ejercicio;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.service.EvaluadorPractica;
import ni.edu.uam.algomentor.service.EvaluadorPasoMatriz;
import ni.edu.uam.algomentor.service.PerfilService;
import ni.edu.uam.algomentor.service.RegistroPracticaService;
import ni.edu.uam.algomentor.util.Animaciones;
import ni.edu.uam.algomentor.util.EntradaAprendizaje;
import ni.edu.uam.algomentor.util.EntradaMatrizVisual;
import ni.edu.uam.algomentor.util.Tema;
import ni.edu.uam.algomentor.util.TextoInterfaz;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PracticaController {

    private static final Logger LOG = Logger.getLogger(PracticaController.class.getName());

    @FXML private ComboBox<Perfil> cmbPerfil;
    @FXML private ComboBox<Ejercicio> cmbEjercicio;
    @FXML private TextField txtNuevoPerfil;
    @FXML private TextField txtRespuesta;
    @FXML private VBox panelOrdenamiento;
    @FXML private VBox panelMatriz;
    @FXML private ComboBox<String> cmbOperacion;
    @FXML private TextField txtDestino;
    @FXML private TextField txtOrigen;
    @FXML private TextField txtFactor;
    @FXML private VBox entradaMatrizEscrita;
    @FXML private Label lblEnunciado;
    @FXML private Label lblResultado;
    @FXML private Button btnVerificar;

    private long inicioNanos;
    private EntradaMatrizVisual matrizVisual;

    @FXML
    private void initialize() {
        matrizVisual = new EntradaMatrizVisual();
        matrizVisual.fijarDimensiones(true);
        matrizVisual.alCambiar(this::limpiarResultado);
        entradaMatrizEscrita.getChildren().add(matrizVisual);
        cmbPerfil.setConverter(new StringConverter<>() {
            @Override public String toString(Perfil valor) { return valor == null ? "" : valor.getNombre(); }
            @Override public Perfil fromString(String texto) { return null; }
        });
        cmbEjercicio.setConverter(new StringConverter<>() {
            @Override public String toString(Ejercicio valor) {
                return valor == null ? "" : "#" + valor.getId() + " · "
                        + TextoInterfaz.algoritmo(valor.getAlgoritmo().getNombre());
            }
            @Override public Ejercicio fromString(String texto) { return null; }
        });
        cmbOperacion.setItems(FXCollections.observableArrayList("Intercambiar filas", "Escalar fila", "Eliminar fila"));
        cmbEjercicio.valueProperty().addListener((obs, anterior, actual) -> {
            limpiarResultado();
            inicioNanos = System.nanoTime();
            boolean matriz = actual != null && actual.getAlgoritmo().getNombre().startsWith("Gauss");
            panelMatriz.setVisible(matriz);
            panelMatriz.setManaged(matriz);
            panelOrdenamiento.setVisible(!matriz);
            panelOrdenamiento.setManaged(!matriz);
            if (matriz) {
                try {
                    Fraccion[][] datos = EntradaAprendizaje.leerMatrizGuardada(actual.getDatos());
                    matrizVisual.cargar(datos);
                } catch (IllegalArgumentException e) {
                    matrizVisual.limpiar();
                    lblResultado.setText("El ejercicio tiene una matriz inválida: " + e.getMessage());
                }
            }
            lblEnunciado.setText(actual == null ? "Selecciona un ejercicio de la biblioteca."
                    : "Algoritmo: " + TextoInterfaz.algoritmo(actual.getAlgoritmo().getNombre())
                    + "\n" + TextoInterfaz.datos(actual));
        });
        txtRespuesta.textProperty().addListener((obs, anterior, actual) -> limpiarResultado());
        txtDestino.textProperty().addListener((obs, anterior, actual) -> limpiarResultado());
        txtOrigen.textProperty().addListener((obs, anterior, actual) -> limpiarResultado());
        txtFactor.textProperty().addListener((obs, anterior, actual) -> limpiarResultado());
        cmbOperacion.valueProperty().addListener((obs, anterior, actual) -> limpiarResultado());
        recargar();
    }

    @FXML
    private void recargar() {
        Animaciones.mostrarResultado(lblResultado, "Cargando perfiles y ejercicios…", Animaciones.Estado.NEUTRO);
        ejecutarEnSegundoPlano(() -> {
            List<Perfil> perfiles = new PerfilService().listarPerfiles();
            List<Ejercicio> ejercicios = new EjercicioDAO().listarConAlgoritmo().stream()
                    .filter(e -> CatalogoAlgoritmos.admite(e.getAlgoritmo().getNombre()))
                    .toList();
            Platform.runLater(() -> {
                cmbPerfil.setItems(FXCollections.observableArrayList(perfiles));
                cmbEjercicio.setItems(FXCollections.observableArrayList(ejercicios));
                Animaciones.mostrarResultado(lblResultado, ejercicios.isEmpty()
                        ? "No hay ejercicios para practicar. Crea uno en Biblioteca > Ejercicios."
                        : "Selecciona un perfil y un ejercicio.", Animaciones.Estado.NEUTRO);
            });
        }, "No se pudieron cargar los datos. Revisa la conexión con la biblioteca e inténtalo de nuevo.");
    }

    @FXML
    private void crearPerfil() {
        String nombre = txtNuevoPerfil.getText();
        try {
            PerfilService.validarNombre(nombre);
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Nombre inválido", e.getMessage());
            return;
        }
        ejecutarEnSegundoPlano(() -> {
            Perfil perfil = new PerfilService().crearPerfil(nombre);
            Platform.runLater(() -> {
                cmbPerfil.getItems().add(perfil);
                cmbPerfil.setValue(perfil);
                txtNuevoPerfil.clear();
                Animaciones.mostrarResultado(lblResultado, "Perfil creado.", Animaciones.Estado.CORRECTO);
            });
        }, "No se pudo crear el perfil. Es posible que ya exista o que la biblioteca no esté disponible.");
    }

    @FXML
    private void verificar() {
        Perfil perfil = cmbPerfil.getValue();
        Ejercicio ejercicio = cmbEjercicio.getValue();
        if (perfil == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Falta perfil", "Selecciona o crea un perfil antes de verificar.");
            return;
        }
        if (ejercicio == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Falta ejercicio", "Selecciona un ejercicio.");
            return;
        }
        String respuesta = respuestaActual(ejercicio);
        boolean correcto;
        List<ErrorOperacion> errores = List.of();
        String mensaje;
        try {
            if (esMatriz(ejercicio)) {
                ResultadoVerificacion verificacion = new EvaluadorPasoMatriz().evaluar(ejercicio.getDatos(),
                        tipoOperacion(), txtDestino.getText(), txtOrigen.getText(), txtFactor.getText(),
                        matrizVisual.texto());
                errores = verificacion.errores();
                correcto = verificacion.correcto();
                mensaje = correcto ? "Operación correcta." : describirErrores(errores);
            } else {
                EvaluadorPractica.Resultado evaluacion = EvaluadorPractica.evaluar(ejercicio.getAlgoritmo().getNombre(),
                        ejercicio.getDatos(), txtRespuesta.getText());
                correcto = evaluacion.correcto();
                mensaje = (correcto ? "Correcto." : "Incorrecto.")
                        + " Respuesta esperada: " + evaluacion.esperado();
            }
        } catch (IllegalArgumentException e) {
            correcto = false;
            mensaje = "Respuesta incorrecta: " + e.getMessage();
        }
        boolean resultado = correcto;
        String mensajeResultado = mensaje;
        List<ErrorOperacion> erroresEvaluados = errores;
        int duracion = (int) Math.min(Integer.MAX_VALUE, Math.max(0, (System.nanoTime() - inicioNanos) / 1_000_000_000L));
        btnVerificar.setDisable(true);
        Animaciones.mostrarResultado(lblResultado, "Registrando verificación…", Animaciones.Estado.NEUTRO);
        ejecutarEnSegundoPlano(() -> {
            new RegistroPracticaService().registrar(perfil.getId(), ejercicio.getId(), respuesta, resultado, duracion, erroresEvaluados);
            Platform.runLater(() -> {
                inicioNanos = System.nanoTime();
                Animaciones.mostrarResultado(lblResultado, mensajeResultado + "\nIntento registrado.",
                        resultado ? Animaciones.Estado.CORRECTO : Animaciones.Estado.ERROR);
                btnVerificar.setDisable(false);
            });
        }, "No se pudo registrar el intento. Revisa la conexión y el esquema.",
                () -> btnVerificar.setDisable(false));
    }

    private void limpiarResultado() {
        lblResultado.setText("");
    }

    private boolean esMatriz(Ejercicio ejercicio) {
        return ejercicio.getAlgoritmo().getNombre().startsWith("Gauss");
    }

    private TipoOperacion tipoOperacion() {
        return switch (cmbOperacion.getValue() == null ? "" : cmbOperacion.getValue()) {
            case "Intercambiar filas" -> TipoOperacion.INTERCAMBIAR_FILAS;
            case "Escalar fila" -> TipoOperacion.ESCALAR_FILA;
            case "Eliminar fila" -> TipoOperacion.ELIMINAR_FILA;
            default -> throw new IllegalArgumentException("Selecciona una operación de filas.");
        };
    }

    private String respuestaActual(Ejercicio ejercicio) {
        return esMatriz(ejercicio)
                ? String.join(" | ", String.valueOf(cmbOperacion.getValue()), txtDestino.getText(),
                        txtOrigen.getText(), txtFactor.getText(), matrizVisual.texto())
                : txtRespuesta.getText();
    }

    private String describirErrores(List<ErrorOperacion> encontrados) {
        StringBuilder texto = new StringBuilder("Operación incorrecta:\n");
        for (ErrorOperacion error : encontrados) {
            if (error.fila() >= 0 && error.columna() >= 0) {
                texto.append("Fila ").append(error.fila() + 1).append(", columna ")
                        .append(error.columna() + 1).append(": ");
            }
            texto.append(error.descripcion()).append('\n');
        }
        return texto.toString();
    }

    private void ejecutarEnSegundoPlano(Runnable accion, String mensajeError) {
        ejecutarEnSegundoPlano(accion, mensajeError, () -> { });
    }

    private void ejecutarEnSegundoPlano(Runnable accion, String mensajeError, Runnable alFallar) {
        Thread hilo = new Thread(() -> {
            try {
                accion.run();
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, mensajeError, e);
                Platform.runLater(() -> {
                    Animaciones.mostrarResultado(lblResultado, mensajeError, Animaciones.Estado.ERROR);
                    alFallar.run();
                });
            }
        }, "algomentor-persistencia");
        hilo.setDaemon(true);
        hilo.start();
    }

    public void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        Tema.estilizar(alerta);
        alerta.setHeaderText(null);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
