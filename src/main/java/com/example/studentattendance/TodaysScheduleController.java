package com.example.studentattendance;


import com.example.studentattendance.models.Schedule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalTime;


public class TodaysScheduleController {


    @FXML
    private TableView<Schedule> scheduleTable;

    @FXML
    private TableColumn<Schedule, String> moduleColumn;

    @FXML
    private TableColumn<Schedule, String> classColumn;

    @FXML
    private TableColumn<Schedule, String> roomColumn;

    @FXML
    private TableColumn<Schedule, String> startTimeColumn;

    @FXML
    private TableColumn<Schedule, String> endTimeColumn;

    @FXML
    private TableColumn<Schedule, String> statusColumn;
    @FXML
    private Label dateLabel;



    private ObservableList<Schedule> scheduleList =
            FXCollections.observableArrayList();



    @FXML
    public void initialize(){

        dateLabel.setText(
                "Today: " + LocalDate.now()
        );


        setupColumns();

        loadSchedule();

    }



    private void setupColumns() {

        moduleColumn.setCellValueFactory(
                data -> data.getValue().moduleNameProperty()
        );

        classColumn.setCellValueFactory(
                data -> data.getValue().classNameProperty()
        );

        roomColumn.setCellValueFactory(
                data -> data.getValue().roomProperty()
        );

        startTimeColumn.setCellValueFactory(
                data -> data.getValue().startTimeProperty()
        );

        endTimeColumn.setCellValueFactory(
                data -> data.getValue().endTimeProperty()
        );

        statusColumn.setCellValueFactory(
                data -> data.getValue().statusProperty()
        );
    }



    @FXML
    public void loadSchedule(){


        scheduleList.clear();


        /*
          Later this will come from MySQL.
          For testing we add sample data.
        */


        scheduleList.add(
                new Schedule(
                        "CS301",
                        "Database Systems",
                        "Lab 1",
                        "08:00",
                        "10:00",
                        "Active"
                )
        );


        scheduleList.add(
                new Schedule(
                        "CS302",
                        "Software Engineering",
                        "Room 5",
                        "13:00",
                        "15:00",
                        "Upcoming"
                )
        );


        scheduleTable.setItems(scheduleList);


    }




    @FXML
    public void startAttendance(){


        Schedule selected =
                scheduleTable.getSelectionModel()
                        .getSelectedItem();


        if(selected == null){

            Alert alert =
                    new Alert(Alert.AlertType.WARNING);

            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Please select a class first"
            );

            alert.show();

            return;
        }



        System.out.println(
                "Starting attendance for "
                        + selected.getModuleName()
        );

    }


}