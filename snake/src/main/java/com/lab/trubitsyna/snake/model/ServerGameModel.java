package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.view.Tile;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerGameModel implements IModel {
    private final static Random generator = new Random();

    private final CopyOnWriteArrayList<IListener> listeners = new CopyOnWriteArrayList<>();
    private static final int PORT = 8000;
    private static final int ADMIN_ID = 0;

    @Getter
    private SnakesProto.GameConfig config;
    private HashSet<SnakesProto.GamePlayer> players;
    private ArrayList<SnakesProto.GameState.Snake> snakes;
    @Getter
    private Field field;
    private HashSet<Food> food;
    private int amountFood;
    private int amountAliveSnakes = 0;

    public void changeConfig() {

    }

    public ServerGameModel(CustomGameConfig customConfig) {
        this.players = new HashSet<>();
        this.snakes = new ArrayList<>();
        this.food = new HashSet<>();
        this.config = SnakesProto.GameConfig.newBuilder()
                .setWidth(customConfig.getWidth())
                .setHeight(customConfig.getHeight())
                .setFoodStatic(customConfig.getFoodStatic())
                .setFoodPerPlayer(customConfig.getFoodPerPlayer())
                .setStateDelayMs(customConfig.getStateDelay())
                .setDeadFoodProb(customConfig.getDeadProbFood())
                .setPingDelayMs(customConfig.getPingDelay())
                .setNodeTimeoutMs(customConfig.getNodeTimeout()).build();
        this.field = new Field(config.getWidth(), config.getHeight());
    }



    public void startGame(String login, SnakesProto.PlayerType playerType) throws GameException {

        //add admin to the players list
        var player = SnakesProto.GamePlayer.newBuilder().setName(login)
                .setId(0).setIpAddress("").setPort(PORT)
                .setRole(SnakesProto.NodeRole.MASTER).setType(playerType).setScore(0).build();

        players.add(player);

        addNewSnake(player.getId());
        amountFood = (config.getFoodStatic() + Math.round(config.getFoodPerPlayer()));
        spawnFood();

        updateModel();

    }

    private void addNewSnake(int playerId) {

        var head = field.findEmptySpace();
        //shift body down
        var body = new Point(0, 1);

        var snake = SnakesProto.GameState.Snake.newBuilder()
                .setState(SnakesProto.GameState.Snake.SnakeState.ALIVE)
                .setPlayerId(ADMIN_ID)
                .setHeadDirection(SnakesProto.Direction.UP)
                .addPoints(head.convertPointToCoord())
                .addPoints(body.convertPointToCoord()).build();
        snakes.add(snake);

        field.deleteEmptyPoint(head);
        field.deleteEmptyPoint(new Point(head.getX() + body.getX(),
                head.getY() + body.getY()));
    }

    public void addNewPlayer(SnakesProto.GamePlayer player) {
        players.add(SnakesProto.GamePlayer.newBuilder().setName(player.getName())
                .setId(player.getId()).setIpAddress(player.getIpAddress()).setPort(player.getPort())
                .setRole(player.getRole()).setType(player.getType()).setScore(0).build());
    }

    private void updateModel() throws GameException {
        for (var snake : snakes) {
            var points = snake.getPointsList();
            var head = points.get(0);
            //set tile for head
            field.setTile(new Point(head.getX(), head.getY()), Tile.SNAKE_HEAD);
            //set tiles for body
            var oldPoint = head;
            for (var point : points) {
                if (point.equals(head)) {
                    continue;
                }
                var pointWithShift = new Point(oldPoint.getX() + point.getX(), oldPoint.getY() + point.getY());
                field.setTile(pointWithShift, Tile.SNAKE_BODY);
                oldPoint = point;
            }
        }
        for (var singleFood : food) {
            field.setTile(singleFood.getPlace(), Tile.FOOD);
        }
        notifyListeners();
    }

    //the first food spawning
    private void spawnFood() throws GameException {
        if (amountFood > field.getAmountEmptyPoint()) {
            throw new GameException("too many food");
        }
        for (int i = 0; i < amountFood; ++i) {
            int place = generator.nextInt(field.getAmountEmptyPoint());
            Food singleFood = new Food(field.getEmptyPoint(place));
            food.add(singleFood);
            field.deleteEmptyPoint(singleFood.getPlace());
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
