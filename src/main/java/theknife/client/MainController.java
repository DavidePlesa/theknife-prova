package theknife.client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import theknife.common.TheKnifeService;
import theknife.common.Utente;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;

public class MainController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

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

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validazione base
        if (username.isEmpty() || password.isEmpty()) {
            mostraAlert(AlertType.WARNING, "Campi vuoti",
                    "Inserisci username e password.");
            return;
        }

        try {
            TheKnifeService service = getService();
            Utente utente = service.login(username, cifra(password));

            if (utente != null) {
                System.out.println("Login OK: " + utente.getNome());
                // TODO: apri la schermata successiva (HomeView, ecc.)
                mostraAlert(AlertType.INFORMATION, "Benvenuto",
                        "Ciao, " + utente.getNome() + "!");
            } else {
                mostraAlert(AlertType.ERROR, "Login fallito",
                        "Username o password errati.");
            }

        } catch (Exception e) {
            mostraAlert(AlertType.ERROR, "Errore di connessione",
                    "Impossibile contattare il server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGuest() {
        System.out.println("Accesso come guest");
        // TODO: apri la HomeView in modalità guest
    }

    @FXML
    private void handleRegistrati() {
        System.out.println("Vai alla registrazione");
        // TODO: apri la RegistrazioneView
    }

    private void mostraAlert(AlertType tipo, String titolo, String messaggio) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}