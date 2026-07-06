package com.example.studentattendance;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import org.bytedeco.javacv.*;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class StudentRegisterController {

    @FXML private TextField fnameField, lnameField, emailField, classField, studentIdField;
    @FXML private Button captureButton, registerButton;
    @FXML private ImageView cameraView;

    private OpenCVFrameGrabber grabber;
    private Java2DFrameConverter converter;
    private boolean faceCaptured = false;

    @FXML
    public void initialize() {
        registerButton.setDisable(true);
        startCamera();
    }

    private volatile boolean cameraActive = false;
    private Thread cameraThread;

    private void startCamera() {
        cameraActive = true;
        cameraThread = new Thread(() -> {
            try {
                grabber = new OpenCVFrameGrabber(0);
                grabber.start();
                converter = new Java2DFrameConverter();

                while (cameraActive) {
                    Frame frame = grabber.grab();
                    if (frame != null) {
                        BufferedImage bufferedImage = converter.getBufferedImage(frame);
                        Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        Platform.runLater(() -> cameraView.setImage(fxImage));
                    }
                    Thread.sleep(30); // ~30 FPS
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        cameraThread.setDaemon(true);
        cameraThread.start();
    }


    @FXML
    public void handleCaptureFace() {
        try (Connection conn = com.example.studentattendance.database.DBConnection.getConnection()) {
            String studentId = studentIdField.getText();
            if (studentId.isEmpty()) {
                showAlert("Error", "Please enter Student ID first.");
                return;
            }

            // Initialize grabber if not already
            if (grabber == null) {
                grabber = new OpenCVFrameGrabber(0); // Default camera
                grabber.start(); // MUST start before grabbing
            }

            for (int i = 1; i <= 2; i++) {
                Frame frame = grabber.grab();
                if (frame == null) {
                    showAlert("Error", "Failed to grab frame from webcam.");
                    return;
                }

                BufferedImage bufferedImage = converter.getBufferedImage(frame);

                // Show captured image in the UI
                Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                Platform.runLater(() -> cameraView.setImage(fxImage));

                // Convert image to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();

                // Insert into face_images table
                String sql = "INSERT INTO face_images (student_id, image_data) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, studentId);
                stmt.setBytes(2, imageBytes);
                stmt.executeUpdate();

                Thread.sleep(500); // Wait between captures
            }

            faceCaptured = true;
            registerButton.setDisable(false);
            showAlert("Success", "2 face images captured and saved.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to capture and store face images.");
        }
    }


    @FXML
    public void handleRegisterStudent() {
        if (!faceCaptured) {
            showAlert("Error", "Please capture face images first.");
            return;
        }

        try (Connection conn = com.example.studentattendance.database.DBConnection.getConnection()) {
            String sql = "INSERT INTO students (fname, lastname, email, class_name, student_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, fnameField.getText());
            stmt.setString(2, lnameField.getText());
            stmt.setString(3, emailField.getText());
            stmt.setString(4, classField.getText());
            stmt.setString(5, studentIdField.getText());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert("Success", "Student registered successfully!");
                resetForm();
            } else {
                showAlert("Error", "Registration failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", e.getMessage());
        }
    }

    private void resetForm() {
        fnameField.clear();
        lnameField.clear();
        emailField.clear();
        classField.clear();
        studentIdField.clear();
        faceCaptured = false;
        registerButton.setDisable(true);
    }

    public void stopCamera() {
        if (grabber != null) {
            try {
                grabber.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner((Stage) fnameField.getScene().getWindow());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
