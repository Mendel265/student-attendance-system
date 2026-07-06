package com.example.studentattendance;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssignClassSemesterController {

    @FXML
    private Label lecturerNameLabel;

    @FXML
    private ComboBox<String> classComboBox;

    @FXML
    private ComboBox<String> semesterComboBox;

    @FXML
    private ListView<String> moduleListView;

    private int lecturerId;
    private String lecturerName;

    public void setLecturerId(int lecturerId, String lecturerName) {
        this.lecturerId = lecturerId;
        this.lecturerName = lecturerName;
        lecturerNameLabel.setText("Assign Modules to: " + lecturerName);
        loadClasses();
        semesterComboBox.setItems(FXCollections.observableArrayList("First Semester", "Second Semester"));
    }

    private void loadClasses() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT class_name FROM students")) {

            List<String> classes = new ArrayList<>();
            while (rs.next()) {
                classes.add(rs.getString("class_name"));
            }
            classComboBox.setItems(FXCollections.observableArrayList(classes));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadModules() {
        String selectedClass = classComboBox.getValue();
        String selectedSemester = semesterComboBox.getValue();

        if (selectedClass != null && selectedSemester != null) {
            loadModules(selectedClass, selectedSemester);
        }
    }

    private void loadModules(String className, String semester) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "");
             PreparedStatement stmt = conn.prepareStatement("SELECT module_name FROM modules WHERE class_name = ? AND semester = ?")) {

            stmt.setString(1, className);
            stmt.setString(2, semester);
            ResultSet rs = stmt.executeQuery();

            List<String> modules = new ArrayList<>();
            while (rs.next()) {
                modules.add(rs.getString("module_name"));
            }
            moduleListView.setItems(FXCollections.observableArrayList(modules));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAssignModules() {
        String selectedClass = classComboBox.getValue();
        String selectedSemester = semesterComboBox.getValue();
        List<String> selectedModules = moduleListView.getSelectionModel().getSelectedItems();

        if (selectedClass == null || selectedSemester == null || selectedModules.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select class, semester, and at least one module.");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "");
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO lecturer_modules (lecturer_id, class_name, semester, module_name) VALUES (?, ?, ?, ?)")) {

            for (String module : selectedModules) {
                stmt.setInt(1, lecturerId);
                stmt.setString(2, selectedClass);
                stmt.setString(3, selectedSemester);
                stmt.setString(4, module);
                stmt.addBatch();
            }

            stmt.executeBatch();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Modules assigned successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to assign modules.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
