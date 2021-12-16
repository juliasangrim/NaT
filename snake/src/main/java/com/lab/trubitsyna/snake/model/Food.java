package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.main.Main;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Random;

public class Food {
    @Getter
    private Point place;

    public Food(Point point) {
        this.place = point;
    }



}
