package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.view.Tile;
import lombok.Getter;

public class Field {
    private final static int FIND_WINDOW_SIZE = 5;
    private final static int LOCAL_MIDDLE = (FIND_WINDOW_SIZE - 1) / 2;
    @Getter
    int width;
    @Getter
    int height;
    Tile[] tileArray;

    Field(int width, int height) {

        this.width = width;
        this.height = height;

        this.tileArray = new Tile[width * height];
    }

    private void initField() {
        for (int y = 0; y < this.height; ++y) {
            for (int x = 0; x < this.width; ++x) {
                this.tileArray[y * width + x] = Tile.BOARD;
            }
        }
    }

    private boolean isFree(Point begin, Point end) {
        for (int y = begin.getY(); y < end.getY(); ++y) {
            for (int x = begin.getX(); x < end.getX(); ++x) {
                if (tileArray[y * FIND_WINDOW_SIZE + x] != Tile.BOARD) {
                    return false;
                }
            }
        }
        return true;
    }

    public Point findEmptySpace() {
        //middle of the empty space
        Point middle = new Point(-1, -1);
        //coordinates for window
        Point beginWindow = new Point(-1, -1);
        Point endWindow = new Point(-1, -1);
        //loop for shifting window 5x5
        for (int yWindow = 0; yWindow < height - FIND_WINDOW_SIZE; ++yWindow) {
            for (int xWindow = 0; xWindow < width - FIND_WINDOW_SIZE; ++xWindow) {
                beginWindow.setX(xWindow);
                beginWindow.setY(yWindow);
                endWindow.setX(xWindow + FIND_WINDOW_SIZE);
                endWindow.setY(yWindow + FIND_WINDOW_SIZE);
                if (isFree(beginWindow, endWindow)) {
                    // find free window
                    middle.setX(beginWindow.getX() + LOCAL_MIDDLE);
                    middle.setY(beginWindow.getY() + LOCAL_MIDDLE);
                    return middle;
                }
            }
        }
        return middle;
    }

    public void setTile(Point point, Tile tile) {
        tileArray[point.getY() * width + point.getX()] = tile;
    }

    public boolean isPointFree(Point point) {
        return tileArray[point.getY() * width + point.getX()] == Tile.BOARD;
    }
}

