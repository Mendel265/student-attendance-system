package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import com.example.studentattendance.models.AttendanceRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;




public class GenerateReportController {

    @FXML private ComboBox<String> classComboBox;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private ComboBox<String> moduleComboBox;
    @FXML private TableView<AttendanceRecord> attendanceTable;

    private int lecturerId;

    public void setLecturerId(int id) {
        this.lecturerId = id;
        loadClassOptions();
    }
    private void loadClassOptions() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT class_name FROM students");
            ResultSet rs = stmt.executeQuery();
            ObservableList<String> classes = FXCollections.observableArrayList();
            while (rs.next()) {
                classes.add(rs.getString("class_name"));
            }
            classComboBox.setItems(classes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        semesterComboBox.setItems(FXCollections.observableArrayList("First", "Second"));

        // Add listeners to load modules when class or semester changes
        classComboBox.setOnAction(event -> loadModule());
        semesterComboBox.setOnAction(event -> loadModule());
    }

    private void loadModule() {
        String selectedClass = classComboBox.getValue();
        String selectedSemester = semesterComboBox.getValue();

        if (selectedClass == null || selectedSemester == null || lecturerId == 0) {
            return; // Make sure all required selections are made
        }

        moduleComboBox.getItems().clear();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT DISTINCT module_name FROM modules WHERE class_name = ? AND semester = ? AND lecturer_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, selectedClass);
            stmt.setString(2, selectedSemester);
            stmt.setInt(3, lecturerId);

            ResultSet rs = stmt.executeQuery();
            ObservableList<String> modules = FXCollections.observableArrayList();
            while (rs.next()) {
                modules.add(rs.getString("module_name"));
            }

            moduleComboBox.setItems(modules);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void handleGenerate() {
        String selectedClass = classComboBox.getValue();
        String selectedSemester = semesterComboBox.getValue();
        String selectedModule = moduleComboBox.getValue();

        if (selectedClass == null || selectedSemester == null || selectedModule == null) {
            showAlert("Please select class, semester, and module.");
            return;
        }

        generateAttendanceReport(selectedClass, selectedSemester, selectedModule);
    }

    private void generateAttendanceReport(String className, String semester, String module) {
        ObservableList<AttendanceRecord> data = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT s.student_id, s.fname, s.lastname,
                       COUNT(a.status) AS scans,
                       MAX(a.status) AS final_status
                FROM students s
                LEFT JOIN attendance a ON s.student_id = a.student_id
                     AND a.class_name = ? AND a.semester = ? AND a.module = ? AND a.lecturer_id = ?
                WHERE s.class_name = ?
                GROUP BY s.student_id
                """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, className);
            stmt.setString(2, semester);
            stmt.setString(3, module);
            stmt.setInt(4, lecturerId);
            stmt.setString(5, className);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String name = rs.getString("fname") + " " + rs.getString("lastname");
                int scans = rs.getInt("scans");
                String status = (scans < 2) ? "Absent" : rs.getString("final_status");
                data.add(new AttendanceRecord(studentId, name, status));
            }

            attendanceTable.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error generating report.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

