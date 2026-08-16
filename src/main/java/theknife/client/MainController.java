package theknife.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import theknife.common.TheKnifeService;
import theknife.common.Utente;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MainController {

    // Assicurati che questi fx:id esistano nel tuo file FXML associato
    @FXML private TextField usernameField;
    @FXML private Label benvenutoLabel;
    @FXML private Label inizialeLabel;

    @FXML
    private void handleLogin() {
        try {
            // 1. Si connette al registry RMI del server
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            
            // 2. Recupera lo stub del servizio remoto
            TheKnifeService service = (TheKnifeService) registry.lookup("TheKnifeService");

            // 3. Prende l'input dalla tua GUI
            String username = usernameField.getText();

            // 4. Invoca il metodo remoto (che internamente userà il DBManager del server)
            Utente utente = service.login(username, "password_fittizia");

            // 5. Aggiorna la grafica della tua schermata
            if (utente != null) {
                benvenutoLabel.setText(utente.getNome());
                if (utente.getNome() != null && !utente.getNome().isEmpty()) {
                    inizialeLabel.setText(String.valueOf(utente.getNome().charAt(0)).toUpperCase());
                }
                System.out.println("Login effettuato con successo!");
            }

        } catch (Exception e) {
            System.err.println("Errore di connessione RMI dal client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}