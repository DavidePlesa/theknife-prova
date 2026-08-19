package theknife.server;

import theknife.common.*;
import theknife.server.GeoTheKnife;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class TheKnifeServiceImpl implements TheKnifeService {

    private DBManager dbManager;
    private UtenteDAO utenteDAO;
    private RistoranteDAO ristoranteDAO;
    private RecensioneDAO recensioneDAO;

    public TheKnifeServiceImpl() {
        this.dbManager = new DBManager();
        this.utenteDAO = new UtenteDAO(dbManager);
        this.ristoranteDAO = new RistoranteDAO(dbManager);
        this.recensioneDAO = new RecensioneDAO(dbManager);
    }

    @Override
    public Utente login(String username, String passwordCifrata) throws RemoteException {
        return utenteDAO.autentica(username, passwordCifrata);
    }

    @Override
    public boolean registrazione(String nome, String cognome, String username, String passwordCifrata,
            String luogoDomicilio, String ruolo) throws RemoteException {
        try {
            return utenteDAO.registra(nome, cognome, username, passwordCifrata, luogoDomicilio, ruolo);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Ristorante> cercaRistorante(String citta, String cucina, Double prezzoMax, Boolean delivery,
            Boolean prenotazione, Integer stelleMin, int pagina) throws RemoteException {
        try {
            return ristoranteDAO.cerca(citta, cucina, prezzoMax,
                    delivery, prenotazione, stelleMin, pagina);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public Ristorante visualizzaRistorante(int idRistorante) throws RemoteException {
        try {
            return ristoranteDAO.findById(idRistorante);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Recensione> visualizzaRecensioni(int idRistorante) throws RemoteException {
        try {
            return recensioneDAO.findByRistorante(idRistorante);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public boolean aggiungiPreferito(int idUtente, int idRistorante) throws RemoteException {
        try {
            return utenteDAO.aggiungiPreferito(idUtente, idRistorante);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean rimuoviPreferito(int idUtente, int idRistorante) throws RemoteException {
        try {
            return utenteDAO.rimuoviPreferito(idUtente, idRistorante);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Ristorante> visualizzaPreferiti(int idUtente) throws RemoteException {
        try {
            return utenteDAO.visualizzaPreferiti(idUtente);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public boolean aggiungiRecensione(int idUtente, int idRistorante, int stelle, String testo) throws RemoteException {
        try {
            return recensioneDAO.inserisci(idUtente, idRistorante, stelle, testo);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean modificaRecensione(int idRecensione, int stelle, String testo) throws RemoteException {
        try {
            return recensioneDAO.modifica(idRecensione, stelle, testo);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean eliminaRecensione(int idRecensione) throws RemoteException {
        try {
            return recensioneDAO.elimina(idRecensione);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean aggiungiRistorante(Ristorante r) throws RemoteException {
        try {
            return ristoranteDAO.inserisci(r);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean rispostaRecensione(int idRecensione, String risposta) throws RemoteException {
        try {
            return recensioneDAO.aggiungiRisposta(idRecensione, risposta);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Ristorante> visualizzaRistorantiPropri(int idRistoratore) throws RemoteException {
        try {
            return ristoranteDAO.findByRistoratore(idRistoratore);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public boolean validaDomicilio(String domicilio) throws RemoteException {
        try {
            return GeoTheKnife.domicilioEsistente(domicilio);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            TheKnifeServiceImpl serverObj = new TheKnifeServiceImpl();
            TheKnifeService stub = (TheKnifeService) UnicastRemoteObject.exportObject(serverObj, 0);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("TheKnifeService", stub);
            System.out.println("Server TheKnife avviato sulla porta 1099.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}