package com.supermarket.pos;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML private BorderPane mainPane;
    @FXML private Button btnBilling, btnVisualStock, btnInventory, btnReports, btnSettings;
    @FXML private TextField scannerInput;

    // Classic Table Variables
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colId, colName;
    @FXML private TableColumn<CartItem, Double> colPrice, colTotal;
    @FXML private TableColumn<CartItem, Integer> colQty;
    @FXML private TableColumn<CartItem, Void> colAction;

    @FXML private Label lblTotalAmount, lblTax;

    private Node billingView;
    private final ProductDAO productDAO = new ProductDAO();
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private final double TAX_RATE = 0.08;

    @FXML
    public void initialize() {
        billingView = mainPane.getCenter();

        colId.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        addDeleteButtonToTable();
        cartTable.setItems(cartItems);

        Platform.runLater(() -> scannerInput.requestFocus());
    }

    // --- NAVIGATION LOGIC ---

    private void setActiveTab(Button activeButton) {
        btnBilling.getStyleClass().remove("active-nav");
        btnVisualStock.getStyleClass().remove("active-nav");
        btnInventory.getStyleClass().remove("active-nav");
        btnReports.getStyleClass().remove("active-nav");
        btnSettings.getStyleClass().remove("active-nav");
        activeButton.getStyleClass().add("active-nav");
    }

    @FXML
    void showBilling(ActionEvent event) {
        mainPane.setCenter(billingView);
        setActiveTab(btnBilling);
        Platform.runLater(() -> scannerInput.requestFocus());
    }

    @FXML
    void showVisualStock(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VisualStock.fxml"));
            mainPane.setCenter(loader.load());
            setActiveTab(btnVisualStock);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void showInventory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Inventory.fxml"));
            mainPane.setCenter(loader.load());
            setActiveTab(btnInventory);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void showReports(ActionEvent event) {
        System.out.println("Reports screen coming soon!");
        setActiveTab(btnReports);
    }

    @FXML
    void showSettings(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Settings.fxml"));
            mainPane.setCenter(loader.load());
            setActiveTab(btnSettings);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- BILLING LOGIC ---

    @FXML
    void handleScan(ActionEvent event) {
        String input = scannerInput.getText().trim();
        if (input.isEmpty()) return;

        Product foundItem = productDAO.getProductByBarcode(input);

        if (foundItem == null) {
            List<Product> manualResults = productDAO.searchProductsByName(input);
            if (manualResults.size() == 1) {
                foundItem = manualResults.get(0);
            } else if (manualResults.size() > 1) {
                showAlert(Alert.AlertType.WARNING, "Multiple Matches", "Found multiple items. Use Manual Search.");
                scannerInput.clear();
                return;
            }
        }

        if (foundItem != null) {
            addToCart(foundItem);
        } else {
            showAlert(Alert.AlertType.WARNING, "Not Found", "Item not found in database.");
        }
        scannerInput.clear();
    }

    @FXML
    void openManualSearch(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Manual Search");
        dialog.setHeaderText("Search Database");
        dialog.setContentText("Enter product name:");

        // Optional: Keep dialogs matching the theme
        dialog.getDialogPane().setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");
        dialog.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: white;");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(keyword -> {
            if (keyword.trim().isEmpty()) return;
            List<Product> matches = productDAO.searchProductsByName(keyword.trim());

            if (matches.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Not Found", "No matches found.");
            } else if (matches.size() == 1) {
                addToCart(matches.get(0));
            } else {
                ChoiceDialog<Product> choice = new ChoiceDialog<>(matches.get(0), matches);
                choice.setTitle("Select Item");
                choice.getDialogPane().setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");
                choice.showAndWait().ifPresent(this::addToCart);
            }
        });
        Platform.runLater(() -> scannerInput.requestFocus());
    }

    private void addToCart(Product foundItem) {
        if (foundItem.getStock() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Out of Stock", foundItem.getName() + " is out of stock!");
            return;
        }

        boolean itemExists = false;
        for (CartItem item : cartItems) {
            if (item.getBarcode().equals(foundItem.getBarcode())) {
                if (item.getQuantity() < foundItem.getStock()) {
                    item.setQuantity(item.getQuantity() + 1);
                    itemExists = true;
                } else {
                    showAlert(Alert.AlertType.WARNING, "Stock Limit", "Not enough stock available.");
                    itemExists = true;
                }
                break;
            }
        }

        if (!itemExists) {
            cartItems.add(new CartItem(foundItem, 1));
        }

        cartTable.refresh();
        calculateTotals();
    }

    private void calculateTotals() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotal();
        }

        double tax = Math.round((subtotal * TAX_RATE) * 100.0) / 100.0;
        double grandTotal = Math.round((subtotal + tax) * 100.0) / 100.0;

        lblTax.setText(String.format("Tax (8%%): $%.2f", tax));
        lblTotalAmount.setText(String.format("$%.2f", grandTotal));
    }

    private void addDeleteButtonToTable() {
        Callback<TableColumn<CartItem, Void>, TableCell<CartItem, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<CartItem, Void> call(final TableColumn<CartItem, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("X");
                    {
                        btn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5px;");
                        btn.setOnAction((ActionEvent event) -> {
                            CartItem item = getTableView().getItems().get(getIndex());
                            cartItems.remove(item);
                            calculateTotals();
                            Platform.runLater(() -> scannerInput.requestFocus());
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) setGraphic(null);
                        else {
                            setGraphic(btn);
                            setAlignment(javafx.geometry.Pos.CENTER);
                        }
                    }
                };
            }
        };
        colAction.setCellFactory(cellFactory);
    }

    @FXML
    void processCheckout(ActionEvent event) {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "No items to checkout.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Finalize sale and deduct stock?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            if (productDAO.processCheckoutStock(cartItems)) {
                cartItems.clear();
                calculateTotals();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Sale Finalized! Stock Deducted.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update database.");
            }
        }
        Platform.runLater(() -> scannerInput.requestFocus());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}