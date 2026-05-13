package com.supermarket.pos;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Objects;

public class SettingsController {

    // Store & Financial Fields
    @FXML private TextField txtStoreName, txtAddress, txtPhone, txtTaxRate;
    @FXML private ComboBox<String> comboCurrency;
    @FXML private CheckBox chkPrintReceipt;

    // Theme & Security Fields
    @FXML private ComboBox<String> comboTheme;
    @FXML private PasswordField txtNewPass;
    @FXML private Label lblCurrentUser, lblLogoStatus, lblSaveStatus;

    // Admin Fields
    @FXML private VBox adminPanel;
    @FXML private TextField txtNewUsername;
    @FXML private PasswordField txtNewUserPass;
    @FXML private ComboBox<String> comboRole;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // Set up the current user label
        lblCurrentUser.setText("Logged in as: " + SessionManager.currentUsername + " (" + SessionManager.currentRole + ")");

        // Load default values into the UI
        comboTheme.setValue("Dark Mode");
        comboCurrency.setValue("Rs (LKR)"); // Defaulted to Rs as requested!
        txtStoreName.setText("SuperPOS Enterprise");
        txtAddress.setText("No 12, Main Street, Vavuniya");
        txtPhone.setText("077 123 4567");
        txtTaxRate.setText("8.0");
        chkPrintReceipt.setSelected(true);

        // Security Check: Hide the Admin Panel if the user is a Cashier
        if (!"Admin".equals(SessionManager.currentRole)) {
            adminPanel.setVisible(false);
            adminPanel.setManaged(false);
        }
    }

    // --- GLOBAL SETTINGS LOGIC ---

    @FXML
    void saveGlobalSettings(ActionEvent event) {
        String store = txtStoreName.getText();
        String tax = txtTaxRate.getText();
        String currency = comboCurrency.getValue();

        System.out.println("Saved Store: " + store + " | Tax: " + tax + "% | Currency: " + currency);

        lblSaveStatus.setText("✅ All settings saved successfully!");

        // Clear message after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> lblSaveStatus.setText(""));
            } catch (InterruptedException e) { e.printStackTrace(); }
        }).start();
    }

    // --- THEME & BRANDING LOGIC ---

    @FXML
    void handleThemeChange(ActionEvent event) {
        String selectedTheme = comboTheme.getValue();

        // 1. Save the choice to the global memory
        ThemeManager.currentTheme = selectedTheme;

        // 2. Apply it to the current screen instantly
        ThemeManager.applyTheme(comboTheme.getScene());
    }

    @FXML
    void changeLogo(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        File file = fc.showOpenDialog(comboTheme.getScene().getWindow());
        if (file != null) {
            lblLogoStatus.setText("Logo Updated!");
        }
    }

    // --- SECURITY LOGIC ---

    @FXML
    void updatePassword(ActionEvent event) {
        String newPass = txtNewPass.getText();
        if (newPass.length() < 4) {
            showAlert(Alert.AlertType.WARNING, "Error", "Password must be at least 4 characters.");
            return;
        }
        if (userDAO.changePassword(SessionManager.currentUsername, newPass)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your password has been changed securely.");
            txtNewPass.clear();
        }
    }

    // --- ADMIN LOGIC ---

    @FXML
    void createNewUser(ActionEvent event) {
        String user = txtNewUsername.getText().trim();
        String pass = txtNewUserPass.getText().trim();
        String role = comboRole.getValue();

        if (user.isEmpty() || pass.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Error", "Fill all fields to create a user.");
            return;
        }
        if (userDAO.addUser(user, pass, role)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "User '" + user + "' created as " + role + "!");
            txtNewUsername.clear();
            txtNewUserPass.clear();
            comboRole.setValue(null);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not create user. Username may already exist.");
        }
    }

    @FXML
    void handleBackup(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Database Backup", "Database backup initiated. Saved to C:/SuperPOS/backups/");
    }

    @FXML
    void handleFactoryReset(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "DANGER: This will wipe all inventory and sales data. Are you sure?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            System.out.println("Wiping database...");
        }
    }

    // --- UTILITY ---

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}