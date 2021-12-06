package com.lab.trubitsyna.snake.controller;

import com.lab.trubitsyna.snake.model.*;
import com.lab.trubitsyna.snake.view.StateSystem;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

public class GameController implements IController{
    @Setter
    private IModel model;
    @Setter
    private IListenerView gameView;

    @Getter
    @FXML
    private Canvas board;

    @FXML
    private Button exitButton;

    @FXML
    private void mouseListenerExit() {
        exitButton.setOnMouseClicked(event -> onExitButtonPressed());
    }

    private void onExitButtonPressed() {
        gameView.render(StateSystem.MENU);
        gameView.noListen(model);
    }

    @FXML
    public void initialize() {
        CustomGameConfig config = new CustomGameConfig();
        model = new GameModel(config);
    }

    @Override
    public void start() throws Exception {
        gameView.listen(model);
        model.startGame();
        model.updateModel();
    }
}
