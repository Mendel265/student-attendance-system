package com.example.studentattendance.models;


public class Schedule {


    private String courseCode;
    private String courseName;
    private String room;
    private String startTime;
    private String endTime;
    private String status;



    public Schedule(String courseCode,
                    String courseName,
                    String room,
                    String startTime,
                    String endTime,
                    String status){

        this.courseCode = courseCode;
        this.courseName = courseName;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;

    }



    public String getCourseCode(){
        return courseCode;
    }


    public String getCourseName(){
        return courseName;
    }


    public String getRoom(){
        return room;
    }


    public String getStartTime(){
        return startTime;
    }


    public String getEndTime(){
        return endTime;
    }


    public String getStatus(){
        return status;
    }


    public javafx.beans.property.StringProperty courseCodeProperty(){
        return new javafx.beans.property.SimpleStringProperty(courseCode);
    }


    public javafx.beans.property.StringProperty courseNameProperty(){
        return new javafx.beans.property.SimpleStringProperty(courseName);
    }


    public javafx.beans.property.StringProperty roomProperty(){
        return new javafx.beans.property.SimpleStringProperty(room);
    }


    public javafx.beans.property.StringProperty startTimeProperty(){
        return new javafx.beans.property.SimpleStringProperty(startTime);
    }


    public javafx.beans.property.StringProperty endTimeProperty(){
        return new javafx.beans.property.SimpleStringProperty(endTime);
    }


    public javafx.beans.property.StringProperty statusProperty(){
        return new javafx.beans.property.SimpleStringProperty(status);
    }

}