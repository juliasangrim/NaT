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
    private ArrayDeque<Point> body;
    private ArrayList<SnakesProto.GameState.Coord> handleCoordList;
    @Getter@Setter
    private SnakesProto.GameState.Snake.SnakeState state;
    @Getter@Setter
    private SnakesProto.Direction direction;
    @Getter@Setter
    private boolean isDead;
    @Getter

    private Point head;
    @Setter
    private int score;
    @Getter
    private int idOwner;


    public Snake(Point head, Point body, int idOwner, SnakesProto.Direction direction) {
        this.head = head;
        this.body = new ArrayDeque<>();
        this.handleCoordList = new ArrayList<>();
        this.body.add(body);
        this.idOwner = idOwner;
        this.state = SnakesProto.GameState.Snake.SnakeState.ALIVE;
        this.direction = direction;
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
                .setPlayerId(idOwner).addAllPoints(handleCoordList).setHeadDirection(direction).build();
    }


    public void converFromProto(SnakesProto.GameState.Snake snake) throws GameException {
        var pointsList = snake.getPointsList();
        head.convertCoordToPoint(pointsList.get(0));
        body.clear();
        for (var point : pointsList) {
            if (!point.equals(pointsList.get(0))) {
                var bodyPoint = new Point(-1, -1);
                bodyPoint.convertCoordToPoint(point);
                body.addLast(bodyPoint);
            }
        }
    }


    public Point getNewHead(SnakesProto.Direction direction, int widthField, int heightField) {
        Point point = new Point(-1, -1);
        switch (direction) {
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



}
