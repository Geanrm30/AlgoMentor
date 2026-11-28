package ni.edu.uam.algomentor;

import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Labeled;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import ni.edu.uam.algomentor.api.EjerciciosApiServer;
import ni.edu.uam.algomentor.controller.CambioContrasenaDialogo;
import ni.edu.uam.algomentor.controller.ElegirPerfilDialogo;
import ni.edu.uam.algomentor.controller.IntercambioEjerciciosController;
import ni.edu.uam.algomentor.controller.LoginController;
import ni.edu.uam.algomentor.controller.ProgresoController;
import ni.edu.uam.algomentor.controller.ResumenController;
import ni.edu.uam.algomentor.model.Perfil;
import ni.edu.uam.algomentor.service.AutorizacionService;
import ni.edu.uam.algomentor.service.CatalogoPersistidoService;
import ni.edu.uam.algomentor.service.ResumenAdministracionService;
import ni.edu.uam.algomentor.util.Actualizable;
import ni.edu.uam.algomentor.util.BarraLateral;
import ni.edu.uam.algomentor.util.Fondo;
import ni.edu.uam.algomentor.util.Iconos;
import ni.edu.uam.algomentor.util.Animaciones;
import ni.edu.uam.algomentor.util.ConfiguracionLogs;
import ni.edu.uam.algomentor.util.HibernateUtil;
import ni.edu.uam.algomentor.util.SesionUsuario;
import ni.edu.uam.algomentor.util.Tema;
import ni.edu.uam.algomentor.util.Tutorial;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class MainApp extends Application {

    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());
    private Stage stage;
    private StackPane contenido;
    private static final String CLAVE_TEMA_CLARO = "tema.claro";
    private static final String CLAVE_TUTORIAL_VISTO = "tutorial.visto";
    private static final String CLAVE_TAMANO_LETRA = "letra.tamano";
    private static final String CLAVE_ALTO_CONTRASTE = "alto.contraste";
    private static final String[] NOMBRES_LETRA = {"Pequeño", "Normal", "Grande", "Muy grande"};
    private static final double[] TAMANOS_LETRA = {12, 13.5, 16, 18.5};
    private int tamanoLetra = Math.max(0, Math.min(3, leerEntero(CLAVE_TAMANO_LETRA, 1)));
    private boolean altoContraste = leerEntero(CLAVE_ALTO_CONTRASTE, 0) == 1;
    private BorderPane raizActual;
    private BarraLateral barraLateral;
    private static final String CLAVE_NAV_CONTRAIDA = "nav.contraida";
    private StackPane envoltura;
    private MenuBar barraMenus;
    private Tutorial tutorial;
    private boolean temaClaro = leerTemaClaro();
    private static final int SEGUNDOS_ACTUALIZACION_AUTOMATICA = 10;
    private final Map<String, Vista> vistas = new HashMap<>();
    private String vistaActual;
    private Timeline actualizacionAutomatica;
    private boolean administrador;
    private CheckMenuItem itemTema;
    private ContextMenu menuContextual;

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        try {
            CatalogoPersistidoService.completarCatalogo();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo sincronizar el catálogo de algoritmos", e);
        }

        Scene scene = new Scene(cargarLogin(), 900, 640);
        URL stylesheet = Tema.hojaEstilos();
        if (stylesheet == null) {
            throw new IOException("No se encontró la hoja de estilos de AlgoMentor");
        }
        scene.getStylesheets().add(stylesheet.toExternalForm());
        Animaciones.instalarEfectosBoton(scene);
        Animaciones.instalarEfectosTarjeta(scene);
        stage.setTitle("AlgoMentor");
        stage.setMinWidth(760);
        stage.setMinHeight(560);
        stage.setMaximized(true);
        stage.setFullScreenExitHint("Pulsa F11 o Esc para salir de pantalla completa.");
        stage.focusedProperty().addListener((obs, antes, ahora) -> {
            if (ahora) {
                actualizarVistaActual();
            } else {
                ocultarMenuContextual();
            }
        });
        // Cualquier clic en la ventana cierra el menú contextual, no solo los clics sobre sus opciones.
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, evento -> ocultarMenuContextual());
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            EjerciciosApiServer.detener();
            HibernateUtil.shutdown();
        });
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
        if (temaClaro) {
            login.getStyleClass().addAll("app-root", "light-theme");
            if (login instanceof StackPane panel) {
                Fondo.instalarLogin(panel, true);
            }
        }
        return login;
    }

    private void mostrarAplicacion(Perfil perfil) {
        SesionUsuario.iniciar(perfil);
        vistas.clear();
        contenido = new StackPane();
        contenido.getStyleClass().add("app-contenido");

        BorderPane raiz = new BorderPane();
        raiz.getStyleClass().add("app-root");
        if (temaClaro) {
            raiz.getStyleClass().add("light-theme");
        }
        administrador = esAdministrador(perfil);
        raiz.setTop(crearBarraSuperior(perfil, raiz, administrador));
        raiz.setCenter(contenido);
        barraLateral = crearBarraLateral();
        raiz.setLeft(barraLateral);
        BorderPane.setMargin(barraLateral, new Insets(14, 0, 14, 14));
        BorderPane.setMargin(raiz.getTop(), new Insets(0));
        instalarMenuContextual();

        envoltura = new StackPane(raiz);
        Fondo.instalar(envoltura, temaClaro, false);
        raizActual = raiz;
        instalarAtajos(stage.getScene());
        aplicarAccesibilidad();
        stage.getScene().setRoot(envoltura);
        Animaciones.fundirEntrada(raiz);
        Animaciones.instalarEfectosBoton(stage.getScene());
        precargarIntercambio();
        if (administrador) {
            mostrarResumenGeneral();
        } else {
            mostrar("progreso", "progreso.fxml", true);
        }
        iniciarActualizacionAutomatica();
        if (!leerTutorialVisto()) {
            PauseTransition espera = new PauseTransition(Duration.millis(1100));
            espera.setOnFinished(evento -> iniciarTutorial());
            espera.play();
        }
    }

    private void mostrarResumenGeneral() {
        ResumenController resumen = mostrar("resumen", "resumen.fxml", true);
        if (resumen != null) {
            resumen.setAlVerProgreso(this::verProgresoDe);
            resumen.setAlExportarInforme(this::exportarInformeDe);
        }
    }

    private void exportarInformeDe(Perfil perfil) {
        ProgresoController progreso = verProgresoDe(perfil);
        if (progreso != null) {
            Platform.runLater(progreso::exportarInforme);
        }
    }

    /** Un usuario exporta su propio informe; un administrador elige primero de qué perfil. */
    private void exportarInformeConEleccion() {
        Perfil propio = SesionUsuario.obtenerPerfil();
        if (!administrador) {
            exportarInformeDe(propio);
            return;
        }
        Thread hilo = new Thread(() -> {
            try {
                List<Perfil> perfiles = new ResumenAdministracionService().listarPerfiles(propio.getId());
                Platform.runLater(() -> ElegirPerfilDialogo.mostrar(stage, perfiles, propio)
                        .ifPresent(this::exportarInformeDe));
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, "No se pudo listar los perfiles", e);
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR,
                        "No se pudo cargar la lista de perfiles. Revisa PostgreSQL.").showAndWait());
            }
        }, "algomentor-lista-perfiles");
        hilo.setDaemon(true);
        hilo.start();
    }

    /** Panel con los informes globales; cada informe vuelve a comprobar el rol en su servicio. */
    private void mostrarInformes() {
        if (administrador) {
            mostrar("informes", "informes.fxml", true);
        }
    }

    private void ocultarMenuContextual() {
        if (menuContextual != null && menuContextual.isShowing()) {
            menuContextual.hide();
        }
    }

    /** Clic derecho en cualquier zona libre: salto rápido a las secciones y a las opciones de vista. */
    private void instalarMenuContextual() {
        contenido.setOnContextMenuRequested(evento -> {
            ocultarMenuContextual();
            menuContextual = new ContextMenu();
            menuContextual.setAutoHide(true);
            menuContextual.getItems().add(elemento("Mi progreso", () -> mostrar("progreso", "progreso.fxml", true)));
            if (administrador) {
                menuContextual.getItems().add(elemento("Resumen general", this::mostrarResumenGeneral));
                menuContextual.getItems().add(elemento("Informes", this::mostrarInformes));
            }
            menuContextual.getItems().addAll(
                    elemento("Simular", () -> mostrar("simulador", "simulador.fxml", false)),
                    elemento("Práctica", () -> mostrar("practica", "practica.fxml", true)),
                    elemento("Comparar algoritmos", () -> mostrar("comparador", "comparador.fxml", false)),
                    elemento("Guía de algoritmos", () -> mostrar("guia", "guia-algoritmos.fxml", false)),
                    new SeparatorMenuItem(),
                    elemento("Exportar informe de progreso (PDF)…", this::exportarInformeConEleccion),
                    elemento("Cambiar tema", () -> {
                        if (itemTema != null) {
                            itemTema.setSelected(!itemTema.isSelected());
                            itemTema.fire();
                        }
                    }),
                    elemento("Pantalla completa", () -> stage.setFullScreen(!stage.isFullScreen())));
            menuContextual.show(contenido, evento.getScreenX(), evento.getScreenY());
            evento.consume();
        });
    }

    /** Vista de administrador: el controlador y el servicio comprueban el rol antes de mostrar nada. */
    private ProgresoController verProgresoDe(Perfil perfil) {
        ProgresoController progreso = mostrar("progreso-perfil", "progreso.fxml", true);
        if (progreso != null) {
            progreso.setAlVolver(this::mostrarResumenGeneral);
            progreso.mostrarPerfil(perfil);
        }
        return progreso;
    }

    /** Solo decide qué menús se muestran; cada servicio con datos globales vuelve a comprobar el rol. */
    private static boolean esAdministrador(Perfil perfil) {
        try {
            return new AutorizacionService().esAdministrador(perfil.getId());
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo consultar el rol de la cuenta", e);
            return false;
        }
    }

    private void iniciarActualizacionAutomatica() {
        detenerActualizacionAutomatica();
        actualizacionAutomatica = new Timeline(new KeyFrame(
                Duration.seconds(SEGUNDOS_ACTUALIZACION_AUTOMATICA), event -> actualizarVistaActual()));
        actualizacionAutomatica.setCycleCount(Timeline.INDEFINITE);
        actualizacionAutomatica.play();
    }

    private void detenerActualizacionAutomatica() {
        if (actualizacionAutomatica != null) {
            actualizacionAutomatica.stop();
            actualizacionAutomatica = null;
        }
    }

    /** Pide a la sección visible que se actualice sola; las demás se recargan al abrirlas. */
    private void actualizarVistaActual() {
        if (vistaActual == null || !SesionUsuario.haySesion()) {
            return;
        }
        Vista vista = vistas.get(vistaActual);
        if (vista != null && vista.controlador() instanceof Actualizable actualizable) {
            actualizable.actualizarAutomaticamente();
        }
    }

    private HBox crearBarraSuperior(Perfil perfil, BorderPane raiz, boolean administrador) {
        MenuBar menu = new MenuBar(crearMenuArchivo(), crearMenuVer(raiz), crearMenuAyuda());
        barraMenus = menu;
        HBox.setHgrow(menu, Priority.ALWAYS);

        Region avatar = new Region();
        avatar.getStyleClass().add("avatar");
        avatar.setMinSize(28, 28);
        avatar.setPrefSize(28, 28);
        avatar.setMaxSize(28, 28);
        Label usuario = new Label(perfil.getNombre() + (administrador ? " · administrador" : ""));
        usuario.getStyleClass().add("sesion-activa");
        HBox chip = new HBox(8, avatar, usuario);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.getStyleClass().add("chip-usuario");

        HBox barra = new HBox(14, menu, chip);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("barra-superior");
        return barra;
    }

    private record Atajo(String combinacion, Runnable accion) { }

    private BarraLateral crearBarraLateral() {
        List<BarraLateral.Grupo> grupos = new java.util.ArrayList<>();
        grupos.add(new BarraLateral.Grupo("Mi avance", List.of(
                nav("progreso", "Mi progreso", "barras", "Ctrl+3", () -> mostrar("progreso", "progreso.fxml", true)),
                nav("retos", "Retos y logros", "estrella", "Ctrl+Shift+R", () -> mostrar("retos", "retos.fxml", true)))));
        grupos.add(new BarraLateral.Grupo("Aprender", List.of(
                nav("simulador", "Simular", "play", "Ctrl+1", () -> mostrar("simulador", "simulador.fxml", false)),
                nav("practica", "Práctica", "lapiz", "Ctrl+2", () -> mostrar("practica", "practica.fxml", true)),
                nav("guia", "Guía de algoritmos", "libro", "Ctrl+8", () -> mostrar("guia", "guia-algoritmos.fxml", false)),
                nav("otros", "Búsqueda y grafos", "grafo", "Ctrl+Shift+8", () -> mostrar("otros", "otros-algoritmos.fxml", false)))));
        grupos.add(new BarraLateral.Grupo("Herramientas", List.of(
                nav("comparador", "Comparar", "comparar", "Ctrl+5", () -> mostrar("comparador", "comparador.fxml", false)),
                nav("verificar", "Comprobar sistema", "check", "Ctrl+6", () -> mostrar("verificar", "verificar-sistema-api.fxml", false)),
                nav("biblioteca", "Biblioteca", "lista", "Ctrl+7", () -> mostrar("biblioteca", "gestion-contenido.fxml", true)))));
        if (administrador) {
            grupos.add(new BarraLateral.Grupo("Administración", List.of(
                    nav("resumen", "Resumen general", "tendencia", "Ctrl+0", this::mostrarResumenGeneral),
                    nav("informes", "Informes", "informe", "Ctrl+9", this::mostrarInformes))));
        }
        boolean contraida = leerEntero(CLAVE_NAV_CONTRAIDA, 0) == 1;
        return new BarraLateral(grupos, contraida, c -> guardarEntero(CLAVE_NAV_CONTRAIDA, c ? 1 : 0));
    }

    private static BarraLateral.Elemento nav(String clave, String texto, String icono, String atajo, Runnable accion) {
        return new BarraLateral.Elemento(clave, texto, icono, atajo, accion);
    }

    /** Los atajos de teclado de las secciones siguen funcionando aunque el menú superior ya no las liste. */
    private void instalarAtajos(Scene escena) {
        escena.getAccelerators().clear();
        List<Atajo> atajos = new java.util.ArrayList<>(List.of(
                new Atajo("Shortcut+1", () -> irA("simulador")),
                new Atajo("Shortcut+2", () -> irA("practica")),
                new Atajo("Shortcut+3", () -> irA("progreso")),
                new Atajo("Shortcut+5", () -> irA("comparador")),
                new Atajo("Shortcut+6", () -> irA("verificar")),
                new Atajo("Shortcut+7", () -> irA("biblioteca")),
                new Atajo("Shortcut+8", () -> irA("guia")),
                new Atajo("Shortcut+Shift+8", () -> irA("otros")),
                new Atajo("Shortcut+Shift+R", () -> irA("retos"))));
        if (administrador) {
            atajos.add(new Atajo("Shortcut+0", () -> irA("resumen")));
            atajos.add(new Atajo("Shortcut+9", () -> irA("informes")));
        }
        for (Atajo atajo : atajos) {
            escena.getAccelerators().put(KeyCombination.keyCombination(atajo.combinacion()), atajo.accion());
        }
    }

    /** Equivale a pulsar la sección en la barra lateral. */
    private void irA(String clave) {
        if (barraLateral != null && barraLateral.contiene(clave)) {
            barraLateral.boton(clave).fire();
        }
    }

    private Menu crearMenuArchivo() {
        MenuItem exportarEjercicios = elemento("Exportar ejercicios…", "Shortcut+Shift+E", () -> {
            IntercambioEjerciciosController c = mostrar("intercambio", "intercambio-ejercicios.fxml", false);
            if (c != null) {
                Platform.runLater(c::exportarEjercicios);
            }
        });
        MenuItem importarEjercicios = elemento("Importar ejercicios…", "Shortcut+I", () -> {
            IntercambioEjerciciosController c = mostrar("intercambio", "intercambio-ejercicios.fxml", false);
            if (c != null) {
                Platform.runLater(c::importarEjercicios);
            }
        });
        MenuItem exportarInforme = elemento("Exportar informe de progreso (PDF)…", "Shortcut+E",
                this::exportarInformeConEleccion);
        MenuItem cambiarContrasena = elemento("Cambiar contraseña…", "Shortcut+Shift+P", () ->
                CambioContrasenaDialogo.mostrar(stage, SesionUsuario.obtenerPerfil().getId()));
        MenuItem cerrarSesion = elemento("Cerrar sesión", "Shortcut+Shift+L", this::cerrarSesion);
        MenuItem salir = elemento("Salir", "Shortcut+Q", () ->
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
        return new Menu("Archivo", null, exportarEjercicios, importarEjercicios, exportarInforme,
                new SeparatorMenuItem(), cambiarContrasena, cerrarSesion, salir);
    }

    private Menu crearMenuVer(BorderPane raiz) {
        CheckMenuItem tema = new CheckMenuItem("Tema claro");
        itemTema = tema;
        tema.setSelected(temaClaro);
        tema.setAccelerator(KeyCombination.keyCombination("Shortcut+T"));
        tema.setOnAction(event -> {
            temaClaro = tema.isSelected();
            guardarTemaClaro(temaClaro);
            raiz.getStyleClass().remove("light-theme");
            if (temaClaro) {
                raiz.getStyleClass().add("light-theme");
            }
            Fondo.instalar(envoltura, temaClaro, false);
        });
        CheckMenuItem pantallaCompleta = new CheckMenuItem("Pantalla completa");
        pantallaCompleta.setAccelerator(KeyCombination.keyCombination("F11"));
        pantallaCompleta.setSelected(stage.isFullScreen());
        pantallaCompleta.setOnAction(event -> stage.setFullScreen(pantallaCompleta.isSelected()));
        stage.fullScreenProperty().addListener((obs, antes, ahora) -> pantallaCompleta.setSelected(ahora));
        Menu letra = new Menu("Tamaño de letra");
        ToggleGroup grupo = new ToggleGroup();
        for (int i = 0; i < NOMBRES_LETRA.length; i++) {
            RadioMenuItem opcion = new RadioMenuItem(NOMBRES_LETRA[i]);
            opcion.setToggleGroup(grupo);
            opcion.setSelected(i == tamanoLetra);
            int posicion = i;
            opcion.setOnAction(evento -> {
                tamanoLetra = posicion;
                guardarEntero(CLAVE_TAMANO_LETRA, posicion);
                aplicarAccesibilidad();
            });
            letra.getItems().add(opcion);
        }
        CheckMenuItem contraste = new CheckMenuItem("Alto contraste");
        contraste.setSelected(altoContraste);
        contraste.setOnAction(evento -> {
            altoContraste = contraste.isSelected();
            guardarEntero(CLAVE_ALTO_CONTRASTE, altoContraste ? 1 : 0);
            aplicarAccesibilidad();
        });
        return new Menu("Ver", null, tema, contraste, letra, new SeparatorMenuItem(), pantallaCompleta);
    }

    private Menu crearMenuAyuda() {
        return new Menu("Ayuda", null, elemento("Tutorial de uso", "F1", this::iniciarTutorial),
                elemento("Acerca de AlgoMentor", () -> {
            Alert acerca = new Alert(Alert.AlertType.INFORMATION);
            acerca.setTitle("Acerca de AlgoMentor");
            acerca.setHeaderText("AlgoMentor");
            acerca.setContentText("Aplicación de escritorio para aprender algoritmos paso a paso: "
                    + "simulación, práctica con corrección, progreso por habilidad e informes en PDF.\n\n"
                    + "Proyecto final de Programación de Aplicaciones de Escritorio, UAM.");
            acerca.showAndWait();
        }));
    }

    private MenuItem elemento(String texto, Runnable accion) {
        MenuItem elemento = new MenuItem(texto);
        elemento.setOnAction(event -> accion.run());
        return elemento;
    }

    private MenuItem elemento(String texto, String atajo, Runnable accion) {
        MenuItem elemento = elemento(texto, accion);
        elemento.setAccelerator(KeyCombination.keyCombination(atajo));
        return elemento;
    }

    private void iniciarTutorial() {
        if (envoltura == null || !SesionUsuario.haySesion() || (tutorial != null && tutorial.activo())) {
            return;
        }
        ocultarMenuContextual();
        List<Tutorial.Paso> pasos = new java.util.ArrayList<>();
        pasos.add(new Tutorial.Paso("Bienvenido a AlgoMentor",
                "Aprende algoritmos viéndolos paso a paso, practica con corrección automática y sigue tu progreso. "
                        + "Este recorrido dura un minuto. Usa Siguiente, o las flechas del teclado; Esc lo cierra.",
                () -> barraMenus));
        pasos.add(new Tutorial.Paso("Menús y atajos",
                "Las secciones están en la barra lateral: el indicador te muestra dónde estás y el botón de las tres "
                        + "rayas la contrae. Los atajos Ctrl+1 a Ctrl+8 abren las secciones y Ctrl+T cambia el tema.",
                () -> barraLateral));
        pasos.add(new Tutorial.Paso("Simulador: configura la entrada",
                "Elige un algoritmo, escribe números separados por coma (o pulsa Generar entrada) y presiona Simular. "
                        + "Debajo aparece una descripción de cómo ordena ese método.",
                () -> tarjeta(0), () -> mostrar("simulador", "simulador.fxml", false)));
        pasos.add(new Tutorial.Paso("Simulador: recorre los pasos",
                "Con Siguiente y Anterior avanzas o retrocedes, o pulsa Reproducir para verlo solo y elige la velocidad. "
                        + "Puedes ver el arreglo como barras y, más abajo, el pseudocódigo con la línea que se ejecuta.",
                () -> tarjeta(1)));
        pasos.add(new Tutorial.Paso("Guía de algoritmos",
                "Una tabla compara los trece métodos de ordenamiento: técnica, mejor, promedio y peor caso, memoria "
                        + "extra y estabilidad. Abajo hay una gráfica del "
                        + "crecimiento de cada complejidad. Búsquedas, grafos y recursividad están en Aprender.",
                () -> tarjeta(0), () -> mostrar("guia", "guia-algoritmos.fxml", false)));
        pasos.add(new Tutorial.Paso("Práctica",
                "Elige un ejercicio y escribe tu respuesta. AlgoMentor la corrige, detecta el tipo de error (signos, "
                        + "fracciones, aritmética o procedimiento) y lo suma a tu progreso.",
                () -> tarjeta(0), () -> mostrar("practica", "practica.fxml", true)));
        pasos.add(new Tutorial.Paso("Comparar algoritmos",
                "Escribe una lista de números y compara cuántas comparaciones, intercambios y escrituras hace cada "
                        + "método con esos mismos datos.",
                () -> contenido, () -> mostrar("comparador", "comparador.fxml", false)));
        pasos.add(new Tutorial.Paso("Mi progreso",
                "Aquí ves tus aciertos, tu racha, el avance por habilidad y el historial. Puedes exportar un informe "
                        + "en PDF desde Archivo.",
                () -> contenido, () -> mostrar("progreso", "progreso.fxml", true)));
        pasos.add(new Tutorial.Paso("Retos y logros",
                "Pon a prueba lo aprendido con un desafío de 60 segundos y un cuestionario teórico, y mira los "
                        + "logros que vas desbloqueando. En Práctica, el modo tutor te da pistas sin revelar la respuesta.",
                () -> contenido, () -> mostrar("retos", "retos.fxml", true)));
        pasos.add(new Tutorial.Paso("Ajustes y ayuda",
                "En Ver puedes cambiar el tema, el tamaño de letra y activar el alto contraste. Este recorrido se repite "
                        + "desde Ayuda, Tutorial de uso (F1).",
                () -> nodoMenu("Ayuda")));
        tutorial = new Tutorial(envoltura, pasos, () -> guardarTutorialVisto(true));
        tutorial.iniciar();
    }

    private Node nodoMenu(String texto) {
        return barraMenus.lookupAll(".menu").stream()
                .filter(nodo -> nodo instanceof Labeled etiqueta && texto.equals(etiqueta.getText()))
                .findFirst().orElse(barraMenus);
    }

    private Node tarjeta(int posicion) {
        List<Node> tarjetas = contenido.lookupAll(".card").stream()
                .filter(nodo -> !nodo.getStyleClass().contains("tutorial-card")).toList();
        return posicion < tarjetas.size() ? tarjetas.get(posicion) : contenido;
    }

    private static boolean leerTutorialVisto() {
        try {
            return Preferences.userNodeForPackage(MainApp.class).getBoolean(CLAVE_TUTORIAL_VISTO, false);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo leer si el tutorial ya se mostró", e);
            return true;
        }
    }

    private static void guardarTutorialVisto(boolean visto) {
        try {
            Preferences.userNodeForPackage(MainApp.class).putBoolean(CLAVE_TUTORIAL_VISTO, visto);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo guardar que el tutorial ya se mostró", e);
        }
    }

    /** Aplica el tamaño de letra y el alto contraste elegidos; se recuerdan entre ejecuciones. */
    private void aplicarAccesibilidad() {
        if (envoltura == null || raizActual == null) {
            return;
        }
        envoltura.setStyle("-fx-font-size: " + TAMANOS_LETRA[tamanoLetra] + "px;");
        raizActual.getStyleClass().remove("alto-contraste");
        if (altoContraste) {
            raizActual.getStyleClass().add("alto-contraste");
        }
    }

    private static int leerEntero(String clave, int predeterminado) {
        try {
            return Preferences.userNodeForPackage(MainApp.class).getInt(clave, predeterminado);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo leer una preferencia", e);
            return predeterminado;
        }
    }

    private static void guardarEntero(String clave, int valor) {
        try {
            Preferences.userNodeForPackage(MainApp.class).putInt(clave, valor);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo guardar una preferencia", e);
        }
    }

    /** El tema elegido se recuerda entre ejecuciones; si las preferencias no están disponibles, se usa el oscuro. */
    private static boolean leerTemaClaro() {
        try {
            return Preferences.userNodeForPackage(MainApp.class).getBoolean(CLAVE_TEMA_CLARO, false);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo leer la preferencia de tema", e);
            return false;
        }
    }

    private static void guardarTemaClaro(boolean claro) {
        try {
            Preferences.userNodeForPackage(MainApp.class).putBoolean(CLAVE_TEMA_CLARO, claro);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo guardar la preferencia de tema", e);
        }
    }

    private void cerrarSesion() {
        if (tutorial != null) {
            tutorial.terminar();
        }
        detenerActualizacionAutomatica();
        SesionUsuario.cerrar();
        vistas.clear();
        vistaActual = null;
        barraLateral = null;
        stage.getScene().getAccelerators().clear();
        try {
            stage.getScene().setRoot(cargarLogin());
            Animaciones.instalarEfectosBoton(stage.getScene());
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo volver al inicio de sesión", e);
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir el inicio de sesión.").showAndWait();
        }
    }

    /** La API local de ejercicios arranca con la vista de intercambio, por eso se carga al iniciar sesión. */
    private void precargarIntercambio() {
        try {
            vistas.put("intercambio", cargarVista("intercambio-ejercicios.fxml"));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "No se pudo preparar el intercambio de ejercicios", e);
        }
    }

    /**
     * Muestra una sección en el área central. Con {@code recargar} la vista se vuelve a cargar para
     * traer datos nuevos; sin él se conserva la ya abierta (por ejemplo, el simulador en curso).
     */
    @SuppressWarnings("unchecked")
    private <T> T mostrar(String clave, String archivo, boolean recargar) {
        try {
            Vista vista = recargar ? null : vistas.get(clave);
            if (vista == null) {
                vista = cargarVista(archivo);
                vistas.put(clave, vista);
                animarPestanasInternas(vista.raiz());
            }
            Parent raiz = vista.raiz();
            vistaActual = clave;
            ocultarMenuContextual();
            contenido.getChildren().setAll(raiz);
            if (barraLateral != null) {
                barraLateral.seleccionar("progreso-perfil".equals(clave) ? "resumen" : clave);
            }
            Platform.runLater(() -> Animaciones.entrarContenido(raiz));
            return (T) vista.controlador();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo abrir la vista " + archivo, e);
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir la sección solicitada.").showAndWait();
            return null;
        }
    }

    private void animarPestanasInternas(Parent... vistas) {
        for (Parent vista : vistas) {
            vista.lookupAll(".content-tabs").stream()
                    .filter(TabPane.class::isInstance)
                    .map(TabPane.class::cast)
                    .forEach(Animaciones::animarPestanas);
        }
    }

    private Vista cargarVista(String nombre) throws IOException {
        URL view = MainApp.class.getResource("/ni/edu/uam/algomentor/view/" + nombre);
        if (view == null) {
            throw new IOException("No se encontró la vista " + nombre);
        }
        FXMLLoader loader = new FXMLLoader(view);
        Parent raiz = loader.load();
        return new Vista(raiz, loader.getController());
    }

    private record Vista(Parent raiz, Object controlador) { }

    public static void main(String[] args) {
        ConfiguracionLogs.aplicar();
        launch(args);
    }
}
