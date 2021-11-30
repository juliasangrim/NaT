package com.lab.trubitsyna.snake.view;

import com.lab.trubitsyna.snake.model.GameModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
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


    public void setHandlerPlayButton() {
        playButton.setOnMouseClicked(event -> loadApp());
    }


    @Override
    public void init() {
        setHandlerPlayButton();
    }

    @Override
    public void render() {
        var root = stage.getScene().getRoot();
    }

    private void loadApp() {
        FXMLLoader appLoader =  new FXMLLoader(MenuView.class.getResource("app.fxml"));
        System.out.println("CLICKED!");
        try {
            stage.setScene(new Scene(appLoader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        stage.show();
    }

//    @Override
//    public void modelChanged(GameModel model) {
//
//    }

//    private void listen() throws GameException {
//        if (model != null) {
//            model.listen(this);
//        }
//    }
//
//    private void noListen() {
//        if (model != null) {
//            model.noListen(this);
//        }
//    }
}
