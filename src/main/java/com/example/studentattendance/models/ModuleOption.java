package com.example.studentattendance.models;

public class ModuleOption {

    private final int id;
    private final String moduleName;
    private final String className;

    public ModuleOption(int id,
                        String moduleName,
                        String className) {

        this.id = id;
        this.moduleName = moduleName;
        this.className = className;
    }

    public int getId() {
        return id;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return moduleName;
    }
}