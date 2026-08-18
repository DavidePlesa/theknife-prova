package theknife.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import theknife.common.TheKnifeService;
import theknife.common.Utente;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;

public class MainController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private static final String CLASS_OK = "field-ok";
    private static final String CLASS_ERROR = "field-error";

    private TheKnifeService getService() throws Exception {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        return (TheKnifeService) registry.lookup("TheKnifeService");
    }

    private String cifra(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Errore cifratura", e);
        }
    }

    /** Resetta i bordi e nasconde il messaggio di errore. */
    private void resetStato() {
        setFieldState(usernameField, false);
        setFieldState(passwordField, false);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void setFieldState(Control field, boolean error) {
        field.getStyleClass().removeAll(CLASS_OK, CLASS_ERROR);
        field.getStyleClass().add(error ? CLASS_ERROR : CLASS_OK);
    }

    /** Mostra il messaggio di errore e colora in rosso i campi indicati. */
    private void mostraErrore(String messaggio, boolean erroreUsername, boolean errorePassword) {
        if (erroreUsername)
            setFieldState(usernameField, true);
        if (errorePassword)
            setFieldState(passwordField, true);
        errorLabel.setText(messaggio);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    private void handleLogin() {
        resetStato();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validazione campi vuoti
        if (username.isEmpty() && password.isEmpty()) {
            mostraErrore("Inserisci username e password.", true, true);
            return;
        }
        if (username.isEmpty()) {
            mostraErrore("Inserisci il tuo username.", true, false);
            return;
        }
        if (password.isEmpty()) {
            mostraErrore("Inserisci la tua password.", false, true);
            return;
        }

        try {
            TheKnifeService service = getService();
            Utente utente = service.login(username, cifra(password));

            if (utente != null) {
                System.out.println("Login OK: " + utente.getNome());
                // TODO: apri la schermata successiva (HomeView, ecc.)
            } else {
                mostraErrore("Username o password errati.", true, true);
            }

        } catch (Exception e) {
            mostraErrore("Impossibile contattare il server: " + e.getMessage(), false, false);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGuest() {
        System.out.println("Accesso come guest");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HomeView.fxml"));
            Parent root = loader.load();

            Stage registerStage = new Stage();
            registerStage.setTitle("TheKnife - Home");
            registerStage.setScene(new Scene(root));
            registerStage.show();
            ((Stage) usernameField.getScene().getWindow()).close();

        } catch (IOException e) {
            System.err.println("Errore durante il caricamento di HomeView.fxml");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegistrati() {
        System.out.println("Vai alla registrazione");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterView.fxml"));
            Parent root = loader.load();

            Stage registerStage = new Stage();
            registerStage.setTitle("TheKnife - Registrazione");
            registerStage.setScene(new Scene(root));
            registerStage.show();
            ((Stage) usernameField.getScene().getWindow()).close();

        } catch (IOException e) {
            System.err.println("Errore durante il caricamento di RegisterView.fxml");
            e.printStackTrace();
        }
    }
}