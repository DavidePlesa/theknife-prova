package theknife.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import theknife.common.Ristorante;

public class RestaurantCardController {

    @FXML private Label nomeLabel;
    @FXML private Label categoriaLabel;
    @FXML private Label localitaLabel;

    public void setRistorante(Ristorante r) {
        nomeLabel.setText(r.getNome());
        categoriaLabel.setText(r.getTipoCucina() != null ? r.getTipoCucina() : "Cucina non specificata");
        localitaLabel.setText(r.getLocation() + (r.getPrice() != null ? " • " + r.getPrice() : ""));
    }
}