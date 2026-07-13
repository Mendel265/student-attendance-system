package com.example.studentattendance.models;

public class Module {

    private int id;
    private String className;
    private String moduleName;
    private String semester;


    public Module(
            int id,
            String className,
            String moduleName,
            String semester
    ) {

        this.id = id;
        this.className = className;
        this.moduleName = moduleName;
        this.semester = semester;

    }


    public int getId(){
        return id;
    }


    public String getClassName(){
        return className;
    }


    public String getModuleName(){
        return moduleName;
    }


    public String getSemester(){
        return semester;
    }


    @Override
    public String toString(){

        return moduleName + " (" + className + ")";

    }

}