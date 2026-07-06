package com.example.studentattendance;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ViewAssignedClassesController {

    @FXML
    private Label lecturerLabel;

    @FXML
    private ListView<String> assignedListView;

    private int lecturerId;
    private String lecturerName;

    public void setLecturerId(int lecturerId, String lecturerName) {
        this.lecturerId = lecturerId;
        this.lecturerName = lecturerName;

        lecturerLabel.setText("Viewing Assignments for: " + lecturerName);
        loadAssignedClassesAndModules();
    }

    private void loadAssignedClassesAndModules() {
        ObservableList<String> assignments = FXCollections.observableArrayList();

        String query = "SELECT class_name, semester, module_name FROM lecturer_modules " +
                "WHERE lecturer_id = ? ORDER BY class_name, semester";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, lecturerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String className = rs.getString("class_name");
                String semester = rs.getString("semester");
                String module = rs.getString("module_name");

                assignments.add("Class: " + className + ", Semester: " + semester + ", Module: " + module);
            }

            assignedListView.setItems(assignments);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
