package com.example.studentattendance.models;

public class AttendanceReportView {
    private String studentId;
    private String name;
    private String status;
    private String checkInTime;
    private String checkOutTime;

    public void AttendanceRecord(String studentId, String name, String status, String checkInTime, String checkOutTime) {
        this.studentId = studentId;
        this.name = name;
        this.status = status;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }

    // Getters
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getCheckInTime() { return checkInTime; }
    public String getCheckOutTime() { return checkOutTime; }
}
