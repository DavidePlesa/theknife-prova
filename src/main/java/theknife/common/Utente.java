package theknife.common;

import java.io.Serializable;

public class Utente implements Serializable {
    private String nome;
    private String cognome;
    private String username;
    private String ruolo;

    public Utente(String nome, String cognome, String username, String ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.ruolo = ruolo;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getUsername() {
        return username;
    }

    public String getRuolo() {
        return ruolo;
    }
}