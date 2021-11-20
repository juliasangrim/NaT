package com.lab.trubitsyna.snake.main;

import com.lab.trubitsyna.snake.view.GameViewFX;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main  extends Application{
    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        var view = new GameViewFX();
        view.init();
        primaryStage = view.getStage();
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
