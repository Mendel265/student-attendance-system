package com.example.studentattendance;

public class AttendanceRecord {

    private String fname;
    private String lastname;
    private String module;
    private String className;
    private String status;
    private String time;


    public AttendanceRecord(
            String fname,
            String lastname,
            String module,
            String className,
            String status,
            String time
    ){

        this.fname = fname;
        this.lastname = lastname;
        this.module = module;
        this.className = className;
        this.status = status;
        this.time = time;

    }


    public String getFname(){
        return fname;
    }


    public String getLastname(){
        return lastname;
    }


    public String getModule(){
        return module;
    }


    public String getClassName(){
        return className;
    }


    public String getStatus(){
        return status;
    }


    public String getTime(){
        return time;
    }
}