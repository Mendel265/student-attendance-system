package com.example.studentattendance;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class LecturerActionsController {

    @FXML
    private Label lecturerNameLabel;

    @FXML
    private Button viewAssignedClassesButton;

    @FXML
    private Button assignClassAndModulesButton;

    private int lecturerId;
    private String lecturerName;

    public void setLecturerData(
            int lecturerId,
            String lecturerName
    ) {

        this.lecturerId = lecturerId;
        this.lecturerName = lecturerName;

        if (lecturerNameLabel != null) {
            lecturerNameLabel.setText(
                    "Actions for: " + lecturerName
            );
        }
    }

    @FXML
    private void handleViewAssignedClasses() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/view_assigned_classes.fxml"
                    )
            );

            Parent root = loader.load();

            ViewAssignedClassesController controller =
                    loader.getController();

            controller.setLecturerId(
                    lecturerId,
                    lecturerName
            );

            Stage stage = new Stage();

            stage.setTitle(
                    "Assigned Modules - "
                            + lecturerName
            );

            stage.setScene(
                    new Scene(root)
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    @FXML
    private void handleAssignClassAndModules() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/assign_class_semester.fxml"
                    )
            );

            Parent root = loader.load();

            AssignClassSemesterController controller =
                    loader.getController();

            controller.setLecturer(
                    lecturerId,
                    lecturerName
            );

            Stage stage = new Stage();

            stage.setTitle(
                    "Assign Modules - "
                            + lecturerName
            );

            stage.setScene(
                    new Scene(root)
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}