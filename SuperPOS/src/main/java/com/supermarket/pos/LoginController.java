package com.supermarket.pos;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    private final UserDAO userDAO = new UserDAO();

    @FXML
    void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        String role = userDAO.authenticate(username, password);

        if (role != null) {
            // Save to session memory!
            SessionManager.currentUsername = username;
            SessionManager.currentRole = role;

            loadDashboard();
        } else {
            lblError.setText("Invalid username or password.");
        }
    }

    private void loadDashboard() {
        try {
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            BorderPane root = loader.load();

            Scene dashboardScene = new Scene(root, 1200, 800);

            // ---> FORCE THE MANAGER TO APPLY THE CORRECT THEME HERE <---
            ThemeManager.applyTheme(dashboardScene);

            stage.setScene(dashboardScene);
            stage.centerOnScreen();
        } catch (Exception e) { e.printStackTrace(); }
    }
}