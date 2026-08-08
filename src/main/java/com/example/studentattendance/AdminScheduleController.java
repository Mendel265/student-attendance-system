package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminScheduleController {

    // =========================================================
    // FORM
    // =========================================================

    @FXML
    private ComboBox<LecturerItem> lecturerComboBox;

    @FXML
    private ComboBox<ModuleItem> moduleComboBox;

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


    // =========================================================
    // TABLE
    // =========================================================

    @FXML
    private TableView<ScheduleItem> scheduleTable;

    @FXML
    private TableColumn<ScheduleItem, String> lecturerColumn;

    @FXML
    private TableColumn<ScheduleItem, String> moduleColumn;

    @FXML
    private TableColumn<ScheduleItem, String> classColumn;

    @FXML
    private TableColumn<ScheduleItem, String> dayColumn;

    @FXML
    private TableColumn<ScheduleItem, String> startTimeColumn;

    @FXML
    private TableColumn<ScheduleItem, String> endTimeColumn;

    @FXML
    private TableColumn<ScheduleItem, String> roomColumn;


    // =========================================================
    // DATA
    // =========================================================

    private final ObservableList<ScheduleItem> scheduleList =
            FXCollections.observableArrayList();

    private ScheduleItem selectedSchedule;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        setupTable();

        loadLecturers();
        loadClasses();
        loadDays();
        loadSchedules();

        lecturerComboBox.setOnAction(event ->
                loadModulesForLecturer()
        );

        moduleComboBox.setOnAction(event ->
                loadClassForSelectedModule()
        );

        scheduleTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {

                    selectedSchedule = newValue;

                    if (newValue != null) {
                        populateForm(newValue);
                    }
                });
    }


    // =========================================================
    // TABLE SETUP
    // =========================================================

    private void setupTable() {

        lecturerColumn.setCellValueFactory(
                new PropertyValueFactory<>("lecturerName")
        );

        moduleColumn.setCellValueFactory(
                new PropertyValueFactory<>("moduleName")
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

        scheduleTable.setItems(scheduleList);
    }


    // =========================================================
    // LOAD LECTURERS
    // =========================================================

    private void loadLecturers() {

        lecturerComboBox.getItems().clear();

        String sql = """
                SELECT lecture_id,
                       first_name,
                       last_name
                FROM lectures
                ORDER BY first_name, last_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String fullName =
                        rs.getString("first_name")
                                + " "
                                + rs.getString("last_name");

                lecturerComboBox.getItems().add(
                        new LecturerItem(
                                rs.getInt("lecture_id"),
                                fullName
                        )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showError(
                    "Database Error",
                    "Failed to load lecturers:\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // LOAD MODULES FOR SELECTED LECTURER
    // =========================================================

    private void loadModulesForLecturer() {

        moduleComboBox.getItems().clear();
        classComboBox.getSelectionModel().clearSelection();

        LecturerItem lecturer =
                lecturerComboBox.getValue();

        if (lecturer == null) {
            return;
        }

        String sql = """
                SELECT m.id,
                       m.module_name,
                       m.class_name
                FROM lecturer_module lm
                INNER JOIN modules m
                    ON m.id = lm.module_id
                WHERE lm.lecturer_id = ?
                ORDER BY m.module_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, lecturer.getId());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    moduleComboBox.getItems().add(
                            new ModuleItem(
                                    rs.getInt("id"),
                                    rs.getString("module_name"),
                                    rs.getString("class_name")
                            )
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showError(
                    "Database Error",
                    "Failed to load assigned modules:\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // LOAD CLASS AFTER MODULE SELECTION
    // =========================================================

    private void loadClassForSelectedModule() {

        ModuleItem module =
                moduleComboBox.getValue();

        if (module == null) {
            return;
        }

        classComboBox.setValue(
                module.getClassName()
        );
    }


    // =========================================================
    // LOAD ALL CLASSES
    // =========================================================

    private void loadClasses() {

        classComboBox.getItems().clear();

        String sql = """
                SELECT DISTINCT class_name
                FROM modules
                WHERE class_name IS NOT NULL
                  AND class_name <> ''
                ORDER BY class_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                classComboBox.getItems().add(
                        rs.getString("class_name")
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showError(
                    "Database Error",
                    "Failed to load classes:\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // LOAD DAYS
    // =========================================================

    private void loadDays() {

        dayComboBox.setItems(
                FXCollections.observableArrayList(
                        "Monday",
                        "Tuesday",
                        "Wednesday",
                        "Thursday",
                        "Friday",
                        "Saturday"
                )
        );
    }


    // =========================================================
    // LOAD SCHEDULE TABLE
    // =========================================================

    private void loadSchedules() {

        scheduleList.clear();

        String sql = """
                SELECT
                    s.schedule_id,
                    s.lecturer_module_id,
                    s.day_of_week,
                    s.start_time,
                    s.end_time,
                    s.room,

                    lm.lecturer_id,
                    lm.module_id,

                    CONCAT(
                        l.first_name,
                        ' ',
                        l.last_name
                    ) AS lecturer_name,

                    m.module_name,
                    m.class_name

                FROM schedule s

                INNER JOIN lecturer_module lm
                    ON lm.lecturer_module_id =
                       s.lecturer_module_id

                INNER JOIN lectures l
                    ON l.lecture_id =
                       lm.lecturer_id

                INNER JOIN modules m
                    ON m.id =
                       lm.module_id

                ORDER BY
                    CASE s.day_of_week
                        WHEN 'Monday' THEN 1
                        WHEN 'Tuesday' THEN 2
                        WHEN 'Wednesday' THEN 3
                        WHEN 'Thursday' THEN 4
                        WHEN 'Friday' THEN 5
                        WHEN 'Saturday' THEN 6
                        ELSE 7
                    END,
                    s.start_time
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                scheduleList.add(
                        new ScheduleItem(
                                rs.getInt("schedule_id"),
                                rs.getInt("lecturer_module_id"),
                                rs.getInt("lecturer_id"),
                                rs.getInt("module_id"),
                                rs.getString("lecturer_name"),
                                rs.getString("module_name"),
                                rs.getString("class_name"),
                                rs.getString("day_of_week"),
                                rs.getString("start_time"),
                                rs.getString("end_time"),
                                rs.getString("room")
                        )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showError(
                    "Database Error",
                    "Failed to load lecture timetable:\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // ADD SCHEDULE
    // =========================================================

    @FXML
    private void handleAddSchedule() {

        LecturerItem lecturer =
                lecturerComboBox.getValue();

        ModuleItem module =
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


        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        if (lecturer == null) {

            showError(
                    "Missing Lecturer",
                    "Please select a lecturer."
            );

            return;
        }

        if (module == null) {

            showError(
                    "Missing Module",
                    "Please select a module."
            );

            return;
        }

        if (className == null) {

            showError(
                    "Missing Class",
                    "Please select a class."
            );

            return;
        }

        if (day == null) {

            showError(
                    "Missing Day",
                    "Please select a day."
            );

            return;
        }

        if (startTime.isEmpty()
                || endTime.isEmpty()) {

            showError(
                    "Missing Time",
                    "Please enter start and end time."
            );

            return;
        }

        if (room.isEmpty()) {

            showError(
                    "Missing Room",
                    "Please enter the room."
            );

            return;
        }


        // -----------------------------------------------------
        // FIND lecturer_module_id
        // -----------------------------------------------------

        int lecturerModuleId =
                findLecturerModuleId(
                        lecturer.getId(),
                        module.getId()
                );

        if (lecturerModuleId <= 0) {

            showError(
                    "Assignment Error",
                    "This module is not assigned to the selected lecturer."
            );

            return;
        }


        // -----------------------------------------------------
        // CHECK DUPLICATE
        // -----------------------------------------------------

        if (scheduleExists(
                lecturerModuleId,
                day,
                startTime,
                endTime
        )) {

            showError(
                    "Schedule Exists",
                    "This lecturer already has this schedule."
            );

            return;
        }


        // -----------------------------------------------------
        // INSERT
        // -----------------------------------------------------

        String sql = """
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

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, lecturerModuleId);
            ps.setString(2, day);
            ps.setString(3, startTime);
            ps.setString(4, endTime);
            ps.setString(5, room);

            int rows =
                    ps.executeUpdate();

            if (rows > 0) {

                showInfo(
                        "Success",
                        "Lecture schedule added successfully."
                );

                loadSchedules();
                handleClear();
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showError(
                    "Database Error",
                    "Failed to add lecture schedule:\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // FIND LECTURER_MODULE_ID
    // =========================================================

    private int findLecturerModuleId(
            int lecturerId,
            int moduleId
    ) {

        String sql = """
                SELECT lecturer_module_id
                FROM lecturer_module
                WHERE lecturer_id = ?
                  AND module_id = ?
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
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

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return -1;
    }


    // =========================================================
    // CHECK EXISTING SCHEDULE
    // =========================================================

    private boolean scheduleExists(
            int lecturerModuleId,
            String day,
            String startTime,
            String endTime
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM schedule
                WHERE lecturer_module_id = ?
                  AND day_of_week = ?
                  AND start_time = ?
                  AND end_time = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, lecturerModuleId);
            ps.setString(2, day);
            ps.setString(3, startTime);
            ps.setString(4, endTime);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return false;
    }


    // =========================================================
    // EDIT SCHEDULE
    // =========================================================

    @FXML
    private void handleEditSchedule() {

        if (selectedSchedule == null) {

            showError(
                    "No Schedule Selected",
                    "Please select a schedule from the timetable."
            );

            return;
        }

        String day =
                dayComboBox.getValue();

        String startTime =
                startTimeField.getText().trim();

        String endTime =
                endTimeField.getText().trim();

        String room =
                roomField.getText().trim();


        if (day == null
                || startTime.isEmpty()
                || endTime.isEmpty()
                || room.isEmpty()) {

            showError(
                    "Invalid Information",
                    "Please complete the schedule fields."
            );

            return;
        }


        String sql = """
                UPDATE schedule
                SET day_of_week = ?,
                    start_time = ?,
                    end_time = ?,
                    room = ?
                WHERE schedule_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, day);
            ps.setString(2, startTime);
            ps.setString(3, endTime);
            ps.setString(4, room);
            ps.setInt(
                    5,
                    selectedSchedule.getScheduleId()
            );

            int rows =
                    ps.executeUpdate();

            if (rows > 0) {

                showInfo(
                        "Success",
                        "Lecture schedule updated successfully."
                );

                loadSchedules();
                handleClear();
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showError(
                    "Database Error",
                    "Failed to update schedule:\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // DELETE SCHEDULE
    // =========================================================

    @FXML
    private void handleDeleteSchedule() {

        if (selectedSchedule == null) {

            showError(
                    "No Schedule Selected",
                    "Please select a schedule first."
            );

            return;
        }


        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle("Delete Schedule");
        alert.setHeaderText(null);

        alert.setContentText(
                "Are you sure you want to delete this lecture schedule?"
        );


        if (alert.showAndWait()
                .orElse(ButtonType.CANCEL)
                != ButtonType.OK) {

            return;
        }


        String sql = """
                DELETE FROM schedule
                WHERE schedule_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    selectedSchedule.getScheduleId()
            );

            int rows =
                    ps.executeUpdate();

            if (rows > 0) {

                showInfo(
                        "Success",
                        "Lecture schedule deleted successfully."
                );

                loadSchedules();
                handleClear();
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showError(
                    "Database Error",
                    "Failed to delete schedule:\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // CLEAR
    // =========================================================

    @FXML
    private void handleClear() {

        lecturerComboBox.getSelectionModel()
                .clearSelection();

        moduleComboBox.getItems().clear();
        moduleComboBox.getSelectionModel()
                .clearSelection();

        classComboBox.getSelectionModel()
                .clearSelection();

        dayComboBox.getSelectionModel()
                .clearSelection();

        startTimeField.clear();
        endTimeField.clear();
        roomField.clear();

        scheduleTable.getSelectionModel()
                .clearSelection();

        selectedSchedule = null;
    }


    // =========================================================
    // POPULATE FORM FOR EDITING
    // =========================================================

    private void populateForm(
            ScheduleItem item
    ) {

        // Lecturer
        for (LecturerItem lecturer :
                lecturerComboBox.getItems()) {

            if (lecturer.getId()
                    == item.getLecturerId()) {

                lecturerComboBox.setValue(lecturer);
                break;
            }
        }


        // Load assigned modules
        loadModulesForLecturer();


        // Module
        for (ModuleItem module :
                moduleComboBox.getItems()) {

            if (module.getId()
                    == item.getModuleId()) {

                moduleComboBox.setValue(module);
                break;
            }
        }


        classComboBox.setValue(
                item.getClassName()
        );

        dayComboBox.setValue(
                item.getDay()
        );

        startTimeField.setText(
                item.getStartTime()
        );

        endTimeField.setText(
                item.getEndTime()
        );

        roomField.setText(
                item.getRoom()
        );
    }


    // =========================================================
    // BACK
    // =========================================================

    @FXML
    private void handleBack() {

        try {

            Stage stage =
                    (Stage) scheduleTable
                            .getScene()
                            .getWindow();

            stage.close();

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Error",
                    "Could not close the schedule window."
            );
        }
    }


    // =========================================================
    // LECTURER ITEM
    // =========================================================

    public static class LecturerItem {

        private final int id;
        private final String name;

        public LecturerItem(
                int id,
                String name
        ) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }


    // =========================================================
    // MODULE ITEM
    // =========================================================

    public static class ModuleItem {

        private final int id;
        private final String moduleName;
        private final String className;

        public ModuleItem(
                int id,
                String moduleName,
                String className
        ) {
            this.id = id;
            this.moduleName = moduleName;
            this.className = className;
        }

        public int getId() {
            return id;
        }

        public String getModuleName() {
            return moduleName;
        }

        public String getClassName() {
            return className;
        }

        @Override
        public String toString() {
            return moduleName;
        }
    }


    // =========================================================
    // SCHEDULE ITEM
    // =========================================================

    public static class ScheduleItem {

        private final int scheduleId;
        private final int lecturerModuleId;
        private final int lecturerId;
        private final int moduleId;

        private final String lecturerName;
        private final String moduleName;
        private final String className;
        private final String day;
        private final String startTime;
        private final String endTime;
        private final String room;


        public ScheduleItem(
                int scheduleId,
                int lecturerModuleId,
                int lecturerId,
                int moduleId,
                String lecturerName,
                String moduleName,
                String className,
                String day,
                String startTime,
                String endTime,
                String room
        ) {

            this.scheduleId = scheduleId;
            this.lecturerModuleId = lecturerModuleId;
            this.lecturerId = lecturerId;
            this.moduleId = moduleId;
            this.lecturerName = lecturerName;
            this.moduleName = moduleName;
            this.className = className;
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
            this.room = room;
        }


        public int getScheduleId() {
            return scheduleId;
        }

        public int getLecturerModuleId() {
            return lecturerModuleId;
        }

        public int getLecturerId() {
            return lecturerId;
        }

        public int getModuleId() {
            return moduleId;
        }

        public String getLecturerName() {
            return lecturerName;
        }

        public String getModuleName() {
            return moduleName;
        }

        public String getClassName() {
            return className;
        }

        public String getDay() {
            return day;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public String getRoom() {
            return room;
        }
    }


    // =========================================================
    // ALERTS
    // =========================================================

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }


    private void showInfo(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}