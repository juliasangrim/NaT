package com.lab.trubitsyna.snake.controller;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.backend.handlers.MulticastSender;
import com.lab.trubitsyna.snake.backend.node.INetHandler;
import com.lab.trubitsyna.snake.backend.node.MasterNetNode;
import com.lab.trubitsyna.snake.backend.node.NetNode;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.model.*;
import com.lab.trubitsyna.snake.view.StateSystem;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GameController implements IController{
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    //private final Logger logger = LoggerFactory.getLogger("APP");
    @Setter
    private GameModel model;
    @Setter
    private IListenerView gameView;
    //TODO : testing
    private MasterNetNode node;

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
    private void addKeyListener() {
        board.getScene().setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W, UP -> {
                    model.changeSnakeDirection(node.getPlayers().get(0), SnakesProto.Direction.UP);
                    System.out.println("W");
                }
                case A, LEFT-> {
                    model.changeSnakeDirection(node.getPlayers().get(0), SnakesProto.Direction.LEFT);
                    System.out.println("L");
                }
                case D, RIGHT -> {
                    model.changeSnakeDirection(node.getPlayers().get(0), SnakesProto.Direction.RIGHT);
                    System.out.println("R");
                }
                case S, DOWN -> {
                    model.changeSnakeDirection(node.getPlayers().get(0), SnakesProto.Direction.DOWN);
                    System.out.println("D");
                }
            }
        });
    }

    @FXML
    private void mouseListenerExit() {
        exit.setOnMouseClicked(event -> onExitButtonPressed());
    }

    private void onExitButtonPressed() {
        state = StateSystem.MENU;
        gameView.render(state, null);
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
        gameView.render(StateSystem.LOAD_GAME, null);

    }



    @Override
    public void start() throws Exception {
        CustomGameConfig config = new CustomGameConfig();
        addKeyListener();
        config.initConfig();
        switch (state) {
            case JOIN_GAME -> {
//                node = new NetNode(gameView, serverConfig, SnakesProto.NodeRole.NORMAL, serverAddr, serverPort);
//                loadGame();
//                node.start();
//                threadPool.submit(() -> node.receiver());
//                threadPool.submit(() -> node.sender(null, node.getJoinMessage(config.getLogin())));
            }
            case NEW_GAME -> {
                MyLogger.getLogger().info("Start game on client!");
                model = new GameModel(config, state);
                node = new MasterNetNode(config, SnakesProto.NodeRole.MASTER, model, 0);
                node.start();
                threadPool.submit(() -> {
                    try {
                        gameView.listen(model);
                        model.startGame();
                        model.updateModel();
                        MyLogger.getLogger().info("FINISH THREADPOOL WITH STARTGAME AND UPDATEMODEL");
                    } catch (GameException e) {
                        e.printStackTrace();
                    }
                });
                MyLogger.getLogger().info("Make thread for game!!");
                threadPool.submit(() -> {
                    while (state == StateSystem.NEW_GAME) {

                        MyLogger.getLogger().info("Starting receiver on server!");
                        node.receiver();
                    }
                });
              //  threadPool.submit(() -> node.sender();

            }
        }

    }
}
