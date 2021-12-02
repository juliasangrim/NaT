package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.view.Tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameModel implements IModel {
    private final static Random generator = new Random();

    private final CopyOnWriteArrayList<IListener> listeners = new CopyOnWriteArrayList<>();

    private Field field;
    private HashSet<Snake> snakes;
    private HashSet<Food> food;
    private int amountFood;
    private int amountAliveSnakes = 0;


    public GameModel(int width, int height, int foodStatic, float foodPerPlayer, float deadFloatProb) throws GameException {
        Field field = new Field(width, height);
        snakes = new HashSet<>();
        //TODO : make possibility add login
        UserInfo admin = new UserInfo("admin", 0);
        addNewPlayer(admin);
        amountFood = foodStatic + Math.round(foodPerPlayer * amountAliveSnakes);
        food = new HashSet<>();
        initFood();
        updateModel();
    }

    public void addNewPlayer(UserInfo user) {
        Snake snake = new Snake(field, user);
        snakes.add(snake);
        amountAliveSnakes++;
    }

    private void updateModel() throws GameException {
        for (var snake : snakes) {
            field.setTile(snake.getHead(), Tile.SNAKE_HEAD);
            for (var point : snake.getBody()) {
                field.setTile(point, Tile.SNAKE_BODY);
            }
        }
        for (var singleFood : food) {
            field.setTile(singleFood.getPlace(), Tile.FOOD);
        }
        notifyListeners();
    }

    //the first food spawning
    private void initFood() {
        for (int i = 0; i < amountFood; ++i) {
            int place = generator.nextInt(field.getAmountEmptyPoint());
            Food singleFood = new Food(field.getEmptyPoint(place));
        }
    }


    //notify listeners about changes
    protected void notifyListeners() throws GameException {
        for (IListener listener : listeners) {
            notifyListener(listener);
        }
    }

    private void notifyListener(IListener listener) throws GameException {
        if (listener == null) {
            throw new GameException("No listeners for our model...");
        }
        listener.modelChanged(this);
    }

    //method for following our model changes
    public void addListener(IListener listener) throws GameException {
        if (listener == null) {
            throw new NullPointerException("Empty param...");
        }
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("Repeat listeners...");
        }
        listeners.add(listener);
        notifyListener(listener);
    }

    //method for stop following our model changes
    public void removeListener(IListener listener) {
        if (listener == null) {
            throw new NullPointerException("Empty param...");
        }
        if (!listeners.contains(listener)) {
            throw new IllegalArgumentException("Model don't have this listener...");
        }
    }

//    private TileType updatePlaceSnake(Point head) {
//        if(head.x < 0 || head.x >= board.getColumnCount() || head.y < 0 || head.y >= board.getRowCount()) {
//            return TileType.SNAKE_BODY; //Pretend we collided with our body.
//        }
//        TileType tile = board.getTile(head.x, head.y);
//
//        if(tile != TileType.FRUIT && snake.getSizeSnake() > Snake.START_LENGTH) {
//            Point tail = snake.deleteTail();
//            board.setTile(tail, null);
//            tile = board.getTile(head.x, head.y);
//        }
//
//        if(tile != TileType.SNAKE_BODY) {
//            board.setTile(snake.getHead(), TileType.SNAKE_BODY);
//            snake.addNewHead(head);
//            board.setTile(head, TileType.SNAKE_HEAD);
//            if (direction.size() > 1) {
//                direction.poll();
//            }
//        }
//
//        return tile;
//    }
//
//    public void startRound() throws IOException, GameException {
//        setHighScores();
//        while (true) {
//            if (state == ViewState.GAME) {
//                if (isNewGame) {
//                    notifyListeners();
//                }
//                if (!isGameEnd) {
//                    if (!isPaused) {
//                        updateGame();
//                    }
//                }
//                if ((state == ViewState.RECORD) && (isGameEnd)) {
//                    addRecord(user);
//                }
//            }
//
//            notifyListeners();
//            if (state == ViewState.GAME) {
//                try {
//                    Thread.sleep(70);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private void updateGame() {
//        Point head = snake.getNewHead(getDirection());
//        TileType collision = updatePlaceSnake(head);
//
//        if (collision == TileType.SNAKE_BODY) {
//            isGameEnd = true;
//            state = RECORD;
//        } else if (collision == TileType.FRUIT){
//            snake.snakeEatFruit();
//            user.score += fruit.getFruitScore();
//            spawnFruit();
//        } else if (fruit.getFruitScore() > 20) {
//            fruit.reduceFruitScore();
//        }
//
//    }
//

}
