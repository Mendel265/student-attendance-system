package com.example.studentattendance;

import com.example.studentattendance.models.ScheduleRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManageScheduleController {

    @FXML
    private TableView<ScheduleRecord> scheduleTable;

    @FXML
    private TableColumn<ScheduleRecord, String> lecturerColumn;

    @FXML
    private TableColumn<ScheduleRecord, String> moduleColumn;

    @FXML
    private TableColumn<ScheduleRecord, String> classColumn;

    @FXML
    private TableColumn<ScheduleRecord, String> dayColumn;

    @FXML
    private TableColumn<ScheduleRecord, String> startTimeColumn;

    @FXML
    private TableColumn<ScheduleRecord, String> endTimeColumn;

    @FXML
    private TableColumn<ScheduleRecord, String> roomColumn;

    private final ObservableList<ScheduleRecord> scheduleList =
            FXCollections.observableArrayList();

    private final String url =
            "jdbc:mysql://localhost:3306/students_attendance";

    private final String user = "root";

    private final String password = "";

    @FXML
    public void initialize() {

        setupColumns();

        loadSchedule();
    }

    private void setupColumns() {

        lecturerColumn.setCellValueFactory(
                new PropertyValueFactory<>("lecturer")
        );

        moduleColumn.setCellValueFactory(
                new PropertyValueFactory<>("module")
        );

        classColumn.setCellValueFactory(
                new PropertyValueFactory<>("className")
        );

        dayColumn.setCellValueFactory(
                new PropertyValueFactory<>("day")
        );

        startTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("startTime")
        );

        endTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("endTime")
        );

        roomColumn.setCellValueFactory(
                new PropertyValueFactory<>("room")
        );
    }

    @FXML
    private void loadSchedule() {

        scheduleList.clear();

        String sql = """
            SELECT
                s.schedule_id,
                CONCAT(l.first_name, ' ', l.last_name) AS lecturer,
                m.module_name,
                m.class_name,
                s.day_of_week,
                s.start_time,
                s.end_time,
                s.room
            FROM schedule s
            JOIN lecturer_module lm
                ON s.lecturer_module_id = lm.lecturer_module_id
            JOIN lectures l
                ON lm.lecturer_id = l.lecture_id
            JOIN modules m
                ON lm.module_id = m.id
            ORDER BY
                FIELD(
                    s.day_of_week,
                    'Monday',
                    'Tuesday',
                    'Wednesday',
                    'Thursday',
                    'Friday',
                    'Saturday',
                    'Sunday'
                ),
                s.start_time
            """;

        try (Connection conn =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                scheduleList.add(
                        new ScheduleRecord(
                                rs.getString("lecturer"),
                                rs.getString("module_name"),
                                rs.getString("class_name"),
                                rs.getString("day_of_week"),
                                rs.getString("start_time"),
                                rs.getString("end_time"),
                                rs.getString("room")
                        )
                );
            }

            scheduleTable.setItems(scheduleList);

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Database Error",
                    "Could not load lecture schedule:\n"
                            + e.getMessage()
            );
        }
    }

    private void showAlert(String title, String message) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}