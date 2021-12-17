package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.gameException.GameException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class Snake {
    @Getter
    private final ArrayDeque<Point> body;
    @Getter
    private int idOwner;
    private final ArrayList<SnakesProto.GameState.Coord> handleCoordList;

    @Getter
    private Point head;
    @Getter@Setter
    private SnakesProto.Direction currDirection;
    @Getter@Setter
    private SnakesProto.Direction newDirection;
    @Getter@Setter
    private boolean isDead;
    @Getter@Setter
    private int score;

    @Getter@Setter
    private SnakesProto.GameState.Snake.SnakeState state;


    public Snake(Point head, Point body, int idOwner, SnakesProto.Direction currDirection) {
        this.head = head;
        this.body = new ArrayDeque<>();
        this.handleCoordList = new ArrayList<>();
        if (body != null) {
            this.body.add(body);
        }
        this.idOwner = idOwner;
        this.state = SnakesProto.GameState.Snake.SnakeState.ALIVE;
        this.currDirection = currDirection;
        this.newDirection = currDirection;
        this.score = 0;
        this.isDead = false;

    }


    public void move(Point newPointBody) {

        body.addFirst(head);
        head = newPointBody;
    }


    public SnakesProto.GameState.Snake convertToProtoSnake() {
        handleCoordList.clear();
        var oldPoint = head;

        for (var point : body) {
            var pointWithShift = new Point(point.getX() - oldPoint.getX(), point.getY() - oldPoint.getY());
            handleCoordList.add(pointWithShift.convertPointToCoord());
            oldPoint = point;
        }
        return SnakesProto.GameState.Snake.newBuilder().setState(state).addPoints(0, head.convertPointToCoord())
                .setPlayerId(idOwner).addAllPoints(handleCoordList).setHeadDirection(currDirection).build();
    }


    public void converFromProto(SnakesProto.GameState.Snake snake)  {
        var pointsList = snake.getPointsList();
        MyLogger.getLogger().info("get poin lists");
        head = new Point(-1, -1);
        head.convertCoordToPoint(pointsList.get(0));
        body.clear();
        var oldPoint = head;
        for (var point : pointsList) {
            if (!point.equals(pointsList.get(0))) {
               var bodyPoint = new Point(-1, -1);
                bodyPoint.convertCoordToPoint(point);
                var shiftedPoint = new Point(oldPoint.getX() + bodyPoint.getX(), oldPoint.getY() + bodyPoint.getY());
                body.addLast(shiftedPoint);
                oldPoint = shiftedPoint;
            }
        }
    }


    public Point getNewHead(int widthField, int heightField) {
        //possible problem
        if (!isOpposite()) {
            currDirection = newDirection;
        }
        Point point = new Point(-1, -1);
        switch (currDirection) {
            case UP -> {
                int newY = (head.getY() + heightField - 1) % heightField;
                point.setX(head.getX());
                point.setY(newY);
            }
            case DOWN -> {
                int newY = (head.getY() + 1) % heightField;
                point.setX(head.getX());
                point.setY(newY);
            }
            case LEFT -> {
                int newX = (head.getX() + widthField - 1) % widthField;
                point.setX(newX);
                point.setY(head.getY());
            }
            case RIGHT -> {
                int newX = (head.getX() + 1) % widthField;
                point.setX(newX);
                point.setY(head.getY());
            }
        }
        return point;
    }

    public Point getTail() {
        return body.getLast();
    }
    public void deleteTail() {
        body.removeLast();
    }

    public void incrementScore() {
        score = score + 1;
    }


    public boolean isOpposite() {
        return currDirection == SnakesProto.Direction.RIGHT && newDirection == SnakesProto.Direction.LEFT ||
                currDirection == SnakesProto.Direction.LEFT && newDirection == SnakesProto.Direction.RIGHT ||
                currDirection == SnakesProto.Direction.UP && newDirection == SnakesProto.Direction.DOWN ||
                currDirection == SnakesProto.Direction.DOWN && newDirection == SnakesProto.Direction.UP;
    }

}
