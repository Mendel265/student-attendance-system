package com.example.studentattendance;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.io.IOException;

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
            // FIXED: Open in a NEW window instead of replacing the dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manage_classes.fxml"));
            Parent root = loader.load();

            // Get the current stage (dashboard)
            Stage dashboardStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create a new stage for Manage Classes
            Stage manageClassesStage = new Stage();
            manageClassesStage.setTitle("Manage Classes");
            manageClassesStage.setScene(new Scene(root));

            // Set the owner to the dashboard stage
            manageClassesStage.initOwner(dashboardStage);

            // Show the new window - dashboard stays open in background
            manageClassesStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleViewReports(ActionEvent event) {
        System.out.println("View Attendance Reports clicked");
        // TODO: Load Attendance Reports screen
    }

    @FXML
    private void handleManageSchedule(ActionEvent event) {
        try {
            // FIXED: Open in a NEW window instead of replacing the dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_schedule.fxml"));
            Parent root = loader.load();

            // Get the current stage (dashboard)
            Stage dashboardStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create a new stage for Manage Schedule
            Stage scheduleStage = new Stage();
            scheduleStage.setTitle("Manage Lecture Schedule");
            scheduleStage.setScene(new Scene(root));

            // Set the owner to the dashboard stage
            scheduleStage.initOwner(dashboardStage);

            // Show the new window - dashboard stays open in background
            scheduleStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.setTitle("Login");
            loginStage.show();

            // Close the dashboard
            ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}