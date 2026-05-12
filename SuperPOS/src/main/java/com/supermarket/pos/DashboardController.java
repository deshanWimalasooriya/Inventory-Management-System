package com.supermarket.pos;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.application.Platform;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private TextField scannerInput;
    @FXML private TableView<?> cartTable;
    @FXML private Label lblTotalAmount;

    // Instantiate our Database Worker
    private final ProductDAO productDAO = new ProductDAO();

    @FXML
    public void initialize() {
        // Keep the scanner text field active
        Platform.runLater(() -> scannerInput.requestFocus());
    }

    @FXML
    void handleScan(ActionEvent event) {
        String input = scannerInput.getText().trim();

        if (!input.isEmpty()) {
            // STEP 1: Assume it's a barcode scanner reading an exact ID
            Product foundItem = productDAO.getProductByBarcode(input);

            // STEP 2: If barcode fails, assume the cashier typed a manual name!
            if (foundItem == null) {
                System.out.println("Barcode not found, searching manually by name...");
                List<Product> manualResults = productDAO.searchProductsByName(input);

                if (manualResults.size() == 1) {
                    // Exactly one match found manually
                    foundItem = manualResults.get(0);
                } else if (manualResults.size() > 1) {
                    // Multiple matches (e.g., they typed "Coffee", but you sell 3 types of coffee)
                    showAlert("Multiple Matches", "Found multiple items for '" + input + "'. Please type a more specific name or use the barcode.");
                    scannerInput.clear();
                    return;
                }
            }

            // STEP 3: Process the final item (whether found by scanner or manual typing)
            if (foundItem != null) {
                System.out.println("✅ ITEM ADDED TO CART:");
                System.out.println("Item: " + foundItem.getName() + " | Price: $" + foundItem.getPrice());
                // (Next Step: We will add 'foundItem' to the visual cartTable here)
            } else {
                showAlert("Item Not Found", "No product found for barcode or name: " + input);
            }

            scannerInput.clear();
        }
    }

    @FXML
    void processCheckout(ActionEvent event) {
        System.out.println("Checkout Initiated!");
    }

    @FXML
    void openInventoryWindow(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Inventory.fxml"));
            VBox root = loader.load();

            // Create a new window (Stage)
            Stage stage = new Stage();
            stage.setTitle("Store Inventory View");
            stage.setScene(new Scene(root, 650, 450));
            stage.show();

            // Re-focus the scanner input on the main window just in case
            Platform.runLater(() -> scannerInput.requestFocus());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not load the Inventory Window.");
        }
    }

    // A simple helper method to pop up an error box for the cashier
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}