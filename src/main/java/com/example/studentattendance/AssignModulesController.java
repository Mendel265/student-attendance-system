package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import com.example.studentattendance.models.Lecturer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.example.studentattendance.models.Module;
import javafx.collections.*;
import java.sql.*;


public class AssignModulesController {


    @FXML
    private Label lecturerLabel;


    @FXML
    private ListView<Module> moduleListView;


    private Lecturer lecturer;



    public void setLecturer(Lecturer lecturer){

        this.lecturer = lecturer;

        lecturerLabel.setText(
                "Assign Modules To: "
                        + lecturer.getName()
        );

        loadModules();

    }



    private void loadModules(){

        ObservableList<Module> modules =
                FXCollections.observableArrayList();


        String sql =
                "SELECT id, module_name, class_name FROM modules";


        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){


            while(rs.next()){

                Module module = new Module(
                        rs.getInt("id"),
                        rs.getString("class_name"),
                        rs.getString("module_name"),
                        rs.getString("semester")
                );

            }


            moduleListView.setItems(modules);


        }catch(Exception e){

            e.printStackTrace();

        }


    }



    @FXML
    private void handleSave(){


        Module selected =
                moduleListView.getSelectionModel()
                        .getSelectedItem();



        if(selected == null){

            Alert alert =
                    new Alert(Alert.AlertType.WARNING);

            alert.setContentText(
                    "Select a module first"
            );

            alert.showAndWait();

            return;
        }



        String sql =
                """
                INSERT INTO lecturer_module
                (lecturer_id,module_id)
                VALUES (?,?)
                """;


        try(Connection conn =
                    DBConnection.getConnection();

            PreparedStatement ps =
                    conn.prepareStatement(sql)){


            ps.setInt(
                    1,
                    lecturer.getId()
            );


            ps.setInt(
                    2,
                    selected.getId()
            );


            ps.executeUpdate();



            Alert alert =
                    new Alert(Alert.AlertType.INFORMATION);

            alert.setContentText(
                    "Module assigned successfully"
            );

            alert.showAndWait();


        }catch(Exception e){

            e.printStackTrace();

        }


    }

}