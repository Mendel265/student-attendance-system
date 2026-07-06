package com.example.studentattendance;



import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;

public class AddModulesController {

    @FXML
    private Label classLabel;
    @FXML
    private TextField moduleNameField;

    private String className;
    private String semester;

    public void setClassAndSemester(String className, String semester) {
        this.className = className;
        this.semester = semester;
        classLabel.setText("Class: " + className + " | " + semester);

    }


    public void setClassName(String className) {
        this.className = className;
        classLabel.setText("Add Module for: " + className);
    }

    @FXML
    private void handleAddModule() {
        String moduleName = moduleNameField.getText().trim();
        if (moduleName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Module name cannot be empty.");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "");
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO modules (class_name, module_name, semester) VALUES (?, ?, ?)")) {

            stmt.setString(1, className);
            stmt.setString(2, moduleName);
            stmt.setString(3, semester);
            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Module added successfully.");
            moduleNameField.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to add module.");
        }
    }



    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}