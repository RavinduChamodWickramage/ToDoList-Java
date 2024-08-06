package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static DBConnection dbConnection;
    private Connection connection;

    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/todolist", "root", "19982001");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static DBConnection getInstance() {
        if(dbConnection == null) {
            dbConnection = new DBConnection();
        }

        return dbConnection;
    }

    public Connection getConnection() {
        return connection;
    }
}
