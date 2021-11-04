package com.nat.lab.controller;

import com.nat.lab.app.AppModel;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public class Controller {
    private AppModel model;

    public Controller() {
        model = new AppModel();
    }


    @FXML
    public TextField searchBar;

    @FXML
    protected void keyListener(KeyEvent event){
        if (event.getCode() == KeyCode.ENTER) {
            model.setPlaceName(searchBar.getText());

        }
    }

    @FXML
    protected void mouseListener() {
        model.setPlaceName(searchBar.getText());
    }

}
