package theknife.server;

import java.sql.*;

public class DBManager {

    private String url;
    private String user;
    private String password;

    public DBManager() {
        this.url = "jdbc:postgresql://localhost:5432/theknife";
        this.user = "postgres";
        this.password = "admin";
    }

    public DBManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    //crea una nuova connessione ogni volta invece di restituirne sempre una
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}