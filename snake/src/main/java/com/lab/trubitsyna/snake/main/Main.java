package com.lab.trubitsyna.snake.main;

import com.lab.trubitsyna.snake.model.GameModel;
import com.lab.trubitsyna.snake.view.MenuView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Main extends Application{
    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("My App");
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(false);
        FXMLLoader menuLoader =  new FXMLLoader(MenuView.class.getResource("menu.fxml"));
        primaryStage.setScene(new Scene(menuLoader.load()));
        MenuView menuView = menuLoader.getController();
        menuView.setStage(primaryStage);
        primaryStage.show();
    }
}
