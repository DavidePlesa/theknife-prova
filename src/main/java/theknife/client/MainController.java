package theknife.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import theknife.common.Utente;

import java.security.MessageDigest;

public class MainController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private static final String CLASS_OK = "field-ok";
    private static final String CLASS_ERROR = "field-error";

    private boolean inAttesa = false; // solo thread JavaFX

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

    private void mostraErrore(String messaggio, boolean erroreUsername, boolean errorePassword) {
        if (erroreUsername) setFieldState(usernameField, true);
        if (errorePassword) setFieldState(passwordField, true);
        errorLabel.setText(messaggio);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    private void handleLogin() {
        if (inAttesa) return;
        resetStato();
        final String username = usernameField.getText().trim();
        final String password = passwordField.getText();

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

        inAttesa = true;
        final String passwordCifrata = cifra(password);

        GestoreRichieste.getInstance().exec(
            new Operazione<Utente>() {
                @Override
                public Utente esegui() throws Exception {
                    return GestoreRichieste.getInstance().getService().login(username, passwordCifrata);
                }
            },
            new Esito<Utente>() {
                @Override
                public void successo(Utente utente) {
                    inAttesa = false;
                    if (usernameField.getScene() == null) return;
                    if (utente == null) {
                        mostraErrore("Username o password errati.", true, true);
                        return;
                    }
                    apriHome(utente);
                }

                @Override
                public void errore(Exception e) {
                    inAttesa = false;
                    mostraErrore("Impossibile contattare il server: " + e.getMessage(), false, false);
                    e.printStackTrace();
                }
            });
    }

    @FXML
    private void handleGuest() {
        if (inAttesa) return;
        resetStato();
        inAttesa = true;

        GestoreRichieste.getInstance().exec(
            new Operazione<Boolean>() {
                @Override
                public Boolean esegui() throws Exception {
                    GestoreRichieste.getInstance().getService(); // verifica che il server risponda
                    return Boolean.TRUE;
                }
            },
            new Esito<Boolean>() {
                @Override
                public void successo(Boolean ok) {
                    inAttesa = false;
                    if (usernameField.getScene() == null) return;
                    apriAddress();
                }

                @Override
                public void errore(Exception e) {
                    inAttesa = false;
                    mostraErrore("Impossibile contattare il server: " + e.getMessage(), false, false);
                    e.printStackTrace();
                }
            });
        apriAddress();
    }

    @FXML
    private void handleRegistrati() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterView.fxml"));
            Parent root = loader.load();
            SceneManager.cambiaView(usernameField, root, "TheKnife - Registrazione");
        } catch (Exception e) {
            mostraErrore("Errore nel caricamento della vista.", false, false);
            e.printStackTrace();
        }
    }

    private void apriHome(Utente utente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HomeView.fxml"));
            Parent root = loader.load();
            HomeController controller = loader.getController();
            controller.init(utente);
            SceneManager.cambiaView(usernameField, root, "TheKnife - Home");
        } catch (Exception e) {
            mostraErrore("Errore nel caricamento della home.", false, false);
            e.printStackTrace();
        }
    }

    private void apriAddress() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddressView.fxml"));
            Parent root = loader.load();
            SceneManager.cambiaView(usernameField, root, "TheKnife - Inserisci Indirizzo");
        } catch (Exception e) {
            mostraErrore("Errore nel caricamento della vista indirizzo.", false, false);
            e.printStackTrace();
        }
    }
}