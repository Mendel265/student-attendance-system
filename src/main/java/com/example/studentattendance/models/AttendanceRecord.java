package com.example.studentattendance.models;

public class AttendanceRecord {
    private String studentId;
    private String name;
    private String status;

    public AttendanceRecord(String studentId, String name, String status) {
        this.studentId = studentId;
        this.name = name;
        this.status = status;
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getStatus() { return status; }
}
