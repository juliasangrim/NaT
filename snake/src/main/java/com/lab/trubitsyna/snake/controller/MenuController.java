package com.lab.trubitsyna.snake.controller;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.backend.handlers.MulticastReciever;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MenuController implements IController {
    //private final Logger logger = LoggerFactory.getLogger("APP");
    @FXML
    private ListView<String> availableGames;
    @FXML
    private Button playButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button exitButton;
    @Setter
    private IView menuView;

    private String serverInfo;
    private final ExecutorService mcReceiverThreadPool = Executors.newCachedThreadPool();
    private StateSystem state = StateSystem.MENU;

    @FXML
    public void mouseListenerPlay() {
        playButton.setOnMouseClicked(event -> onPlayButtonPressed());
    }

    @FXML
    public void mouseListenerSettings() {
        settingsButton.setOnMouseClicked(event -> onSettingsButtonPressed());
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
        MyLogger.getLogger().info("Thread pool for McReceiver shutdown!");
        menuView.render(state, null);
    }

    private void onPlayButtonPressed() {
        state = StateSystem.NEW_GAME;
        mcReceiverThreadPool.shutdown();
        MyLogger.getLogger().info("Thread pool for McReceiver shutdown!");
        menuView.render(state, null);
    }

    private void onSettingsButtonPressed() {
        state = StateSystem.CONFIG;
        mcReceiverThreadPool.shutdown();
        MyLogger.getLogger().info("Thread pool for McReceiver shutdown!");
        menuView.render(state, null);
    }
    public void onExitButtonPressed() {
        state = StateSystem.EXIT;
        mcReceiverThreadPool.shutdown();
        MyLogger.getLogger().info("Thread pool for McReceiver shutdown!");
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    private void addAvailableServer(NetNode client) {
       // logger.info("Update list of available games...");
        availableGames.getItems().clear();
        var listAvailableServers  = client.getAvailableGames();
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
                //TODO with render
                availableGames.refresh();
            }
        }
      //  logger.info("Update successfully!");
    }

    @Override
    public void start() {
        var client = new NetNode();
        MulticastReciever multicastReciever = new MulticastReciever(client, NetNode.MULTICAST_PORT, NetNode.MULTICAST_ADDR);
        mcReceiverThreadPool.submit(() ->{
            multicastReciever.init();
            while (state == StateSystem.MENU) {
                multicastReciever.run();
                Platform.runLater(() -> {
                    if (client.isMapChange()) {
                        addAvailableServer(client);
                    }
                });
            }
            multicastReciever.close();
        });

    }
}
