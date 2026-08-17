package theknife.server;

import theknife.common.Recensione;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecensioneDAO {

    private DBManager dbManager;

    public RecensioneDAO(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<Recensione> findByRistorante(int idRistorante) throws SQLException {
        List<Recensione> lista = new ArrayList<>();
        String query = "SELECT * FROM Recensioni WHERE id_ristorante = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, idRistorante);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) lista.add(mapRecensione(rs));
            }
        }
        return lista;
    }

    public boolean inserisci(int idUtente, int idRistorante,
            int stelle, String testo) throws SQLException {
        String query = """
            INSERT INTO Recensioni (id_ristorante, id_utente, stelle, testo)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, idRistorante);
            pstmt.setInt(2, idUtente);
            pstmt.setInt(3, stelle);
            pstmt.setString(4, testo);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean modifica(int id, int stelle, String testo) throws SQLException {
        String query = "UPDATE Recensioni SET stelle = ?, testo = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, stelle);
            pstmt.setString(2, testo);
            pstmt.setInt(3, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean elimina(int id) throws SQLException {
        String query = "DELETE FROM Recensioni WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean aggiungiRisposta(int id, String risposta) throws SQLException {
        String query = "UPDATE Recensioni SET risposta = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, risposta);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    private Recensione mapRecensione(ResultSet rs) throws SQLException {
        return new Recensione(
            rs.getInt("id"),
            rs.getInt("id_ristorante"),
            rs.getInt("id_utente"),
            rs.getInt("stelle"),
            rs.getString("testo"),
            rs.getString("risposta")
        );
    }
}