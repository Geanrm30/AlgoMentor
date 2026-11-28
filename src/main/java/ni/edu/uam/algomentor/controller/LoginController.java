package ni.edu.uam.algomentor.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.service.AutenticacionService;
import ni.edu.uam.algomentor.util.Animaciones;
import ni.edu.uam.algomentor.util.CampoFlotante;
import ni.edu.uam.algomentor.util.Fondo;
import ni.edu.uam.algomentor.util.FondoVidrio;
import ni.edu.uam.algomentor.util.VidrioEsmerilado;
import ni.edu.uam.algomentor.util.FormularioLiquido;
import ni.edu.uam.algomentor.util.LogoMarca;
import ni.edu.uam.algomentor.util.MostrarContrasena;
import ni.edu.uam.algomentor.util.TarjetaCristal;
import ni.edu.uam.algomentor.util.UsuarioRecordado;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inicio de sesión y registro. Una pastilla se abre como líquido en los campos del formulario; al enviar, los campos se
 * funden en la pastilla, que gira mientras se verifica y termina con un check. Cambiar entre iniciar sesión y crear cuenta
 * encoge la tarjeta de vidrio a un mosaico con el logo y la expande con el otro formulario.
 */
public class LoginController {

    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());
    private static final double ANCHO = 364;
    private static final double ALTO_CAMPO = 54;
    private static final double LADO_MOSAICO = 150;
    private static final long MINIMO_GIRO_MS = 800;
    /** Rebote con sobreimpulso: sale rápido, se pasa un poco y se asienta. */
    private static final Interpolator REBOTE = new Interpolator() {
        @Override
        protected double curve(double t) {
            double u = t - 1;
            return 1 + 2.70158 * u * u * u + 1.70158 * u * u;
        }
    };

    @FXML private StackPane raiz;
    @FXML private StackPane tarjeta;
    @FXML private VBox contenido;
    @FXML private HBox cabecera;
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private VBox contenedorFormulario;
    @FXML private Button btnModo;
    @FXML private Label lblEstado;

    private final TextField txtUsuario = new TextField();
    private final PasswordField txtContrasena = new PasswordField();
    private final CheckBox chkMostrarContrasena = new CheckBox("Mostrar contraseña");
    private final CheckBox chkRecordarUsuario = new CheckBox("Recordar mi usuario en este equipo");
    private final TextField txtNuevoUsuario = new TextField();
    private final PasswordField txtNuevaContrasena = new PasswordField();
    private final PasswordField txtConfirmarContrasena = new PasswordField();
    private final CheckBox chkMostrarNuevaContrasena = new CheckBox("Mostrar contraseñas");
    private final Button pastilla = new Button();

    private final UsuarioRecordado usuarioRecordado = new UsuarioRecordado();
    private Consumer<Perfil> alAutenticar = perfil -> { };
    private FormularioLiquido formulario;
    private List<Region> filasInicio;
    private List<Region> filasRegistro;
    private String recordado;
    private boolean registro;
    private boolean ocupado;
    /** 1: la tarjeta mantiene fija su parte superior; 0: queda centrada (el mosaico del cambio de modo). */
    private final DoubleProperty ancla = new SimpleDoubleProperty(1);

    @FXML
    private void initialize() {
        Fondo.instalarLogin(raiz, false);
        TarjetaCristal.instalar(tarjeta, 34);
        if (FondoVidrio.hayCaptura()) {
            VidrioEsmerilado.instalar(tarjeta, 16, (ancho, alto) -> {
                Polygon forma = new Polygon();
                for (double[] punto : TarjetaCristal.puntos(ancho, alto, 34)) {
                    forma.getPoints().addAll(punto[0], punto[1]);
                }
                return forma;
            });
        }
        cabecera.getChildren().addAll(LogoMarca.crear(28), etiquetaMarca());

        formulario = new FormularioLiquido(ANCHO, pastilla);
        contenedorFormulario.getChildren().add(formulario);
        construirFilas();
        formulario.definir(filasInicio);
        anclarTarjeta();

        recordado = usuarioRecordado.leer().orElse(null);
        if (recordado != null) {
            txtUsuario.setText(recordado);
            chkRecordarUsuario.setSelected(true);
        }
        pastilla.setOnAction(evento -> enviar());
        btnModo.setOnAction(evento -> cambiarModo());
        aplicarTextos();
        pastilla.setText(textoReposo());
        Platform.runLater(() -> Animaciones.entrarTarjetaLogin(tarjeta));
    }

    /**
     * La tarjeta crece al abrirse el formulario; si estuviera centrada, saltaría hacia arriba. Se envuelve en un
     * contenedor que se desplaza la mitad de lo que le falta por crecer, y así la parte superior no se mueve.
     */
    private void anclarTarjeta() {
        StackPane anclaje = new StackPane();
        anclaje.setPickOnBounds(false);
        int posicion = raiz.getChildren().indexOf(tarjeta);
        raiz.getChildren().remove(tarjeta);
        anclaje.getChildren().add(tarjeta);
        raiz.getChildren().add(posicion, anclaje);
        anclaje.translateYProperty().bind(Bindings.createDoubleBinding(
                () -> ancla.get() * (formulario.getPrefHeight() - formulario.alturaTotalProperty().get()) / 2,
                ancla, formulario.prefHeightProperty(), formulario.alturaTotalProperty()));
    }

    public void setAlAutenticar(Consumer<Perfil> accion) {
        alAutenticar = accion;
    }

    private static Label etiquetaMarca() {
        Label marca = new Label("AlgoMentor");
        marca.getStyleClass().add("brand");
        return marca;
    }

    // ---------- Construcción del formulario ----------

    private void construirFilas() {
        CampoFlotante cUsuario = new CampoFlotante("Usuario", txtUsuario, ANCHO, ALTO_CAMPO);
        CampoFlotante cContrasena = new CampoFlotante("Contraseña", txtContrasena, ANCHO, ALTO_CAMPO);
        txtUsuario.setOnAction(evento -> txtContrasena.requestFocus());
        txtContrasena.setOnAction(evento -> enviar());
        MostrarContrasena.vincular(chkMostrarContrasena, txtContrasena);
        filasInicio = List.of(cUsuario, cContrasena, extras(62, chkMostrarContrasena, chkRecordarUsuario));

        double mitad = (ANCHO - 10) / 2;
        CampoFlotante cNuevo = new CampoFlotante("Usuario (3 a 40 caracteres)", txtNuevoUsuario, ANCHO, ALTO_CAMPO);
        CampoFlotante cNueva = new CampoFlotante("Contraseña (mín. 8)", txtNuevaContrasena, mitad, ALTO_CAMPO);
        CampoFlotante cConfirmar = new CampoFlotante("Confirmar", txtConfirmarContrasena, mitad, ALTO_CAMPO);
        txtNuevoUsuario.setOnAction(evento -> txtNuevaContrasena.requestFocus());
        txtNuevaContrasena.setOnAction(evento -> txtConfirmarContrasena.requestFocus());
        txtConfirmarContrasena.setOnAction(evento -> enviar());
        MostrarContrasena.vincular(chkMostrarNuevaContrasena, txtNuevaContrasena, txtConfirmarContrasena);
        HBox dobles = new HBox(10, cNueva, cConfirmar);
        fijarTamano(dobles, ANCHO, ALTO_CAMPO);
        filasRegistro = List.of(cNuevo, dobles, extras(30, chkMostrarNuevaContrasena));
    }

    private static VBox extras(double alto, CheckBox... casillas) {
        VBox caja = new VBox(8, casillas);
        fijarTamano(caja, ANCHO, alto);
        return caja;
    }

    private static void fijarTamano(Region region, double ancho, double alto) {
        region.setMinSize(ancho, alto);
        region.setPrefSize(ancho, alto);
        region.setMaxSize(ancho, alto);
    }

    // ---------- Textos de cada modo ----------

    private String textoReposo() {
        if (registro) {
            return "Crear cuenta";
        }
        return recordado != null ? "Continuar como " + recordado : "Iniciar sesión";
    }

    private String textoAbierto() {
        return registro ? "Crear cuenta" : "Entrar";
    }

    private void aplicarTextos() {
        lblTitulo.setText(registro ? "Crea tu cuenta" : "Bienvenido de nuevo");
        lblSubtitulo.setText(registro ? "Guarda tu progreso con un perfil propio y privado."
                : "Ingresa tus credenciales para acceder a tu espacio de aprendizaje.");
        btnModo.setText(registro ? "¿Ya tienes cuenta? Iniciar sesión" : "¿Primera vez? Crear una cuenta");
        lblEstado.setText(registro ? "Usuario de 3 a 40 caracteres y contraseña de al menos 8."
                : "Tu cuenta tiene un perfil propio y privado.");
    }

    // ---------- Flujo principal ----------

    private void enviar() {
        if (ocupado) {
            return;
        }
        if (!formulario.abierto()) {
            abrirFormulario();
            return;
        }
        if (registro) {
            enviarRegistro();
        } else {
            enviarInicio();
        }
    }

    private void abrirFormulario() {
        pastilla.setText(textoAbierto());
        formulario.abrir(this::enfocarInicial);
    }

    private void enfocarInicial() {
        if (!registro && recordado != null && txtContrasena.isVisible()) {
            txtContrasena.requestFocus();
        } else {
            formulario.enfocarPrimero();
        }
    }

    private void enviarInicio() {
        String usuario = txtUsuario.getText();
        boolean recordar = chkRecordarUsuario.isSelected();
        char[] contrasena = txtContrasena.getText().toCharArray();
        txtContrasena.clear();
        ejecutarAutenticacion("Verificando credenciales…", () -> {
            try {
                Optional<Perfil> perfil = new AutenticacionService().iniciarSesion(usuario, contrasena);
                if (perfil.isEmpty()) {
                    throw new CredencialesInvalidasException();
                }
                if (recordar) {
                    usuarioRecordado.guardar(usuario);
                    recordado = usuario;
                } else {
                    usuarioRecordado.olvidar();
                    recordado = null;
                }
                return perfil.get();
            } finally {
                Arrays.fill(contrasena, '\0');
            }
        });
    }

    private void enviarRegistro() {
        String usuario = txtNuevoUsuario.getText();
        char[] contrasena = txtNuevaContrasena.getText().toCharArray();
        char[] confirmacion = txtConfirmarContrasena.getText().toCharArray();
        if (!Arrays.equals(contrasena, confirmacion)) {
            lblEstado.setText("Las contraseñas no coinciden.");
            Animaciones.sacudir(tarjeta);
            Arrays.fill(contrasena, '\0');
            Arrays.fill(confirmacion, '\0');
            return;
        }
        txtNuevaContrasena.clear();
        txtConfirmarContrasena.clear();
        ejecutarAutenticacion("Creando cuenta…", () -> {
            try {
                return new AutenticacionService().registrar(usuario, contrasena);
            } finally {
                Arrays.fill(contrasena, '\0');
                Arrays.fill(confirmacion, '\0');
            }
        });
    }

    private void ejecutarAutenticacion(String mensajeInicial, AccionAutenticacion accion) {
        ocupado = true;
        lblEstado.setText(mensajeInicial);
        boolean[] animacionLista = {false};
        Runnable[] resultado = {null};
        long[] inicioGiro = {0};
        formulario.cerrar(() -> {
            formulario.pastillaOcupada();
            inicioGiro[0] = System.currentTimeMillis();
            animacionLista[0] = true;
            if (resultado[0] != null) {
                mostrarTrasGiro(resultado[0], inicioGiro[0]);
            }
        });
        Thread hilo = new Thread(() -> {
            Runnable fin;
            try {
                Perfil perfil = accion.ejecutar();
                fin = () -> exito(perfil);
            } catch (CredencialesInvalidasException e) {
                fin = () -> fallo("Usuario o contraseña incorrectos.");
            } catch (IllegalArgumentException e) {
                String mensaje = e.getMessage();
                fin = () -> fallo(mensaje);
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "No se pudo completar la autenticación", e);
                fin = () -> fallo("No se pudo acceder a las cuentas. Revisa PostgreSQL y ejecuta schema.sql.");
            }
            Runnable terminado = fin;
            Platform.runLater(() -> {
                if (animacionLista[0]) {
                    mostrarTrasGiro(terminado, inicioGiro[0]);
                } else {
                    resultado[0] = terminado;
                }
            });
        }, "algomentor-autenticacion");
        hilo.setDaemon(true);
        hilo.start();
    }

    /** El giro de la pastilla se ve al menos un instante, aunque la verificación sea casi inmediata. */
    private void mostrarTrasGiro(Runnable accion, long inicio) {
        long restante = MINIMO_GIRO_MS - (System.currentTimeMillis() - inicio);
        if (restante <= 0) {
            accion.run();
            return;
        }
        PauseTransition espera = new PauseTransition(Duration.millis(restante));
        espera.setOnFinished(evento -> accion.run());
        espera.play();
    }

    private void exito(Perfil perfil) {
        lblEstado.setText("");
        formulario.pastillaListo("Bienvenido, " + perfil.getNombre(), () -> alAutenticar.accept(perfil));
    }

    private void fallo(String mensaje) {
        ocupado = false;
        lblEstado.setText(mensaje);
        Animaciones.sacudir(tarjeta);
        formulario.pastillaNormal(textoAbierto());
        formulario.abrir(this::enfocarInicial);
    }

    // ---------- Cambio de modo con transición de la tarjeta ----------

    private void cambiarModo() {
        if (ocupado) {
            return;
        }
        ocupado = true;
        Runnable encoger = this::encogerTarjeta;
        if (formulario.abierto()) {
            formulario.cerrar(encoger);
        } else {
            encoger.run();
        }
    }

    private void fijarTarjeta(double ancho, double alto) {
        tarjeta.setMinSize(ancho, alto);
        tarjeta.setPrefSize(ancho, alto);
        tarjeta.setMaxSize(ancho, alto);
    }

    private void liberarTarjeta() {
        tarjeta.setMinWidth(Region.USE_COMPUTED_SIZE);
        tarjeta.setPrefWidth(420);
        tarjeta.setMaxWidth(420);
        tarjeta.setMinHeight(Region.USE_COMPUTED_SIZE);
        tarjeta.setPrefHeight(Region.USE_COMPUTED_SIZE);
        tarjeta.setMaxHeight(Region.USE_PREF_SIZE);
    }

    /** La tarjeta se encoge a un mosaico con el logo; allí se cambia de formulario y luego se expande. */
    private void encogerTarjeta() {
        double ancho = tarjeta.getWidth();
        double alto = tarjeta.getHeight();
        fijarTarjeta(ancho, alto);
        StackPane mosaico = crearMosaico();
        tarjeta.getChildren().add(mosaico);

        FadeTransition ocultar = new FadeTransition(Duration.millis(200), contenido);
        ocultar.setToValue(0);
        Timeline encoger = new Timeline(new KeyFrame(Duration.millis(440),
                new KeyValue(ancla, 0, Interpolator.EASE_BOTH),
                new KeyValue(tarjeta.minWidthProperty(), LADO_MOSAICO, Interpolator.EASE_BOTH),
                new KeyValue(tarjeta.prefWidthProperty(), LADO_MOSAICO, Interpolator.EASE_BOTH),
                new KeyValue(tarjeta.maxWidthProperty(), LADO_MOSAICO, Interpolator.EASE_BOTH),
                new KeyValue(tarjeta.minHeightProperty(), LADO_MOSAICO, Interpolator.EASE_BOTH),
                new KeyValue(tarjeta.prefHeightProperty(), LADO_MOSAICO, Interpolator.EASE_BOTH),
                new KeyValue(tarjeta.maxHeightProperty(), LADO_MOSAICO, Interpolator.EASE_BOTH)));
        SequentialTransition ensamblar = animarMosaico(mosaico);
        PauseTransition cambio = new PauseTransition(Duration.millis(1)); // el cambio ocurre con la tarjeta ya encogida
        cambio.setOnFinished(evento -> {
            registro = !registro;
            formulario.definir(registro ? filasRegistro : filasInicio);
            aplicarTextos();
            pastilla.setText(textoReposo());
            expandirTarjeta(mosaico);
        });
        new SequentialTransition(new ParallelTransition(ocultar, encoger), ensamblar, cambio).play();
    }

    private void expandirTarjeta(StackPane mosaico) {
        double[] destino = tamanoNatural();
        FadeTransition quitarMosaico = new FadeTransition(Duration.millis(220), mosaico);
        quitarMosaico.setToValue(0);
        Timeline expandir = new Timeline(new KeyFrame(Duration.millis(520),
                new KeyValue(ancla, 1, Interpolator.EASE_BOTH),
                new KeyValue(tarjeta.minWidthProperty(), destino[0], Interpolator.EASE_BOTH),
                new KeyValue(tarjeta.prefWidthProperty(), destino[0], Interpolator.EASE_BOTH),
                new KeyValue(tarjeta.maxWidthProperty(), destino[0], Interpolator.EASE_BOTH),
                new KeyValue(tarjeta.minHeightProperty(), destino[1], Interpolator.EASE_BOTH),
                new KeyValue(tarjeta.prefHeightProperty(), destino[1], Interpolator.EASE_BOTH),
                new KeyValue(tarjeta.maxHeightProperty(), destino[1], Interpolator.EASE_BOTH)));
        FadeTransition mostrar = new FadeTransition(Duration.millis(280), contenido);
        mostrar.setToValue(1);
        mostrar.setDelay(Duration.millis(260));
        ParallelTransition salida = new ParallelTransition(quitarMosaico, expandir, mostrar);
        salida.setOnFinished(evento -> {
            tarjeta.getChildren().remove(mosaico);
            liberarTarjeta();
            ocupado = false;
            abrirFormulario();
        });
        salida.play();
    }

    /** Tamaño que tendría la tarjeta con el formulario cerrado, medido sin mostrarlo. */
    private double[] tamanoNatural() {
        liberarTarjeta();
        tarjeta.applyCss();
        double ancho = tarjeta.prefWidth(-1);
        double alto = tarjeta.prefHeight(ancho);
        fijarTarjeta(LADO_MOSAICO, LADO_MOSAICO);
        return new double[]{ancho, alto};
    }

    private StackPane crearMosaico() {
        Group marca = LogoMarca.crear(56);
        Circle anillo1 = anillo();
        Circle anillo2 = anillo();
        StackPane mosaico = new StackPane(anillo1, anillo2, marca);
        mosaico.setMouseTransparent(true);
        mosaico.setOpacity(0);
        mosaico.getProperties().put("anillos", List.of(anillo1, anillo2));
        mosaico.getProperties().put("marca", marca);
        return mosaico;
    }

    private static Circle anillo() {
        Circle anillo = new Circle(34);
        anillo.setFill(Color.TRANSPARENT);
        anillo.setStroke(Color.web("#67e8f9"));
        anillo.setStrokeWidth(2);
        anillo.setOpacity(0);
        return anillo;
    }

    /** Las dos piezas del logo se ensamblan y un anillo de luz late hacia fuera, como en una carga. */
    @SuppressWarnings("unchecked")
    private SequentialTransition animarMosaico(StackPane mosaico) {
        Group marca = (Group) mosaico.getProperties().get("marca");
        List<Polygon> piezas = (List<Polygon>) marca.getProperties().get(LogoMarca.CLAVE_PIEZAS);
        List<Circle> anillos = (List<Circle>) mosaico.getProperties().get("anillos");
        piezas.get(0).setTranslateY(-16);
        piezas.get(1).setTranslateY(16);
        marca.setRotate(-50);
        marca.setScaleX(0.5);
        marca.setScaleY(0.5);

        FadeTransition aparecer = new FadeTransition(Duration.millis(180), mosaico);
        aparecer.setToValue(1);
        Timeline ensamblar = new Timeline(new KeyFrame(Duration.millis(560),
                new KeyValue(marca.rotateProperty(), 0, REBOTE),
                new KeyValue(marca.scaleXProperty(), 1, REBOTE),
                new KeyValue(marca.scaleYProperty(), 1, REBOTE),
                new KeyValue(piezas.get(0).translateYProperty(), 0, REBOTE),
                new KeyValue(piezas.get(1).translateYProperty(), 0, REBOTE)));
        ParallelTransition pulsos = new ParallelTransition();
        for (int i = 0; i < anillos.size(); i++) {
            Circle anillo = anillos.get(i);
            ScaleTransition crecer = new ScaleTransition(Duration.millis(600), anillo);
            crecer.setFromX(0.7);
            crecer.setFromY(0.7);
            crecer.setToX(2.0);
            crecer.setToY(2.0);
            FadeTransition desvanecer = new FadeTransition(Duration.millis(600), anillo);
            desvanecer.setFromValue(0.85);
            desvanecer.setToValue(0);
            ParallelTransition pulso = new ParallelTransition(crecer, desvanecer);
            pulso.setDelay(Duration.millis(120 + i * 260));
            pulsos.getChildren().add(pulso);
        }
        TranslateTransition asentar = new TranslateTransition(Duration.millis(1), mosaico);
        return new SequentialTransition(aparecer, new ParallelTransition(ensamblar, pulsos), asentar);
    }

    @FunctionalInterface
    private interface AccionAutenticacion {
        Perfil ejecutar();
    }

    private static final class CredencialesInvalidasException extends RuntimeException { }
}
