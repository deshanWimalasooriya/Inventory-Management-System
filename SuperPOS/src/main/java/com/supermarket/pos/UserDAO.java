package com.supermarket.pos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public String authenticate(String username, String password) {
        String query = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role"); // Returns "Admin" or "Cashier"
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null; // Login failed
    }

    public boolean changePassword(String username, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean addUser(String username, String password, String role) {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }
}