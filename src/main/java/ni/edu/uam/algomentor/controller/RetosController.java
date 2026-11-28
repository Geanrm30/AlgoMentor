package ni.edu.uam.algomentor.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import ni.edu.uam.algomentor.dao.EstadisticasDAO;
import ni.edu.uam.algomentor.service.CuestionarioService;
import ni.edu.uam.algomentor.service.DesafioService;
import ni.edu.uam.algomentor.service.LogrosService;
import ni.edu.uam.algomentor.util.Animaciones;
import ni.edu.uam.algomentor.util.EfectosBoton;
import ni.edu.uam.algomentor.util.PuntajesLocales;
import ni.edu.uam.algomentor.util.SesionUsuario;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Desafío cronometrado, cuestionario teórico y logros del perfil. */
public class RetosController {

    private static final Logger LOG = Logger.getLogger(RetosController.class.getName());
    private static final int PREGUNTAS = 10;

    // Desafío
    @FXML private Button btnIniciarDesafio;
    @FXML private Label lblTiempo;
    @FXML private Label lblPuntos;
    @FXML private Label lblRacha;
    @FXML private Label lblMejorDesafio;
    @FXML private ProgressBar barraTiempo;
    @FXML private Label lblPreguntaDesafio;
    @FXML private TextField txtRespuestaDesafio;
    @FXML private Button btnResponderDesafio;
    @FXML private Label lblRetroDesafio;
    // Cuestionario
    @FXML private Button btnIniciarCuestionario;
    @FXML private Label lblProgresoCuestionario;
    @FXML private Label lblMejorCuestionario;
    @FXML private VBox tarjetaPregunta;
    @FXML private Label lblPreguntaCuestionario;
    @FXML private VBox panelOpciones;
    @FXML private Label lblRetroCuestionario;
    @FXML private Button btnSiguientePregunta;
    // Logros
    @FXML private Label lblResumenLogros;
    @FXML private FlowPane panelLogros;

    private DesafioService partida;
    private DesafioService.Pregunta preguntaActual;
    private Timeline reloj;
    private int segundosRestantes;
    private int ronda;

    private List<CuestionarioService.Pregunta> preguntas = List.of();
    private int posicion;
    private int aciertosCuestionario;
    private boolean respondida;

    private long perfilId;

    @FXML
    private void initialize() {
        perfilId = SesionUsuario.obtenerPerfil().getId();
        lblMejorDesafio.setText("Tu mejor puntaje: " + PuntajesLocales.mejorDesafio(perfilId));
        lblMejorCuestionario.setText("Tu mejor resultado: " + PuntajesLocales.mejorCuestionario(perfilId) + " de " + PREGUNTAS);
        // Si el usuario cambia de sección, el desafío en curso no debe seguir corriendo en segundo plano.
        btnIniciarDesafio.sceneProperty().addListener((obs, antes, ahora) -> {
            if (ahora == null) {
                detenerReloj();
            }
        });
        cargarLogros();
    }

    // ---------- Desafío ----------

    @FXML
    private void iniciarDesafio() {
        detenerReloj();
        partida = new DesafioService();
        ronda = 0;
        segundosRestantes = DesafioService.SEGUNDOS;
        actualizarMarcador();
        txtRespuestaDesafio.setDisable(false);
        btnResponderDesafio.setDisable(false);
        btnIniciarDesafio.setText("Reiniciar");
        siguienteDesafio();
        txtRespuestaDesafio.requestFocus();
        reloj = new Timeline(new KeyFrame(Duration.seconds(1), evento -> {
            segundosRestantes--;
            actualizarMarcador();
            if (segundosRestantes <= 0) {
                terminarDesafio();
            }
        }));
        reloj.setCycleCount(DesafioService.SEGUNDOS);
        reloj.play();
        Animaciones.mostrarResultado(lblRetroDesafio, "¡Tiempo corriendo!", Animaciones.Estado.NEUTRO);
    }

    @FXML
    private void responderDesafio() {
        if (partida == null || preguntaActual == null || txtRespuestaDesafio.isDisabled()) {
            return;
        }
        boolean correcta = partida.responder(preguntaActual, txtRespuestaDesafio.getText());
        Animaciones.mostrarResultado(lblRetroDesafio, correcta
                        ? "¡Correcto! Racha de " + partida.racha() + "."
                        : "Incorrecto. Era " + preguntaActual.esperada() + ".",
                correcta ? Animaciones.Estado.CORRECTO : Animaciones.Estado.ERROR);
        EfectosBoton.resultado(btnResponderDesafio, correcta);
        ronda++;
        actualizarMarcador();
        siguienteDesafio();
    }

    private void siguienteDesafio() {
        preguntaActual = partida.nuevaPregunta(ronda);
        lblPreguntaDesafio.setText(preguntaActual.enunciado());
        txtRespuestaDesafio.clear();
        Animaciones.entrar(lblPreguntaDesafio, 0, 8);
    }

    private void actualizarMarcador() {
        lblTiempo.setText("Tiempo: " + Math.max(0, segundosRestantes) + " s");
        barraTiempo.setProgress(Math.max(0, segundosRestantes) / (double) DesafioService.SEGUNDOS);
        lblPuntos.setText("Puntos: " + partida.puntos());
        lblRacha.setText("Racha: " + partida.racha());
    }

    private void terminarDesafio() {
        detenerReloj();
        txtRespuestaDesafio.setDisable(true);
        btnResponderDesafio.setDisable(true);
        btnIniciarDesafio.setText("Jugar de nuevo");
        boolean record = PuntajesLocales.registrarDesafio(perfilId, partida.puntos());
        lblPreguntaDesafio.setText("Se acabó el tiempo.");
        Animaciones.mostrarResultado(lblRetroDesafio, "Puntaje final: " + partida.puntos() + " · "
                + partida.aciertos() + " aciertos · " + partida.errores() + " errores · mejor racha "
                + partida.mejorRacha() + (record ? "\n¡Nuevo récord personal!" : ""),
                record ? Animaciones.Estado.CORRECTO : Animaciones.Estado.NEUTRO);
        lblMejorDesafio.setText("Tu mejor puntaje: " + PuntajesLocales.mejorDesafio(perfilId));
        cargarLogros();
    }

    private void detenerReloj() {
        if (reloj != null) {
            reloj.stop();
            reloj = null;
        }
    }

    // ---------- Cuestionario ----------

    @FXML
    private void iniciarCuestionario() {
        preguntas = CuestionarioService.generar(System.nanoTime(), PREGUNTAS);
        posicion = 0;
        aciertosCuestionario = 0;
        btnIniciarCuestionario.setText("Reiniciar");
        tarjetaPregunta.setVisible(true);
        tarjetaPregunta.setManaged(true);
        mostrarPregunta();
    }

    private void mostrarPregunta() {
        CuestionarioService.Pregunta pregunta = preguntas.get(posicion);
        respondida = false;
        lblProgresoCuestionario.setText("Pregunta " + (posicion + 1) + " de " + preguntas.size()
                + " · Aciertos: " + aciertosCuestionario);
        lblPreguntaCuestionario.setText(pregunta.enunciado());
        lblRetroCuestionario.setText("");
        btnSiguientePregunta.setVisible(false);
        btnSiguientePregunta.setManaged(false);
        panelOpciones.getChildren().clear();
        for (int i = 0; i < pregunta.opciones().size(); i++) {
            int elegida = i;
            Button opcion = new Button(pregunta.opciones().get(i));
            opcion.setMaxWidth(Double.MAX_VALUE);
            opcion.setWrapText(true);
            opcion.setOnAction(evento -> responderPregunta(pregunta, elegida));
            panelOpciones.getChildren().add(opcion);
        }
        Animaciones.entrarEscalonado(panelOpciones.getChildren(), 10);
        Animaciones.entrar(lblPreguntaCuestionario, 0, 8);
    }

    private void responderPregunta(CuestionarioService.Pregunta pregunta, int elegida) {
        if (respondida) {
            return;
        }
        respondida = true;
        boolean correcta = elegida == pregunta.correcta();
        if (correcta) {
            aciertosCuestionario++;
        }
        for (int i = 0; i < panelOpciones.getChildren().size(); i++) {
            var opcion = panelOpciones.getChildren().get(i);
            opcion.setDisable(i != pregunta.correcta() && i != elegida);
            if (i == pregunta.correcta()) {
                opcion.getStyleClass().add("opcion-correcta");
            } else if (i == elegida) {
                opcion.getStyleClass().add("opcion-incorrecta");
                Animaciones.sacudir(opcion);
            }
        }
        boolean ultima = posicion + 1 >= preguntas.size();
        Animaciones.mostrarResultado(lblRetroCuestionario, (correcta ? "¡Correcto! " : "Incorrecto. ") + pregunta.explicacion(),
                correcta ? Animaciones.Estado.CORRECTO : Animaciones.Estado.ERROR);
        lblProgresoCuestionario.setText("Pregunta " + (posicion + 1) + " de " + preguntas.size()
                + " · Aciertos: " + aciertosCuestionario);
        btnSiguientePregunta.setText(ultima ? "Ver resultado" : "Siguiente pregunta");
        btnSiguientePregunta.setVisible(true);
        btnSiguientePregunta.setManaged(true);
    }

    @FXML
    private void siguientePregunta() {
        if (posicion + 1 < preguntas.size()) {
            posicion++;
            mostrarPregunta();
            return;
        }
        boolean record = PuntajesLocales.registrarCuestionario(perfilId, aciertosCuestionario);
        panelOpciones.getChildren().clear();
        btnSiguientePregunta.setVisible(false);
        btnSiguientePregunta.setManaged(false);
        lblPreguntaCuestionario.setText("Resultado: " + aciertosCuestionario + " de " + preguntas.size());
        Animaciones.mostrarResultado(lblRetroCuestionario, (aciertosCuestionario == preguntas.size()
                        ? "¡Perfecto! " : aciertosCuestionario >= 7 ? "Muy bien. " : "Sigue practicando. ")
                        + (record ? "¡Nuevo récord personal!" : "Puedes intentarlo de nuevo con preguntas distintas."),
                aciertosCuestionario >= 7 ? Animaciones.Estado.CORRECTO : Animaciones.Estado.NEUTRO);
        lblMejorCuestionario.setText("Tu mejor resultado: " + PuntajesLocales.mejorCuestionario(perfilId) + " de " + PREGUNTAS);
        btnIniciarCuestionario.setText("Jugar de nuevo");
        cargarLogros();
    }

    // ---------- Logros ----------

    @FXML
    private void cargarLogros() {
        Thread hilo = new Thread(() -> {
            long intentos = 0, correctos = 0, incorrectos = 0;
            int racha = 0, dominados = 0;
            boolean conBase = true;
            try {
                EstadisticasDAO dao = new EstadisticasDAO();
                var resumen = dao.obtenerResumenIntentos(perfilId);
                intentos = resumen.total();
                correctos = resumen.correctos();
                incorrectos = resumen.incorrectos();
                racha = LogrosService.rachaMaxima(dao.listarIntentosRecientes(perfilId, 200));
                dominados = LogrosService.algoritmosDominados(dao.listarRendimientoPorAlgoritmo(perfilId));
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, "No se pudieron leer las estadísticas para los logros", e);
                conBase = false;
            }
            var datos = new LogrosService.Datos(intentos, correctos, incorrectos, racha, dominados,
                    PuntajesLocales.mejorDesafio(perfilId), PuntajesLocales.mejorCuestionario(perfilId));
            boolean disponible = conBase;
            Platform.runLater(() -> mostrarLogros(LogrosService.evaluar(datos), disponible));
        }, "algomentor-logros");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarLogros(List<LogrosService.Logro> logros, boolean conBase) {
        panelLogros.getChildren().clear();
        long conseguidos = logros.stream().filter(LogrosService.Logro::desbloqueado).count();
        lblResumenLogros.setText(conseguidos + " de " + logros.size() + " logros desbloqueados."
                + (conBase ? "" : " No se pudo leer tu historial de práctica; revisa PostgreSQL."));
        for (LogrosService.Logro logro : logros) {
            VBox tarjeta = new VBox(6);
            tarjeta.getStyleClass().addAll("card", "logro", logro.desbloqueado() ? "logro-ok" : "logro-bloqueado");
            tarjeta.setPrefWidth(250);
            Label titulo = new Label((logro.desbloqueado() ? "★ " : "") + logro.titulo());
            titulo.getStyleClass().add("section-title");
            Label descripcion = new Label(logro.descripcion());
            descripcion.setWrapText(true);
            ProgressBar barra = new ProgressBar(logro.progreso());
            barra.setMaxWidth(Double.MAX_VALUE);
            Label detalle = new Label(logro.desbloqueado() ? "¡Desbloqueado!" : logro.detalle());
            detalle.getStyleClass().add("subtitle");
            tarjeta.getChildren().addAll(titulo, descripcion, barra, detalle);
            panelLogros.getChildren().add(tarjeta);
        }
        if (ni.edu.uam.algomentor.util.FondoVidrio.hayCaptura()) {
            Platform.runLater(() -> ni.edu.uam.algomentor.util.VidrioEsmerilado.decorar(panelLogros));
        }
        Animaciones.entrarEscalonado(panelLogros.getChildren(), 16);
    }
}
