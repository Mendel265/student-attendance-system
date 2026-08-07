package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import com.example.studentattendance.models.Lecturer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class ManageLecturerController {

    @FXML
    private TableView<Lecturer> lecturerTable;

    @FXML
    private TableColumn<Lecturer, Integer> idColumn;

    @FXML
    private TableColumn<Lecturer, String> nameColumn;

    @FXML
    private TableColumn<Lecturer, String> emailColumn;

    private Lecturer selectedLecturer;

    @FXML
    public void initialize() {
        setupTable();
        loadLecturers();

        lecturerTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {
                    selectedLecturer = newValue;
                });
    }

    private void setupTable() {
        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        emailColumn.setCellValueFactory(
                new PropertyValueFactory<>("email")
        );
    }

    private void loadLecturers() {
        // Clear existing items first
        lecturerTable.getItems().clear();

        String sql = """
                SELECT lecture_id,
                       first_name,
                       last_name,
                       email
                FROM lectures
                ORDER BY first_name, last_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Lecturer lecturer = new Lecturer(
                        rs.getInt("lecture_id"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("email")
                );

                lecturerTable.getItems().add(lecturer);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Database Error", "Failed to load lecturers: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) lecturerTable.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Could not close the window.");
        }
    }



    @FXML
    private void handleAssignModules() {
        if (selectedLecturer == null) {
            showError(
                    "No Lecturer Selected",
                    "Please select a lecturer first."
            );
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/assign_modules.fxml")
            );

            Parent root = loader.load();

            AssignModulesController controller = loader.getController();
            controller.setLecturer(selectedLecturer);

            Stage stage = new Stage();
            stage.setTitle("Assign Modules - " + selectedLecturer.getName());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh the table if any changes were made
            loadLecturers();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not open assign modules dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddLecturer() {
        try {
            // Load the add lecturer dialog
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/add_lecturer.fxml")
            );

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Lecturer");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh the table after adding
            loadLecturers();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not open add lecturer dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditLecturer() {
        if (selectedLecturer == null) {
            showError(
                    "No Lecturer Selected",
                    "Please select a lecturer first."
            );
            return;
        }

        // Show an edit dialog using a TextInputDialog or custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Lecturer");
        dialog.setHeaderText("Editing: " + selectedLecturer.getName());

        // Set up the dialog buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        TextField nameField = new TextField(selectedLecturer.getName());
        TextField emailField = new TextField(selectedLecturer.getEmail());

        // Set up the layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Enable/disable save button based on input validation
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Add validation listeners
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(newVal.trim().isEmpty() || emailField.getText().trim().isEmpty());
        });
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(newVal.trim().isEmpty() || nameField.getText().trim().isEmpty());
        });

        // Show the dialog and process the result
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButtonType) {
            // Perform the update
            String newName = nameField.getText().trim();
            String newEmail = emailField.getText().trim();

            // Parse name into first and last
            String[] nameParts = newName.split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            String sql = "UPDATE lectures SET first_name = ?, last_name = ?, email = ? WHERE lecture_id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setString(3, newEmail);
                ps.setInt(4, selectedLecturer.getId());

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    showInfo("Success", "Lecturer updated successfully.");
                    loadLecturers(); // Refresh the table
                } else {
                    showError("Error", "Failed to update lecturer.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Database Error", "Failed to update lecturer: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteLecturer() {
        if (selectedLecturer == null) {
            showError(
                    "No Lecturer Selected",
                    "Please select a lecturer first."
            );
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Lecturer");
        alert.setHeaderText(null);
        alert.setContentText(
                "Are you sure you want to delete "
                        + selectedLecturer.getName()
                        + "?"
        );

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Perform deletion
            String sql = "DELETE FROM lectures WHERE lecture_id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, selectedLecturer.getId());
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    showInfo("Success", "Lecturer deleted successfully.");
                    loadLecturers(); // Refresh the table
                } else {
                    showError("Error", "Failed to delete lecturer.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Database Error", "Failed to delete lecturer: " + e.getMessage());
            }
        }
    }

    // ============= Alert Helper Methods =============

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}