package com.example.studentattendance;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.sql.*;

public class ManageLecturerController {

    @FXML
    private ListView<String> lecturerListView;

    @FXML
    public void initialize() {
        loadLecturers();

        lecturerListView.setOnMouseClicked(this::handleLecturerClick);
    }

    private void loadLecturers() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/students_attendance", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username FROM users WHERE role = 'lecturer'")
        ) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("username");
                lecturerListView.getItems().add(id + ": " + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleLecturerClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selected = lecturerListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Extract ID and Name
                String[] parts = selected.split(":");
                int lecturerId = Integer.parseInt(parts[0].trim());
                String lecturerName = parts[1].trim();

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lecturer_actions.fxml"));
                    Parent root = loader.load();

                    LecturerActionsController controller = loader.getController();
                    controller.setLecturerData(lecturerId, lecturerName);

                    Stage stage = new Stage();
                    stage.setTitle("Lecturer Actions");
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
