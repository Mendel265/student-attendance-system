package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import com.example.studentattendance.models.Lecturer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Label;
import javafx.stage.Stage;

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
    private ListView<String> moduleListView;


    private Lecturer lecturer;

    /*
     * Stores the database IDs of the modules currently
     * displayed in the ListView.
     */
    private final ObservableList<Integer> moduleIds =
            FXCollections.observableArrayList();


    public void setLecturer(Lecturer lecturer) {

        this.lecturer = lecturer;

        if (lecturerNameLabel != null && lecturer != null) {

            lecturerNameLabel.setText(
                    "Assign to: " + lecturer.getName()
            );
        }

        loadClasses();
    }

    @FXML
    public void initialize() {

        classComboBox.setOnAction(event -> {

            semesterComboBox.getItems().clear();
            moduleListView.getItems().clear();
            moduleIds.clear();

            if (classComboBox.getValue() != null) {
                loadSemesters();
            }
        });
    }


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

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not load classes:\n"
                            + e.getMessage()
            );
        }
    }

    private void loadSemesters() {

        semesterComboBox.getItems().clear();

        String selectedClass =
                classComboBox.getValue();

        String sql = """
                SELECT DISTINCT semester
                FROM modules
                WHERE class_name = ?
                AND semester IS NOT NULL
                ORDER BY semester
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, selectedClass);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                semesterComboBox.getItems().add(
                        rs.getString("semester")
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not load semesters:\n"
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void handleLoadModules() {

        String selectedClass =
                classComboBox.getValue();

        String selectedSemester =
                semesterComboBox.getValue();


        if (selectedClass == null
                || selectedClass.isBlank()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Select Class",
                    "Please select a class first."
            );

            return;
        }


        if (selectedSemester == null
                || selectedSemester.isBlank()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Select Semester",
                    "Please select a semester first."
            );

            return;
        }


        moduleListView.getItems().clear();
        moduleIds.clear();


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

            ResultSet rs = ps.executeQuery();


            while (rs.next()) {

                int moduleId =
                        rs.getInt("id");

                String moduleName =
                        rs.getString("module_name");


                moduleIds.add(moduleId);

                moduleListView.getItems().add(
                        moduleName
                );
            }


            moduleListView.getSelectionModel()
                    .setSelectionMode(
                            SelectionMode.MULTIPLE
                    );


            if (moduleListView.getItems().isEmpty()) {

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "No Modules",
                        "No modules were found for "
                                + selectedClass
                                + " - "
                                + selectedSemester
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not load modules:\n"
                            + e.getMessage()
            );
        }
    }


    @FXML
    private void handleAssignModules() {

        if (lecturer == null) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "No Lecturer",
                    "No lecturer has been selected."
            );

            return;
        }


        if (moduleListView.getItems().isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Modules",
                    "Load the modules first."
            );

            return;
        }


        ObservableList<Integer> selectedIndices =
                moduleListView
                        .getSelectionModel()
                        .getSelectedIndices();


        if (selectedIndices.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Modules Selected",
                    "Please select at least one module."
            );

            return;
        }


        String sql = """
                INSERT INTO lecturer_module
                    (lecturer_id, module_id)
                VALUES (?, ?)
                """;


        String checkSql = """
                SELECT COUNT(*)
                FROM lecturer_module
                WHERE lecturer_id = ?
                AND module_id = ?
                """;


        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);


            try {

                for (Integer index : selectedIndices) {

                    int moduleId =
                            moduleIds.get(index);

                    try (PreparedStatement check =
                                 conn.prepareStatement(checkSql)) {

                        check.setInt(
                                1,
                                lecturer.getId()
                        );

                        check.setInt(
                                2,
                                moduleId
                        );


                        ResultSet rs =
                                check.executeQuery();


                        if (rs.next()
                                && rs.getInt(1) > 0) {

                            continue;
                        }
                    }

                    try (PreparedStatement ps =
                                 conn.prepareStatement(sql)) {

                        ps.setInt(
                                1,
                                lecturer.getId()
                        );

                        ps.setInt(
                                2,
                                moduleId
                        );

                        ps.executeUpdate();
                    }
                }


                conn.commit();


                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Assignment Successful",
                        "Module(s) successfully assigned to "
                                + lecturer.getName()
                );


                // Close window
                Stage stage =
                        (Stage) moduleListView
                                .getScene()
                                .getWindow();

                stage.close();


            } catch (SQLException e) {

                conn.rollback();

                throw e;
            }


        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not assign modules:\n"
                            + e.getMessage()
            );
        }
    }

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert =
                new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
