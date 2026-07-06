package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SignUpController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML
    public void handleSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Validation Error", "All fields are required.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.executeUpdate();

            showAlert("Success", "Account created successfully!");
            goToLogin();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred during signup.");
        }
    }

    @FXML
    public void goToLogin() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not load login screen.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}