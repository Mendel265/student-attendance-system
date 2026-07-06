package com.example.studentattendance;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;

public class TrainFaceModelController {

    @FXML private Button trainButton;
    @FXML private Label statusLabel;

    private LBPHFaceRecognizer faceRecognizer;

    private final String url = "jdbc:mysql://localhost:3306/students_attendance";
    private final String user = "root";
    private final String password = "";

    private final String MODEL_FILE = "trained_model.xml";
    private final String LABEL_MAP_FILE = "label_map.csv";

    @FXML
    public void initialize() {
        faceRecognizer = LBPHFaceRecognizer.create();
        statusLabel.setText("Status: Ready to train model");
    }

    @FXML
    public void trainModel() {
        statusLabel.setText("Status: Training started...");
        trainButton.setDisable(true);

        new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT student_id, image_data FROM face_images WHERE image_data IS NOT NULL")) {

                List<Mat> images = new ArrayList<>();
                List<Integer> labels = new ArrayList<>();
                Map<String, Integer> labelMap = new HashMap<>();
                int labelCounter = 0;

                while (rs.next()) {
                    String studentIdStr = rs.getString("student_id");
                    byte[] faceBytes = rs.getBytes("image_data");

                    if (faceBytes != null) {
                        Mat img = imdecode(new Mat(new BytePointer(faceBytes)), IMREAD_GRAYSCALE);
                        if (!img.empty()) {
                            resize(img, img, new Size(200, 200));
                            images.add(img);

                            int label;
                            if (labelMap.containsKey(studentIdStr)) {
                                label = labelMap.get(studentIdStr);
                            } else {
                                label = labelCounter++;
                                labelMap.put(studentIdStr, label);
                            }
                            labels.add(label);
                        }
                    }
                }

                if (images.size() > 0) {
                    MatVector matVector = new MatVector(images.size());
                    Mat labelsMat = new Mat(images.size(), 1, CV_32SC1);

                    for (int i = 0; i < images.size(); i++) {
                        matVector.put(i, images.get(i));
                        labelsMat.ptr(i, 0).putInt(labels.get(i));
                    }

                    faceRecognizer.train(matVector, labelsMat);
                    faceRecognizer.save(MODEL_FILE);
                    saveLabelMap(labelMap);

                    Platform.runLater(() -> statusLabel.setText("Status: Training completed for " + images.size() + " face images."));
                } else {
                    Platform.runLater(() -> statusLabel.setText("Status: No face images found to train."));
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> statusLabel.setText("Status: Error during training - " + e.getMessage()));
            } finally {
                Platform.runLater(() -> trainButton.setDisable(false));
            }
        }).start();
    }

    private void saveLabelMap(Map<String, Integer> labelMap) {
        File file = new File(LABEL_MAP_FILE);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (Map.Entry<String, Integer> entry : labelMap.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> statusLabel.setText("Status: Model saved, but failed to save label map."));
        }
    }
}
