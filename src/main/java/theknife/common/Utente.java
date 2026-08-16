package theknife.common;

import java.io.Serializable;

public class Utente implements Serializable {

    private int id;
    private String nome;
    private String cognome;
    private String username;
    private String ruolo;

    // Costruttore completo (usato dal DAO dopo il login dal DB)
    public Utente(int id, String nome, String cognome, String username, String ruolo) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.ruolo = ruolo;
    }

    // Costruttore senza id (usato per la registrazione)
    public Utente(String nome, String cognome, String username, String ruolo) {
        this.id = -1;
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.ruolo = ruolo;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getUsername() { return username; }
    public String getRuolo() { return ruolo; }
}