package com.example.studentattendance;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class LecturerDashboardController {

    private int lecturerId;

    @FXML
    private Label totalStudentsLabel;

    @FXML
    private Label attendancePercentageLabel;

    @FXML
    private Label assignedClassesLabel;

    @FXML
    private TableView<AttendanceRecord> attendanceTable;

    @FXML
    private TableColumn<AttendanceRecord, String> fnameColumn;

    @FXML
    private TableColumn<AttendanceRecord, String> lastnameColumn;

    @FXML
    private TableColumn<AttendanceRecord, String> moduleColumn;

    @FXML
    private TableColumn<AttendanceRecord, String> classColumn;

    @FXML
    private TableColumn<AttendanceRecord, String> statusColumn;

    @FXML
    private TableColumn<AttendanceRecord, String> timeColumn;

    private ObservableList<AttendanceRecord> attendanceList = FXCollections.observableArrayList();

    private final String url = "jdbc:mysql://localhost:3306/students_attendance";
    private final String user = "root";
    private final String password = "";

    @FXML
    public void initialize() {
        setupAttendanceTable();
    }

    @FXML
    private void onPrintAttendance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/print_attendance_dialog.fxml"));
            Parent root = loader.load();

            PrintAttendanceDialogController controller = loader.getController();
            controller.setLecturerId(lecturerId);
            controller.initializeData();

            Stage stage = new Stage();
            stage.setTitle("Print Attendance Report");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open print dialog: " + e.getMessage());
        }
    }

    private void setupAttendanceTable() {
        fnameColumn.setCellValueFactory(new PropertyValueFactory<>("fname"));
        lastnameColumn.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        moduleColumn.setCellValueFactory(new PropertyValueFactory<>("module"));
        classColumn.setCellValueFactory(new PropertyValueFactory<>("className"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
    }

    private void loadRecentAttendance() {
        String sql = """
            SELECT
                s.fname,
                s.lastname,
                m.module_name,
                m.class_name,
                a.status,
                a.check_out_time AS time
            FROM attendance a
            JOIN students s ON a.student_id = s.student_id
            JOIN schedule sc ON a.schedule_id = sc.schedule_id
            JOIN lecturer_module lm ON sc.lecturer_module_id = lm.lecturer_module_id
            JOIN modules m ON lm.module_id = m.id
            WHERE lm.lecturer_id = ?
            ORDER BY a.check_out_time DESC
            LIMIT 10
        """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lecturerId);
            ResultSet rs = ps.executeQuery();
            attendanceList.clear();

            while (rs.next()) {
                attendanceList.add(new AttendanceRecord(
                        rs.getString("fname"),
                        rs.getString("lastname"),
                        rs.getString("module"),
                        rs.getString("class_name"),
                        rs.getString("status"),
                        rs.getString("time")
                ));
            }

            attendanceTable.setItems(attendanceList);

        } catch (Exception e) {
            e.printStackTrace();
            // If table doesn't exist, show empty state
            attendanceTable.setItems(FXCollections.observableArrayList());
        }
    }

    public void setLecturerId(int id) {
        this.lecturerId = id;
        loadDashboardStatistics();
        loadRecentAttendance();
    }

    private void loadDashboardStatistics() {
        loadTotalStudents();
        loadTodayAttendance();
        loadAssignedClasses();
    }

    private void loadTotalStudents() {
        String sql = "SELECT COUNT(*) FROM students";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                totalStudentsLabel.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            totalStudentsLabel.setText("0");
        }
    }

    private void loadTodayAttendance() {
        String sql = """
            SELECT COUNT(*) 
            FROM attendance
            WHERE attendance_date = CURDATE()
            AND status = 'present'
        """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int present = rs.getInt(1);
                attendancePercentageLabel.setText(present + "%");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            attendancePercentageLabel.setText("0%");
        }
    }

    private void loadAssignedClasses() {

        String sql = """
        SELECT COUNT(DISTINCT lm.module_id)
        FROM lecturer_module lm
        WHERE lm.lecturer_id = ?
    """;

        try (Connection conn =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    assignedClassesLabel.setText(
                            String.valueOf(rs.getInt(1))
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            assignedClassesLabel.setText("0");
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Student Attendance System - Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to logout and load login screen.");
        }
    }

    @FXML
    public void handleRegisterStudent(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/student_register.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Register Student");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open student registration: " + e.getMessage());
        }
    }

    @FXML
    public void handleTakeAttendance(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/take_attendance.fxml"));
            Parent root = loader.load();

            TakeAttendanceController controller = loader.getController();
            controller.setLecturerId(lecturerId);

            stage.setScene(new Scene(root));
            stage.setTitle("Take Attendance");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load Take Attendance screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleTrainModel(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/train_face_model.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Train Face Model");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Train Model: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}