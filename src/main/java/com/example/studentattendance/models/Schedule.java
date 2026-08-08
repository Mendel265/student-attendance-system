package com.example.studentattendance.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Schedule {

    private final StringProperty moduleName;
    private final StringProperty className;
    private final StringProperty room;
    private final StringProperty startTime;
    private final StringProperty endTime;
    private final StringProperty status;

    public Schedule(String moduleName,
                    String className,
                    String room,
                    String startTime,
                    String endTime,
                    String status) {

        this.moduleName = new SimpleStringProperty(moduleName);
        this.className = new SimpleStringProperty(className);
        this.room = new SimpleStringProperty(room);
        this.startTime = new SimpleStringProperty(startTime);
        this.endTime = new SimpleStringProperty(endTime);
        this.status = new SimpleStringProperty(status);
    }

    public String getModuleName() {
        return moduleName.get();
    }

    public StringProperty moduleNameProperty() {
        return moduleName;
    }

    public String getClassName() {
        return className.get();
    }

    public StringProperty classNameProperty() {
        return className;
    }

    public String getRoom() {
        return room.get();
    }

    public StringProperty roomProperty() {
        return room;
    }

    public String getStartTime() {
        return startTime.get();
    }

    public StringProperty startTimeProperty() {
        return startTime;
    }

    public String getEndTime() {
        return endTime.get();
    }

    public StringProperty endTimeProperty() {
        return endTime;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }
}
