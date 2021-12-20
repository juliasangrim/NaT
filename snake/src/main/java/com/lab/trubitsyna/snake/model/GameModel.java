package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
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
    private int stateOrder;
    @Getter
    @Setter
    private SnakesProto.GameConfig config;
    @Getter
    private final ConcurrentHashMap<Integer, Snake> snakes;
    @Getter
    private final ConcurrentHashMap<Integer, Player> players;
    @Getter
    private Field field;
    @Getter
    private final ArrayList<Food> food;
    private int amountFood;


    public GameModel(CustomGameConfig customConfig) {
        this.players = new ConcurrentHashMap<>();
        this.snakes = new ConcurrentHashMap<>();
        this.food = new ArrayList<>();
        this.config = customConfig.convertToProto();
        this.field = new Field(config.getWidth(), config.getHeight());
        this.amountFood = (config.getFoodStatic() + players.size() * Math.round(config.getFoodPerPlayer()));
        this.stateOrder = 0;
    }

    public GameModel() {
        this.players = new ConcurrentHashMap<>();
        this.snakes = new ConcurrentHashMap<>();
        this.food = new ArrayList<>();
        this.config = null;
        this.field = null;
        this.amountFood = 0;
        this.stateOrder = 0;
    }

    public void addConfig(SnakesProto.GameConfig config) {
        this.config = config;
        //every time create field it's not good i thought
        this.field = new Field(config.getWidth(), config.getHeight());
        this.amountFood = (config.getFoodStatic() + players.size() * Math.round(config.getFoodPerPlayer()));
    }

    public void changeSnakesDirection(Player snakeOwner, SnakesProto.Direction newDirection) {
        var snake = snakes.get(snakeOwner.getId());
        snake.setNewDirection(newDirection);
        snakes.put(snakeOwner.getId(), snake);
    }

    private void increaseStateOrder() {
        stateOrder = stateOrder + 1;
    }


    public void updateClientModel(SnakesProto.GameState newStateModel) {
        field.initField();
        stateOrder = newStateModel.getStateOrder();
//        MyLogger.getLogger().info("UPD STATE ORDER");
//        MyLogger.getLogger().info(String.valueOf(config));
        players.clear();
        snakes.clear();
        if (!newStateModel.getSnakesList().isEmpty()) {
            for (var snakeProto : newStateModel.getSnakesList()) {
//                MyLogger.getLogger().info("loop");
                var snake = new Snake(null, null, snakeProto.getPlayerId(), snakeProto.getHeadDirection());
//                MyLogger.getLogger().info("create snake");

                snake.converFromProto(snakeProto);
//                MyLogger.getLogger().info("converted snake");
                snakes.put(snake.getIdOwner(), snake);

            }
        }
        for (var player : newStateModel.getPlayers().getPlayersList()) {
            players.put(player.getId(), new Player(player));
        }

//        MyLogger.getLogger().info("Snakes convert");
        food.clear();
        for (var singleFood : newStateModel.getFoodsList()) {
            var point = new Point(-1, -1);
            point.convertCoordToPoint(singleFood);
            food.add(new Food(point));
        }
//        MyLogger.getLogger().info("Food convert");
        updateModel();
    }


    public void oneTurnGame() throws GameException {
//        MyLogger.getLogger().info("I'm in game loop");
//        MyLogger.getLogger().info("I'm spawn food");
//        MyLogger.getLogger().info("Update gamer ");
        spawnFood();
        updateGame();
        updateModel();
        increaseStateOrder();
//        MyLogger.getLogger().info("UPDATE ");
    }

    private Snake getNewSnake(int playerId) throws GameException {
        var head = field.findEmptySpace();
        if (field.isThereFreeSpace()) {
            Point body = new Point(0, 0);
            int random = Math.abs(generator.nextInt() % 4) + 1;
            SnakesProto.Direction dir = SnakesProto.Direction.forNumber(random);
            switch (Objects.requireNonNull(dir)) {
                case RIGHT -> {

                    body.setX(head.getX() + 1);
                    body.setY(head.getY());
//                    MyLogger.getLogger().debug("Snake direction RIGHT");
                }
                case UP -> {
                    body.setX(head.getX());
                    body.setY(head.getY() - 1);
//                    MyLogger.getLogger().debug("Snake direction UP");
                }
                case DOWN -> {
                    body.setX(head.getX());
                    body.setY(head.getY() + 1);
//                    MyLogger.getLogger().debug("Snake direction DOWN");
                }
                case LEFT -> {
                    body.setX(head.getX() - 1);
                    body.setY(head.getY());
//                    MyLogger.getLogger().debug("Snake direction LEFT");
                }
                default -> {
                    throw new GameException("No such direction, please check code...");
                }
            }
            var snake = new Snake(head, body, playerId, dir);
//            MyLogger.getLogger().info("Server make snake for client!!!!");
            field.deleteEmptyPoint(head);
            field.deleteEmptyPoint(body);
            return snake;
        } else {
            MyLogger.getLogger().info("ERROR: no more space for new snakes");
            throw new GameException("Can't add snake. No free space.");
        }
    }

    @Override
    public boolean addNewPlayer(Player player) {
        try {
            snakes.put(player.getId(), getNewSnake(player.getId()));
            MyLogger.getLogger().info("Add client to list!");
            players.put(player.getId(), player);
            return true;
        } catch (GameException e) {
            return false;
        }
    }

    private boolean isProbabilityFood() {
        double prob = Math.random();
        return prob < config.getDeadFoodProb();
    }

    private void probSpawnFrut(Snake snake) {
        if (isProbabilityFood()) {
            food.add(new Food(snake.getHead()));
        } else {
            field.addEmptyPont(snake.getHead());
        }
        for (var body : snake.getBody()) {
            if (isProbabilityFood()) {
                food.add(new Food(body));
            } else {
                field.addEmptyPont(snake.getHead());
            }
        }
    }

    private void updateSnakesOnField() {
        for (var snake : snakes.values()) {
            //set tile for head
            if (snake.isDead()) {
                field.setTile(snake.getHead(), Tile.BOARD);
                for (var bodyPoint : snake.getBody()) {
                    field.setTile(bodyPoint, Tile.BOARD);
                }
                snakes.remove(snake.getIdOwner());
            } else {
                field.setTile(snake.getHead(), Tile.SNAKE_HEAD);
                for (var bodyPoint : snake.getBody()) {
                    field.setTile(bodyPoint, Tile.SNAKE_BODY);
                }
            }
        }

    }


    private void updateFoodOnField() {
        for (var singleFood : food) {
            field.setTile(singleFood.getPlace(), Tile.FOOD);
        }
    }

    public void updateModel() {
        updateSnakesOnField();
//        MyLogger.getLogger().info("Upd snake");
        updateFoodOnField();
        try {
//            MyLogger.getLogger().info("I want to upd view.");
            notifyListeners();
//            MyLogger.getLogger().info("Update success!");
        } catch (GameException e) {
            MyLogger.getLogger().error("ERROR: no update view.");
        }
        MyLogger.getLogger().info("Notified listeners...");
    }

    //the first food spawning
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
        for (var snake : snakes.values()) {
            if (snake.getState() == SnakesProto.GameState.Snake.SnakeState.ALIVE) {
                amountAliveSnakes++;
            }
        }
        return amountAliveSnakes;
    }

    private void updatePlaceSnake(Snake snake) {
        var newHead = snake.getNewHead(field.getWidth(), field.getHeight());
        Tile tile = field.getTile(newHead.getX(), newHead.getY());

        if (tile != Tile.FOOD) { // mb
            field.setTile(snake.getTail(), Tile.BOARD);
            field.setTile(snake.getHead(), Tile.SNAKE_BODY);
            field.addEmptyPont(snake.getTail());
            snake.deleteTail();
        }

        snake.move(newHead);
    }

    private void countCollision() {
        for (var snake : snakes.values()) {
            Tile collision = field.getTile(snake.getHead());
//            MyLogger.getLogger().info("Update snake");
            if (collision == Tile.FOOD) {
                players.get(snake.getIdOwner()).incrementScore();
                food.removeIf(singleFood -> singleFood.getPlace().equals(snake.getHead()));
                try {
                    spawnFood();
                } catch (GameException e) {
                    MyLogger.getLogger().info("No space for new food");
                }
            }
            if (snake.getBody().contains(snake.getHead())) {
                snake.setDead(true);
                probSpawnFrut(snake);
            }
            for (var otherSnake : snakes.values()) {

                if (snake.getIdOwner() != otherSnake.getIdOwner()) {
                    if (snake.getHead().equals(otherSnake.getHead())) {
                        players.get(snake.getIdOwner()).incrementScore();
                        players.get(otherSnake.getIdOwner()).incrementScore();
                        snake.setDead(true);
                        otherSnake.setDead(true);
                        probSpawnFrut(snake);
                        probSpawnFrut(otherSnake);
                    } else {
                        if (otherSnake.getBody().contains(snake.getHead())) {
                            players.get(otherSnake.getIdOwner()).incrementScore();
                            snake.setDead(true);
                            probSpawnFrut(snake);
                        }
                    }
                }
            }
        }
    }

    private void updateGame() {
        //move all snakes
        if (snakes != null) {
            for (var snake : snakes.values()) {
                updatePlaceSnake(snake);
            }
            countCollision();
        }
    }
    //notify listeners about changes

    @Override
    public void notifyListeners() throws GameException {
        for (IListener listener : listeners) {
            notifyListener(listener);
        }
    }

    private void notifyListener(IListener listener) throws GameException {
        if (listener == null) {
            throw new GameException("No listeners for our model...");
        }
        StringBuilder message = new StringBuilder();
        int i = 0;
        for (var player : players.values()) {
            ++i;
            if (player.getRole() == SnakesProto.NodeRole.VIEWER) {
                message.append(String.format("%s. %s \n Snake is dead! \n", i, player.getName()));
            } else {
                message.append(String.format("%s. %s \n Score: %s \n", i, player.getName(), player.getScore()));
            }
        }
        Platform.runLater(() -> listener.modelChanged(this));
        Platform.runLater(() -> listener.modelChanged(message.toString()));
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
       // notifyListener(listener);
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
