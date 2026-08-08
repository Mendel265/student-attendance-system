package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AssignClassSemesterController {

    @FXML
    private Label lecturerNameLabel;

    @FXML
    private ComboBox<String> classComboBox;

    @FXML
    private ComboBox<String> semesterComboBox;

    @FXML
    private ListView<ModuleItem> moduleListView;

    private int lecturerId;
    private String lecturerName;

    public void setLecturer(int lecturerId, String lecturerName) {

        this.lecturerId = lecturerId;
        this.lecturerName = lecturerName;

        if (lecturerNameLabel != null) {
            lecturerNameLabel.setText(
                    "Assign Modules to: " + lecturerName
            );
        }
    }

    @FXML
    public void initialize() {

        // Allow selecting multiple modules
        moduleListView.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);

        loadClasses();
        loadSemesters();

        // Automatically load modules when both are selected
        classComboBox.setOnAction(event -> loadModules());
        semesterComboBox.setOnAction(event -> loadModules());
    }


    // =========================================================
    // LOAD CLASSES
    // =========================================================

    private void loadClasses() {

        classComboBox.getItems().clear();

        String sql = """
                SELECT DISTINCT class_name
                FROM modules
                ORDER BY class_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
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
                    "Failed to load classes: " + e.getMessage()
            );
        }
    }


    // =========================================================
    // LOAD SEMESTERS
    // =========================================================

    private void loadSemesters() {

        semesterComboBox.getItems().clear();

        String sql = """
                SELECT DISTINCT semester
                FROM modules
                ORDER BY semester
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                semesterComboBox.getItems().add(
                        rs.getString("semester")
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showError(
                    "Database Error",
                    "Failed to load semesters: " + e.getMessage()
            );
        }
    }


    // =========================================================
    // LOAD MODULES
    // =========================================================

    private void loadModules() {

        String selectedClass =
                classComboBox.getValue();

        String selectedSemester =
                semesterComboBox.getValue();

        moduleListView.getItems().clear();

        if (selectedClass == null ||
                selectedSemester == null) {

            return;
        }

        String sql = """
                SELECT id, module_name
                FROM modules
                WHERE class_name = ?
                  AND semester = ?
                ORDER BY module_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, selectedClass);
            ps.setString(2, selectedSemester);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    ModuleItem module =
                            new ModuleItem(
                                    rs.getInt("id"),
                                    rs.getString("module_name")
                            );

                    moduleListView.getItems().add(module);
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showError(
                    "Database Error",
                    "Failed to load modules: "
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // ASSIGN SELECTED MODULES
    // =========================================================

    @FXML
    private void handleAssignModules() {

        // Make sure lecturer exists
        if (lecturerId <= 0) {

            showError(
                    "No Lecturer",
                    "No lecturer was selected."
            );

            return;
        }

        // Make sure class and semester were selected
        String selectedClass =
                classComboBox.getValue();

        String selectedSemester =
                semesterComboBox.getValue();

        if (selectedClass == null ||
                selectedSemester == null) {

            showError(
                    "Missing Selection",
                    "Please select a class and semester."
            );

            return;
        }

        // Get selected modules
        var selectedModules =
                moduleListView
                        .getSelectionModel()
                        .getSelectedItems();

        if (selectedModules.isEmpty()) {

            showError(
                    "No Modules Selected",
                    "Please select at least one module."
            );

            return;
        }


        String insertSql = """
                INSERT INTO lecturer_module
                (lecturer_id, module_id)
                VALUES (?, ?)
                """;


        try (Connection conn =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(insertSql)) {


            int assignedCount = 0;
            int skippedCount = 0;


            for (ModuleItem module : selectedModules) {

                // Prevent duplicate assignment
                if (isAlreadyAssigned(
                        conn,
                        lecturerId,
                        module.getId())) {

                    skippedCount++;
                    continue;
                }


                ps.setInt(
                        1,
                        lecturerId
                );

                ps.setInt(
                        2,
                        module.getId()
                );

                ps.addBatch();

                assignedCount++;
            }


            // Execute only if there are new assignments
            if (assignedCount > 0) {
                ps.executeBatch();
            }


            // Show appropriate result
            if (assignedCount > 0 &&
                    skippedCount > 0) {

                showInfo(
                        "Assignment Complete",
                        assignedCount
                                + " module(s) assigned to "
                                + lecturerName
                                + ".\n\n"
                                + skippedCount
                                + " module(s) were already assigned."
                );

            } else if (assignedCount > 0) {

                showInfo(
                        "Assignment Successful",
                        assignedCount
                                + " module(s) assigned to "
                                + lecturerName
                                + " successfully."
                );

            } else {

                showInfo(
                        "Already Assigned",
                        "All selected modules are already assigned "
                                + "to "
                                + lecturerName
                                + "."
                );
            }


            // Clear selection
            moduleListView
                    .getSelectionModel()
                    .clearSelection();


        } catch (SQLException e) {

            e.printStackTrace();

            showError(
                    "Database Error",
                    "Failed to assign modules: "
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // CHECK DUPLICATE ASSIGNMENT
    // =========================================================

    private boolean isAlreadyAssigned(
            Connection conn,
            int lecturerId,
            int moduleId
    ) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM lecturer_module
                WHERE lecturer_id = ?
                  AND module_id = ?
                """;


        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    lecturerId
            );

            ps.setInt(
                    2,
                    moduleId
            );


            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }


    // =========================================================
    // MODULE ITEM
    // =========================================================

    public static class ModuleItem {

        private final int id;
        private final String moduleName;


        public ModuleItem(
                int id,
                String moduleName
        ) {

            this.id = id;
            this.moduleName = moduleName;
        }


        public int getId() {
            return id;
        }


        public String getModuleName() {
            return moduleName;
        }


        @Override
        public String toString() {
            return moduleName;
        }
    }


    // =========================================================
    // ERROR ALERT
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


    // =========================================================
    // INFORMATION ALERT
    // =========================================================

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
