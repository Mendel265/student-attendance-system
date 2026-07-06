package com.example.studentattendance;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SemesterSelectionController {

    @FXML
    private Label classLabel;

    private String className;

    public void setSelectedClass(String className) {
        this.className = className;
        classLabel.setText("Select Semester for: " + className);
    }

    @FXML
    private void handleFirstSemester() {
        openModulesPage("First Semester");
    }

    @FXML
    private void handleSecondSemester() {
        openModulesPage("Second Semester");
    }

    private void openModulesPage(String semester) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/class_modules.fxml"));
            Parent root = loader.load();

            ClassModulesController controller = loader.getController();
            controller.setClassAndSemester(className, semester);

            Stage stage = new Stage();
            stage.setTitle("Modules - " + className + " - " + semester);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}