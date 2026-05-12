package com.supermarket.pos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public Product getProductByBarcode(String barcode) {
        String query = "SELECT name, price, stock FROM products WHERE barcode = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // We found it! Build a Product object and send it back.
                return new Product(
                        barcode,
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product: " + e.getMessage());
        }

        // Return null if the product wasn't found in the database
        return null;
    }

    // Add this right below your getProductByBarcode method
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT barcode, name, price, stock FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getString("barcode"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all products: " + e.getMessage());
        }

        return products;
    }

    // 1. Search by Name (For Manual Entry)
    public List<Product> searchProductsByName(String keyword) {
        List<Product> products = new ArrayList<>();
        // The '%' symbols allow for partial matches (e.g., typing "Coff" finds "Coffee")
        String query = "SELECT barcode, name, price, stock FROM products WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(new Product(
                        rs.getString("barcode"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching by name: " + e.getMessage());
        }
        return products;
    }

    // 2. Update Stock safely in the Database
    public boolean addStock(String barcode, int quantityToAdd) {
        // Notice we do 'stock = stock + ?' to prevent math errors if multiple people use the system
        String query = "UPDATE products SET stock = stock + ? WHERE barcode = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, quantityToAdd);
            stmt.setString(2, barcode);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Returns true if the update was successful

        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }
}