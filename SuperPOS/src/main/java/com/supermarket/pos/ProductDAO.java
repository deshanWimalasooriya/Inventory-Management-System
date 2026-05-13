package com.supermarket.pos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public Product getProductByBarcode(String barcode) {
        String query = "SELECT name, price, stock, image_path FROM products WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Product(barcode, rs.getString("name"), rs.getDouble("price"), rs.getInt("stock"), rs.getString("image_path"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product: " + e.getMessage());
        }
        return null;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT barcode, name, price, stock, image_path FROM products";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(new Product(rs.getString("barcode"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock"), rs.getString("image_path")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all products: " + e.getMessage());
        }
        return products;
    }

    public List<Product> searchProductsByName(String keyword) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT barcode, name, price, stock, image_path FROM products WHERE name LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(new Product(rs.getString("barcode"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock"), rs.getString("image_path")));
            }
        } catch (SQLException e) {
            System.err.println("Error searching by name: " + e.getMessage());
        }
        return products;
    }

    public List<Product> getFilteredProducts(String statusFilter) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT barcode, name, price, stock, image_path FROM products ";
        if (statusFilter.equals("In Stock")) {
            query += "WHERE stock > 0";
        } else if (statusFilter.equals("Out of Stock")) {
            query += "WHERE stock = 0";
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(new Product(rs.getString("barcode"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock"), rs.getString("image_path")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean addStock(String barcode, int quantityToAdd) {
        String query = "UPDATE products SET stock = stock + ? WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, quantityToAdd);
            stmt.setString(2, barcode);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }

    // Adds a new product to the MySQL database
    public boolean addProduct(Product p) {
        String query = "INSERT INTO products (barcode, name, price, stock, image_path) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, p.getBarcode());
            stmt.setString(2, p.getName());
            stmt.setDouble(3, p.getPrice());
            stmt.setInt(4, p.getStock());
            stmt.setString(5, p.getImagePath());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Returns true if insertion worked

        } catch (SQLException e) {
            System.err.println("Error adding new product: " + e.getMessage());
            return false;
        }
    }

    // Deducts stock for a whole cart at once
    public boolean processCheckoutStock(List<CartItem> cart) {
        String query = "UPDATE products SET stock = stock - ? WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Turn off auto-commit to ensure the whole cart succeeds or fails together
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                for (CartItem item : cart) {
                    stmt.setInt(1, item.getQuantity());
                    stmt.setString(2, item.getBarcode());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit(); // Commit the transaction
                return true;
            } catch (SQLException e) {
                conn.rollback(); // If one fails, undo all of them
                System.err.println("Checkout failed, rolling back: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}