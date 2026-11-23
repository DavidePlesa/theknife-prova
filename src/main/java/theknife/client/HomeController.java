package theknife.client;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import theknife.common.Ristorante;
import theknife.common.Utente;

import java.util.List;

public class HomeController {

    private static final int PAGE_SIZE = 10;
    private static final int COLONNE_GRIGLIA = 3;

    // --- Top bar ---
    @FXML private Label benvenutoLabel;
    @FXML private Label inizialeLabel;

    // --- Sidebar filters ---
    @FXML private ComboBox<String> comboCucina;
    @FXML private ComboBox<String> comboCitta;
    @FXML private Spinner<Integer> spinnerValutazione;
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

    private Utente utente;
    private int paginaCorrente = 1;
    private int numeroRichiesta = 0; // solo thread JavaFX

    public void init(Utente utente) {
        this.utente = utente;

        if (utente != null) {
            benvenutoLabel.setText(utente.getNome() + "!");
            inizialeLabel.setText(String.valueOf(utente.getNome().charAt(0)).toUpperCase());
        } else {
            benvenutoLabel.setText("Guest!");
            inizialeLabel.setText("?");
        }
        caricaRistoranti();
    }

    @FXML
    public void initialize() {
        sliderRaggio.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
                raggioValore.setText(newVal.intValue() + " km");
            }
        });

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
        final String citta = normalizzaCombo(comboCitta);
        final String cucina = normalizzaCombo(comboCucina);
        final Double prezzoMax = null;
        final Boolean delivery = checkDelivery.isSelected() ? Boolean.TRUE : null;
        final Boolean prenotazione = checkPrenotazione.isSelected() ? Boolean.TRUE : null;

        Integer valStelle = spinnerValutazione.getValue();
        final Integer stelleMin = (valStelle != null && valStelle > 0) ? valStelle : null;

        final int pagina = paginaCorrente;
        final int idRichiesta = ++numeroRichiesta;

        labelRisultati.setText("Ricerca in corso...");
        btnPrev.setDisable(true);
        btnNext.setDisable(true);

        GestoreRichieste.getInstance().esegui(
            new Operazione<List<Ristorante>>() {
                @Override
                public List<Ristorante> esegui() throws Exception {
                    return GestoreRichieste.getInstance().getService().cercaRistorante(
                            citta, cucina, prezzoMax, delivery, prenotazione, stelleMin, pagina);
                }
            },
            new Esito<List<Ristorante>>() {
                @Override
                public void successo(List<Ristorante> risultato) {
                    if (idRichiesta != numeroRichiesta) return; // risposta obsoleta
                    aggiornaGriglia(risultato);
                }

                @Override
                public void errore(Exception e) {
                    if (idRichiesta != numeroRichiesta) return;
                    e.printStackTrace();
                    labelRisultati.setText("Errore di connessione al server.");
                    btnPrev.setDisable(paginaCorrente <= 1);
                }
            });
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
        numeroRichiesta++; // eventuali risposte in arrivo vengono scartate
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent root = loader.load();
            SceneManager.cambiaView(benvenutoLabel, root, "TheKnife - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}