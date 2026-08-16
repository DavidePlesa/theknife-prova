package theknife.common;

import java.io.Serializable;

public class Recensione implements Serializable {

    private int id;
    private int idRistorante;
    private int idUtente;
    private int stelle;
    private String testo;
    private String risposta;

    public Recensione(int id, int idRistorante, int idUtente, int stelle, String testo, String risposta) {
        this.id = id;
        this.idRistorante = idRistorante;
        this.idUtente = idUtente;
        this.stelle = stelle;
        this.testo = testo;
        this.risposta = risposta;
    }

    public int getId() {
        return id;
    }

    public int getIdRistorante() {
        return idRistorante;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public int getStelle() {
        return stelle;
    }

    public String getTesto() {
        return testo;
    }

    public String getRisposta() {
        return risposta;
    }
}