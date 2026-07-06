package com.example.studentattendance;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacpp.*;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imencode;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;

public class TakeAttendanceController {

    @FXML private Label statusLabel;
    @FXML private ImageView webcamView;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private ComboBox<String> moduleComboBox;
    @FXML private ComboBox<String> classComboBox;
    @FXML private Button checkInButton;
    @FXML private Button checkOutButton;

    private VideoCapture capture;
    private CascadeClassifier faceDetector;
    private boolean scanning = false;

    private final Map<Integer, String> studentNameMap = new HashMap<>();
    private final Map<Integer, String> studentIdMap = new HashMap<>(); // <label, student_id>

    private LBPHFaceRecognizer faceRecognizer;
    private final String url = "jdbc:mysql://localhost:3306/students_attendance";
    private final String user = "root";
    private final String password = "";

    private int lecturerId;

    public void setLecturerId(int id) {
        this.lecturerId = id;
        System.out.println("Lecturer ID received in TakeAttendanceController: " + id);
    }

    public void initialize() {
        faceDetector = new CascadeClassifier("src/main/resources/haarcascades/haarcascade_frontalface_default.xml");
        faceRecognizer = LBPHFaceRecognizer.create();

        loadSemesters();
        trainRecognizer();

        semesterComboBox.setOnAction(e -> {
            moduleComboBox.getItems().clear();
            classComboBox.getItems().clear();
            loadModulesForSemester();
        });

        moduleComboBox.setOnAction(e -> {
            classComboBox.getItems().clear();
            loadClassesForSemesterAndModule();
        });
    }

    private void loadSemesters() {
        semesterComboBox.getItems().addAll("First Semester", "Second Semester");
    }

    private void loadModulesForSemester() {
        String semester = semesterComboBox.getValue();
        if (semester == null) return;

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT DISTINCT module_name FROM lecturer_modules WHERE lecturer_id = ? AND LOWER(semester) = ?"
            );
            stmt.setInt(1, lecturerId);
            stmt.setString(2, semester.trim().toLowerCase());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                moduleComboBox.getItems().add(rs.getString("module_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadClassesForSemesterAndModule() {
        String semester = semesterComboBox.getValue();
        String module = moduleComboBox.getValue();
        if (semester == null || module == null) return;

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT DISTINCT class_name FROM lecturer_modules WHERE lecturer_id = ? AND semester = ? AND module_name = ?"
            );
            stmt.setInt(1, lecturerId);
            stmt.setString(2, semester.trim());
            stmt.setString(3, module.trim());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                classComboBox.getItems().add(rs.getString("class_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void trainRecognizer() {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT student_id, image_data FROM face_images")) {

            List<Mat> images = new ArrayList<>();
            List<Integer> labels = new ArrayList<>();
            Map<String, Integer> labelMap = new HashMap<>();
            int[] labelCounter = {0};

            while (rs.next()) {
                String studentId = rs.getString("student_id");
                byte[] data = rs.getBytes("image_data");

                if (data != null) {
                    Mat img = imdecode(new Mat(new BytePointer(data)), IMREAD_GRAYSCALE);
                    if (!img.empty()) {
                        resize(img, img, new Size(200, 200));
                        images.add(img);

                        int label = labelMap.computeIfAbsent(studentId, k -> labelCounter[0]++);
                        labels.add(label);
                        studentIdMap.put(label, studentId);

                        try (PreparedStatement ps = conn.prepareStatement("SELECT fname FROM students WHERE student_id = ?")) {
                            ps.setString(1, studentId);
                            try (ResultSet nameRs = ps.executeQuery()) {
                                if (nameRs.next()) {
                                    studentNameMap.put(label, nameRs.getString("fname"));
                                }
                            }
                        }
                    }
                }
            }

            if (!images.isEmpty()) {
                MatVector matVector = new MatVector(images.size());
                Mat labelMat = new Mat(images.size(), 1, opencv_core.CV_32SC1);
                for (int i = 0; i < images.size(); i++) {
                    matVector.put(i, images.get(i));
                    labelMat.ptr(i, 0).putInt(labels.get(i));
                }
                faceRecognizer.train(matVector, labelMat);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startFaceRecognition(String statusToMark) {
        if (scanning) return;
        scanning = true;
        Platform.runLater(() -> statusLabel.setText("Status: Scanning..."));

        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            Platform.runLater(() -> {
                showAlert("Camera Error", "Unable to open the camera.");
                statusLabel.setText("Status: Camera not available.");
            });
            scanning = false;
            return;
        }

        Thread thread = new Thread(() -> {
            Mat frame = new Mat();
            while (scanning && capture.read(frame)) {
                Mat gray = new Mat();
                cvtColor(frame, gray, COLOR_BGR2GRAY);
                RectVector faces = new RectVector();
                faceDetector.detectMultiScale(gray, faces);

                for (int i = 0; i < faces.size(); i++) {
                    Rect face = faces.get(i);
                    rectangle(frame, face, new Scalar(0, 255, 0, 1));
                    Mat faceMat = new Mat(gray, face);
                    resize(faceMat, faceMat, new Size(200, 200));

                    IntPointer label = new IntPointer(1);
                    DoublePointer confidence = new DoublePointer(1);
                    faceRecognizer.predict(faceMat, label, confidence);

                    int predictedLabel = label.get(0);
                    double conf = confidence.get(0);

                    if (studentIdMap.containsKey(predictedLabel) && conf < 80) {
                        String studentId = studentIdMap.get(predictedLabel);
                        String studentName = studentNameMap.getOrDefault(predictedLabel, "Unknown");

                        Platform.runLater(() -> {
                            showAlert("Match Found", "Student: " + studentName + "\nConfidence: " + String.format("%.2f", conf));
                            statusLabel.setText("Status: Attendance marked for " + studentName);
                        });

                        markAttendance(studentId, statusToMark);
                        scanning = false;
                        break;
                    } else {
                        Platform.runLater(() -> statusLabel.setText("Status: Unknown face or low confidence (" + String.format("%.2f", conf) + ")"));
                    }
                }

                Image fxImg = mat2Image(frame);
                Platform.runLater(() -> webcamView.setImage(fxImg));
            }

            capture.release();
            if (scanning) {
                Platform.runLater(() -> statusLabel.setText("Status: Scan ended without match."));
            }
            scanning = false;
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void markAttendance(String studentId, String status) {
        String className = classComboBox.getValue();
        String module = moduleComboBox.getValue();
        if (className == null || module == null) {
            Platform.runLater(() -> showAlert("Missing Selection", "Please select both class and module."));
            return;
        }

        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            if (status.equals("pending")) {
                PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO attendance (student_id, class_name, attendance_date, check_in_time, status, module, lecturer_id) VALUES (?, ?, ?, ?, ?, ?, ?)"
                );
                insert.setString(1, studentId);
                insert.setString(2, className);
                insert.setDate(3, Date.valueOf(date));
                insert.setTime(4, Time.valueOf(time));
                insert.setString(5, "pending");
                insert.setString(6, module);
                insert.setInt(7, lecturerId);
                insert.executeUpdate();

            } else if (status.equals("present")) {
                PreparedStatement update = conn.prepareStatement(
                        "UPDATE attendance SET check_out_time = ?, status = ? WHERE student_id = ? AND class_name = ? AND attendance_date = ? AND status = 'pending'"
                );
                update.setTime(1, Time.valueOf(time));
                update.setString(2, "present");
                update.setString(3, studentId);
                update.setString(4, className);
                update.setDate(5, Date.valueOf(date));

                if (update.executeUpdate() == 0) {
                    PreparedStatement insert = conn.prepareStatement(
                            "INSERT INTO attendance (student_id, class_name, attendance_date, check_out_time, status, module, lecturer_id) VALUES (?, ?, ?, ?, ?, ?, ?)"
                    );
                    insert.setString(1, studentId);
                    insert.setString(2, className);
                    insert.setDate(3, Date.valueOf(date));
                    insert.setTime(4, Time.valueOf(time));
                    insert.setString(5, "present");
                    insert.setString(6, module);
                    insert.setInt(7, lecturerId);
                    insert.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Image mat2Image(Mat mat) {
        BytePointer bytePointer = new BytePointer();
        imencode(".png", mat, bytePointer);
        byte[] byteArray = new byte[(int) bytePointer.limit()];
        bytePointer.get(byteArray);
        return new Image(new ByteArrayInputStream(byteArray));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onCheckIn() {
        startFaceRecognition("pending");
    }

    @FXML
    private void onCheckOut() {
        startFaceRecognition("present");
    }
}
