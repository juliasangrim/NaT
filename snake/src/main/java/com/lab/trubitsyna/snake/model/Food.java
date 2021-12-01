package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.main.Main;

import java.util.ArrayList;
import java.util.Random;

public class Food {
    private int amountFood;
    private double foodPerPlayer;
    ArrayList<Point> foodPoints;

    private final static Random generator = new Random();

    public Food(int amountFood, double foodPerPlayer, int amountAliveSnakes, Field field) {
        foodPoints = new ArrayList<>();
        for (int i = 0; i < amountFood + Math.round( foodPerPlayer * amountAliveSnakes) ; ++i) {
            Point foodPoint = new Point(generator.nextInt(field.width), generator.nextInt(field.height));
            if (field.isPointFree(foodPoint)) {
                foodPoints.add(foodPoint);
            }
            //nothing happen when the cell is full
            //TODO : try to fix

        }
    }
}
