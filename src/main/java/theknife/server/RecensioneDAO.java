package theknife.server;

import theknife.common.Recensione;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Nel database una recensione ha: voto, commento, id_recensione_padre.
 * Una risposta del ristoratore è una riga con id_recensione_padre valorizzato (e voto NULL).
 * Gli alias "stelle", "testo", "risposta" mantengono invariata la classe Recensione.
 */
public class RecensioneDAO {

    private DBManager dbManager;

    public RecensioneDAO(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<Recensione> findByRistorante(int idRistorante) throws SQLException {
        List<Recensione> lista = new ArrayList<>();
        String query = """
                SELECT r.id, r.id_ristorante, r.id_utente,
                       r.voto AS stelle,
                       r.commento AS testo,
                       (SELECT f.commento
                          FROM recensioni f
                         WHERE f.id_recensione_padre = r.id
                         ORDER BY f.data_inserimento
                         LIMIT 1) AS risposta
                FROM recensioni r
                WHERE r.id_ristorante = ? AND r.id_recensione_padre IS NULL
                ORDER BY r.data_inserimento DESC
                """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
                INSERT INTO recensioni (id_ristorante, id_utente, voto, commento)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idRistorante);
            pstmt.setInt(2, idUtente);
            pstmt.setInt(3, stelle);
            pstmt.setString(4, testo);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean modifica(int id, int stelle, String testo) throws SQLException {
        // Solo recensioni principali: le risposte non hanno voto
        String query = """
                UPDATE recensioni SET voto = ?, commento = ?
                WHERE id = ? AND id_recensione_padre IS NULL
                """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, stelle);
            pstmt.setString(2, testo);
            pstmt.setInt(3, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean elimina(int id) throws SQLException {
        // Le eventuali risposte vengono eliminate dal database (ON DELETE CASCADE)
        String query = "DELETE FROM recensioni WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Aggiunge (o sostituisce) la risposta del ristoratore alla recensione con questo id.
     * L'autore della risposta è il proprietario del ristorante.
     */
    public boolean aggiungiRisposta(int id, String risposta) throws SQLException {
        String aggiorna = "UPDATE recensioni SET commento = ? WHERE id_recensione_padre = ?";
        String inserisci = """
                INSERT INTO recensioni (id_ristorante, id_utente, id_recensione_padre, commento)
                SELECT rec.id_ristorante, rist.id_proprietario, rec.id, ?
                FROM recensioni rec
                JOIN ristorantitheknife rist ON rist.id = rec.id_ristorante
                WHERE rec.id = ? AND rec.id_recensione_padre IS NULL
                """;
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(aggiorna)) {
                pstmt.setString(1, risposta);
                pstmt.setInt(2, id);
                if (pstmt.executeUpdate() > 0)
                    return true; // esisteva già una risposta: sostituita
            }
            try (PreparedStatement pstmt = conn.prepareStatement(inserisci)) {
                pstmt.setString(1, risposta);
                pstmt.setInt(2, id);
                return pstmt.executeUpdate() > 0;
            }
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