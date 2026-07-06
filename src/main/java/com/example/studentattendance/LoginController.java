package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT role, id FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int userId = rs.getInt("id");

                if (role.equals("admin")) {
                    loadDashboard("/fxml/admin_dashboard.fxml", "Admin Dashboard", -1); // No ID needed
                } else if (role.equals("lecturer")) {
                    loadDashboard("/fxml/lecturer_dashboard.fxml", "Lecturer Dashboard", userId);
                }
            } else {
                showAlert("Login Failed", "Invalid credentials. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred during login.");
        }
    }

    private void loadDashboard(String fxmlPath, String title, int lecturerId) throws Exception {
        Stage stage = (Stage) usernameField.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // If it's the lecturer dashboard, pass the lecturerId
        if (lecturerId != -1 && fxmlPath.contains("lecturer_dashboard")) {
            LecturerDashboardController controller = loader.getController();
            controller.setLecturerId(lecturerId);
        }

        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }


    @FXML
    public void goToSignup(ActionEvent event) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/signup.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Signup");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load signup screen.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
