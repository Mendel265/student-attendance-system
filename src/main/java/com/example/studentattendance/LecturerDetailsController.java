package com.example.studentattendance;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.*;

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
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "");
             PreparedStatement stmt = conn.prepareStatement("SELECT fname, lastname FROM lecturers WHERE id = ?")) {

            stmt.setInt(1, lecturerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String fname = rs.getString("fname");
                String lname = rs.getString("lastname");
                lecturerFullName = fname + " " + lname;
                lecturerLabel.setText("Lecturer: " + lecturerFullName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewAssignedClasses() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view_assigned_classes.fxml"));
            Parent root = loader.load();

            ViewAssignedClassesController controller = loader.getController();
            controller.setLecturerId(lecturerId, lecturerFullName);  // <-- Pass both id and name

            Stage stage = new Stage();
            stage.setTitle("Assigned Classes & Modules");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAssignToClass() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/assign_class_semester.fxml"));
            Parent root = loader.load();

            AssignClassSemesterController controller = loader.getController();
            controller.setLecturerId(lecturerId, lecturerFullName);  // <-- Pass both id and name

            Stage stage = new Stage();
            stage.setTitle("Assign to Class and Modules");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
