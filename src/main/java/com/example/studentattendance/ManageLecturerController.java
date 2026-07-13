package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import com.example.studentattendance.models.Lecturer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;

public class ManageLecturerController {


    @FXML
    private TableView<Lecturer> lecturerTable;

    @FXML
    private TableColumn<Lecturer, Integer> idColumn;

    @FXML
    private TableColumn<Lecturer, String> nameColumn;

    @FXML
    private TableColumn<Lecturer, String> emailColumn;


    private Lecturer selectedLecturer;



    @FXML
    public void initialize() {


        setupTable();

        loadLecturers();


        lecturerTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {

                    selectedLecturer = newValue;

                });

    }



    private void setupTable(){


        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );


        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );


        emailColumn.setCellValueFactory(
                new PropertyValueFactory<>("email")
        );


    }




    private void loadLecturers(){


        String sql = """
                SELECT lecture_id,
                       first_name,
                       last_name,
                       email
                FROM lectures
                """;


        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){


            while(rs.next()){


                Lecturer lecturer =
                        new Lecturer(

                                rs.getInt("lecture_id"),

                                rs.getString("first_name")
                                        + " "
                                        + rs.getString("last_name"),

                                rs.getString("email")
                        );


                lecturerTable.getItems()
                        .add(lecturer);


            }


        }catch(Exception e){

            e.printStackTrace();

        }

    }




    @FXML
    private void handleAssignModules(){


        if(selectedLecturer == null){

            showError(
                    "No Lecturer Selected",
                    "Please select a lecturer first."
            );

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


            controller.setLecturer(selectedLecturer);



            Stage stage = new Stage();


            stage.setTitle("Assign Modules");


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

        showInfo(
                "Add Lecturer",
                "Add lecturer feature coming soon."
        );

    }





    @FXML
    private void handleEditLecturer(){


        if(selectedLecturer == null){

            showError(
                    "No Lecturer Selected",
                    "Select a lecturer first."
            );

            return;

        }


        showInfo(
                "Edit Lecturer",
                "Editing: "
                        + selectedLecturer.getName()
        );


    }





    @FXML
    private void handleDeleteLecturer(){


        if(selectedLecturer == null){

            showError(
                    "No Lecturer Selected",
                    "Select a lecturer first."
            );

            return;

        }


        Alert alert =
                new Alert(Alert.AlertType.CONFIRMATION);


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

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();

    }





    private void showInfo(
            String title,
            String message
    ){

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();

    }

}