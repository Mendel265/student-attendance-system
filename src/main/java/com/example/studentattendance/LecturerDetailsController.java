package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LecturerDetailsController {

    @FXML
    private Label lecturerLabel;

    private int lecturerId;
    private String lecturerFullName;

    public void setLecturerId(int lecturerId) {
        this.lecturerId = lecturerId;
        loadLecturerName();
    }

    private void loadLecturerName() {

        String sql = """
                SELECT first_name, last_name
                FROM lectures
                WHERE lecture_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lecturerId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    String firstName =
                            rs.getString("first_name");

                    String lastName =
                            rs.getString("last_name");

                    lecturerFullName =
                            firstName + " " + lastName;

                    lecturerLabel.setText(
                            "Lecturer: " + lecturerFullName
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();

            lecturerLabel.setText(
                    "Lecturer: Unknown"
            );
        }
    }

    @FXML
    private void handleViewAssignedClasses() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/view_assigned_classes.fxml"
                    )
            );

            Parent root = loader.load();

            ViewAssignedClassesController controller =
                    loader.getController();

            controller.setLecturerId(
                    lecturerId,
                    lecturerFullName
            );

            Stage stage = new Stage();

            stage.setTitle(
                    "Assigned Classes & Modules"
            );

            stage.setScene(
                    new Scene(root)
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

}