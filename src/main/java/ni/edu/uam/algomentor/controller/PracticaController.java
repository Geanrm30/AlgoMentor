package ni.edu.uam.algomentor.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import ni.edu.uam.algomentor.dao.EjercicioDAO;
import ni.edu.uam.algomentor.engine.CatalogoAlgoritmos;
import ni.edu.uam.algomentor.engine.ErrorOperacion;
import ni.edu.uam.algomentor.engine.Fraccion;
import ni.edu.uam.algomentor.engine.ResultadoVerificacion;
import ni.edu.uam.algomentor.engine.TipoOperacion;
import ni.edu.uam.algomentor.model.Ejercicio;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.service.EvaluadorPasoMatriz;
import ni.edu.uam.algomentor.service.EvaluadorPractica;
import ni.edu.uam.algomentor.service.RegistroPracticaService;
import ni.edu.uam.algomentor.util.Animaciones;
import ni.edu.uam.algomentor.util.EntradaAprendizaje;
import ni.edu.uam.algomentor.util.EntradaMatrizVisual;
import ni.edu.uam.algomentor.util.SesionUsuario;
import ni.edu.uam.algomentor.util.Tema;
import ni.edu.uam.algomentor.util.TextoInterfaz;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PracticaController {

    private static final Logger LOG = Logger.getLogger(PracticaController.class.getName());

    @FXML private ComboBox<Ejercicio> cmbEjercicio;
    @FXML private TextField txtRespuesta;
    @FXML private Label lblPerfilActual;
    @FXML private VBox panelOrdenamiento;
    @FXML private VBox panelMatriz;
    @FXML private ComboBox<String> cmbOperacion;
    @FXML private TextField txtDestino;
    @FXML private TextField txtOrigen;
    @FXML private TextField txtFactor;
    @FXML private Label lblAyudaOperacion;
    @FXML private Label lblDestino;
    @FXML private Label lblOrigen;
    @FXML private Label lblFactor;
    @FXML private Label lblFormula;
    @FXML private Region campoDestino;
    @FXML private Region campoOrigen;
    @FXML private Region campoFactor;
    @FXML private Button btnRestablecer;
    @FXML private VBox entradaMatrizEscrita;
    @FXML private Label lblEnunciado;
    @FXML private Label lblResultado;
    @FXML private Button btnVerificar;

    private long inicioNanos;
    private EntradaMatrizVisual matrizVisual;
    private int filasMatriz;

    @FXML
    private void initialize() {
        lblPerfilActual.setText("Sesión: " + SesionUsuario.obtenerPerfil().getNombre());
        matrizVisual = new EntradaMatrizVisual();
        matrizVisual.fijarDimensiones(true);
        matrizVisual.alCambiar(this::limpiarResultado);
        entradaMatrizEscrita.getChildren().add(matrizVisual);
        cmbEjercicio.setConverter(new StringConverter<>() {
            @Override
            public String toString(Ejercicio valor) {
                return valor == null ? "" : "#" + valor.getId() + " · "
                        + TextoInterfaz.algoritmo(valor.getAlgoritmo().getNombre());
            }

            @Override
            public Ejercicio fromString(String texto) {
                return null;
            }
        });
        cmbOperacion.setItems(FXCollections.observableArrayList(
                "Intercambiar filas", "Escalar fila", "Eliminar fila"));
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
                    filasMatriz = datos.length;
                    matrizVisual.cargar(datos);
                } catch (IllegalArgumentException e) {
                    matrizVisual.limpiar();
                    Animaciones.mostrarResultado(lblResultado,
                            "El ejercicio tiene una matriz inválida: " + e.getMessage(),
                            Animaciones.Estado.ERROR);
                }
            } else {
                filasMatriz = 0;
                matrizVisual.limpiar();
            }
            actualizarOperacion();
            lblEnunciado.setText(actual == null ? "Selecciona un ejercicio de la biblioteca."
                    : "Algoritmo: " + TextoInterfaz.algoritmo(actual.getAlgoritmo().getNombre())
                    + "\n" + TextoInterfaz.datos(actual));
        });
        txtRespuesta.textProperty().addListener((obs, anterior, actual) -> limpiarResultado());
        txtDestino.textProperty().addListener((obs, anterior, actual) -> {
            limpiarResultado();
            actualizarFormula();
        });
        txtOrigen.textProperty().addListener((obs, anterior, actual) -> {
            limpiarResultado();
            actualizarFormula();
        });
        txtFactor.textProperty().addListener((obs, anterior, actual) -> {
            limpiarResultado();
            actualizarFormula();
        });
        cmbOperacion.valueProperty().addListener((obs, anterior, actual) -> {
            limpiarResultado();
            actualizarOperacion();
        });
        actualizarOperacion();
        recargar();
    }

    @FXML
    private void recargar() {
        Animaciones.mostrarResultado(lblResultado, "Cargando ejercicios…", Animaciones.Estado.NEUTRO);
        ejecutarEnSegundoPlano(() -> {
            List<Ejercicio> ejercicios = new EjercicioDAO().listarConAlgoritmo().stream()
                    .filter(e -> CatalogoAlgoritmos.admite(e.getAlgoritmo().getNombre()))
                    .toList();
            Platform.runLater(() -> {
                cmbEjercicio.setItems(FXCollections.observableArrayList(ejercicios));
                Animaciones.mostrarResultado(lblResultado, ejercicios.isEmpty()
                        ? "No hay ejercicios para practicar. Crea uno en Biblioteca > Ejercicios."
                        : "Selecciona un ejercicio.", Animaciones.Estado.NEUTRO);
            });
        }, "No se pudieron cargar los datos. Revisa la conexión con la biblioteca e inténtalo de nuevo.");
    }

    @FXML
    private void verificar() {
        Perfil perfil = SesionUsuario.obtenerPerfil();
        Ejercicio ejercicio = cmbEjercicio.getValue();
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
                ResultadoVerificacion verificacion = new EvaluadorPasoMatriz().evaluar(
                        ejercicio.getDatos(), tipoOperacion(), txtDestino.getText(),
                        txtOrigen.getText(), txtFactor.getText(), matrizVisual.texto());
                errores = verificacion.errores();
                correcto = verificacion.correcto();
                matrizVisual.marcarResultado(correcto, errores.stream()
                        .filter(error -> error.fila() >= 0 && error.columna() >= 0)
                        .map(error -> new int[] {error.fila(), error.columna()})
                        .toList());
                mensaje = correcto ? "Operación correcta." : describirErrores(errores);
            } else {
                EvaluadorPractica.Resultado evaluacion = EvaluadorPractica.evaluar(
                        ejercicio.getAlgoritmo().getNombre(), ejercicio.getDatos(),
                        txtRespuesta.getText());
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
        int duracion = (int) Math.min(Integer.MAX_VALUE,
                Math.max(0, (System.nanoTime() - inicioNanos) / 1_000_000_000L));
        btnVerificar.setDisable(true);
        Animaciones.mostrarResultado(lblResultado, "Registrando verificación…",
                Animaciones.Estado.NEUTRO);
        ejecutarEnSegundoPlano(() -> {
            new RegistroPracticaService().registrar(perfil.getId(), ejercicio.getId(),
                    respuesta, resultado, duracion, erroresEvaluados);
            Platform.runLater(() -> {
                inicioNanos = System.nanoTime();
                Animaciones.mostrarResultado(lblResultado,
                        mensajeResultado + "\nIntento registrado.",
                        resultado ? Animaciones.Estado.CORRECTO : Animaciones.Estado.ERROR);
                btnVerificar.setDisable(false);
            });
        }, "No se pudo registrar el intento. Revisa la conexión y el esquema.",
                () -> btnVerificar.setDisable(false));
    }

    @FXML
    private void restablecerMatriz() {
        matrizVisual.restablecer();
        limpiarResultado();
    }

    private void limpiarResultado() {
        lblResultado.setText("");
    }

    private static void mostrarCampo(Region campo, boolean visible) {
        campo.setVisible(visible);
        campo.setManaged(visible);
    }

    /** Muestra solo los datos que necesita la operación elegida y explica qué hace. */
    private void actualizarOperacion() {
        String operacion = cmbOperacion.getValue();
        boolean intercambiar = "Intercambiar filas".equals(operacion);
        boolean escalar = "Escalar fila".equals(operacion);
        boolean eliminar = "Eliminar fila".equals(operacion);
        mostrarCampo(campoDestino, intercambiar || escalar || eliminar);
        mostrarCampo(campoOrigen, intercambiar || eliminar);
        mostrarCampo(campoFactor, escalar || eliminar);
        if (!intercambiar && !eliminar) {
            txtOrigen.clear();
        }
        if (!escalar && !eliminar) {
            txtFactor.clear();
        }
        if (intercambiar) {
            lblAyudaOperacion.setText("Cambia de lugar dos filas de la matriz.");
            lblDestino.setText("Fila A");
            lblOrigen.setText("Fila B");
        } else if (escalar) {
            lblAyudaOperacion.setText("Multiplica toda una fila por un número distinto de cero.");
            lblDestino.setText("Fila");
            lblFactor.setText("Se multiplica por");
        } else if (eliminar) {
            lblAyudaOperacion.setText("Resta a una fila un múltiplo de otra para hacer ceros: "
                    + "la fila destino cambia y la fila origen queda igual.");
            lblDestino.setText("Fila destino");
            lblOrigen.setText("Fila origen");
            lblFactor.setText("Factor k");
        } else {
            lblAyudaOperacion.setText("Elige una operación para ver los datos que necesita.");
        }
        actualizarFormula();
    }

    /** Escribe la operación con las filas y el factor tecleados y avisa de datos imposibles. */
    private void actualizarFormula() {
        String operacion = cmbOperacion.getValue();
        String a = valorOInterrogacion(txtDestino, "?");
        String b = valorOInterrogacion(txtOrigen, "?");
        String k = valorOInterrogacion(txtFactor, "k");
        String formula;
        if ("Intercambiar filas".equals(operacion)) {
            formula = "F" + a + " ↔ F" + b;
        } else if ("Escalar fila".equals(operacion)) {
            formula = "F" + a + " ← (" + k + ") · F" + a;
        } else if ("Eliminar fila".equals(operacion)) {
            formula = "F" + a + " ← F" + a + " − (" + k + ") · F" + b;
        } else {
            lblFormula.setText("");
            mostrarCampo(lblFormula, false);
            return;
        }
        String problema = problemaDeOperacion(operacion);
        mostrarCampo(lblFormula, true);
        lblFormula.getStyleClass().remove("formula-invalida");
        if (problema == null) {
            lblFormula.setText(formula);
        } else {
            lblFormula.setText(formula + "\n" + problema);
            lblFormula.getStyleClass().add("formula-invalida");
        }
    }

    private static String valorOInterrogacion(TextField campo, String porDefecto) {
        String texto = campo.getText() == null ? "" : campo.getText().strip();
        return texto.isEmpty() ? porDefecto : texto;
    }

    /** Devuelve el motivo si lo tecleado ya no puede ser válido, o {@code null} si todo va bien por ahora. */
    private String problemaDeOperacion(String operacion) {
        Integer destino = leerFilaTecleada(txtDestino);
        Integer origen = leerFilaTecleada(txtOrigen);
        if (!txtDestino.getText().isBlank() && destino == null) {
            return "La fila debe ser un entero positivo.";
        }
        if (!txtOrigen.getText().isBlank() && origen == null
                && !"Escalar fila".equals(operacion)) {
            return "La fila origen debe ser un entero positivo.";
        }
        if (filasMatriz > 0) {
            if (destino != null && destino > filasMatriz) {
                return "La fila " + destino + " no existe: la matriz tiene " + filasMatriz + " filas.";
            }
            if (origen != null && origen > filasMatriz && !"Escalar fila".equals(operacion)) {
                return "La fila " + origen + " no existe: la matriz tiene " + filasMatriz + " filas.";
            }
        }
        if (destino != null && destino.equals(origen) && !"Escalar fila".equals(operacion)) {
            return "Las dos filas deben ser distintas.";
        }
        if (!"Intercambiar filas".equals(operacion) && !txtFactor.getText().isBlank()) {
            try {
                if (Fraccion.leer(txtFactor.getText()).esCero() && "Escalar fila".equals(operacion)) {
                    return "No se puede multiplicar una fila por cero.";
                }
            } catch (IllegalArgumentException | ArithmeticException e) {
                return "El factor debe ser un entero o una fracción, por ejemplo 3/4.";
            }
        }
        return null;
    }

    private static Integer leerFilaTecleada(TextField campo) {
        try {
            int fila = Integer.parseInt(campo.getText().strip());
            return fila >= 1 ? fila : null;
        } catch (NumberFormatException e) {
            return null;
        }
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
                ? String.join(" | ", String.valueOf(cmbOperacion.getValue()),
                txtDestino.getText(), txtOrigen.getText(), txtFactor.getText(), matrizVisual.texto())
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
