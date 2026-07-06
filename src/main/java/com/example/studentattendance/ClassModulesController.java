package com.example.studentattendance;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ClassModulesController {


    private String className;
    private String semester;
    @FXML
    private Label infoLabel;




    public void setClassAndSemester(String className, String semester) {
        this.className = className;
        this.semester = semester;
        infoLabel.setText("Modules for " + className + " - " + semester);
    }

    @FXML
    private void handleViewModules() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view_modules.fxml"));
            Parent root = loader.load();
            ViewModulesController controller = loader.getController();
            controller.setClassAndSemester(className, semester);

            Stage stage = new Stage();
            stage.setTitle("View Modules - " + className + "-" + semester);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddModule() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_modules.fxml"));
            Parent root = loader.load();
            AddModulesController controller = loader.getController();
            controller.setClassAndSemester(className, semester);

            Stage stage = new Stage();
            stage.setTitle("Add Module - " + className + "-" + semester);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
