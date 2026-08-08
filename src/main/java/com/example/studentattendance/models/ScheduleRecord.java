package com.example.studentattendance.models;

public class ScheduleRecord {

    private final String lecturer;
    private final String module;
    private final String className;
    private final String day;
    private final String startTime;
    private final String endTime;
    private final String room;

    public ScheduleRecord(
            String lecturer,
            String module,
            String className,
            String day,
            String startTime,
            String endTime,
            String room) {

        this.lecturer = lecturer;
        this.module = module;
        this.className = className;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    public String getLecturer() {
        return lecturer;
    }

    public String getModule() {
        return module;
    }

    public String getClassName() {
        return className;
    }

    public String getDay() {
        return day;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getRoom() {
        return room;
    }
}