package theknife.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AddressController {

    @FXML private TextField addressField;
    @FXML private Label errorLabel;

    private static final String CLASS_OK = "field-ok";
    private static final String CLASS_ERROR = "field-error";

    private boolean inAttesa = false;

    private void resetStato() {
        setFieldState(addressField, false);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void setFieldState(Control field, boolean error) {
        field.getStyleClass().removeAll(CLASS_OK, CLASS_ERROR);
        field.getStyleClass().add(error ? CLASS_ERROR : CLASS_OK);
    }

    private void mostraErrore(String messaggio, boolean erroreCampo) {
        if (erroreCampo) setFieldState(addressField, true);
        errorLabel.setText(messaggio);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    private void handleConferma() {
        if (inAttesa) return;
        resetStato();
        final String indirizzo = addressField.getText().trim();

        if (indirizzo.isEmpty()) {
            mostraErrore("Inserisci un indirizzo o una città per continuare.", true);
            return;
        }

        inAttesa = true;

        GestoreRichieste.getInstance().exec(
            new Operazione<Boolean>() {
                @Override
                public Boolean esegui() throws Exception {
                    return GestoreRichieste.getInstance().getService().validaDomicilio(indirizzo);
                }
            },
            new Esito<Boolean>() {
                @Override
                public void successo(Boolean valido) {
                    inAttesa = false;
                    if (addressField.getScene() == null) return;
                    if (!Boolean.TRUE.equals(valido)) {
                        mostraErrore("Indirizzo non trovato. Riprova.", true);
                        return;
                    }
                    apriHome();
                }

                @Override
                public void errore(Exception e) {
                    inAttesa = false;
                    mostraErrore("Impossibile contattare il server: " + e.getMessage(), false);
                    e.printStackTrace();
                }
            });
    }

    private void apriHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HomeView.fxml"));
            Parent root = loader.load();
            HomeController controller = loader.getController();
            controller.init(null);
            SceneManager.cambiaView(addressField, root, "TheKnife - Home");
        } catch (Exception e) {
            mostraErrore("Errore nel caricamento della home.", false);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleIndietro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent root = loader.load();
            SceneManager.cambiaView(addressField, root, "TheKnife - Login");
        } catch (Exception e) {
            mostraErrore("Errore nel ritorno alla schermata di login.", false);
            e.printStackTrace();
        }
    }
}