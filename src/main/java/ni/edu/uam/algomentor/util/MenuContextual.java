package ni.edu.uam.algomentor.util;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.ArrayList;
import java.util.List;

/** Menús de clic derecho para las tablas: acciones sobre la fila elegida y copiar la fila. */
public final class MenuContextual {

    private MenuContextual() { }

    /** Acción que se habilita solo cuando hay una fila seleccionada. */
    public static MenuItem sobreSeleccion(TableView<?> tabla, String texto, Runnable accion) {
        MenuItem item = new MenuItem(texto);
        item.setOnAction(evento -> accion.run());
        item.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        return item;
    }

    /** Acción que no depende de la selección. */
    public static MenuItem accion(String texto, Runnable accion) {
        MenuItem item = new MenuItem(texto);
        item.setOnAction(evento -> accion.run());
        return item;
    }

    /** Instala un menú con las acciones dadas y, al final, «Copiar fila». */
    public static <T> void instalar(TableView<T> tabla, MenuItem... acciones) {
        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(acciones);
        if (acciones.length > 0) {
            menu.getItems().add(new SeparatorMenuItem());
        }
        menu.getItems().add(sobreSeleccion(tabla, "Copiar fila", () -> {
            T fila = tabla.getSelectionModel().getSelectedItem();
            if (fila != null) {
                ClipboardContent contenido = new ClipboardContent();
                contenido.putString(textoFila(tabla, fila));
                Clipboard.getSystemClipboard().setContent(contenido);
            }
        }));
        tabla.setContextMenu(menu);
    }

    /** Valores de las columnas separados por tabulador, para pegarlos en una hoja de cálculo. */
    static <T> String textoFila(TableView<T> tabla, T fila) {
        List<String> valores = new ArrayList<>();
        for (TableColumn<T, ?> columna : tabla.getColumns()) {
            Object valor = columna.getCellData(fila);
            valores.add(valor == null ? "" : valor.toString());
        }
        return String.join("\t", valores);
    }
}
