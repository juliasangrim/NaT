package com.lab.trubitsyna.snake.controller;

import com.lab.trubitsyna.snake.view.IView;
import com.lab.trubitsyna.snake.view.StateSystem;
import com.lab.trubitsyna.snake.view.View;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lombok.Setter;

public class MenuController implements IController {
    @FXML
    private Button playButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button exitButton;
    @Setter
    private IView menuView;


    @FXML
    public void mouseListenerPlay() {
        playButton.setOnMouseClicked(event -> onPlayButtonPressed());
    }

    @FXML
    public void mouseListenerSettings() {
        playButton.setOnMouseClicked(event -> onSettingsButtonPressed());
    }

    @FXML
    public void mouseListenerExit() {
        playButton.setOnMouseClicked(event -> onExitButtonPressed());
    }

    private void onPlayButtonPressed() {
        menuView.render(StateSystem.NEW_GAME);
    }

    private void onSettingsButtonPressed() {
        menuView.render(StateSystem.CONFIG);
    }

    private void onExitButtonPressed() {

    }


    @Override
    public void start() throws Exception {

    }
}
