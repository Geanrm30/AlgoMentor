package ni.edu.uam.algomentor;

import javafx.application.Application;
import ni.edu.uam.algomentor.util.ConfiguracionLogs;

public class Launcher {
    public static void main(String[] args) {
        ConfiguracionLogs.aplicar();
        Application.launch(MainApp.class, args);
    }
}