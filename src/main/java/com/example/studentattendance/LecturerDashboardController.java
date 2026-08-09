package com.example.studentattendance;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalTime;


public class LecturerDashboardController {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/students_attendance";

    private static final String DB_USER = "root";

    private static final String DB_PASSWORD = "";


    private int lecturerId = 0;

    @FXML
    private Label totalStudentsLabel;

    @FXML
    private Label attendancePercentageLabel;

    @FXML
    private Label assignedClassesLabel;

    @FXML
    private TableView<ScheduleRecord> scheduleTable;

    @FXML
    private TableColumn<ScheduleRecord, String> scheduleTimeColumn;

    @FXML
    private TableColumn<ScheduleRecord, String> scheduleModuleColumn;

    @FXML
    private TableColumn<ScheduleRecord, String> scheduleClassColumn;

    @FXML
    private TableColumn<ScheduleRecord, String> scheduleStatusColumn;


    private final ObservableList<ScheduleRecord> scheduleList =
            FXCollections.observableArrayList();

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


    private final ObservableList<AttendanceRecord> attendanceList =
            FXCollections.observableArrayList();

    @FXML
    private PieChart attendanceChart;

    @FXML
    private ListView<String> activityList;


    @FXML
    public void initialize() {

        setupScheduleTable();

        setupAttendanceTable();

        if (attendanceChart != null) {
            attendanceChart.setTitle("Today's Attendance");
            attendanceChart.setLegendVisible(true);
        }
    }


    private Connection getConnection() throws SQLException {

        return DriverManager.getConnection(
                DB_URL,
                DB_USER,
                DB_PASSWORD
        );
    }

    public void setLecturerId(int id) {

        this.lecturerId = id;

        System.out.println(
                "Lecturer Dashboard - Lecturer ID: "
                        + lecturerId
        );

        loadDashboard();
    }

    private void loadDashboard() {

        if (lecturerId <= 0) {

            System.err.println(
                    "ERROR: Lecturer ID is not set."
            );

            showDashboardError();

            return;
        }

        System.out.println(
                "Loading dashboard for lecturer ID: "
                        + lecturerId
        );

        loadTotalStudents();

        loadTodayAttendance();

        loadAssignedClasses();

        loadTodaySchedule();

        loadAttendanceOverview();

        loadRecentAttendance();

        loadRecentActivity();
    }


    private void loadTotalStudents() {

        String sql = """
        SELECT COUNT(DISTINCT s.student_id) AS total_students
        FROM students s
        JOIN modules m
            ON s.class_name = m.class_name
        JOIN lecturer_module lm
            ON lm.module_id = m.id
        WHERE lm.lecturer_id = ?
    """;

        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    int count = rs.getInt("total_students");

                    totalStudentsLabel.setText(
                            String.valueOf(count)
                    );

                    System.out.println(
                            "Total students: " + count
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            totalStudentsLabel.setText("0");
        }
    }

    private void loadTodayAttendance() {

        String sql = """
            SELECT
                COUNT(*) AS total,
                SUM(
                    CASE
                        WHEN a.status = 'present'
                        THEN 1
                        ELSE 0
                    END
                ) AS present
            FROM attendance a
            JOIN schedule sc
                ON a.schedule_id = sc.schedule_id
            JOIN lecturer_module lm
                ON sc.lecturer_module_id =
                   lm.lecturer_module_id
            WHERE lm.lecturer_id = ?
              AND a.attendance_date = CURDATE()
        """;

        try (
                Connection conn = getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    int total =
                            rs.getInt("total");

                    int present =
                            rs.getInt("present");

                    double percentage = 0;

                    if (total > 0) {

                        percentage =
                                ((double) present / total)
                                        * 100;
                    }

                    attendancePercentageLabel.setText(
                            String.format(
                                    "%.0f%%",
                                    percentage
                            )
                    );

                    System.out.println(
                            "Today's attendance: "
                                    + present
                                    + "/"
                                    + total
                                    + " = "
                                    + percentage
                                    + "%"
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            attendancePercentageLabel.setText(
                    "0%"
            );
        }
    }

    private void loadAssignedClasses() {

        String sql = """
        SELECT COUNT(DISTINCT lm.lecturer_module_id) AS assigned_classes
        FROM lecturer_module lm
        WHERE lm.lecturer_id = ?
    """;

        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    int count = rs.getInt("assigned_classes");

                    assignedClassesLabel.setText(
                            String.valueOf(count)
                    );

                    System.out.println(
                            "Assigned classes/modules: " + count
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            assignedClassesLabel.setText("0");
        }
    }

    private void setupScheduleTable() {

        if (scheduleTimeColumn == null) {
            return;
        }

        scheduleTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("time")
        );

        scheduleModuleColumn.setCellValueFactory(
                new PropertyValueFactory<>("module")
        );

        scheduleClassColumn.setCellValueFactory(
                new PropertyValueFactory<>("className")
        );

        scheduleStatusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        scheduleTable.setItems(scheduleList);

        scheduleTable.setPlaceholder(
                new Label(
                        "No lectures scheduled for today"
                )
        );
    }


    private void loadTodaySchedule() {

        scheduleList.clear();

        String sql = """
        SELECT
            sc.start_time,
            sc.end_time,
            m.module_name,
            m.class_name
        FROM schedule sc
        JOIN lecturer_module lm
            ON sc.lecturer_module_id =
               lm.lecturer_module_id
        JOIN modules m
            ON lm.module_id = m.id
        WHERE lm.lecturer_id = ?
          AND sc.day_of_week = DAYNAME(CURDATE())
        ORDER BY sc.start_time ASC
    """;

        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, lecturerId);

            System.out.println(
                    "Loading schedule for lecturer ID: "
                            + lecturerId
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String start = formatTime(
                            rs.getString("start_time")
                    );

                    String end = formatTime(
                            rs.getString("end_time")
                    );

                    String module = rs.getString("module_name");

                    String className = rs.getString("class_name");

                    String status = getScheduleStatus(
                            rs.getString("start_time"),
                            rs.getString("end_time")
                    );

                    scheduleList.add(
                            new ScheduleRecord(
                                    start + " - " + end,
                                    module,
                                    className,
                                    status
                            )
                    );

                    System.out.println(
                            "Schedule: "
                                    + start
                                    + " - "
                                    + end
                                    + " | "
                                    + module
                                    + " | "
                                    + className
                    );
                }
            }

            scheduleTable.refresh();

            System.out.println(
                    "Schedule records found: "
                            + scheduleList.size()
            );

        } catch (SQLException e) {

            e.printStackTrace();

            scheduleList.clear();
        }
    }

    private String getScheduleStatus(
            String startTime,
            String endTime
    ) {

        try {

            LocalTime start =
                    LocalTime.parse(
                            startTime.substring(0, 8)
                    );

            LocalTime end =
                    LocalTime.parse(
                            endTime.substring(0, 8)
                    );

            LocalTime now =
                    LocalTime.now();


            if (now.isBefore(start)) {

                return "Upcoming";
            }

            if (now.isAfter(end)) {

                return "Completed";
            }

            return "In Progress";

        } catch (Exception e) {

            return "Scheduled";
        }
    }

    private String formatTime(String time) {

        if (time == null) {

            return "--:--";
        }

        if (time.length() >= 5) {

            return time.substring(0, 5);
        }

        return time;
    }

    private void setupAttendanceTable() {

        if (fnameColumn == null) {
            return;
        }

        fnameColumn.setCellValueFactory(
                new PropertyValueFactory<>("fname")
        );

        lastnameColumn.setCellValueFactory(
                new PropertyValueFactory<>("lastname")
        );

        moduleColumn.setCellValueFactory(
                new PropertyValueFactory<>("module")
        );

        classColumn.setCellValueFactory(
                new PropertyValueFactory<>("className")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        timeColumn.setCellValueFactory(
                new PropertyValueFactory<>("time")
        );

        attendanceTable.setItems(
                attendanceList
        );

        attendanceTable.setPlaceholder(
                new Label(
                        "No recent attendance records"
                )
        );
    }

    private void loadRecentAttendance() {

        attendanceList.clear();

        String sql = """
            SELECT
                s.fname,
                s.lastname,
                m.module_name,
                m.class_name,
                a.status,
                COALESCE(
                    a.check_in_time,
                    a.check_out_time
                ) AS attendance_time
            FROM attendance a
            JOIN students s
                ON a.student_id = s.student_id
            JOIN schedule sc
                ON a.schedule_id = sc.schedule_id
            JOIN lecturer_module lm
                ON sc.lecturer_module_id =
                   lm.lecturer_module_id
            JOIN modules m
                ON lm.module_id = m.id
            WHERE lm.lecturer_id = ?
            ORDER BY
                a.attendance_date DESC,
                attendance_time DESC
            LIMIT 10
        """;

        try (
                Connection conn = getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    attendanceList.add(
                            new AttendanceRecord(
                                    rs.getString("fname"),
                                    rs.getString("lastname"),
                                    rs.getString(
                                            "module_name"
                                    ),
                                    rs.getString(
                                            "class_name"
                                    ),
                                    rs.getString("status"),
                                    rs.getString(
                                            "attendance_time"
                                    )
                            )
                    );
                }
            }

            attendanceTable.refresh();

            System.out.println(
                    "Recent attendance records: "
                            + attendanceList.size()
            );

        } catch (SQLException e) {

            e.printStackTrace();

            attendanceList.clear();
        }
    }


    private void loadAttendanceOverview() {

        String sql = """
            SELECT
                SUM(
                    CASE
                        WHEN a.status = 'present'
                        THEN 1
                        ELSE 0
                    END
                ) AS present,

                SUM(
                    CASE
                        WHEN a.status = 'absent'
                        THEN 1
                        ELSE 0
                    END
                ) AS absent,

                SUM(
                    CASE
                        WHEN a.status = 'pending'
                        THEN 1
                        ELSE 0
                    END
                ) AS pending

            FROM attendance a

            JOIN schedule sc
                ON a.schedule_id = sc.schedule_id

            JOIN lecturer_module lm
                ON sc.lecturer_module_id =
                   lm.lecturer_module_id

            WHERE lm.lecturer_id = ?
              AND a.attendance_date = CURDATE()
        """;

        try (
                Connection conn = getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    int present =
                            rs.getInt("present");

                    int absent =
                            rs.getInt("absent");

                    int pending =
                            rs.getInt("pending");


                    ObservableList<PieChart.Data> data =
                            FXCollections.observableArrayList();


                    if (present > 0) {

                        data.add(
                                new PieChart.Data(
                                        "Present",
                                        present
                                )
                        );
                    }

                    if (absent > 0) {

                        data.add(
                                new PieChart.Data(
                                        "Absent",
                                        absent
                                )
                        );
                    }

                    if (pending > 0) {

                        data.add(
                                new PieChart.Data(
                                        "Pending",
                                        pending
                                )
                        );
                    }


                    if (data.isEmpty()) {

                        data.add(
                                new PieChart.Data(
                                        "No Attendance",
                                        1
                                )
                        );
                    }

                    attendanceChart.setData(data);
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    private void loadRecentActivity() {

        if (activityList == null) {
            return;
        }

        ObservableList<String> activities =
                FXCollections.observableArrayList();


        String sql = """
            SELECT
                s.fname,
                s.lastname,
                a.status,
                m.module_name,
                COALESCE(
                    a.check_in_time,
                    a.check_out_time
                ) AS attendance_time
            FROM attendance a
            JOIN students s
                ON a.student_id = s.student_id
            JOIN schedule sc
                ON a.schedule_id = sc.schedule_id
            JOIN lecturer_module lm
                ON sc.lecturer_module_id =
                   lm.lecturer_module_id
            JOIN modules m
                ON lm.module_id = m.id
            WHERE lm.lecturer_id = ?
            ORDER BY
                a.attendance_date DESC,
                attendance_time DESC
            LIMIT 6
        """;


        try (
                Connection conn = getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(1, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String activity =
                            rs.getString("fname")
                                    + " "
                                    + rs.getString("lastname")
                                    + " marked "
                                    + rs.getString("status")
                                    + " for "
                                    + rs.getString("module_name")
                                    + " at "
                                    + formatTime(
                                    rs.getString(
                                            "attendance_time"
                                    )
                            );

                    activities.add(activity);
                }
            }

            activityList.setItems(
                    activities
            );

        } catch (SQLException e) {

            e.printStackTrace();

            activityList.setItems(
                    FXCollections.observableArrayList()
            );
        }
    }

    @FXML
    public void handleTakeAttendance(
            ActionEvent event
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/take_attendance.fxml"
                            )
                    );

            Parent root =
                    loader.load();


            TakeAttendanceController controller =
                    loader.getController();

            controller.setLecturerId(
                    lecturerId
            );


            Stage stage =
                    (Stage)
                            ((Node) event.getSource())
                                    .getScene()
                                    .getWindow();


            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Take Attendance"
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Failed to load Take Attendance:\n"
                            + e.getMessage()
            );
        }
    }


    @FXML
    public void handleRegisterStudent(
            ActionEvent event
    ) {

        try {

            Stage stage =
                    (Stage)
                            ((Node) event.getSource())
                                    .getScene()
                                    .getWindow();


            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(
                                    "/fxml/student_register.fxml"
                            )
                    );


            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Register Student"
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Could not open student registration:\n"
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void handleTrainModel(
            ActionEvent event
    ) {

        try {

            Stage stage =
                    (Stage)
                            ((Node) event.getSource())
                                    .getScene()
                                    .getWindow();


            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(
                                    "/fxml/train_face_model.fxml"
                            )
                    );


            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Train Face Model"
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Could not open Train Model:\n"
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void onPrintAttendance(
            ActionEvent event
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/print_attendance_dialog.fxml"
                            )
                    );

            Parent root =
                    loader.load();


            PrintAttendanceDialogController controller =
                    loader.getController();

            controller.setLecturerId(
                    lecturerId
            );

            controller.initializeData();


            Stage stage =
                    new Stage();

            stage.setTitle(
                    "Print Attendance Report"
            );

            stage.setScene(
                    new Scene(root)
            );

            stage.initModality(
                    Modality.APPLICATION_MODAL
            );

            stage.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Could not open print dialog:\n"
                            + e.getMessage()
            );
        }
    }


    @FXML
    public void handleLogout(
            ActionEvent event
    ) {

        try {

            Stage stage =
                    (Stage)
                            ((Node) event.getSource())
                                    .getScene()
                                    .getWindow();


            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(
                                    "/fxml/login.fxml"
                            )
                    );


            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Student Attendance System - Login"
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Failed to logout."
            );
        }
    }

    private void showDashboardError() {

        totalStudentsLabel.setText("0");

        attendancePercentageLabel.setText("0%");

        assignedClassesLabel.setText("0");

        if (scheduleList != null) {
            scheduleList.clear();
        }

        if (attendanceList != null) {
            attendanceList.clear();
        }

        System.err.println(
                "Dashboard cannot load because lecturerId = 0."
        );
    }

    public void refreshDashboard() {

        loadDashboard();
    }

    public static class ScheduleRecord {

        private final String time;
        private final String module;
        private final String className;
        private final String status;


        public ScheduleRecord(
                String time,
                String module,
                String className,
                String status
        ) {

            this.time = time;
            this.module = module;
            this.className = className;
            this.status = status;
        }


        public String getTime() {
            return time;
        }


        public String getModule() {
            return module;
        }


        public String getClassName() {
            return className;
        }


        public String getStatus() {
            return status;
        }
    }

    public static class AttendanceRecord {

        private final String fname;
        private final String lastname;
        private final String module;
        private final String className;
        private final String status;
        private final String time;


        public AttendanceRecord(
                String fname,
                String lastname,
                String module,
                String className,
                String status,
                String time
        ) {

            this.fname = fname;
            this.lastname = lastname;
            this.module = module;
            this.className = className;
            this.status = status;
            this.time = time;
        }


        public String getFname() {
            return fname;
        }


        public String getLastname() {
            return lastname;
        }


        public String getModule() {
            return module;
        }


        public String getClassName() {
            return className;
        }


        public String getStatus() {
            return status;
        }


        public String getTime() {
            return time;
        }
    }

    private void showAlert(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}