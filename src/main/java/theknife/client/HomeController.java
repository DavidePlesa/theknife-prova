package theknife.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

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
    @FXML private javafx.scene.layout.GridPane gridPane;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Label labelPagina;

    @FXML
    public void initialize() {
        // Slider → update label
        sliderRaggio.valueProperty().addListener((obs, oldVal, newVal) ->
            raggioValore.setText((int) newVal.doubleValue() + " km")
        );
    }

    @FXML
    private void handleCerca() {
        System.out.println("Avvio ricerca ristoranti...");
        // TODO: Inserisci qui la logica di ricerca basata sui filtri selezionati
    }
}