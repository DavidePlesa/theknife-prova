package theknife.server;

import theknife.common.Ristorante;
import theknife.common.Utente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtenteDAO {

    private DBConnection dbManager;

    public UtenteDAO(DBConnection dbManager) {
        this.dbManager = dbManager;
    }

    public Utente autentica(String username, String passwordCifrata) {
        String query = "SELECT id, nome, cognome, username, ruolo FROM Utenti WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordCifrata);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Utente(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getString("username"),
                        rs.getString("ruolo")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean registra(String nome, String cognome, String username,
            String passwordCifrata, String dataNascita,
            String luogoDomicilio, String ruolo) throws SQLException {
        String query = """
            INSERT INTO Utenti
            (nome, cognome, username, password, data_nascita, luogo_domicilio, ruolo)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, nome);
            pstmt.setString(2, cognome);
            pstmt.setString(3, username);
            pstmt.setString(4, passwordCifrata);
            if (dataNascita != null && !dataNascita.isEmpty()) {
                pstmt.setDate(5, Date.valueOf(dataNascita));
            } else {
                pstmt.setNull(5, Types.DATE);
            }
            pstmt.setString(6, luogoDomicilio);
            pstmt.setString(7, ruolo);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean aggiungiPreferito(int idUtente, int idRistorante) throws SQLException {
        String query = "INSERT INTO Preferiti (id_utente, id_ristorante) VALUES (?, ?)";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, idUtente);
            pstmt.setInt(2, idRistorante);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean rimuoviPreferito(int idUtente, int idRistorante) throws SQLException {
        String query = "DELETE FROM Preferiti WHERE id_utente = ? AND id_ristorante = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, idUtente);
            pstmt.setInt(2, idRistorante);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Ristorante> visualizzaPreferiti(int idUtente) throws SQLException {
        List<Ristorante> lista = new ArrayList<>();
        String query = """
            SELECT r.* FROM RistorantiTheKnife r
            JOIN Preferiti p ON r.id = p.id_ristorante
            WHERE p.id_utente = ?
            """;
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, idUtente);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Ristorante(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("nazione"),
                        rs.getString("citta"),
                        rs.getString("indirizzo"),
                        rs.getDouble("latitudine"),
                        rs.getDouble("longitudine"),
                        rs.getDouble("prezzo_medio"),
                        rs.getBoolean("delivery"),
                        rs.getBoolean("prenotazione"),
                        rs.getString("tipo_cucina"),
                        rs.getInt("id_ristoratore")
                    ));
                }
            }
        }
        return lista;
    }
}