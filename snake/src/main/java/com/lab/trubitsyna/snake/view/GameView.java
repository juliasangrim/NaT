package com.lab.trubitsyna.snake.view;

import com.lab.trubitsyna.snake.controller.MenuController;
import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.main.Main;
import com.lab.trubitsyna.snake.model.GameModel;
import com.lab.trubitsyna.snake.model.IListener;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;


public class GameView implements IView, IListener {
    public final static int WIDTH_WINDOW = 1400;
    public final static int HEIGHT_WINDOW = 900;
    public final static int SIZE_BOARD = 800;


    public final static int ROWS = 20;
    public final static int COLUMNS = 20;

    public final static int TILE_SIZE = SIZE_BOARD / ROWS;
    public Button playButton;
    public Button settingsButton;
    public Button exitButton;
    GraphicsContext gc;
    GameModel model;

    @Setter
    Stage stage = null;
    @Setter
    MenuController controller = new MenuController();


    @Override
    public void init() {
    }




    @Override
    public void render() {
        drawTiles(gc);
    }

    public void drawTiles(GraphicsContext gc) {
        for (int i = 0; i < ROWS; ++i) {
            for (int  j = 0; j < COLUMNS; ++j) {
                if ((i + j) % 2  == 0) {
                    gc.setFill(Color.CYAN);

                } else {
                    gc.setFill(Color.HOTPINK);
                }
                gc.fillRect(i *  TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    @Override
    public void modelChanged(GameModel model) {

    }

    private void listen() throws GameException {
        if (model != null) {
            model.listen(this);
        }
    }

    private void noListen() {
        if (model != null) {
            model.noListen(this);
        }
    }
}
