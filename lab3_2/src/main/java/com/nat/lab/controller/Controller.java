package com.nat.lab.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nat.lab.app.AppModel;
import com.nat.lab.app.ModelException;
import com.nat.lab.app.State;
import com.nat.lab.app.WeatherInfo;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class Controller {
    @FXML
    public ListView<String> listPlaces;
    @FXML
    public TextArea weather;
    @FXML
    public ImageView icon;
    @FXML
    public ListView<String> listPlacesNear;
    @FXML
    public TextArea info;
    @FXML
    public TextField searchBar;

    private AppModel model;


    public Controller() {
        model = new AppModel();
    }

    //update list of places which the user mean
    public void updateSearchList() {
        listPlaces.getItems().clear();
        listPlacesNear.getItems().clear();
        weather.clear();
        icon.setImage(null);
        info.clear();

        for (com.nat.lab.app.Place curPos : model.getListPlaces()) {
            listPlaces.getItems().add(curPos.toString());
        }
        listPlaces.refresh();
    }

    //update when there is errors like bad response
    public void updateError() {
        //for write in list under search bar
        if (model.getState() == State.SEARCH) {
            listPlaces.getItems().clear();
            listPlacesNear.getItems().clear();
            weather.clear();
            icon.setImage(null);
            info.clear();
            listPlaces.getItems().add("No such place. Please, search something else!");
        } else {
            //for write in list under weather pane
            listPlacesNear.getItems().clear();
            listPlacesNear.getItems().clear();
            weather.clear();
            icon.setImage(null);
            info.clear();
            listPlacesNear.getItems().add("API don't have info about this place!");
        }
    }

    //get image from url address with
    private Image getIcon(WeatherInfo weather) throws IOException {
        URL urlIcon = new URL(String.format("https://openweathermap.org/img/wn/%s@2x.png", weather.getIcon()));
        BufferedImage image = ImageIO.read(urlIcon);
        ImageIO.write(image, "PNG", new File("icon.png"));
        File fileIcon = new File("icon.png");
        String iconLocation = fileIcon.toURI().toString();
        return new Image(iconLocation);
    }


    //update information about weather and list of near places
    public void updateNearPlaceInfo() throws ExecutionException, InterruptedException, IOException {
        weather.clear();
        info.clear();
        listPlacesNear.getItems().clear();

        //get weather info()
        var weather = model.getWeather().get();
        this.weather.appendText(weather.toString());
        //set image of weather
        icon.setImage(getIcon(weather));

        //get near places list
        var nearbyPlaces = model.getListNearbyPlaces().get();
        //we may get empty response, so the list may be empty
        if (nearbyPlaces.isEmpty()) {
            listPlacesNear.getItems().add("API don't have info about this place!");
        } else {
            for (com.nat.lab.app.PlaceInfo curPos : nearbyPlaces) {
                listPlacesNear.getItems().add(curPos.toString());
            }
        }
        listPlacesNear.refresh();
    }

    //update list of places which the user mean
    public void updatePlaceInfo() throws ExecutionException, InterruptedException {
        info.clear();

        var information = model.getInformation().get();

        //
        if (information.getTitle() == null && information.getText() == null) {
            info.appendText("API don't have description about this place. Sorry!!!");
        } else {
            info.appendText(information.toString());
        }
    }

    //all communicate here
    private void handleSearch() throws UnsupportedEncodingException, ExecutionException, JsonProcessingException, InterruptedException, ModelException {
        model.setPlaceName(searchBar.getText());
        model.searchPossiblePlaces();
        updateSearchList();

        //click on places which user mean
        listPlaces.setOnMouseClicked(event -> {
            try {
                model.searchAdditionInfo(listPlaces.getSelectionModel().getSelectedIndex());
                updateNearPlaceInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //click on places nearby
        listPlacesNear.setOnMouseClicked(event -> {
            try {
                model.setInfoAboutPlace(listPlacesNear.getSelectionModel().getSelectedIndex());
                updatePlaceInfo();
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
