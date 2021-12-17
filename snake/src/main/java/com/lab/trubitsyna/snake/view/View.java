package com.lab.trubitsyna.snake.view;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.controller.GameController;
import com.lab.trubitsyna.snake.controller.IListenerView;
import com.lab.trubitsyna.snake.controller.MenuController;
import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.model.*;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import lombok.Setter;


public class View implements IListenerView {
    @Setter
    private Stage stage;
    private Canvas board;
    private TextArea rate;

    private String serverInfo;
    private SnakesProto.GameConfig config;

    public View(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void render(StateSystem stateSystem, String message) {
        try {
            switch (stateSystem) {
                case NEW_GAME -> loadGame();
                case MENU -> loadMenu();
                case CONFIG -> loadSetting();
                case JOIN_GAME -> loadJoinGame();
                case LOAD_GAME -> loadInfoAboutConnection();
                case ERROR_LOAD_GAME -> loadInfoAboutError(message);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        stage.show();

    }

    @Override
    public void sendServerInfoToGameController(String message) {
        this.serverInfo = message;
    }

    @Override
    public void sendConfigToGameController(SnakesProto.GameConfig config) {
        this.config = config;
    }

    private void loadInfoAboutConnection() {
        var gc = board.getGraphicsContext2D();
        gc.clearRect(0, 0, board.getWidth(), board.getHeight());
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFontSmoothingType(FontSmoothingType.LCD);
        gc.fillText(
                "CONNECTION....",
                Math.round(board.getWidth()  / 2),
                Math.round(board.getHeight() / 2)
        );
    }

    private void loadInfoAboutError(String message) {
        var gc = board.getGraphicsContext2D();
        gc.clearRect(0, 0, board.getWidth(), board.getHeight());
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFontSmoothingType(FontSmoothingType.LCD);
        gc.fillText(
                message,
                Math.round(board.getWidth()  / 2),
                Math.round(board.getHeight() / 2)
        );
    }

    private void loadGame() throws Exception{
        FXMLLoader gameLoader =  new FXMLLoader(View.class.getResource("app.fxml"));
        stage.setScene(new Scene(gameLoader.load()));
        stage.centerOnScreen();

        GameController controller = gameLoader.getController();
        board = controller.getBoard();
        rate = controller.getTextField();
        controller.setGameView(this);
        controller.setState(StateSystem.NEW_GAME);
        controller.start();
        stage.setOnCloseRequest(t -> controller.onExitWindowButtonPressed());
    }

    public void loadMenu() throws Exception{
        FXMLLoader menuLoader =  new FXMLLoader(View.class.getResource("menu.fxml"));
        stage.setScene(new Scene(menuLoader.load()));
        MenuController controller = menuLoader.getController();
        controller.setMenuView(this);

        controller.start();
        stage.setOnCloseRequest(t -> controller.onExitButtonPressed());
    }

    private void loadSetting() throws Exception{
        FXMLLoader gameLoader =  new FXMLLoader(View.class.getResource("app.fxml"));
        stage.setScene(new Scene(gameLoader.load()));
       // stage.setOnCloseRequest(t -> controller.onExitButtonPressed());
    }

    private void loadJoinGame() throws Exception{
        FXMLLoader gameLoader =  new FXMLLoader(View.class.getResource("app.fxml"));
        stage.setScene(new Scene(gameLoader.load()));
        stage.centerOnScreen();

        GameController controller = gameLoader.getController();
        rate = controller.getTextField();
        board = controller.getBoard();
        controller.setGameView(this);
        controller.setServerPort(Integer.parseInt(serverInfo.split("\n")[3].split(" ")[1]));
        controller.setServerAddr(serverInfo.split("\n")[4].split(" ")[1]);
        controller.setServerConfig(config);
        controller.setState(StateSystem.JOIN_GAME);


        controller.start();
        stage.setOnCloseRequest(t -> controller.onExitWindowButtonPressed());
    }

    private void drawBackground(GraphicsContext gc, Field field, double tileSize) {
        for (int y = 0; y <  field.getHeight(); ++y) {
            for (int x = 0; x < field.getWidth(); ++x) {
                if ((x + y) % 2 == 0) {
                    gc.setFill(Color.LIGHTGOLDENRODYELLOW);
                } else {
                    gc.setFill(Color.KHAKI);
                }
                gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }

    }

    private void drawElements(GraphicsContext gc, Field field, double tileSize) {
        for (int y = 0; y <  field.getHeight(); ++y) {
            for (int x = 0; x < field.getWidth(); ++x) {
                Tile tile = field.getTile(new Point(x, y));
                if (tile == Tile.SNAKE_HEAD) {
                    gc.setFill(Color.MEDIUMPURPLE);
                }                if (tile == Tile.SNAKE_BODY) {
                    gc.setFill(Color.LIMEGREEN);
                }
                if (tile == Tile.FOOD) {
                    var image = new Image("pngegg.png");
                    gc.drawImage(image, x * tileSize, y * tileSize, tileSize, tileSize);
                }
                if (tile != Tile.BOARD && tile != Tile.FOOD) {
                    gc.fillOval(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }
    }

    private double getTileSize(Field field) {
        int width = field.getWidth();
        int height = field.getHeight();
        double tileSize;
        if (width > height) {
            tileSize = board.getWidth() / width;
        } else {
            tileSize = board.getHeight()/ height;
        }
        return tileSize;
    }

    @Override
    public void modelChanged(GameModel model) {
        var gc = board.getGraphicsContext2D();
        var field = model.getField();
        drawBackground(gc, field, getTileSize(field));
        drawElements(gc, field, getTileSize(field));

    }

    @Override
    public void modelChanged(String message) {
        rate.clear();
        rate.setText(message);
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
