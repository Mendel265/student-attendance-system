package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import com.example.studentattendance.models.Lecturer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ManageLecturerController {

    @FXML
    private ListView<Lecturer> lecturerListView;


    private Lecturer selectedLecturer;


    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setupLecturerList();

        loadLecturers();


        lecturerListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {

                    selectedLecturer = newValue;

                });

    }


    private void setupLecturerList(){

        lecturerListView.setCellFactory(param -> new ListCell<>() {

            @Override
            protected void updateItem(
                    Lecturer lecturer,
                    boolean empty
            ){

                super.updateItem(lecturer, empty);


                if(empty || lecturer == null){

                    setText(null);

                }else{

                    setText(
                            lecturer.getName()
                                    + " - "
                                    + lecturer.getEmail()
                    );

                }

            }

        });

    }



    private void loadLecturers(){

        String sql = """
                SELECT 
                    id,
                    username,
                    email
                FROM users
                WHERE role = 'lecturer'
                """;


        try(
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ){

            while(rs.next()){


                Lecturer lecturer = new Lecturer(

                        rs.getInt("id"),

                        rs.getString("username"),

                        rs.getString("email")

                );


                lecturerListView
                        .getItems()
                        .add(lecturer);

            }


        }catch(SQLException e){

            e.printStackTrace();

            showError(
                    "Database Error",
                    e.getMessage()
            );

        }

    }




    @FXML
    private void handleAssignModules(){


        if(selectedLecturer == null){


            Alert alert = new Alert(
                    Alert.AlertType.WARNING
            );


            alert.setTitle(
                    "No Lecturer Selected"
            );


            alert.setHeaderText(null);


            alert.setContentText(
                    "Please select a lecturer before assigning modules."
            );


            alert.showAndWait();


            return;

        }



        try{


            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/fxml/assign_modules.fxml"
                                    )
                    );


            Parent root = loader.load();



            AssignModulesController controller =
                    loader.getController();



            controller.setLecturer(
                    selectedLecturer
            );



            Stage stage = new Stage();


            stage.setTitle(
                    "Assign Modules"
            );


            stage.setScene(
                    new Scene(root)
            );


            stage.show();



        }catch(Exception e){

            e.printStackTrace();

        }

    }

    @FXML
    private void handleAddLecturer(){

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Add Lecturer");
        alert.setHeaderText(null);
        alert.setContentText(
                "Add lecturer feature coming soon."
        );

        alert.showAndWait();

    }



    @FXML
    private void handleEditLecturer(){

        if(selectedLecturer == null){

            showError(
                    "No Lecturer Selected",
                    "Please select a lecturer to edit."
            );

            return;
        }


        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Edit Lecturer");

        alert.setHeaderText(null);

        alert.setContentText(
                "Editing: "
                        + selectedLecturer.getName()
        );

        alert.showAndWait();

    }





    @FXML
    private void handleDeleteLecturer(){

        if(selectedLecturer == null){

            showError(
                    "No Lecturer Selected",
                    "Please select a lecturer to delete."
            );

            return;

        }


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Delete Lecturer");

        alert.setHeaderText(null);

        alert.setContentText(
                "Delete "
                        + selectedLecturer.getName()
                        + "?"
        );


        alert.showAndWait();

    }



    private void showError(
            String title,
            String message
    ){

        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );


        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();

    }

}