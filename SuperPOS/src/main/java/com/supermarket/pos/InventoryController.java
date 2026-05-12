package com.supermarket.pos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import javafx.scene.control.TextField;



public class InventoryController {

    @FXML private TableView<Product> inventoryTable;
    @FXML private TableColumn<Product, String> colBarcode;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    // Add these variables at the top
    @FXML private TextField txtUpdateBarcode;
    @FXML private TextField txtAddQty;

    private final ProductDAO productDAO = new ProductDAO();

    @FXML
    public void initialize() {
        // Link the columns directly to the variable names in Product.java
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Fetch data and fill the table!
        loadInventoryData();
    }

    // Add this method at the bottom
    @FXML
    void handleUpdateStock() {
        String barcode = txtUpdateBarcode.getText().trim();
        String qtyText = txtAddQty.getText().trim();

        if (barcode.isEmpty() || qtyText.isEmpty()) {
            System.err.println("Please enter both barcode and quantity.");
            return;
        }

        try {
            int qtyToAdd = Integer.parseInt(qtyText);

            // Send the update to the MySQL Database!
            boolean success = productDAO.addStock(barcode, qtyToAdd);

            if (success) {
                System.out.println("✅ Stock updated successfully for: " + barcode);
                txtUpdateBarcode.clear();
                txtAddQty.clear();

                // Refresh the table to show the new stock levels instantly
                loadInventoryData();
            } else {
                System.err.println("❌ Failed to update. Is the barcode correct?");
            }
        } catch (NumberFormatException e) {
            System.err.println("Quantity must be a valid number!");
        }
    }

    private void loadInventoryData() {
        List<Product> productList = productDAO.getAllProducts();
        ObservableList<Product> observableList = FXCollections.observableArrayList(productList);
        inventoryTable.setItems(observableList);
    }
}