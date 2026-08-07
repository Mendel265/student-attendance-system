package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ManageClassesController {

    @FXML
    private ListView<String> classListView;

    @FXML
    public void initialize() {
        loadClasses();
        classListView.setOnMouseClicked(this::handleClassClick);
    }

    private void loadClasses() {
        classListView.getItems().clear();

        Set<String> classSet = new HashSet<>();
        String sql = "SELECT DISTINCT class_name FROM students ORDER BY class_name";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                classSet.add(rs.getString("class_name"));
            }
            classListView.getItems().addAll(classSet);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to load classes: " + e.getMessage());
        }
    }

    private void handleClassClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selectedClass = classListView.getSelectionModel().getSelectedItem();
            if (selectedClass != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/semester_selection.fxml"));
                    Parent root = loader.load();

                    SemesterSelectionController controller = loader.getController();
                    controller.setSelectedClass(selectedClass);

                    Stage stage = new Stage();
                    stage.setTitle("Select Semester for " + selectedClass);
                    stage.setScene(new Scene(root));
                    stage.show();

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", "Could not open semester selection: " + e.getMessage());
                }
            }
        }
    }

    // ============= Navigation Methods =============

    @FXML
    private void handleBack() {
        try {
            // Get the current stage
            Stage stage = (Stage) classListView.getScene().getWindow();

            // If this window has an owner (the dashboard), show it and close this one
            if (stage.getOwner() != null) {
                Stage ownerStage = (Stage) stage.getOwner();
                ownerStage.show();
                stage.close();
            } else {
                // If no owner, just close this window
                // The dashboard should still be open in the background
                stage.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Could not close the window.");
        }
    }

    // ============= Helper Methods =============

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}