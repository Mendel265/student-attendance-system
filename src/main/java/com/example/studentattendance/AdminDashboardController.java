package com.example.studentattendance;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class AdminDashboardController {

    @FXML
    public void handleManageLecturers(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manage_lecturer.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Manage Lecturers");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void handleManageStudents(ActionEvent event) {
        System.out.println("Manage Students clicked");
        // TODO: Load Manage Students screen
    }
    @FXML
    private void handleManageClasses(ActionEvent event) {
        try {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/manage_classes.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Manage Classes");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void handleViewReports(ActionEvent event) {
        System.out.println("View Attendance Reports clicked");
        // TODO: Load Attendance Reports screen
    }

    public void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Login");
            stage.show();

            // Close the dashboard
            ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}









