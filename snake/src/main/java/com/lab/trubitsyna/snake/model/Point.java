package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.protoClass.SnakesProto;
import lombok.Getter;
import lombok.Setter;

public class Point {
    @Getter @Setter
    private int x;
    @Getter @Setter
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public SnakesProto.GameState.Coord convertPointToCoord() {
        return SnakesProto.GameState.Coord.newBuilder().setX(x).setY(y).build();
    }

}
