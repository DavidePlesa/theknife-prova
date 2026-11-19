package theknife.client;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import theknife.common.Ristorante;
import theknife.common.TheKnifeService;
import theknife.common.Utente;

import java.util.List;

public class HomeController {

    private static final int PAGE_SIZE = 10;
    private static final int COLONNE_GRIGLIA = 3;

    // --- Top bar ---
    @FXML
    private Label benvenutoLabel;
    @FXML
    private Label inizialeLabel;

    // --- Sidebar filters ---
    @FXML
    private ComboBox<String> comboCucina;
    @FXML
    private ComboBox<String> comboCitta;
    @FXML
    private Spinner<Integer> spinnerValutazione;
    @FXML
    private CheckBox checkDelivery;
    @FXML
    private CheckBox checkPrenotazione;
    @FXML
    private Slider sliderRaggio;
    @FXML
    private Label raggioValore;
    @FXML
    private Button btnLogout;

    // --- Center ---
    @FXML
    private Label labelRisultati;
    @FXML
    private GridPane gridPane;
    @FXML
    private Button btnPrev;
    @FXML
    private Button btnNext;
    @FXML
    private Label labelPagina;

    private TheKnifeService service;
    private Utente utente;
    private int paginaCorrente = 1;

    public void init(Utente utente, TheKnifeService service) {
        this.utente = utente;
        this.service = service;

        if (utente != null) {
            benvenutoLabel.setText(utente.getNome() + "!");
            inizialeLabel.setText(String.valueOf(utente.getNome().charAt(0)).toUpperCase());
        } else {
            benvenutoLabel.setText("Guest!");
            inizialeLabel.setText("?");
        }
        // Carica subito tutti i ristoranti
        caricaRistoranti();
    }

    @FXML
    public void initialize() {
        sliderRaggio.valueProperty()
                .addListener((obs, oldVal, newVal) -> raggioValore.setText((int) newVal.doubleValue() + " km"));

        // Inizializza lo spinner (0 = nessun filtro stelle, da 1 a 5 = stelle minime)
        if (spinnerValutazione.getValueFactory() == null) {
            spinnerValutazione.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));
        }
    }

    @FXML
    private void handleCerca() {
        paginaCorrente = 1;
        caricaRistoranti();
    }

    @FXML
    private void handlePrev() {
        if (paginaCorrente > 1) {
            paginaCorrente--;
            caricaRistoranti();
        }
    }

    @FXML
    private void handleNext() {
        paginaCorrente++;
        caricaRistoranti();
    }

    private void caricaRistoranti() {
        if (service == null)
            return;

        String citta = normalizzaCombo(comboCitta);
        String cucina = normalizzaCombo(comboCucina);
        Double prezzoMax = null;
        Boolean delivery = checkDelivery.isSelected() ? true : null;
        Boolean prenotazione = checkPrenotazione.isSelected() ? true : null;

        Integer valStelle = spinnerValutazione.getValue();
        Integer stelleMin = (valStelle != null && valStelle > 0) ? valStelle : null;

        final int pagina = this.paginaCorrente;

        labelRisultati.setText("Ricerca in corso...");
        btnPrev.setDisable(true);
        btnNext.setDisable(true);

        Task<List<Ristorante>> task = new Task<>() {
            @Override
            protected List<Ristorante> call() throws Exception {
                return service.cercaRistorante(citta, cucina, prezzoMax, delivery, prenotazione, stelleMin, pagina);
            }
        };

        task.setOnSucceeded(e -> aggiornaGriglia(task.getValue()));

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            labelRisultati.setText("Errore di connessione al server.");
            btnPrev.setDisable(paginaCorrente <= 1);
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void aggiornaGriglia(List<Ristorante> ristoranti) {
        gridPane.getChildren().clear();
        labelPagina.setText("Pagina " + paginaCorrente);
        btnPrev.setDisable(paginaCorrente <= 1);

        if (ristoranti == null || ristoranti.isEmpty()) {
            labelRisultati.setText("Nessun ristorante trovato.");
            btnNext.setDisable(true);
            return;
        }

        int col = 0;
        int row = 0;

        for (Ristorante r : ristoranti) {
            try {
                // Assicurati che il nome del file FXML corrisponda esattamente al tuo
                FXMLLoader loader = new FXMLLoader(getClass().getResource("RestaurantCardView.fxml"));
                Parent card = loader.load();

                RestaurantCardController cardController = loader.getController();
                cardController.setRistorante(r);

                gridPane.add(card, col, row);

                col++;
                if (col == COLONNE_GRIGLIA) {
                    col = 0;
                    row++;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        labelRisultati.setText("Mostrati " + ristoranti.size() + " ristoranti");
        btnNext.setDisable(ristoranti.size() < PAGE_SIZE);
    }

    private String normalizzaCombo(ComboBox<String> combo) {
        String val = combo.isEditable() ? combo.getEditor().getText() : combo.getValue();
        if (val == null || val.isBlank() || val.equalsIgnoreCase("Tutte")) {
            return null;
        }
        return val.trim();
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