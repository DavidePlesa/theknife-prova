package theknife.server;

import theknife.common.Ristorante;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RistoranteDAO {

    private DBConnection dbManager;

    public RistoranteDAO(DBConnection dbManager) {
        this.dbManager = dbManager;
    }

    public List<Ristorante> cerca(String citta, String cucina, Double prezzoMax, Boolean delivery, Boolean prenotazione,
            Integer stelleMin, int pagina) throws SQLException {

        List<Ristorante> lista = new ArrayList<>();
        int pageSize = 10;
        int offset = (pagina - 1) * pageSize;

        // Costruisce la query dinamicamente
        StringBuilder query = new StringBuilder(
                "SELECT r.* FROM RistorantiTheKnife r ");

        if (stelleMin != null) {
            query.append("LEFT JOIN Recensioni rec ON r.id = rec.id_ristorante ");
        }

        query.append("WHERE LOWER(r.citta) = LOWER(?) ");

        if (cucina != null)
            query.append("AND LOWER(r.tipo_cucina) = LOWER(?) ");
        if (prezzoMax != null)
            query.append("AND r.prezzo_medio <= ? ");
        if (delivery != null)
            query.append("AND r.delivery = ? ");
        if (prenotazione != null)
            query.append("AND r.prenotazione = ? ");

        if (stelleMin != null) {
            query.append("GROUP BY r.id HAVING AVG(rec.stelle) >= ? ");
        }

        query.append("LIMIT ? OFFSET ?");

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query.toString())) {

            int i = 1;
            pstmt.setString(i++, citta);
            if (cucina != null)
                pstmt.setString(i++, cucina);
            if (prezzoMax != null)
                pstmt.setDouble(i++, prezzoMax);
            if (delivery != null)
                pstmt.setBoolean(i++, delivery);
            if (prenotazione != null)
                pstmt.setBoolean(i++, prenotazione);
            if (stelleMin != null)
                pstmt.setInt(i++, stelleMin);
            pstmt.setInt(i++, pageSize);
            pstmt.setInt(i, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRistorante(rs));
                }
            }
        }
        return lista;
    }

    public Ristorante findById(int id) throws SQLException {
        String query = "SELECT * FROM RistorantiTheKnife WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return mapRistorante(rs);
            }
        }
        return null;
    }

    public boolean inserisci(Ristorante r) throws SQLException {
        String query = """
                INSERT INTO RistorantiTheKnife
                (nome, nazione, citta, indirizzo, latitudine, longitudine,
                 prezzo_medio, delivery, prenotazione, tipo_cucina, id_ristoratore)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, r.getNome());
            pstmt.setString(2, r.getNazione());
            pstmt.setString(3, r.getCitta());
            pstmt.setString(4, r.getIndirizzo());
            pstmt.setDouble(5, r.getLatitudine());
            pstmt.setDouble(6, r.getLongitudine());
            pstmt.setDouble(7, r.getPrezzoMedio());
            pstmt.setBoolean(8, r.isDelivery());
            pstmt.setBoolean(9, r.isPrenotazione());
            pstmt.setString(10, r.getTipoCucina());
            pstmt.setInt(11, r.getIdRistoratore());
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Ristorante> findByRistoratore(int idRistoratore) throws SQLException {
        List<Ristorante> lista = new ArrayList<>();
        String query = "SELECT * FROM RistorantiTheKnife WHERE id_ristoratore = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, idRistoratore);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    lista.add(mapRistorante(rs));
            }
        }
        return lista;
    }

    // Mappa un ResultSet in un oggetto Ristorante
    private Ristorante mapRistorante(ResultSet rs) throws SQLException {
        return new Ristorante(
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
        );
    }
}