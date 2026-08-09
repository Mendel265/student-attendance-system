package com.example.studentattendance;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

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
    private Label lectureDateLabel;

    @FXML
    private TableView<LectureRow> todayLecturesTable;

    @FXML
    private TableColumn<LectureRow, String> timeColumn;

    @FXML
    private TableColumn<LectureRow, String> moduleColumn;

    @FXML
    private TableColumn<LectureRow, String> lecturerColumn;

    @FXML
    private TableColumn<LectureRow, String> roomColumn;

    @FXML
    private TableColumn<LectureRow, String> classColumn;
    @FXML
    private Label presentAttendanceLabel;

    @FXML
    private Label absentAttendanceLabel;

    @FXML
    private Label pendingAttendanceLabel;

    @FXML
    private Label totalAttendanceLabel;

    @FXML
    public void initialize() {

        setupLectureTable();

        setTodayDate();

        loadDashboardStatistics();

        loadTodayLectures();
    }


    // ============================================================
    // DATABASE CONNECTION
    // ============================================================

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

        String sql =
                "SELECT COUNT(*) " +
                        "FROM lectures";

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

        String sql =
                "SELECT COUNT(*) " +
                        "FROM students";

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
                "SELECT COUNT(DISTINCT class_name) " +
                        "FROM students " +
                        "WHERE class_name IS NOT NULL " +
                        "AND TRIM(class_name) <> ''";

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
                "SELECT " +
                        "COUNT(*) AS total, " +
                        "SUM(CASE " +
                        "    WHEN LOWER(status) = 'present' " +
                        "    THEN 1 ELSE 0 " +
                        "END) AS present, " +
                        "SUM(CASE " +
                        "    WHEN LOWER(status) = 'absent' " +
                        "    THEN 1 ELSE 0 " +
                        "END) AS absent, " +
                        "SUM(CASE " +
                        "    WHEN LOWER(status) = 'pending' " +
                        "    THEN 1 ELSE 0 " +
                        "END) AS pending " +
                        "FROM attendance " +
                        "WHERE attendance_date = CURDATE()";

        try (
                Connection connection = getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            if (resultSet.next()) {

                int total =
                        resultSet.getInt("total");

                int present =
                        resultSet.getInt("present");

                int absent =
                        resultSet.getInt("absent");

                int pending =
                        resultSet.getInt("pending");


                // Main dashboard card
                attendanceCountLabel.setText(
                        String.valueOf(present)
                );


                // Attendance summary
                presentAttendanceLabel.setText(
                        String.valueOf(present)
                );

                absentAttendanceLabel.setText(
                        String.valueOf(absent)
                );

                pendingAttendanceLabel.setText(
                        String.valueOf(pending)
                );

                totalAttendanceLabel.setText(
                        String.valueOf(total)
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            attendanceCountLabel.setText("0");

            presentAttendanceLabel.setText("0");

            absentAttendanceLabel.setText("0");

            pendingAttendanceLabel.setText("0");

            totalAttendanceLabel.setText("0");
        }
    }

    private void setTodayDate() {

        LocalDate today = LocalDate.now();

        String day =
                today.getDayOfWeek()
                        .getDisplayName(
                                TextStyle.FULL,
                                Locale.ENGLISH
                        );

        String date =
                today.format(
                        DateTimeFormatter.ofPattern(
                                "dd MMMM yyyy"
                        )
                );

        lectureDateLabel.setText(
                day + ", " + date
        );
    }
    private void setupLectureTable() {

        timeColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getTime()
                        )
        );

        moduleColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getModule()
                        )
        );

        lecturerColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getLecturer()
                        )
        );

        roomColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getRoom()
                        )
        );

        classColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getClassName()
                        )
        );

        todayLecturesTable.setPlaceholder(
                new Label(
                        "No lectures scheduled for today"
                )
        );
    }

    private void loadTodayLectures() {

        ObservableList<LectureRow> lectures =
                FXCollections.observableArrayList();


        String sql =
                "SELECT " +
                        "s.schedule_id, " +
                        "s.start_time, " +
                        "s.end_time, " +
                        "s.room, " +
                        "CONCAT(l.first_name, ' ', l.last_name) AS lecturer, " +
                        "m.module_name, " +
                        "m.class_name " +
                        "FROM schedule s " +
                        "JOIN lecturer_module lm " +
                        "    ON s.lecturer_module_id = lm.lecturer_module_id " +
                        "JOIN lectures l " +
                        "    ON lm.lecturer_id = l.lecture_id " +
                        "JOIN modules m " +
                        "    ON lm.module_id = m.id " +
                        "WHERE LOWER(TRIM(s.day_of_week)) = " +
                        "      LOWER(DAYNAME(CURDATE())) " +
                        "ORDER BY s.start_time";


        try (
                Connection connection = getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                String startTime =
                        resultSet.getString("start_time");

                String endTime =
                        resultSet.getString("end_time");

                String time =
                        formatTime(startTime)
                                + " - "
                                + formatTime(endTime);


                String lecturer =
                        resultSet.getString("lecturer");

                String module =
                        resultSet.getString("module_name");

                String room =
                        resultSet.getString("room");

                String className =
                        resultSet.getString("class_name");


                lectures.add(
                        new LectureRow(
                                time,
                                module,
                                lecturer,
                                room,
                                className
                        )
                );
            }


            todayLecturesTable.setItems(
                    lectures
            );

        } catch (SQLException e) {

            e.printStackTrace();

            todayLecturesTable.setItems(
                    FXCollections.observableArrayList()
            );
        }
    }

    private String formatTime(String time) {

        if (time == null || time.isBlank()) {

            return "--:--";
        }

        try {

            if (time.length() >= 5) {

                return time.substring(0, 5);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return time;
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

            stage.setTitle(
                    "Manage Lecturers"
            );

            stage.setScene(
                    new Scene(root)
            );

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @FXML
    public void handleManageStudents(ActionEvent event) {

        System.out.println(
                "Manage Students clicked"
        );
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

        } catch (IOException e) {

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

            loginStage.setTitle(
                    "Student Attendance System - Login"
            );

            loginStage.show();


            Stage currentStage =
                    (Stage) ((Button) event.getSource())
                            .getScene()
                            .getWindow();

            currentStage.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void refreshDashboard() {

        setTodayDate();

        loadDashboardStatistics();

        loadTodayLectures();
    }

    public static class LectureRow {

        private final String time;

        private final String module;

        private final String lecturer;

        private final String room;

        private final String className;


        public LectureRow(
                String time,
                String module,
                String lecturer,
                String room,
                String className
        ) {

            this.time = time;

            this.module = module;

            this.lecturer = lecturer;

            this.room = room;

            this.className = className;
        }


        public String getTime() {

            return time;
        }


        public String getModule() {

            return module;
        }


        public String getLecturer() {

            return lecturer;
        }


        public String getRoom() {

            return room;
        }


        public String getClassName() {

            return className;
        }
    }
}
