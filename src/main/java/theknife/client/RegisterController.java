package theknife.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import theknife.common.TheKnifeService;
import java.security.MessageDigest;

public class RegisterController {

    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField domicilioField;
    @FXML private RadioButton radioCliente;
    @FXML private RadioButton radioRistoratore;
    @FXML private Label errorLabel;

    private static final String CLASS_OK = "field-ok";
    private static final String CLASS_ERROR = "field-error";
    private TheKnifeService service;

    public void init(TheKnifeService service) {
        this.service = service;
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

    private void setFieldState(Control field, boolean error) {
        field.getStyleClass().removeAll(CLASS_OK, CLASS_ERROR);
        field.getStyleClass().add(error ? CLASS_ERROR : CLASS_OK);
    }

    private void resetStato() {
        for (Control c : new Control[]{nomeField, cognomeField, usernameField, passwordField, domicilioField})
            setFieldState(c, false);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void mostraErrore(String messaggio, Control... campiErrore) {
        for (Control c : campiErrore)
            setFieldState(c, true);
        errorLabel.setText(messaggio);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    private void handleRegistrati() {
        resetStato();

        String nome = nomeField.getText().trim();
        String cognome = cognomeField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String domicilio = domicilioField.getText().trim();
        String ruolo = radioCliente.isSelected() ? "cliente" : "ristoratore";

        if (nome.isEmpty()) { mostraErrore("Inserisci il tuo nome.", nomeField); return; }
        if (cognome.isEmpty()) { mostraErrore("Inserisci il tuo cognome.", cognomeField); return; }
        if (username.isEmpty()) { mostraErrore("Scegli un username.", usernameField); return; }
        if (password.isEmpty()) { mostraErrore("Inserisci una password.", passwordField); return; }

        try {
            boolean ok = service.registrazione(
                    nome, cognome, username,
                    cifra(password),
                    domicilio.isEmpty() ? null : domicilio,
                    ruolo);

            if (ok) {
                System.out.println("Registrazione completata per: " + username);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("TheKnife - Login");
                stage.setScene(new Scene(root));
                stage.show();
                ((Stage) nomeField.getScene().getWindow()).close();
            } else {
                mostraErrore("Username già in uso. Scegline un altro.", usernameField);
            }
        } catch (Exception e) {
            mostraErrore("Impossibile contattare il server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTornaLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("TheKnife - Login");
            stage.setScene(new Scene(root));
            stage.show();
            ((Stage) nomeField.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}