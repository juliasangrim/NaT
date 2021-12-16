module com.lab.trubitsyna.snake {
    requires javafx.fxml;
    requires javafx.controls;

    requires org.kordamp.bootstrapfx.core;
    requires lombok;
    requires com.google.protobuf;

    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    opens com.lab.trubitsyna.snake.main to javafx.fxml;
    exports com.lab.trubitsyna.snake.main;

    opens com.lab.trubitsyna.snake.view to javafx.fxml;
    exports com.lab.trubitsyna.snake.view;

    opens com.lab.trubitsyna.snake.controller to javafx.fxml;
    exports com.lab.trubitsyna.snake.controller;

    opens com.lab.trubitsyna.snake.model to javafx.fxml;
    exports com.lab.trubitsyna.snake.model;

    opens com.lab.trubitsyna.snake.gameException to javafx.fxml;
    exports com.lab.trubitsyna.snake.gameException;


    opens com.lab.trubitsyna.snake.backend.protoClass to com.google.protobuf;
    exports com.lab.trubitsyna.snake.backend.protoClass;

}