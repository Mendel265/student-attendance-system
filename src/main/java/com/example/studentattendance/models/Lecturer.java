package com.example.studentattendance.models;

import javafx.beans.property.*;

public class Lecturer {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty email;

    public Lecturer(int id, String name, String email) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }
}
