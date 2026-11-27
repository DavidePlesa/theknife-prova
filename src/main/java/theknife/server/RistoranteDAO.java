package theknife.server;

import theknife.common.Ristorante;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RistoranteDAO {

    // Tutte le cucine di un ristorante in una sola stringa ("Italian, Pizza"):
    // evita le righe duplicate che si ottenevano con il JOIN su ristorante_cucina.
    // (package-private: la usa anche UtenteDAO)
    static final String CUCINE_AGG = """
            (SELECT string_agg(t.nome, ', ' ORDER BY t.nome)
               FROM ristorante_cucina rc
               JOIN tipicucina t ON t.id = rc.id_cucina
              WHERE rc.id_ristorante = r.id) AS tipo_cucina
            """;

    // Distanza in km con correzione cos(latitudine) sulla longitudine.
    // Contiene 3 segnaposto: lat, lon, lat.
    private static final String DISTANZA_KM =
            "sqrt(power((l.latitudine - ?) * 111.0, 2) "
          + "+ power((l.longitudine - ?) * 111.0 * cos(radians(?)), 2))";

    private DBManager dbManager;

    public RistoranteDAO(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Due modalità che si escludono a vicenda:
     *  - citta != null  -> solo i ristoranti di quella città, nessun filtro di distanza
     *                      (citta è un valore di luoghi.location, es. "Milan, Italy";
     *                       va bene anche l'inizio, es. "milan", senza distinzione di maiuscole)
     *  - citta == null  -> ristoranti entro raggioKm da (lat, lon), ordinati per distanza
     * Se mancano lat/lon/raggioKm (es. ospite) e non c'è città, nessun filtro geografico.
     * fasciaPrezzo = numero di simboli del prezzo (1 = "$", 2 = "$$", ...).
     */
    public List<Ristorante> cerca(String citta, String cucina, Integer fasciaPrezzo,
            Boolean delivery, Boolean prenotazione, Integer stelleMin,
            Double lat, Double lon, Integer raggioKm, int pagina) throws SQLException {

        List<Ristorante> lista = new ArrayList<>();
        int pageSize = 10;
        int offset = (pagina - 1) * pageSize;

        boolean hasCitta = citta != null && !citta.isBlank();
        boolean hasCucina = cucina != null && !cucina.isBlank();
        boolean perDistanza = !hasCitta && lat != null && lon != null && raggioKm != null;

        StringBuilder query = new StringBuilder(
                "SELECT r.*, l.location, l.address, l.latitudine, l.longitudine, "
              + CUCINE_AGG
              + "FROM ristorantitheknife r "
              + "JOIN luoghi l ON l.id = r.id_luogo "
              + "WHERE 1=1 ");

        // L'ordine in cui si accodano i filtri è lo stesso dei set... più sotto.
        if (perDistanza)
            query.append("AND ").append(DISTANZA_KM).append(" <= ? ");
        if (hasCitta)
            query.append("AND starts_with(LOWER(l.location), LOWER(?)) ");
        if (hasCucina)
            query.append("""
                    AND EXISTS (SELECT 1
                                  FROM ristorante_cucina rc
                                  JOIN tipicucina t ON t.id = rc.id_cucina
                                 WHERE rc.id_ristorante = r.id
                                   AND LOWER(t.nome) = LOWER(?))\s
                    """);
        if (fasciaPrezzo != null)
            query.append("AND char_length(r.price) = ? ");
        if (delivery != null)
            query.append("AND r.delivery = ? ");
        if (prenotazione != null)
            query.append("AND r.booking = ? ");
        if (stelleMin != null)
            query.append("AND (SELECT AVG(voto) FROM recensioni WHERE id_ristorante = r.id) >= ? ");

        if (perDistanza)
            query.append("ORDER BY ").append(DISTANZA_KM).append(", r.id ");
        else
            query.append("ORDER BY r.id ");
        query.append("LIMIT ? OFFSET ?");

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
            int i = 1;
            if (perDistanza) {
                pstmt.setDouble(i++, lat);
                pstmt.setDouble(i++, lon);
                pstmt.setDouble(i++, lat);
                pstmt.setInt(i++, raggioKm);
            }
            if (hasCitta)
                pstmt.setString(i++, citta.trim());
            if (hasCucina)
                pstmt.setString(i++, cucina.trim());
            if (fasciaPrezzo != null)
                pstmt.setInt(i++, fasciaPrezzo);
            if (delivery != null)
                pstmt.setBoolean(i++, delivery);
            if (prenotazione != null)
                pstmt.setBoolean(i++, prenotazione);
            if (stelleMin != null)
                pstmt.setInt(i++, stelleMin);
            if (perDistanza) {
                pstmt.setDouble(i++, lat);
                pstmt.setDouble(i++, lon);
                pstmt.setDouble(i++, lat);
            }
            pstmt.setInt(i++, pageSize);
            pstmt.setInt(i, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    lista.add(mapRistoranteCompatto(rs));
            }
        }
        return lista;
    }

    /**
     * Elenco per la combobox: i valori distinti di luoghi.location, così come sono nel database.
     */
    public List<String> elencoCitta() throws SQLException {
        List<String> citta = new ArrayList<>();
        String query = "SELECT DISTINCT location FROM luoghi WHERE location IS NOT NULL ORDER BY location";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next())
                citta.add(rs.getString(1));
        }
        return citta;
    }

    public Ristorante findById(int id) throws SQLException {
        String query = "SELECT r.*, l.location, l.address, l.latitudine, l.longitudine, "
                + CUCINE_AGG
                + "FROM ristorantitheknife r "
                + "JOIN luoghi l ON l.id = r.id_luogo "
                + "WHERE r.id = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return mapRistoranteCompleto(rs);
            }
        }
        return null;
    }

    public boolean inserisci(Ristorante r) throws SQLException {
        String queryLuogo = """
                INSERT INTO luoghi (location, address, latitudine, longitudine)
                VALUES (?, ?, ?, ?)
                """;
        String queryRistorante = """
                INSERT INTO ristorantitheknife
                (nome, id_proprietario, id_luogo, price, phone_number, url,
                 website_url, award, green_star, facilities_and_services,
                 description, delivery, booking)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false); // transazione: o tutto o niente
            try {
                int idLuogo;
                try (PreparedStatement pstmt = conn.prepareStatement(
                        queryLuogo, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, r.getLocation());
                    pstmt.setString(2, r.getAddress());
                    pstmt.setDouble(3, r.getLatitudine());
                    pstmt.setDouble(4, r.getLongitudine());
                    pstmt.executeUpdate();
                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        if (!keys.next())
                            throw new SQLException("Inserimento luogo fallito");
                        idLuogo = keys.getInt(1);
                    }
                }

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
                    boolean ok = pstmt.executeUpdate() > 0;
                    conn.commit();
                    return ok;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Ristorante> findByRistoratore(int idRistoratore) throws SQLException {
        List<Ristorante> lista = new ArrayList<>();
        String query = "SELECT r.*, l.location, l.address, l.latitudine, l.longitudine, "
                + CUCINE_AGG
                + "FROM ristorantitheknife r "
                + "JOIN luoghi l ON l.id = r.id_luogo "
                + "WHERE r.id_proprietario = ? "
                + "ORDER BY r.id";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idRistoratore);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    lista.add(mapRistoranteCompatto(rs));
            }
        }
        return lista;
    }

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
                rs.getString("tipo_cucina"));
    }

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
                rs.getString("tipo_cucina"));
    }
}