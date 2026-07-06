package com.example.studentattendance;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.sql.*;

public class ViewModulesController {

    @FXML
    private Label classLabel;
    @FXML
    private ListView<String> modulesListView;

    private String className;
    private String semester;

    public void setClassAndSemester(String className, String semester) {
        this.className = className;
        this.semester = semester;
        classLabel.setText("Class: " + className + " | " + semester);
        loadModules();
    }


    public void setClassName(String className) {
        this.className = className;
        classLabel.setText("Modules for: " + className);
        loadModules();
    }
    @FXML
    private void handleViewModules() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view_modules.fxml"));
            Parent root = loader.load();
            ViewModulesController controller = loader.getController();
            controller.setClassAndSemester(className, semester);

            Stage stage = new Stage();
            stage.setTitle("View Modules");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void loadModules() {
        modulesListView.getItems().clear();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "");
             PreparedStatement stmt = conn.prepareStatement("SELECT module_name FROM modules WHERE class_name = ?")) {

            stmt.setString(1, className);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                modulesListView.getItems().add(rs.getString("module_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


