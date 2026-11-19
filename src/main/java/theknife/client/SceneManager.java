package theknife.client;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class SceneManager {

    public static void cambiaView(Node nodoQualsiasi, Parent nuovoRoot, String titolo) {
        Stage stage = (Stage) nodoQualsiasi.getScene().getWindow();
        stage.setTitle(titolo);
        stage.getScene().setRoot(nuovoRoot);

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        // Se la risoluzione è bassa, massimizza direttamente la finestra così sfrutta tutto lo spazio
        if (screen.getWidth() <= 900 || screen.getHeight() <= 650) {
            stage.setX(screen.getMinX());
            stage.setY(screen.getMinY());
            stage.setWidth(screen.getWidth());
            stage.setHeight(screen.getHeight());
        } else if (!stage.isMaximized() && !stage.isFullScreen()) {
            stage.sizeToScene();
            stage.centerOnScreen();
        }
    }
}