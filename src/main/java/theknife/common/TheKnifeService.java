package theknife.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TheKnifeService extends Remote {

    // Auth
    Utente login(String username, String passwordCifrata) throws RemoteException;

    boolean registrazione(String nome, String cognome, String username, String passwordCifrata, String luogoDomicilio,
            String ruolo) throws RemoteException;

    boolean validaDomicilio(String domicilio) throws RemoteException; // ← AGGIUNTA

    // Ricerca ristoranti
    List<Ristorante> cercaRistorante(String citta, String cucina, Double prezzoMax, Boolean delivery,
            Boolean prenotazione, Integer stelleMin, int pagina) throws RemoteException;

    // Dettaglio
    Ristorante visualizzaRistorante(int idRistorante) throws RemoteException;

    List<Recensione> visualizzaRecensioni(int idRistorante) throws RemoteException;

    // Preferiti (clienti)
    boolean aggiungiPreferito(int idUtente, int idRistorante) throws RemoteException;

    boolean rimuoviPreferito(int idUtente, int idRistorante) throws RemoteException;

    List<Ristorante> visualizzaPreferiti(int idUtente) throws RemoteException;

    // Recensioni (clienti)
    boolean aggiungiRecensione(int idUtente, int idRistorante, int stelle, String testo) throws RemoteException;

    boolean modificaRecensione(int idRecensione, int stelle, String testo) throws RemoteException;

    boolean eliminaRecensione(int idRecensione) throws RemoteException;

    // Ristoratori
    boolean aggiungiRistorante(Ristorante r) throws RemoteException;

    boolean rispostaRecensione(int idRecensione, String risposta) throws RemoteException;

    List<Ristorante> visualizzaRistorantiPropri(int idRistoratore) throws RemoteException;
}