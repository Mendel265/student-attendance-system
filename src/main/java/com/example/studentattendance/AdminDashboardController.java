package com.example.studentattendance;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardController {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/students_attendance";

    private static final String DB_USER = "root";

    private static final String DB_PASSWORD = "";

    @FXML
    private Label lecturerCountLabel;

    @FXML
    private Label studentCountLabel;

    @FXML
    private Label classCountLabel;

    @FXML
    private Label attendanceCountLabel;

    @FXML
    public void initialize() {

        loadDashboardStatistics();
    }

    private Connection getConnection() throws SQLException {

        return DriverManager.getConnection(
                DB_URL,
                DB_USER,
                DB_PASSWORD
        );
    }

    private void loadDashboardStatistics() {

        loadLecturerCount();
        loadStudentCount();
        loadClassCount();
        loadTodayAttendance();
    }

    private void loadLecturerCount() {

        String sql = "SELECT COUNT(*) FROM lectures";

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            if (resultSet.next()) {

                int count = resultSet.getInt(1);

                lecturerCountLabel.setText(
                        String.valueOf(count)
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            lecturerCountLabel.setText("0");
        }
    }

    private void loadStudentCount() {

        String sql = "SELECT COUNT(*) FROM students";

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            if (resultSet.next()) {

                int count = resultSet.getInt(1);

                studentCountLabel.setText(
                        String.valueOf(count)
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            studentCountLabel.setText("0");
        }
    }

    private void loadClassCount() {

        String sql =
                "SELECT COUNT(DISTINCT class_name) FROM students";

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            if (resultSet.next()) {

                int count = resultSet.getInt(1);

                classCountLabel.setText(
                        String.valueOf(count)
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            classCountLabel.setText("0");
        }
    }

    private void loadTodayAttendance() {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM attendance " +
                        "WHERE attendance_date = CURDATE() " +
                        "AND status = 'present'";

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            if (resultSet.next()) {

                int count = resultSet.getInt(1);

                attendanceCountLabel.setText(
                        String.valueOf(count)
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            attendanceCountLabel.setText("0");
        }
    }

    @FXML
    public void handleManageLecturers(ActionEvent event) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/manage_lecturer.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage stage = new Stage();

            stage.setTitle("Manage Lecturers");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @FXML
    public void handleManageStudents(ActionEvent event) {

        System.out.println("Manage Students clicked");

    }

    @FXML
    private void handleManageClasses(ActionEvent event) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/manage_classes.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage dashboardStage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            Stage manageClassesStage =
                    new Stage();

            manageClassesStage.setTitle(
                    "Manage Classes"
            );

            manageClassesStage.setScene(
                    new Scene(root)
            );

            manageClassesStage.initOwner(
                    dashboardStage
            );

            manageClassesStage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @FXML
    public void handleViewReports(ActionEvent event) {

        System.out.println(
                "View Attendance Reports clicked"
        );

    }

    @FXML
    private void handleManageSchedule(ActionEvent event) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/admin_schedule.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage dashboardStage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            Stage scheduleStage =
                    new Stage();

            scheduleStage.setTitle(
                    "Manage Lecture Schedule"
            );

            scheduleStage.setScene(
                    new Scene(root)
            );

            scheduleStage.initOwner(
                    dashboardStage
            );

            scheduleStage.show();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/login.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage loginStage =
                    new Stage();

            loginStage.setScene(
                    new Scene(root)
            );

            loginStage.setTitle("Login");

            loginStage.show();

            Stage currentStage =
                    (Stage) ((Button) event.getSource())
                            .getScene()
                            .getWindow();

            currentStage.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}