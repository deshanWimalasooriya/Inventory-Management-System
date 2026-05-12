package com.supermarket.pos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Update these credentials to match your local MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/superpos";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Change to your actual MySQL password

    public static Connection getConnection() {
        try {
            // Establishes the link using the MySQL driver downloaded by Maven
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return null;
        }
    }
}