package com.lab.trubitsyna.snake.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;


public class MenuView implements IView{

    @FXML
    public Button playButton;
    @FXML
    public Button settingsButton;
    @FXML
    public Button exitButton;

    @Setter
    Stage stage;

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
        FXMLLoader gameLoader =  new FXMLLoader(MenuView.class.getResource("app.fxml"));
        try {
            stage.setScene(new Scene(gameLoader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        GameView gameView = gameLoader.getController();

        stage.show();
    }

    private void onSettingsButtonPressed() {
        FXMLLoader gameLoader =  new FXMLLoader(MenuView.class.getResource("app.fxml"));
        try {
            stage.setScene(new Scene(gameLoader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.show();
    }

    private void onExitButtonPressed() {
        stage.close();

    }


}
