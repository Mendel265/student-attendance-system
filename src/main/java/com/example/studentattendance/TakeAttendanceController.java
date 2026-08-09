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
    private volatile boolean scanning = false;

    private final Map<Integer, String> studentNameMap = new HashMap<>();
    private final Map<Integer, String> studentIdMap = new HashMap<>(); // <label, student_id>

    private LBPHFaceRecognizer faceRecognizer;
    private final String url = "jdbc:mysql://localhost:3306/students_attendance";
    private final String user = "root";
    private final String password = "";

    private int lecturerId;

    // ---- Session state ----
    private enum SessionState {
        IDLE, CHECK_IN_ACTIVE, CHECK_IN_CLOSED, CHECK_OUT_ACTIVE, COMPLETE
    }

    private SessionState state = SessionState.IDLE;

    // students already marked in the *current* scanning session, to avoid duplicate DB writes
    private final Set<String> sessionCheckIns = new HashSet<>();
    private final Set<String> sessionCheckOuts = new HashSet<>();

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

        checkOutButton.setDisable(true); // can't check out before a check-in round has run
        updateStatus("Idle — select a class to begin");
    }

    private void loadSemesters() {
        semesterComboBox.getItems().addAll("First Semester", "Second Semester");
    }

    private void loadModulesForSemester() {
        String semester = semesterComboBox.getValue();

        if (semester == null || semester.trim().isEmpty()) {
            return;
        }

        moduleComboBox.getItems().clear();

        String sql = """
        SELECT DISTINCT m.id, m.module_name
        FROM lecturer_module lm
        INNER JOIN modules m ON lm.module_id = m.id
        WHERE lm.lecturer_id = ?
          AND LOWER(TRIM(m.semester)) = LOWER(TRIM(?))
        ORDER BY m.module_name
        """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lecturerId);
            stmt.setString(2, semester);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                moduleComboBox.getItems().add(
                        rs.getString("module_name")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadClassesForSemesterAndModule() {
        String semester = semesterComboBox.getValue();
        String module = moduleComboBox.getValue();

        if (semester == null || module == null ||
                semester.trim().isEmpty() || module.trim().isEmpty()) {
            return;
        }

        classComboBox.getItems().clear();

        String sql = """
        SELECT DISTINCT m.class_name
        FROM lecturer_module lm
        INNER JOIN modules m ON lm.module_id = m.id
        WHERE lm.lecturer_id = ?
          AND LOWER(TRIM(m.semester)) = LOWER(TRIM(?))
          AND LOWER(TRIM(m.module_name)) = LOWER(TRIM(?))
        ORDER BY m.class_name
        """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lecturerId);
            stmt.setString(2, semester);
            stmt.setString(3, module);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    classComboBox.getItems().add(
                            rs.getString("class_name")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() ->
                    showAlert(
                            "Database Error",
                            "Unable to load classes for the selected module.\n\n"
                                    + e.getMessage()
                    )
            );
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

    // ================= CHECK-IN =================

    @FXML
    private void onCheckIn() {
        if (state == SessionState.IDLE || state == SessionState.CHECK_IN_CLOSED) {
            startCheckInSession();
        } else if (state == SessionState.CHECK_IN_ACTIVE) {
            stopScanningSession();
        }
    }

    private void startCheckInSession() {
        if (classComboBox.getValue() == null || moduleComboBox.getValue() == null) {
            showAlert("Missing Selection", "Please select semester, module and class first.");
            return;
        }

        state = SessionState.CHECK_IN_ACTIVE;
        sessionCheckIns.clear();
        checkInButton.setText("⏹  Stop Check-In");
        checkOutButton.setDisable(true);
        setComboBoxesDisabled(true);

        startFaceRecognition("pending");
    }

    // ================= CHECK-OUT =================

    @FXML
    private void onCheckOut() {
        if (state == SessionState.CHECK_IN_CLOSED) {
            startCheckOutSession();
        } else if (state == SessionState.CHECK_OUT_ACTIVE) {
            stopScanningSession();
        }
    }

    private void startCheckOutSession() {
        state = SessionState.CHECK_OUT_ACTIVE;
        sessionCheckOuts.clear();
        checkOutButton.setText("⏹  Stop Check-Out");
        checkInButton.setDisable(true);

        startFaceRecognition("present");
    }

    // ================= SHARED STOP HANDLER =================

    private void stopScanningSession() {
        scanning = false; // background thread will exit its loop and release the capture

        if (state == SessionState.CHECK_IN_ACTIVE) {
            state = SessionState.CHECK_IN_CLOSED;
            checkInButton.setText("▶  Start Check-In");
            checkOutButton.setDisable(false);
            setComboBoxesDisabled(false);
            updateStatus("Check-In closed — " + sessionCheckIns.size() + " student(s) marked in");

        } else if (state == SessionState.CHECK_OUT_ACTIVE) {
            state = SessionState.COMPLETE;
            checkOutButton.setText("▶  Start Check-Out");
            checkInButton.setDisable(false);
            setComboBoxesDisabled(false);
            updateStatus("Check-Out closed — " + sessionCheckOuts.size() + " student(s) marked out");
        }
    }

    private void setComboBoxesDisabled(boolean disabled) {
        semesterComboBox.setDisable(disabled);
        moduleComboBox.setDisable(disabled);
        classComboBox.setDisable(disabled);
    }

    // ================= FACE RECOGNITION LOOP =================

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
            // roll the UI back since the session never really started
            Platform.runLater(this::stopScanningSession);
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

                        // only write to the DB once per student per session
                        boolean alreadyMarked = statusToMark.equals("pending")
                                ? sessionCheckIns.contains(studentId)
                                : sessionCheckOuts.contains(studentId);

                        if (!alreadyMarked) {
                            if (statusToMark.equals("pending")) {
                                sessionCheckIns.add(studentId);
                            } else {
                                sessionCheckOuts.add(studentId);
                            }

                            markAttendance(studentId, statusToMark);

                            Platform.runLater(() -> updateStatus(
                                    (statusToMark.equals("pending") ? "Checked in: " : "Checked out: ")
                                            + studentName + " (" + String.format("%.0f", conf) + " conf)"
                            ));
                        }
                        // no break — keep scanning so the next student can walk up
                    }
                }

                Image fxImg = mat2Image(frame);
                Platform.runLater(() -> webcamView.setImage(fxImg));
            }

            capture.release();
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
                        "INSERT INTO attendance (student_id, schedule_id, status, check_in_time)\n" +
                                "VALUES (?, ?, ?, ?)"
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
                    // no matching pending row (e.g. student checked out without a check-in on record)
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

    private void updateStatus(String message) {
        statusLabel.setText("Status: " + message);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}