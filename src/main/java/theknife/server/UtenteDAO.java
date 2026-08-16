package theknife.server;

import theknife.common.Utente;
import java.sql.*;

public class UtenteDAO {

    public Utente autentica(String username, String passwordCifrata) {
        String query = "SELECT nome, cognome, username, ruolo FROM Utenti WHERE username = ? AND password = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, passwordCifrata); // Nota: idealmente qui si confronta l'hash
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Utente(
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
        return null; // Autenticazione fallita
    }
}