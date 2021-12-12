package com.lab.trubitsyna.snake.main;

import com.lab.trubitsyna.snake.controller.MenuController;
import com.lab.trubitsyna.snake.view.View;
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

        primaryStage.fullScreenProperty();
        FXMLLoader menuLoader =  new FXMLLoader(View.class.getResource("menu.fxml"));
        primaryStage.setScene(new Scene(menuLoader.load()));
        MenuController menuController = menuLoader.getController();
        View menu = new View(primaryStage);
        menuController.setMenuView(menu);
        menuController.start();
        primaryStage.setOnCloseRequest(t -> menuController.onExitButtonPressed());
        primaryStage.show();
    }


}
