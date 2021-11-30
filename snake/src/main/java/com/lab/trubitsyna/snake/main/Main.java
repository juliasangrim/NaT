package com.lab.trubitsyna.snake.main;

import com.lab.trubitsyna.snake.controller.MenuController;
import com.lab.trubitsyna.snake.model.GameModel;
import com.lab.trubitsyna.snake.view.GameView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Main extends Application{
    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        GameModel model = new GameModel();
        MenuController controller = new MenuController();
        primaryStage.setTitle("My App");
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(false);
        FXMLLoader root =  new FXMLLoader(GameView.class.getResource("menu.fxml"));
        primaryStage.setScene(new Scene(root.load(), Color.SEASHELL));

        GameView gameView = root.getController();
        gameView.setStage(primaryStage);
        gameView.setController(controller);
        primaryStage.show();
    }
}
