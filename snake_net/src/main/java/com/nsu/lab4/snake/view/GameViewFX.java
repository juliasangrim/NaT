package com.nsu.lab4.snake.view;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class GameViewFX implements IGameView {
    public final static int WIDTH = 600;

    public final static int HEIGHT = 600;

    private static final String SCENE_COLOR = "yellow";
    private Stage stage;
    private Scene scene;
    private Group g;
    private Pane canvas;
    private GridPane grid;
    private StackPane stack;
    private Color bodyColor;
    @Override
    public void init() {
        stage = new Stage();
        stage.setTitle("Snake");

        canvas = new Pane();
        canvas.setStyle("-fx-background-color: "+SCENE_COLOR);
        canvas.setPrefSize(WIDTH,HEIGHT);

        stack = new StackPane();
        grid = new GridPane();

        g = new Group();
        scene = new Scene(g, WIDTH, HEIGHT);
        scene.setFill(Color.web(SCENE_COLOR));

        render();
    }

    @Override
    public void render() {

    }
}
