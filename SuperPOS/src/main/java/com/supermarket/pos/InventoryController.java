package com.supermarket.pos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class InventoryController {

    @FXML private TableView<Product> inventoryTable;
    @FXML private TableColumn<Product, String> colBarcode, colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;

    @FXML private TextField searchField;
    @FXML private TextField txtUpdateBarcode, txtAddQty;
    @FXML private Label lblStatus;

    private final ProductDAO productDAO = new ProductDAO();
    private ObservableList<Product> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Link columns
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Load data into table
        loadInventoryData();

        // REAL-TIME SEARCH LOGIC
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                inventoryTable.setItems(masterData);
            } else {
                ObservableList<Product> filteredData = FXCollections.observableArrayList();
                String searchString = newValue.toLowerCase();

                for (Product product : masterData) {
                    // Check if barcode or name contains the typed text
                    if (product.getBarcode().toLowerCase().contains(searchString) ||
                            product.getName().toLowerCase().contains(searchString)) {
                        filteredData.add(product);
                    }
                }
                inventoryTable.setItems(filteredData);
            }
        });
    }

    @FXML
    void loadInventoryData() {
        masterData.clear();
        masterData.addAll(productDAO.getAllProducts());
        inventoryTable.setItems(masterData);
        searchField.clear(); // Reset search when refreshing
        lblStatus.setText("");
    }

    @FXML
    void handleUpdateStock() {
        String barcode = txtUpdateBarcode.getText().trim();
        String qtyText = txtAddQty.getText().trim();

        if (barcode.isEmpty() || qtyText.isEmpty()) {
            lblStatus.setStyle("-fx-text-fill: #ef4444;"); // Red error color
            lblStatus.setText("Missing fields!");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyText);
            if (productDAO.addStock(barcode, qty)) {
                txtUpdateBarcode.clear();
                txtAddQty.clear();
                loadInventoryData();
                lblStatus.setStyle("-fx-text-fill: #10b981;"); // Green success color
                lblStatus.setText("Stock Added!");
            } else {
                lblStatus.setStyle("-fx-text-fill: #ef4444;");
                lblStatus.setText("Barcode not found.");
            }
        } catch (NumberFormatException e) {
            lblStatus.setStyle("-fx-text-fill: #ef4444;");
            lblStatus.setText("Qty must be a number.");
        }
    }
}