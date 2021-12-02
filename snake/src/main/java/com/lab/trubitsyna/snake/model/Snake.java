package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.protoClass.SnakesProto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class Snake {
    @Getter
    private UserInfo user;
    @Getter
    private Point head;
    @Getter
    private HashSet<Point> body;
    @Getter @Setter
    private Direction direction;



    public Snake(Field field, UserInfo owner) {
        this.user = owner;
        head = field.findEmptySpace();
        if (head.getX() == -1 || head.getY() == -1) {
            //TODO: check task
            System.err.println("Can't generate snake");
        } else {
            direction = Direction.UP;
            body = new HashSet<>();
            body.add(new Point(head.getX(), head.getY() + 1));
        }
        field.deleteEmptyPoint(head);
        for (var point : body) {
            field.deleteEmptyPoint(point);
        }
    }


}
