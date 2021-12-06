package com.lab.trubitsyna.snake.view;

import com.lab.trubitsyna.snake.controller.GameController;
import com.lab.trubitsyna.snake.controller.IListenerView;
import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.model.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.Setter;


public class View implements IListenerView {

    @Setter
    private Stage stage;

    private Canvas board;

    public View(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void render(StateSystem stateSystem) {
        try {
            switch (stateSystem) {
                case NEW_GAME -> loadGame();
                case MENU -> loadMenu();
                case CONFIG -> loadSetting();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        stage.show();

    }

    private void loadGame() throws Exception{
        FXMLLoader gameLoader =  new FXMLLoader(View.class.getResource("app.fxml"));
        stage.setScene(new Scene(gameLoader.load()));
        //TODO : it will be cool if i can move  to onPlayButtonPressed
        GameController controller = gameLoader.getController();
        board = controller.getBoard();
        controller.setGameView(this);
        controller.start();
    }

    public void loadMenu() throws Exception{
        FXMLLoader menuLoader =  new FXMLLoader(View.class.getResource("menu.fxml"));
        stage.setScene(new Scene(menuLoader.load()));
    }

    private void loadSetting() throws Exception{
        FXMLLoader gameLoader =  new FXMLLoader(View.class.getResource("app.fxml"));
        stage.setScene(new Scene(gameLoader.load()));
    }

    private void drawBackground(GraphicsContext gc, Field field) {
        //TODO: duplicated
        int width = field.getWidth();
        int height = field.getHeight();

        double tile_width = board.getWidth() / width;
        double tile_height = board.getHeight() / height;
        ///
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Tile tile = field.getTile(new Point(x, y));
                if ((x + y) % 2 == 0) {
                    gc.setFill(Color.LIGHTGOLDENRODYELLOW);
                } else {
                    gc.setFill(Color.KHAKI);
                }
                gc.fillRect(x * tile_width, y * tile_height, tile_width, tile_height);
            }
        }

    }

    private void drawElements(GraphicsContext gc, Field field) {
        //TODO : duplicated
        int width = field.getWidth();
        int height = field.getHeight();

        double tile_width = board.getWidth() / width;
        double tile_height = board.getHeight() / height;
        ////
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Tile tile = field.getTile(new Point(x, y));
                if ( tile == Tile.SNAKE_HEAD || tile == Tile.SNAKE_BODY) {
                    gc.setFill(Color.GREEN);
                }
                if (tile == Tile.MY_SNAKE_HEAD) {
                    gc.setFill(Color.DARKGREEN);
                }
                if (tile == Tile.FOOD) {
                    gc.setFill(Color.RED);
                }
                if (tile != Tile.BOARD) {
                    gc.fillOval(x * tile_width, y * tile_height, tile_width, tile_height);
                }
            }
        }
    }

    @Override
    public void modelChanged(GameModel model) {
        var gc = board.getGraphicsContext2D();
        drawBackground(gc, model.getField());
        drawElements(gc, model.getField());

    }

    @Override
    public void listen(IModel newModel) throws GameException {
        if (newModel != null) {
            newModel.addListener(this);
        }
    }
    @Override
    public void noListen(IModel oldModel) {
        if (oldModel != null) {
            oldModel.removeListener(this);
        }
    }


}
