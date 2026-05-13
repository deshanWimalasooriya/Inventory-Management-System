package com.supermarket.pos;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class VisualStockController {

    @FXML private FlowPane gridPane;
    @FXML private ComboBox<String> filterComboBox;

    // The New Form Components
    @FXML private VBox addFormPanel;
    @FXML private TextField txtBarcode, txtName, txtPrice, txtStock, txtImagePath;

    private final ProductDAO productDAO = new ProductDAO();

    @FXML
    public void initialize() {
        loadGrid("All Items");
    }

    @FXML
    void applyFilters() {
        loadGrid(filterComboBox.getValue());
    }

    // --- FORM PANEL LOGIC ---

    @FXML
    void openAddForm() {
        // Shows the panel
        addFormPanel.setVisible(true);
        addFormPanel.setManaged(true);
    }

    @FXML
    void closeAddForm() {
        // Hides the panel and clears the fields
        addFormPanel.setVisible(false);
        addFormPanel.setManaged(false);
        clearForm();
    }

    @FXML
    void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        // Opens the file explorer window
        File selectedFile = fileChooser.showOpenDialog(gridPane.getScene().getWindow());
        if (selectedFile != null) {
            // Save the absolute path so the program knows exactly where to look
            txtImagePath.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    void saveNewProduct() {
        try {
            String barcode = txtBarcode.getText().trim();
            String name = txtName.getText().trim();
            double price = Double.parseDouble(txtPrice.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());
            String imagePath = txtImagePath.getText().trim();

            if (barcode.isEmpty() || name.isEmpty() || imagePath.isEmpty()) {
                showAlert("Error", "Please fill in all fields and select an image.");
                return;
            }

            // Create the Product object
            Product newProduct = new Product(barcode, name, price, stock, imagePath);

            // Send to database
            if (productDAO.addProduct(newProduct)) {
                System.out.println("✅ New Product Saved Successfully!");
                closeAddForm();
                loadGrid(filterComboBox.getValue()); // Instantly refresh the visual grid
            } else {
                showAlert("Database Error", "Could not save the product. Does this barcode already exist?");
            }

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please ensure Price and Stock are valid numbers.");
        }
    }

    private void clearForm() {
        txtBarcode.clear();
        txtName.clear();
        txtPrice.clear();
        txtStock.clear();
        txtImagePath.clear();
    }

    // --- GRID GENERATION LOGIC ---

    private void loadGrid(String filter) {
        gridPane.getChildren().clear();
        for (Product product : productDAO.getFilteredProducts(filter)) {
            gridPane.getChildren().add(createProductCard(product));
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("product-card");

        ImageView imageView = new ImageView();
        try {
            Image img;
            String path = p.getImagePath();
            // Check if the database path is an absolute computer path (e.g., C:\images\...)
            if (path.contains("\\") || path.contains("/")) {
                img = new Image("file:" + path);
            } else {
                // Otherwise look in our internal resources folder
                img = new Image(getClass().getResourceAsStream("/images/" + path));
            }
            imageView.setImage(img);
        } catch (Exception e) {
            System.err.println("Could not load image: " + p.getImagePath());
        }

        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // CHANGE THIS:
        Label nameLbl = new Label(p.getName());
        nameLbl.getStyleClass().add("product-card-title"); // Using CSS class instead of setStyle!

        Label priceLbl = new Label("$" + p.getPrice());
        priceLbl.getStyleClass().add("product-card-price"); // Using CSS class!

        Label stockLbl = new Label("Stock: " + p.getStock());
        stockLbl.getStyleClass().add("product-card-stock"); // Using CSS class!

        Button btnEdit = new Button("Edit Item");
        // We can leave the button styled like this, or assign it a style class too.
        btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
        card.getChildren().addAll(imageView, nameLbl, priceLbl, stockLbl, btnEdit);
        return card;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}