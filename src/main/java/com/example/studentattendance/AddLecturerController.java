package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddLecturerController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField departmentField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;


    @FXML
    private void handleRegister() {

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String department = departmentField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();


        if (firstName.isEmpty()
                || lastName.isEmpty()
                || email.isEmpty()
                || department.isEmpty()
                || username.isEmpty()
                || password.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Missing Information",
                    "Please fill in all fields."
            );

            return;
        }


        // Validate email
        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Invalid Email",
                    "Please enter a valid email address."
            );

            return;
        }


        // Minimum password length
        if (password.length() < 6) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Weak Password",
                    "Password must contain at least 6 characters."
            );

            return;
        }


        Connection conn = null;

        try {

            conn = DBConnection.getConnection();

            conn.setAutoCommit(false);


            String checkEmailSql = """
                    SELECT COUNT(*)
                    FROM users
                    WHERE email = ?
                    """;

            try (PreparedStatement ps =
                         conn.prepareStatement(checkEmailSql)) {

                ps.setString(1, email);

                var rs = ps.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {

                    conn.rollback();

                    showAlert(
                            Alert.AlertType.WARNING,
                            "Email Already Exists",
                            "A user with this email already exists."
                    );

                    return;
                }
            }


            // -----------------------------------------
            // Check username
            // -----------------------------------------

            String checkUsernameSql = """
                    SELECT COUNT(*)
                    FROM users
                    WHERE username = ?
                    """;

            try (PreparedStatement ps =
                         conn.prepareStatement(checkUsernameSql)) {

                ps.setString(1, username);

                var rs = ps.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {

                    conn.rollback();

                    showAlert(
                            Alert.AlertType.WARNING,
                            "Username Already Exists",
                            "That username is already in use."
                    );

                    return;
                }
            }

            String lecturerSql = """
                    INSERT INTO lectures
                    (first_name, last_name, email, department)
                    VALUES (?, ?, ?, ?)
                    """;

            try (PreparedStatement ps =
                         conn.prepareStatement(lecturerSql)) {

                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setString(3, email);
                ps.setString(4, department);

                ps.executeUpdate();
            }


            // -----------------------------------------
            // Create login account
            // -----------------------------------------

            String userSql = """
                    INSERT INTO users
                    (username, email, password, role)
                    VALUES (?, ?, ?, ?)
                    """;

            try (PreparedStatement ps =
                         conn.prepareStatement(userSql)) {

                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, "lecturer");

                ps.executeUpdate();
            }


            // Everything succeeded
            conn.commit();


            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Lecturer Registered",
                    "Lecturer registered successfully.\n\n"
                            + "Username: " + username
                            + "\nEmail: " + email
            );


            // Close Add Lecturer window
            Stage stage =
                    (Stage) firstNameField
                            .getScene()
                            .getWindow();

            stage.close();


        } catch (SQLException e) {

            // Roll back if something went wrong
            if (conn != null) {

                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Failed to register lecturer:\n"
                            + e.getMessage()
            );


        } finally {

            if (conn != null) {

                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @FXML
    private void handleCancel() {

        Stage stage =
                (Stage) firstNameField
                        .getScene()
                        .getWindow();

        stage.close();
    }


    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}

