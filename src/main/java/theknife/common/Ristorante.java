package theknife.common;

import java.io.Serializable;

public class Ristorante implements Serializable {

    private int id;
    private String nome;
    private String nazione;
    private String citta;
    private String indirizzo;
    private double latitudine;
    private double longitudine;
    private double prezzoMedio;
    private boolean delivery;
    private boolean prenotazione;
    private String tipoCucina;
    private int idRistoratore;

    public Ristorante(int id, String nome, String nazione, String citta, String indirizzo, double latitudine, double longitudine, double prezzoMedio, boolean delivery, boolean prenotazione, String tipoCucina, int idRistoratore) {
        this.id = id;
        this.nome = nome;
        this.nazione = nazione;
        this.citta = citta;
        this.indirizzo = indirizzo;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.prezzoMedio = prezzoMedio;
        this.delivery = delivery;
        this.prenotazione = prenotazione;
        this.tipoCucina = tipoCucina;
        this.idRistoratore = idRistoratore;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNazione() {
        return nazione;
    }

    public String getCitta() {
        return citta;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public double getPrezzoMedio() {
        return prezzoMedio;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public boolean isPrenotazione() {
        return prenotazione;
    }

    public String getTipoCucina() {
        return tipoCucina;
    }

    public int getIdRistoratore() {
        return idRistoratore;
    }
}