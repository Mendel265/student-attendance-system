package com.example.studentattendance;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ManageClassesController {

    @FXML
    private ListView<String> classListView;

    @FXML
    public void initialize() {
        loadClasses();
        classListView.setOnMouseClicked(this::handleClassClick);
    }

    private void loadClasses() {
        Set<String> classSet = new HashSet<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT class_name FROM students")) {

            while (rs.next()) {
                classSet.add(rs.getString("class_name"));
            }
            classListView.getItems().addAll(classSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleClassClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selectedClass = classListView.getSelectionModel().getSelectedItem();
            if (selectedClass != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/semester_selection.fxml"));
                    Parent root = loader.load();

                    // Pass class name to the next controller
                    SemesterSelectionController controller = loader.getController();
                    controller.setSelectedClass(selectedClass);

                    Stage stage = new Stage();
                    stage.setTitle("Select Semester for " + selectedClass);
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}