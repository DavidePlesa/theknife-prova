package theknife.server;

import theknife.common.Ristorante;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RistoranteDAO {

    private DBManager dbManager;

    public RistoranteDAO(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<Ristorante> cerca(String location, String cucina,
            Double prezzoMax, Boolean delivery, Boolean prenotazione,
            Integer stelleMin, int pagina) throws SQLException {

        List<Ristorante> lista = new ArrayList<>();
        int pageSize = 10;
        int offset = (pagina - 1) * pageSize;

        StringBuilder query = new StringBuilder("""
                SELECT r.*, l.location, l.address, l.latitudine, l.longitudine,
                       t.nome AS tipo_cucina
                FROM ristorantitheknife r
                JOIN luoghi l ON r.id_luogo = l.id
                LEFT JOIN ristorante_cucina rc ON r.id = rc.id_ristorante
                LEFT JOIN tipicucina t ON rc.id_cucina = t.id
                """);

        if (stelleMin != null)
            query.append("LEFT JOIN recensioni rec ON r.id = rec.id_ristorante ");

        query.append("WHERE LOWER(l.location) LIKE LOWER(?) ");

        if (cucina != null)
            query.append("AND LOWER(t.nome) = LOWER(?) ");
        if (delivery != null)
            query.append("AND r.delivery = ? ");
        if (prenotazione != null)
            query.append("AND r.booking = ? ");

        if (stelleMin != null)
            query.append("GROUP BY r.id, l.location, l.address, l.latitudine, l.longitudine, t.nome ")
                 .append("HAVING AVG(rec.voto) >= ? ");
        else
            query.append("GROUP BY r.id, l.location, l.address, l.latitudine, l.longitudine, t.nome ");

        query.append("LIMIT ? OFFSET ?");

        try (PreparedStatement pstmt =
                dbManager.getConnection().prepareStatement(query.toString())) {

            int i = 1;
            pstmt.setString(i++, "%" + location + "%");
            if (cucina != null)      pstmt.setString(i++, cucina);
            if (delivery != null)    pstmt.setBoolean(i++, delivery);
            if (prenotazione != null) pstmt.setBoolean(i++, prenotazione);
            if (stelleMin != null)   pstmt.setInt(i++, stelleMin);
            pstmt.setInt(i++, pageSize);
            pstmt.setInt(i, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    lista.add(mapRistoranteCompatto(rs));
            }
        }
        return lista;
    }

    public Ristorante findById(int id) throws SQLException {
        String query = """
                SELECT r.*, l.location, l.address, l.latitudine, l.longitudine,
                       t.nome AS tipo_cucina
                FROM ristorantitheknife r
                JOIN luoghi l ON r.id_luogo = l.id
                LEFT JOIN ristorante_cucina rc ON r.id = rc.id_ristorante
                LEFT JOIN tipicucina t ON rc.id_cucina = t.id
                WHERE r.id = ?
                """;
        try (PreparedStatement pstmt =
                dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return mapRistoranteCompleto(rs);
            }
        }
        return null;
    }

    public boolean inserisci(Ristorante r) throws SQLException {
        Connection conn = dbManager.getConnection();
        // Prima inserisci il luogo e recupera l'id generato
        String queryLuogo = """
                INSERT INTO luoghi (location, address, latitudine, longitudine)
                VALUES (?, ?, ?, ?)
                """;
        int idLuogo;
        try (PreparedStatement pstmt = conn.prepareStatement(
                queryLuogo, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, r.getLocation());
            pstmt.setString(2, r.getAddress());
            pstmt.setDouble(3, r.getLatitudine());
            pstmt.setDouble(4, r.getLongitudine());
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (!keys.next()) throw new SQLException("Inserimento luogo fallito");
            idLuogo = keys.getInt(1);
        }

        // Poi inserisci il ristorante
        String queryRistorante = """
                INSERT INTO ristorantitheknife
                (nome, id_proprietario, id_luogo, price, phone_number, url,
                 website_url, award, green_star, facilities_and_services,
                 description, delivery, booking)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(queryRistorante)) {
            pstmt.setString(1, r.getNome());
            pstmt.setInt(2, r.getIdProprietario());
            pstmt.setInt(3, idLuogo);
            pstmt.setString(4, r.getPrice());
            pstmt.setString(5, r.getPhoneNumber());
            pstmt.setString(6, r.getUrl());
            pstmt.setString(7, r.getWebsiteUrl());
            pstmt.setString(8, r.getAward());
            pstmt.setBoolean(9, r.isGreenStar());
            pstmt.setString(10, r.getFacilitiesAndServices());
            pstmt.setString(11, r.getDescription());
            pstmt.setBoolean(12, r.isDelivery());
            pstmt.setBoolean(13, r.isBooking());
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Ristorante> findByRistoratore(int idRistoratore) throws SQLException {
        List<Ristorante> lista = new ArrayList<>();
        String query = """
                SELECT r.*, l.location, l.address, l.latitudine, l.longitudine,
                       t.nome AS tipo_cucina
                FROM ristorantitheknife r
                JOIN luoghi l ON r.id_luogo = l.id
                LEFT JOIN ristorante_cucina rc ON r.id = rc.id_ristorante
                LEFT JOIN tipicucina t ON rc.id_cucina = t.id
                WHERE r.id_proprietario = ?
                GROUP BY r.id, l.location, l.address, l.latitudine, l.longitudine, t.nome
                """;
        try (PreparedStatement pstmt =
                dbManager.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, idRistoratore);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    lista.add(mapRistoranteCompatto(rs));
            }
        }
        return lista;
    }

    // Mappa completa con tutti i campi (per visualizzaRistorante)
    private Ristorante mapRistoranteCompleto(ResultSet rs) throws SQLException {
        return new Ristorante(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getInt("id_proprietario"),
            rs.getString("location"),
            rs.getString("address"),
            rs.getDouble("latitudine"),
            rs.getDouble("longitudine"),
            rs.getString("price"),
            rs.getString("phone_number"),
            rs.getString("url"),
            rs.getString("website_url"),
            rs.getString("award"),
            rs.getBoolean("green_star"),
            rs.getString("facilities_and_services"),
            rs.getString("description"),
            rs.getBoolean("delivery"),
            rs.getBoolean("booking"),
            rs.getString("tipo_cucina")
        );
    }

    // Mappa compatta per liste e ricerche
    private Ristorante mapRistoranteCompatto(ResultSet rs) throws SQLException {
        return new Ristorante(
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
            rs.getString("tipo_cucina")
        );
    }
}