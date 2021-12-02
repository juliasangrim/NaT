package com.lab.trubitsyna.snake.view;

import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.model.GameModel;
import com.lab.trubitsyna.snake.model.IListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class GameView implements IView, IListener {

    @FXML
    Canvas canvas;
    @FXML
    Button exitButton;

    Stage stage;

    void sendMessage() {}

    void getMessage() {}



    void drawTiles() {

    }

    @Override
    public void modelChanged(GameModel model) {

    }

    private void listen(GameModel newModel) throws GameException {
        if (newModel != null) {
            newModel.addListener(this);
        }
    }

    private void noListen(GameModel oldModel) {
        if (oldModel != null) {
            oldModel.removeListener(this);
        }
    }

    public void loadScene() {
        FXMLLoader gameLoader =  new FXMLLoader(MenuView.class.getResource("menu.fxml"));
        System.out.println("CLICKED!");
        try {
            stage.setScene(new Scene(gameLoader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        GameView gameView = gameLoader.getController();

        stage.show();
    }
}
