package ni.edu.uam.algomentor.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ni.edu.uam.algomentor.service.ComandosTerminal;

public class TerminalController {

    @FXML private TextArea txtSalida;
    @FXML private TextField txtComando;

    @FXML
    private void initialize() {
        txtSalida.setText("AlgoMentor > Escribe ayuda para comenzar.\n");
    }

    @FXML
    private void ejecutar() {
        String comando = txtComando.getText();
        try {
            String resultado = ComandosTerminal.ejecutar(comando);
            txtSalida.setText("limpiar".equalsIgnoreCase(comando.strip()) ? "" :
                    txtSalida.getText() + "> " + comando + "\n" + resultado + "\n");
            txtComando.clear();
            txtSalida.positionCaret(txtSalida.getLength());
        } catch (IllegalArgumentException e) {
            txtSalida.appendText("Error: " + e.getMessage() + "\n");
        }
    }
}
