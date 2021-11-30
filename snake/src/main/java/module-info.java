module com.lab.trubitsyna.snake {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires lombok;

    requires com.google.protobuf;



    opens com.lab.trubitsyna.snake.main to javafx.fxml;
    exports com.lab.trubitsyna.snake.main;

    opens com.lab.trubitsyna.snake.view to javafx.fxml;
    exports com.lab.trubitsyna.snake.view;

    opens com.lab.trubitsyna.snake.controller to javafx.fxml;
    exports com.lab.trubitsyna.snake.controller;
}