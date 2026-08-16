package theknife.server;

import java.sql.*;

public class DBConnection {

    private Connection connection;

    public DBConnection() {
        try {
            connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/theknife", "postgres", "admin");
            System.out.println("Server: Connessione con il DB riuscita.");
        } catch (SQLException e) {
            System.out.println("Server: Connessione con il DB fallita.");
            e.printStackTrace();
        }
    }

    public DBConnection(String url, String user, String psw) {
        try {
            connection = DriverManager.getConnection(url, user, psw);
            System.out.println("Server: Connessione con il DB riuscita.");
        } catch (SQLException e) {
            System.out.println("Server: Connessione con il DB fallita.");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}