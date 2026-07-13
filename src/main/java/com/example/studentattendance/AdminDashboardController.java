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

    @FXML
    private void handleManageSchedule(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin_schedule.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Manage Lecture Schedule");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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









