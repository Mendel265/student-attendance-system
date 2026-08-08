package com.example.studentattendance;

import com.example.studentattendance.models.LecturerOption;
import com.example.studentattendance.models.ModuleOption;
import com.example.studentattendance.models.ScheduleRecord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManageScheduleController {

    @FXML
    private ComboBox<LecturerOption> lecturerComboBox;

    @FXML
    private ComboBox<ModuleOption> moduleComboBox;

    @FXML
    private ComboBox<String> classComboBox;

    @FXML
    private ComboBox<String> dayComboBox;

    @FXML
    private TextField startTimeField;

    @FXML
    private TextField endTimeField;

    @FXML
    private TextField roomField;

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

    private final ObservableList<LecturerOption> lecturers =
            FXCollections.observableArrayList();

    private final ObservableList<ModuleOption> modules =
            FXCollections.observableArrayList();


    private final String url =
            "jdbc:mysql://localhost:3306/students_attendance";

    private final String user = "root";

    private final String password = "";


    @FXML
    public void initialize() {

        setupColumns();

        setupDays();

        loadLecturers();

        loadModules();

        loadSchedule();

        moduleComboBox.setOnAction(event -> {

            ModuleOption selected =
                    moduleComboBox.getValue();

            classComboBox.getItems().clear();

            if (selected != null) {

                classComboBox.getItems().add(
                        selected.getClassName()
                );
            }
        });
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


    private void setupDays() {

        dayComboBox.setItems(
                FXCollections.observableArrayList(
                        "Monday",
                        "Tuesday",
                        "Wednesday",
                        "Thursday",
                        "Friday",
                        "Saturday",
                        "Sunday"
                )
        );
    }


    private void loadLecturers() {

        lecturers.clear();

        String sql = """
                SELECT
                    lecture_id,
                    first_name,
                    last_name
                FROM lectures
                ORDER BY first_name, last_name
                """;

        try (Connection conn =
                     DriverManager.getConnection(
                             url,
                             user,
                             password
                     );

             PreparedStatement ps =
                     conn.prepareStatement(sql);

             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                lecturers.add(
                        new LecturerOption(
                                rs.getInt("lecture_id"),
                                rs.getString("first_name")
                                        + " "
                                        + rs.getString("last_name")
                        )
                );
            }

            lecturerComboBox.setItems(lecturers);

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not load lecturers",
                    e.getMessage()
            );
        }
    }


    private void loadModules() {

        modules.clear();

        String sql = """
                SELECT
                    id,
                    module_name,
                    class_name
                FROM modules
                ORDER BY module_name
                """;

        try (Connection conn =
                     DriverManager.getConnection(
                             url,
                             user,
                             password
                     );

             PreparedStatement ps =
                     conn.prepareStatement(sql);

             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                modules.add(
                        new ModuleOption(
                                rs.getInt("id"),
                                rs.getString("module_name"),
                                rs.getString("class_name")
                        )
                );
            }

            moduleComboBox.setItems(modules);

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not load modules",
                    e.getMessage()
            );
        }
    }


    @FXML
    public void loadSchedule() {

        scheduleList.clear();

        String sql = """
                SELECT
                    s.schedule_id,
                    lm.lecturer_module_id,

                    CONCAT(
                        l.first_name,
                        ' ',
                        l.last_name
                    ) AS lecturer,

                    m.module_name,
                    m.class_name,

                    s.day_of_week,
                    s.start_time,
                    s.end_time,
                    s.room

                FROM schedule s

                JOIN lecturer_module lm
                    ON s.lecturer_module_id =
                       lm.lecturer_module_id

                JOIN lectures l
                    ON lm.lecturer_id =
                       l.lecture_id

                JOIN modules m
                    ON lm.module_id =
                       m.id

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
                     DriverManager.getConnection(
                             url,
                             user,
                             password
                     );

             PreparedStatement ps =
                     conn.prepareStatement(sql);

             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                scheduleList.add(
                        new ScheduleRecord(

                                rs.getInt("schedule_id"),

                                rs.getInt(
                                        "lecturer_module_id"
                                ),

                                rs.getString(
                                        "lecturer"
                                ),

                                rs.getString(
                                        "module_name"
                                ),

                                rs.getString(
                                        "class_name"
                                ),

                                rs.getString(
                                        "day_of_week"
                                ),

                                rs.getString(
                                        "start_time"
                                ),

                                rs.getString(
                                        "end_time"
                                ),

                                rs.getString(
                                        "room"
                                )
                        )
                );
            }

            scheduleTable.setItems(scheduleList);

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not load schedule",
                    e.getMessage()
            );
        }
    }


    @FXML
    private void handleAddSchedule() {

        LecturerOption lecturer =
                lecturerComboBox.getValue();

        ModuleOption module =
                moduleComboBox.getValue();

        String className =
                classComboBox.getValue();

        String day =
                dayComboBox.getValue();

        String startTime =
                startTimeField.getText().trim();

        String endTime =
                endTimeField.getText().trim();

        String room =
                roomField.getText().trim();


        if (lecturer == null ||
                module == null ||
                className == null ||
                day == null ||
                startTime.isEmpty() ||
                endTime.isEmpty() ||
                room.isEmpty()) {

            showWarning(
                    "Please complete all schedule fields."
            );

            return;
        }


        try (Connection conn =
                     DriverManager.getConnection(
                             url,
                             user,
                             password
                     )) {


            // First find/create the lecturer-module relationship

            int lecturerModuleId =
                    getLecturerModuleId(
                            conn,
                            lecturer.getId(),
                            module.getId()
                    );


            if (lecturerModuleId == -1) {

                String insertLM = """
                        INSERT INTO lecturer_module
                            (lecturer_id, module_id)
                        VALUES (?, ?)
                        """;

                try (PreparedStatement ps =
                             conn.prepareStatement(
                                     insertLM,
                                     java.sql.Statement.RETURN_GENERATED_KEYS
                             )) {

                    ps.setInt(
                            1,
                            lecturer.getId()
                    );

                    ps.setInt(
                            2,
                            module.getId()
                    );

                    ps.executeUpdate();

                    try (ResultSet keys =
                                 ps.getGeneratedKeys()) {

                        if (keys.next()) {

                            lecturerModuleId =
                                    keys.getInt(1);
                        }
                    }
                }
            }


            // Insert the actual timetable entry

            String insertSchedule = """
                    INSERT INTO schedule
                        (
                            lecturer_module_id,
                            day_of_week,
                            start_time,
                            end_time,
                            room
                        )
                    VALUES (?, ?, ?, ?, ?)
                    """;


            try (PreparedStatement ps =
                         conn.prepareStatement(
                                 insertSchedule
                         )) {

                ps.setInt(
                        1,
                        lecturerModuleId
                );

                ps.setString(
                        2,
                        day
                );

                ps.setString(
                        3,
                        startTime
                );

                ps.setString(
                        4,
                        endTime
                );

                ps.setString(
                        5,
                        room
                );

                ps.executeUpdate();
            }


            showInfo(
                    "Schedule added successfully."
            );

            loadSchedule();

            handleClear();

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not add schedule",
                    e.getMessage()
            );
        }
    }


    private int getLecturerModuleId(
            Connection conn,
            int lecturerId,
            int moduleId) throws Exception {

        String sql = """
                SELECT lecturer_module_id
                FROM lecturer_module
                WHERE lecturer_id = ?
                  AND module_id = ?
                """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, lecturerId);
            ps.setInt(2, moduleId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt(
                            "lecturer_module_id"
                    );
                }
            }
        }

        return -1;
    }


    @FXML
    private void handleDeleteSchedule() {

        ScheduleRecord selected =
                scheduleTable
                        .getSelectionModel()
                        .getSelectedItem();


        if (selected == null) {

            showWarning(
                    "Select a schedule to delete."
            );

            return;
        }


        String sql = """
                DELETE FROM schedule
                WHERE schedule_id = ?
                """;


        try (Connection conn =
                     DriverManager.getConnection(
                             url,
                             user,
                             password
                     );

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    selected.getScheduleId()
            );

            ps.executeUpdate();

            loadSchedule();

            handleClear();

            showInfo(
                    "Schedule deleted successfully."
            );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not delete schedule",
                    e.getMessage()
            );
        }
    }


    @FXML
    private void handleUpdateSchedule() {

        ScheduleRecord selected =
                scheduleTable
                        .getSelectionModel()
                        .getSelectedItem();


        if (selected == null) {

            showWarning(
                    "Select a schedule to update."
            );

            return;
        }


        String day =
                dayComboBox.getValue();

        String start =
                startTimeField.getText().trim();

        String end =
                endTimeField.getText().trim();

        String room =
                roomField.getText().trim();


        if (day == null ||
                start.isEmpty() ||
                end.isEmpty() ||
                room.isEmpty()) {

            showWarning(
                    "Please provide day, time and room."
            );

            return;
        }


        String sql = """
                UPDATE schedule
                SET
                    day_of_week = ?,
                    start_time = ?,
                    end_time = ?,
                    room = ?
                WHERE schedule_id = ?
                """;


        try (Connection conn =
                     DriverManager.getConnection(
                             url,
                             user,
                             password
                     );

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, day);
            ps.setString(2, start);
            ps.setString(3, end);
            ps.setString(4, room);

            ps.setInt(
                    5,
                    selected.getScheduleId()
            );

            ps.executeUpdate();

            loadSchedule();

            handleClear();

            showInfo(
                    "Schedule updated successfully."
            );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not update schedule",
                    e.getMessage()
            );
        }
    }


    @FXML
    private void handleClear() {

        lecturerComboBox.getSelectionModel()
                .clearSelection();

        moduleComboBox.getSelectionModel()
                .clearSelection();

        classComboBox.getItems().clear();

        dayComboBox.getSelectionModel()
                .clearSelection();

        startTimeField.clear();

        endTimeField.clear();

        roomField.clear();

        scheduleTable.getSelectionModel()
                .clearSelection();
    }


    private void showWarning(String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }


    private void showInfo(String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }


    private void showError(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}