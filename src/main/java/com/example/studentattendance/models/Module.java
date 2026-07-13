package com.example.studentattendance.models;

public class Module {

    private int id;
    private String className;
    private String moduleName;
    private String semester;


    public Module() {
    }


    public Module(int id, String className, String moduleName, String semester) {
        this.id = id;
        this.className = className;
        this.moduleName = moduleName;
        this.semester = semester;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getClassName() {
        return className;
    }


    public void setClassName(String className) {
        this.className = className;
    }


    public String getModuleName() {
        return moduleName;
    }


    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }


    public String getSemester() {
        return semester;
    }


    public void setSemester(String semester) {
        this.semester = semester;
    }


    @Override
    public String toString() {
        return moduleName + " (" + className + ")";
    }
}