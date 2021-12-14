package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.view.StateSystem;
import com.lab.trubitsyna.snake.view.Tile;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameModel implements IModel {
    private final static Random generator = new Random();
    private final CopyOnWriteArrayList<IListener> listeners = new CopyOnWriteArrayList<>();

    @Getter
    private SnakesProto.GameConfig config;
    @Getter
    private ConcurrentHashMap<SnakesProto.GamePlayer, Snake> players;
    @Getter
    private Field field;
    private String login;
    private ArrayList<Food> food;
    private int amountFood;
    private int amountAliveSnakes = 0;
    @Setter
    private StateSystem state;

    public void changeConfig() {

    }

    public GameModel(CustomGameConfig customConfig, StateSystem state) {
        this.players = new ConcurrentHashMap<>();
        this.food = new ArrayList<>();
        this.config = customConfig.convertToProto();
        this.field = new Field(config.getWidth(), config.getHeight());
        this.login = customConfig.getLogin();
        this.amountFood = (config.getFoodStatic() + players.size() * Math.round(config.getFoodPerPlayer()));
        this.state = state;
    }

    public void changeSnakeDirection(SnakesProto.GamePlayer snakeOwner, SnakesProto.Direction newDirection) {
        var snake = players.get(snakeOwner);
        System.out.println("DO " + players.get(snakeOwner).getDirection());
        MyLogger.getLogger().info("Amount of players " + players.size());
        var currDirection = snake.getDirection();
        if (currDirection == SnakesProto.Direction.RIGHT && newDirection == SnakesProto.Direction.LEFT) {
            return;
        }
        if (currDirection == SnakesProto.Direction.LEFT && newDirection == SnakesProto.Direction.RIGHT) {
            return;
        }
        if (currDirection == SnakesProto.Direction.UP && newDirection == SnakesProto.Direction.DOWN) {
            return;
        }
        if (currDirection == SnakesProto.Direction.DOWN && newDirection == SnakesProto.Direction.UP) {
            return;
        }

        snake.setDirection(newDirection);
        players.put(snakeOwner, snake);
        System.out.println("POSLE " + players.get(snakeOwner).getDirection());
    }


    public void startGame() throws GameException {
        while (state == StateSystem.JOIN_GAME || state == StateSystem.NEW_GAME) {
            MyLogger.getLogger().info("I'm in game loop");
            updateGame();
            updateModel();
            try {
                Thread.sleep(config.getStateDelayMs());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private Snake getNewSnake(int playerId) throws GameException {
        var head = field.findEmptySpace();
        if (field.isThereFreeSpace()) {
            Point body = new Point(0, 0);
            int random = Math.abs(generator.nextInt() % 4) + 1;
            SnakesProto.Direction dir = SnakesProto.Direction.forNumber(random);
            System.out.println(dir + " " + random);
            switch (Objects.requireNonNull(dir)) {
                case RIGHT -> {

                    body.setX(head.getX() + 1);
                    body.setY(head.getY());
                    MyLogger.getLogger().debug("Snake direction RIGHT");
                }
                case UP -> {
                    body.setX(head.getX());
                    body.setY(head.getY() - 1);
                    MyLogger.getLogger().debug("Snake direction UP");
                }
                case DOWN -> {
                    body.setX(head.getX());
                    body.setY(head.getY() + 1);
                    MyLogger.getLogger().debug("Snake direction DOWN");
                }
                case LEFT -> {
                    body.setX(head.getX() - 1);
                    body.setY(head.getY());
                    MyLogger.getLogger().debug("Snake direction LEFT");
                }
                default -> {
                    throw new GameException("No such direction, please check code...");
                }
            }
            var snake = new Snake(head, body, playerId, dir);
            MyLogger.getLogger().info("Server make snake for client!!!!");
            field.deleteEmptyPoint(head);
            field.deleteEmptyPoint(body);
            return snake;
        } else {
            MyLogger.getLogger().info("ERROR: no more space for new snakes");
            throw new GameException("Can't add snake. No free space.");
        }
    }

    @Override
    public boolean addNewPlayer(SnakesProto.GamePlayer player) {
        try {
            players.put(player, getNewSnake(player.getId()));
            MyLogger.getLogger().info("Add client to list!");
            return true;
        } catch (GameException e) {
            return false;
        }
    }

    private boolean probSpawnFrut() {
        double prob = Math.random();
        return prob < config.getDeadFoodProb();
    }

    private void updateSnakeOnField() {
        for (var player : players.keySet()) {
            var snake = players.get(player);
            //set tile for head
            if (!snake.isDead()) {
                field.setTile(snake.getHead(), Tile.SNAKE_HEAD);
                field.deleteEmptyPoint(snake.getHead());
                for (var bodyPoint : snake.getBody()) {
                    field.setTile(bodyPoint, Tile.SNAKE_BODY);
                    field.deleteEmptyPoint(snake.getHead());
                }
            } else {
                if (probSpawnFrut()) {
                    field.setTile(snake.getHead(), Tile.FOOD);
                    field.deleteEmptyPoint(snake.getHead());
                } else {
                    field.setTile(snake.getHead(), Tile.BOARD);
                    field.addEmptyPont(snake.getHead());
                }
                for (var point : snake.getBody()) {
                    if (probSpawnFrut()) {
                        field.setTile(point, Tile.FOOD);
                        field.deleteEmptyPoint(snake.getHead());
                    } else {
                        field.setTile(point, Tile.BOARD);
                        field.addEmptyPont(point);
                    }
                }
                //TODO : snake deleted?
                players.remove(player);
            }
        }
    }

    private void updateFoodOnField() {
        for (var singleFood : food) {
            field.setTile(singleFood.getPlace(), Tile.FOOD);
            field.deleteEmptyPoint(singleFood.getPlace());
        }
    }

    public void updateModel()  {
        updateSnakeOnField();
        try {
            spawnFood();
        } catch (GameException e) {
            e.printStackTrace();
        }
        updateFoodOnField();
        try {
            notifyListeners();
        } catch (GameException e) {
            MyLogger.getLogger().error("ERROR: no update view.");
        }
        MyLogger.getLogger().info("Notified listeners...");
    }

    //the first food spawning
    //TODO : count food on field
    private void spawnFood() throws GameException {
        int foodOnField = field.countFood();
        int amountAliveSnakes = countAliveSnakes();
        amountFood = config.getFoodStatic() + amountAliveSnakes * Math.round(config.getFoodPerPlayer());
        if (amountFood - foodOnField > field.getAmountEmptyPoint()) {
            throw new GameException("No space for food.");
        }
        for (int i = 0; i < amountFood - foodOnField; ++i) {
            boolean isFoodGenerated = false;
            Point pointForFood = null;
            while (!isFoodGenerated) {
                int place = generator.nextInt(field.getAmountEmptyPoint());
                pointForFood = field.getEmptyPoint(place);
                if (!field.isSnake(pointForFood) &&
                                field.getTile(pointForFood) != Tile.FOOD) {
                  isFoodGenerated = true;
                }
            }
            Food singleFood = new Food(pointForFood);
            food.add(singleFood);
            field.deleteEmptyPoint(singleFood.getPlace());
        }
    }


    private int countAliveSnakes() {
        int amountAliveSnakes = 0;
        for (var snake : players.values()) {
            if (snake.getState() == SnakesProto.GameState.Snake.SnakeState.ALIVE) {
                amountAliveSnakes++;
            }
        }
        return amountAliveSnakes;
    }

    private Point updatePlaceSnake(Snake snake) {
        var newHead = snake.getNewHead(snake.getDirection(), field.getWidth(), field.getHeight());
        Tile tile = field.getTile(newHead.getX(), newHead.getY());

        if (tile != Tile.FOOD) {
            field.setTile(snake.getTail(), Tile.BOARD);
            field.setTile(snake.getHead(), Tile.SNAKE_BODY);
            field.addEmptyPont(snake.getTail());
            snake.deleteTail();
        }

        snake.move(newHead);
        return new Point(newHead.getX(), newHead.getY());
    }

    private void updateGame() {
        for (var snake : players.values()) {
            Point newHead = updatePlaceSnake(snake);
            Tile collision = field.getTile(newHead.getX(), newHead.getY());
            MyLogger.getLogger().info("Update snake");
            if (collision == Tile.SNAKE_BODY || collision == Tile.SNAKE_HEAD) {
                snake.setDead(true);
            } else if (collision == Tile.FOOD) {
                snake.incrementScore();
                food.removeIf(singleFood -> singleFood.getPlace().equals(newHead));
                try {
                    spawnFood();
                } catch (GameException e) {
                    MyLogger.getLogger().info("No space for new food");
                }
            }

        }
    }
    //notify listeners about changes

    @Override
    public void notifyListeners() throws GameException {
        for (IListener listener : listeners) {
            System.out.println("Notify listener");
            notifyListener(listener);
        }
    }

    private void notifyListener(IListener listener) throws GameException {
        if (listener == null) {
            throw new GameException("No listeners for our model...");
        }
        Platform.runLater(() -> listener.modelChanged(this));
    }
    //method for following our model changes

    @Override
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

    @Override
    public void removeListener(IListener listener) {
        if (listener == null) {
            throw new NullPointerException("Empty param...");
        }
        if (!listeners.contains(listener)) {
            throw new IllegalArgumentException("Model don't have this listener...");
        }
    }

}
