package com.lab.trubitsyna.snake.view;

import com.lab.trubitsyna.snake.gameException.GameException;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;


public class GameViewFX implements IGameView {
    public final static int WIDTH_WINDOW = 1400;
    public final static int HEIGHT_WINDOW = 900;
    public final static int SIZE_BOARD = 800;


    public final static int ROWS = 20;
    public final static int COLUMNS = 20;

    public final static int TILE_SIZE = SIZE_BOARD / ROWS;
    GraphicsContext gc;


    private static final String SCENE_COLOR = "yellow";
    @Getter
    private Stage stage;
    @Override
    public void init() {
        stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Snake Game");
        Group root = new Group();
        Canvas canvas = new Canvas(WIDTH_WINDOW, HEIGHT_WINDOW);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, SIZE_BOARD, SIZE_BOARD);
        stage.setScene(scene);

        gc = canvas.getGraphicsContext2D();

        render();
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

//    private void listen() throws GameException {
//        if (model != null) {
//            model.listen(this);
//        }
//    }

//    private void noListen() {
//        if (model != null) {
//            model.noListen(this);
//        }
//    }
}
