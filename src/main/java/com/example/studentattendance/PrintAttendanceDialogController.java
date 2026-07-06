package com.example.studentattendance;

import com.example.studentattendance.models.AttendanceRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintAttendanceDialogController {


    @FXML
    private Button printAttendanceButton;
    @FXML
    private ComboBox<String> semesterComboBox;

    @FXML
    private ComboBox<String> moduleComboBox;

    @FXML
    private ComboBox<String> classComboBox;

    private int lecturerId;  // Passed from login or dashboard

    // Called from the dashboard before showing the window
    public void setLecturerId(int lecturerId) {
        this.lecturerId = lecturerId;
    }

    // Called after lecturerId is set
    public void initializeData() {
        loadSemesters();
        loadClasses();
        setupModuleComboBoxListener();
        javafx.application.Platform.runLater(this::loadModules);
    }

    private void loadSemesters() {
        List<String> semesters = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "")) {
            String sql = "SELECT DISTINCT semester FROM lecturer_modules WHERE lecturer_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, lecturerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                semesters.add(rs.getString("semester"));
            }
        } catch (SQLException e) {
            showAlert("DB Error", "Failed to load semesters: " + e.getMessage());
        }
        semesterComboBox.setItems(FXCollections.observableArrayList(semesters));
        if (!semesters.isEmpty()) semesterComboBox.getSelectionModel().selectFirst();
    }

    private void loadClasses() {
        List<String> classes = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "")) {
            String sql = "SELECT DISTINCT class_name FROM lecturer_modules WHERE lecturer_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, lecturerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                classes.add(rs.getString("class_name"));
            }
        } catch (SQLException e) {
            showAlert("DB Error", "Failed to load classes: " + e.getMessage());
        }
        classComboBox.setItems(FXCollections.observableArrayList(classes));
        if (!classes.isEmpty()) classComboBox.getSelectionModel().selectFirst();
    }

    private void setupModuleComboBoxListener() {
        semesterComboBox.setOnAction(e -> loadModules());
        classComboBox.setOnAction(e -> loadModules());
    }

    private void loadModules() {
        String selectedSemester = semesterComboBox.getValue();
        String selectedClass = classComboBox.getValue();

        System.out.println("Loading modules for:");
        System.out.println("Lecturer ID: " + lecturerId);
        System.out.println("Semester: " + selectedSemester);
        System.out.println("Class: " + selectedClass);

        if (selectedSemester == null || selectedClass == null) {
            moduleComboBox.setItems(FXCollections.observableArrayList());
            return;
        }

        List<String> modules = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "")) {
            String sql = "SELECT DISTINCT module_name FROM lecturer_modules WHERE lecturer_id = ? AND semester = ? AND class_name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, lecturerId);
            stmt.setString(2, selectedSemester);
            stmt.setString(3, selectedClass);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String module = rs.getString("module_name");
                System.out.println("Found module: " + module);
                modules.add(module);
            }
        } catch (SQLException e) {
            showAlert("DB Error", "Failed to load modules: " + e.getMessage());
        }

        moduleComboBox.setItems(FXCollections.observableArrayList(modules));
        if (!modules.isEmpty()) moduleComboBox.getSelectionModel().selectFirst();
    }


    private void generateAttendanceReport(String semester, String module, String className) {
        ObservableList<AttendanceRecord> attendanceData = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "")) {
            String sql = "SELECT s.student_id, s.fname, s.lastname, a.status, a.check_in_time, a.check_out_time " +
                    "FROM students s LEFT JOIN attendance a ON s.student_id = a.student_id " +
                    "AND a.module = ? AND a.class_name = ? WHERE s.class_name = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, module);
            stmt.setString(2, className);
            stmt.setString(3, className);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String name = rs.getString("fname") + " " + rs.getString("lastname");
                String status = rs.getString("status");

                if (status == null || status.equalsIgnoreCase("pending")) {
                    status = "Absent";
                } else if (status.equalsIgnoreCase("present")) {
                    status = "Present";
                }

                String checkIn = rs.getString("check_in_time");
                String checkOut = rs.getString("check_out_time");

                attendanceData.add(new AttendanceRecord(studentId, name, status));
            }

            // Load new FXML page and pass data
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/attendance_report_view.fxml"));
            Parent root = loader.load();

            AttendanceReportViewController controller = loader.getController();
            controller.setAttendanceData(attendanceData);

            Stage stage = new Stage();
            stage.setTitle("Attendance Report");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("DB Error", "Failed to load attendance report.");
        }
    }




    @FXML
    private void onGenerateReport(ActionEvent event) {
        String semester = semesterComboBox.getValue();
        String module = moduleComboBox.getValue();
        String className = classComboBox.getValue();

        if (semester == null || module == null || className == null) {
            showAlert("Missing Selection", "Please select Semester, Module, and Class.");
            return;
        }

        generateAttendanceReport(semester, module, className);
    }



    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
