package com.nat.lab.view;


import com.nat.lab.app.AppModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AppView extends Application {
    private AppModel model;

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("My App");
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(false);
        FXMLLoader root =  new FXMLLoader(AppView.class.getResource("sample.fxml"));

        stage.setScene(new Scene(root.load(), Color.SEASHELL));

        stage.show();
    }

}
