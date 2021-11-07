module com.nat.lab {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.kordamp.bootstrapfx.core;
    requires lombok;
    requires java.net.http;
    requires org.apache.commons.lang3;
    requires java.desktop;


    opens com.nat.lab.app to org.apache.commons.lang3, com.fasterxml.jackson.databind;
    exports com.nat.lab.app;
    opens com.nat.lab.controller to javafx.fxml;
    exports com.nat.lab.controller;

    opens com.nat.lab.view to javafx.fxml;
    exports com.nat.lab.view;
}