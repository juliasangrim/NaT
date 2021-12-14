package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import lombok.Getter;
import lombok.Setter;


//class for convenient work with class Field
public class Point  {
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

    public void convertCoordToPoint(SnakesProto.GameState.Coord coord) {
        this.x = coord.getX();
        this.y = coord.getY();
    }

    @Override
    public boolean equals(Object obj) {
        // If the object is compared with itself then return true
        if (obj == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(obj instanceof Point)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Point point = (Point) obj;

        // Compare the data members and return accordingly
        return this.x == point.getX()
                && this.y == point.getY();

    }

}
