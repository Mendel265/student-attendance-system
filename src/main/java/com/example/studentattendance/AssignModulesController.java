package com.example.studentattendance;

import com.example.studentattendance.database.DBConnection;
import com.example.studentattendance.models.Lecturer;
import com.example.studentattendance.models.Module;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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


        String sql = "SELECT * FROM modules";


        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {


            while(rs.next()) {

                Module module = new Module();

                module.setId(rs.getInt("id"));
                module.setClassName(rs.getString("class_name"));
                module.setModuleName(rs.getString("module_name"));
                module.setSemester(rs.getString("semester"));

                modules.add(module);


                System.out.println(
                        rs.getInt("id") + " " +
                                rs.getString("module_name")
                );

            }


            moduleListView.setItems(modules);


        }
        catch(Exception e){
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


        try(Connection conn = DBConnection.getConnection()){


            // CHECK IF MODULE IS ALREADY ASSIGNED
            String checkSql =
                    "SELECT COUNT(*) FROM lecturer_module WHERE lecturer_id=? AND module_id=?";


            PreparedStatement checkStmt =
                    conn.prepareStatement(checkSql);


            checkStmt.setInt(
                    1,
                    lecturer.getId()
            );


            checkStmt.setInt(
                    2,
                    selected.getId()
            );


            ResultSet rs =
                    checkStmt.executeQuery();


            if(rs.next() && rs.getInt(1) > 0){

                Alert alert =
                        new Alert(Alert.AlertType.WARNING);

                alert.setContentText(
                        "This module is already assigned to this lecturer"
                );

                alert.showAndWait();

                return;
            }



            // INSERT ONLY IF IT DOES NOT EXIST
            String sql =
                    """
                    INSERT INTO lecturer_module
                    (lecturer_id,module_id)
                    VALUES (?,?)
                    """;


            PreparedStatement ps =
                    conn.prepareStatement(sql);


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