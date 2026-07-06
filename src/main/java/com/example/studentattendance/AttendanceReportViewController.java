package com.example.studentattendance;

import com.example.studentattendance.models.AttendanceRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AttendanceReportViewController {
    @FXML
    private TableView<AttendanceRecord> attendanceTable;

    @FXML
    private TableColumn<AttendanceRecord, String> studentIdCol;

    @FXML
    private TableColumn<AttendanceRecord, String> nameCol;

    @FXML
    private TableColumn<AttendanceRecord, String> statusCol;

    @FXML
    private TableColumn<AttendanceRecord, String> checkInCol;

    @FXML
    private TableColumn<AttendanceRecord, String> checkOutCol;

    public void initialize() {
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        checkOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));
    }

    public void setAttendanceData(ObservableList<AttendanceRecord> data) {
        attendanceTable.setItems(data);
    }
}
