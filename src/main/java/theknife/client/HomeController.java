package theknife.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import theknife.common.TheKnifeService;
import theknife.common.Utente;

public class HomeController {

    // --- Top bar ---
    @FXML private Label benvenutoLabel;
    @FXML private Label inizialeLabel;

    // --- Sidebar filters ---
    @FXML private ComboBox<?> comboCucina;
    @FXML private ComboBox<?> comboCitta;
    @FXML private Spinner<?> spinnerValutazione;
    @FXML private CheckBox checkDelivery;
    @FXML private CheckBox checkPrenotazione;
    @FXML private Slider sliderRaggio;
    @FXML private Label raggioValore;
    @FXML private Button btnLogout;

    // --- Center ---
    @FXML private Label labelRisultati;
    @FXML private GridPane gridPane;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Label labelPagina;

    private TheKnifeService service;
    private Utente utente;

    public void init(Utente utente, TheKnifeService service) {
        this.utente = utente;
        this.service = service;

        if (utente != null) {
            benvenutoLabel.setText("Ciao, " + utente.getNome() + "!");
            inizialeLabel.setText(String.valueOf(utente.getNome().charAt(0)).toUpperCase());
        } else {
            benvenutoLabel.setText("Ciao, ospite!");
            inizialeLabel.setText("?");
        }
    }

    @FXML
    public void initialize() {
        sliderRaggio.valueProperty().addListener((obs, oldVal, newVal) ->
            raggioValore.setText((int) newVal.doubleValue() + " km")
        );
    }

    @FXML
    private void handleCerca() {
        System.out.println("Avvio ricerca ristoranti...");
        // TODO: logica di ricerca con this.service
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("TheKnife - Login");
            stage.setScene(new Scene(root));
            stage.show();
            ((Stage) benvenutoLabel.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}