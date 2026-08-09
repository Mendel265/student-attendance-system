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

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void handleLogin(ActionEvent event) {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {

            showAlert(
                    "Login Failed",
                    "Please enter your username and password."
            );

            return;
        }

        try (Connection conn = DBConnection.getConnection()) {


            String sql = """
                      SELECT
                            u.role,
                            l.lecture_id
                        FROM users u
                        LEFT JOIN lectures l
                            ON u.username = l.email
                        WHERE u.username = ?
                        AND u.password = ?
                      """;

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String role = rs.getString("role");


                if ("admin".equalsIgnoreCase(role)) {

                    loadDashboard(
                            "/fxml/admin_dashboard.fxml",
                            "Admin Dashboard",
                            -1
                    );

                }


                else if ("lecturer".equalsIgnoreCase(role)) {

                    int lectureId =
                            rs.getInt("lecture_id");


                    if (rs.wasNull()) {

                        showAlert(
                                "Login Error",
                                "This lecturer account is not linked to a lecturer record."
                        );

                        return;
                    }


                    System.out.println(
                            "Lecturer login successful."
                    );

                    System.out.println(
                            "Username: " + username
                    );

                    System.out.println(
                            "Lecture ID: " + lectureId
                    );


                    /*
                     * Pass the actual lectures.lecture_id
                     * to the lecturer dashboard.
                     */

                    loadDashboard(
                            "/fxml/lecturer_dashboard.fxml",
                            "Lecturer Dashboard",
                            lectureId
                    );

                }


                else {

                    showAlert(
                            "Login Failed",
                            "Invalid user role."
                    );
                }

            }

            else {

                showAlert(
                        "Login Failed",
                        "Invalid username or password."
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "An error occurred during login:\n"
                            + e.getMessage()
            );
        }
    }


    private void loadDashboard(
            String fxmlPath,
            String title,
            int lecturerId
    ) throws Exception {

        Stage stage =
                (Stage) usernameField
                        .getScene()
                        .getWindow();


        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(fxmlPath)
                );


        Parent root = loader.load();


        if (
                lecturerId != -1
                        && fxmlPath.contains("lecturer_dashboard")
        ) {

            LecturerDashboardController controller =
                    loader.getController();


            controller.setLecturerId(
                    lecturerId
            );
        }


        stage.setScene(
                new Scene(root)
        );

        stage.setTitle(title);

        stage.show();
    }

    @FXML
    public void goToSignup(ActionEvent event) {

        try {

            Stage stage =
                    (Stage) usernameField
                            .getScene()
                            .getWindow();


            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(
                                    "/fxml/signup.fxml"
                            )
                    );


            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Signup"
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Unable to load signup screen."
            );
        }
    }

    private void showAlert(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}