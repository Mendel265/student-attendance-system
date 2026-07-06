module com.example.studentattendance {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.bytedeco.javacv;
    requires java.desktop;
    requires org.bytedeco.opencv;
    requires javafx.swing;
    requires jdk.jfr;


    opens com.example.studentattendance to javafx.fxml;
    exports com.example.studentattendance;
    exports com.example.studentattendance.models;
    opens com.example.studentattendance.models to javafx.fxml;
}