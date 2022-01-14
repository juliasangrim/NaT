package com.lab.trubitsyna.snake.controller;

import com.lab.trubitsyna.snake.backend.mcHandlers.MenuHandler;
import com.lab.trubitsyna.snake.backend.mcHandlers.MulticastReciever;
import com.lab.trubitsyna.snake.backend.node.NetNode;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.view.IView;
import com.lab.trubitsyna.snake.view.InfoGame;
import com.lab.trubitsyna.snake.view.StateSystem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import lombok.Setter;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MenuController implements IController {
    private final ExecutorService mcReceiverThreadPool = Executors.newCachedThreadPool();
    @FXML
    private ListView<String> availableGames;
    @FXML
    private Button playButton;
    @FXML
    private Button exitButton;
    @Setter
    private IView menuView;

    private String serverInfo;
    private StateSystem state = StateSystem.MENU;

    @FXML
    public void mouseListenerPlay() {
        playButton.setOnMouseClicked(event -> onPlayButtonPressed());
    }


    @FXML
    public void mouseListenerExit() {

        exitButton.setOnMouseClicked(event -> onExitButtonPressed());
    }

    @FXML
    private void listListener() {
        availableGames.setOnMouseClicked(event-> {
            int index = availableGames.getSelectionModel().getSelectedIndex();
            System.out.println(index);
            if (index >= 0) {
                serverInfo = availableGames.getItems().get(index);
                if (!Objects.equals(serverInfo, "No game found.")) {
                    menuView.sendServerInfoToGameController(serverInfo);
                }
            }
            onGamePressed();
        });
    }

    private void onGamePressed() {
        state = StateSystem.JOIN_GAME;
        mcReceiverThreadPool.shutdown();
        menuView.render(state, null);
    }

    private void onPlayButtonPressed() {
        state = StateSystem.NEW_GAME;
        mcReceiverThreadPool.shutdown();
        menuView.render(state, null);
    }

    public void onExitButtonPressed() {
        state = StateSystem.EXIT;
        mcReceiverThreadPool.shutdown();
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    private void addAvailableServer(MenuHandler handler) {
        availableGames.getItems().clear();
        var listAvailableServers  = handler.getAvailableGames();
        if (listAvailableServers.isEmpty()) {
            availableGames.getItems().add("No game found.");
        } else {
            var info = new InfoGame();
            for (var server : listAvailableServers.keySet()) {
                var msg = listAvailableServers.get(server).getKey();
                info.setAmountPlayers(msg.getPlayers().getPlayersList().size());
                info.setWidthField(msg.getConfig().getWidth());
                info.setHeightField(msg.getConfig().getHeight());
                info.setAddr(server.getValue().getHostAddress());
                info.setPort(server.getKey());
                for (var player : msg.getPlayers().getPlayersList()) {
                    if (player.getRole() == SnakesProto.NodeRole.MASTER) {
                        info.setMasterName(player.getName());
                    }
                }
                availableGames.getItems().add(info.toString());
                availableGames.refresh();
            }
        }
    }

    @Override
    public void start() {
        var handler = new MenuHandler();
        MulticastReciever multicastReciever = new MulticastReciever(handler, NetNode.MULTICAST_PORT, NetNode.MULTICAST_ADDR);
        mcReceiverThreadPool.submit(() ->{
            multicastReciever.init();
            while (state == StateSystem.MENU) {
                multicastReciever.run();
                Platform.runLater(() -> {
                    if (handler.isMapChange()) {
                        addAvailableServer(handler);
                    }
                });
            }
            multicastReciever.close();
        });

    }
}
