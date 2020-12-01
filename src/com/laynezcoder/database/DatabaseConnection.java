package com.laynezcoder.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {

    private Connection connection;
    private static DatabaseConnection instance;
    private static final String DATABASE = "YOUR_DATABASE";
    private static final String TIME_ZONE = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DATABASE + TIME_ZONE;
    private static final String USER = "YOUR_USER";
    private static final String PASSWORD = "YOUR_PASSWORD";

    public DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
