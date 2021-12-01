package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.protoClass.SnakesProto;

import java.util.ArrayList;

public class Snake {
    Point head;
    ArrayList<Point> body;
    Direction direction;

    public Snake(Field field) {
        head = field.findEmptySpace();
        if (head.getX() == -1 || head.getY() == -1) {
            //TODO: check task
            System.err.println("Can't generate snake");
        } else {
            direction = Direction.UP;
            body = new ArrayList<>();
            body.add(new Point(head.getX(), head.getY() + 1));
        }
    }


}
