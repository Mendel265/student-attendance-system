package com.example.studentattendance;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class LecturerDashboardController {


    private int lecturerId;



    @FXML
    private void onPrintAttendance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/print_attendance_dialog.fxml"));
            Parent root = loader.load();

            // Get the controller for the dialog
            PrintAttendanceDialogController controller = loader.getController();

            // Pass the lecturerId and initialize its data
            controller.setLecturerId(lecturerId);
            controller.initializeData();

            // Show the dialog
            Stage stage = new Stage();
            stage.setTitle("Print Attendance Report");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }







    public void setLecturerId(int id) {
        this.lecturerId = id;
        // You can now load modules/classes based on this ID
        System.out.println("Logged-in Lecturer ID: " + id);

    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Student Attendance System - Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to logout and load login screen.");
            alert.showAndWait();
        }
    }
    @FXML
    public void handleRegisterStudent(ActionEvent event){
        try {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/student_register.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Register Student");
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
        }
    }
    @FXML
    public void handleTakeAttendance(ActionEvent event) {
        try {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/take_attendance.fxml")); // FIX: Create loader first
            Parent root = loader.load();

            // Pass lecturer ID to TakeAttendanceController
            TakeAttendanceController controller = loader.getController();
            controller.setLecturerId(lecturerId);

            stage.setScene(new Scene(root));
            stage.setTitle("Take Attendance");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to load Take Attendance screen.");
            alert.showAndWait();
        }
    }


    @FXML
    private void handleTrainModel(ActionEvent event){
        try {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/train_face_model.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Train Face Model");
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
