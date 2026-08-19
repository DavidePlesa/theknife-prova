package theknife.server;

import theknife.common.Ristorante;
import theknife.common.Utente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtenteDAO {

    private DBManager dbManager;

    public UtenteDAO(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public Utente autentica(String username, String passwordCifrata) {
        String query = "SELECT * FROM utenti WHERE username = ? AND password_hash = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, passwordCifrata);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Utente(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getString("username"),
                        rs.getString("ruolo"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean registra(String nome, String cognome, String username,
            String passwordCifrata, String luogoDomicilio, String ruolo) throws SQLException {
        String query = """
                INSERT INTO utenti (nome, cognome, username, password_hash, indirizzo_geolocalizzato, ruolo)
                VALUES (?, ?, ?, ?, ?, ?::tipo_utente)
                """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nome);
            pstmt.setString(2, cognome);
            pstmt.setString(3, username);
            pstmt.setString(4, passwordCifrata);
            pstmt.setString(5, luogoDomicilio);
            pstmt.setString(6, ruolo);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean aggiungiPreferito(int idUtente, int idRistorante) throws SQLException {
        String query = "INSERT INTO preferiti (id_utente, id_ristorante) VALUES (?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idUtente);
            pstmt.setInt(2, idRistorante);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean rimuoviPreferito(int idUtente, int idRistorante) throws SQLException {
        String query = "DELETE FROM preferiti WHERE id_utente = ? AND id_ristorante = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idUtente);
            pstmt.setInt(2, idRistorante);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Ristorante> visualizzaPreferiti(int idUtente) throws SQLException {
        List<Ristorante> lista = new ArrayList<>();
        String query = """
                SELECT r.*, l.location, l.address, l.latitudine, l.longitudine, t.nome AS tipo_cucina
                FROM ristorantitheknife r
                JOIN preferiti p ON r.id = p.id_ristorante
                JOIN luoghi l ON r.id_luogo = l.id
                LEFT JOIN ristorante_cucina rc ON r.id = rc.id_ristorante
                LEFT JOIN tipicucina t ON rc.id_cucina = t.id
                WHERE p.id_utente = ?
                GROUP BY r.id, l.location, l.address, l.latitudine, l.longitudine, t.nome
                """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idUtente);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Ristorante(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getInt("id_proprietario"),
                            rs.getString("location"),
                            rs.getString("address"),
                            rs.getDouble("latitudine"),
                            rs.getDouble("longitudine"),
                            rs.getString("price"),
                            rs.getBoolean("delivery"),
                            rs.getBoolean("booking"),
                            rs.getString("tipo_cucina")));
                }
            }
        }
        return lista;
    }
}