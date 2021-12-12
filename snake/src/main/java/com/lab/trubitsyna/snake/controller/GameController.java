package com.lab.trubitsyna.snake.controller;

import com.lab.trubitsyna.snake.backend.handlers.MulticastSender;
import com.lab.trubitsyna.snake.backend.node.INetHandler;
import com.lab.trubitsyna.snake.backend.node.MasterNetNode;
import com.lab.trubitsyna.snake.backend.node.NetNode;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.model.*;
import com.lab.trubitsyna.snake.view.StateSystem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GameController implements IController{
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    @Setter
    private GameModel model;
    @Setter
    private IListenerView gameView;
    private INetHandler node;

    @Setter
    private StateSystem state;

    @Getter @FXML
    private Canvas board;

    @FXML
    private Button exit;

    //for client
    @Setter
    private int serverPort;
    @Setter
    private String serverAddr;
    @Setter
    private SnakesProto.GameConfig serverConfig;

    @FXML
    public void mouseListenerExit() {
        exit.setOnMouseClicked(event -> onExitButtonPressed());
    }

    private void onExitButtonPressed() {
        state = StateSystem.MENU;
        gameView.render(state);
        node.end();
        gameView.noListen(model);
    }

    public void onExitWindowButtonPressed() {
        node.end();
        gameView.noListen(model);
        Platform.exit();
        System.exit(0);
    }

    private void loadGame() {
        gameView.render(StateSystem.LOAD_GAME);

    }

    private void loadError() {
        gameView.render(StateSystem.ERROR_LOAD_GAME);
        node.end();
    }


    @Override
    public void start() throws Exception {
        CustomGameConfig config = new CustomGameConfig();
        config.initConfig();
        switch (state) {
            case JOIN_GAME -> {
                node = new NetNode(gameView, serverConfig, SnakesProto.NodeRole.NORMAL, serverAddr, serverPort);
                loadGame();
                node.start();
                threadPool.submit(() -> node.receiver());
                threadPool.submit(() -> node.sender(null, node.getJoinMessage(config.getLogin())));
            }
            case NEW_GAME -> {
                model = new GameModel(config);
                node = new MasterNetNode(config, SnakesProto.NodeRole.MASTER, model, 0);
                node.start();
                gameView.listen(model);
                model.startGame();
                model.updateModel();

                threadPool.submit(() -> node.receiver());
              //  threadPool.submit(() -> node.sender();

            }
        }

    }
}
