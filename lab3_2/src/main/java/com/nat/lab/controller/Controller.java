package com.nat.lab.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nat.lab.app.AppModel;
import com.nat.lab.app.ModelException;
import com.nat.lab.app.State;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;


public class Controller {
    @FXML
    public ListView<String> list;
    @FXML
    public Label labelSearch;
    private AppModel model;

    public Controller() {
        model = new AppModel();
    }


    @FXML
    public TextField searchBar;

    public void updateSearchList() {
        list.getItems().clear();
        for (com.nat.lab.app.Place curPos : model.getListPlaces()) {
            list.getItems().add(curPos.toString());
        }
        list.refresh();
    }

    public void updateError() {
        if (model.getState() == State.SEARCH) {
            list.getItems().clear();
            list.getItems().add("No such place. Please, search something else!");
        } else {
            System.out.println("TODO");
        }
    }

    public void updateInfoPane() {

    }

    private void handleSearch() throws UnsupportedEncodingException, ExecutionException, JsonProcessingException, InterruptedException, ModelException {
        model.setPlaceName(new String(searchBar.getText().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        model.search();
        updateSearchList();

        list.setOnMouseClicked(event -> {
            System.out.println("clicked on " + list.getSelectionModel().getSelectedIndex());
            try {
                model.searchInfo(list.getSelectionModel().getSelectedIndex());
                updateInfoPane();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    @FXML
    protected void keyListener(KeyEvent event)  {
        if (event.getCode() == KeyCode.ENTER) {
            try {
                handleSearch();
            } catch (Exception e) {
                e.printStackTrace();
                updateError();
            }
        }
    }

    @FXML
    protected void mouseListener()  {
        try {
            handleSearch();
        } catch (Exception e) {
            e.printStackTrace();
            updateError();
        }
    }

}
