package theknife.common;

import java.io.Serializable;

public class Ristorante implements Serializable {

    private int id;
    private String nome;
    private int idProprietario;
    // Da luoghi
    private String location; // nazione/città
    private String address; // indirizzo
    private double latitudine;
    private double longitudine;
    // Da ristorantitheknife
    private String price; // testo (es. "€€", "30-50€")
    private String phoneNumber;
    private String url;
    private String websiteUrl;
    private String award;
    private boolean greenStar;
    private String facilitiesAndServices;
    private String description;
    private boolean delivery;
    private boolean booking; // prenotazione nel DB si chiama booking
    // Da tipicucina (può essere multipla, ma per semplicità prendiamo la prima)
    private String tipoCucina;

    public Ristorante(int id, String nome, int idProprietario,
            String location, String address,
            double latitudine, double longitudine,
            String price, String phoneNumber, String url,
            String websiteUrl, String award, boolean greenStar,
            String facilitiesAndServices, String description,
            boolean delivery, boolean booking, String tipoCucina) {
        this.id = id;
        this.nome = nome;
        this.idProprietario = idProprietario;
        this.location = location;
        this.address = address;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.price = price;
        this.phoneNumber = phoneNumber;
        this.url = url;
        this.websiteUrl = websiteUrl;
        this.award = award;
        this.greenStar = greenStar;
        this.facilitiesAndServices = facilitiesAndServices;
        this.description = description;
        this.delivery = delivery;
        this.booking = booking;
        this.tipoCucina = tipoCucina;
    }

    // Costruttore compatto per liste/ricerca (senza tutti i dettagli)
    public Ristorante(int id, String nome, int idProprietario,
            String location, String address,
            double latitudine, double longitudine,
            String price, boolean delivery, boolean booking,
            String tipoCucina) {
        this(id, nome, idProprietario, location, address,
                latitudine, longitudine, price, null, null,
                null, null, false, null, null, delivery, booking, tipoCucina);
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getIdProprietario() {
        return idProprietario;
    }

    public String getLocation() {
        return location;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public String getPrice() {
        return price;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUrl() {
        return url;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getAward() {
        return award;
    }

    public boolean isGreenStar() {
        return greenStar;
    }

    public String getFacilitiesAndServices() {
        return facilitiesAndServices;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public boolean isBooking() {
        return booking;
    }

    public String getTipoCucina() {
        return tipoCucina;
    }

    // Alias per compatibilità col codice esistente
    public boolean isPrenotazione() {
        return booking;
    }

    public String getNazione() {
        return location;
    }

    public String getCitta() {
        return location;
    }

    public String getIndirizzo() {
        return address;
    }

    public int getIdRistoratore() {
        return idProprietario;
    }
}