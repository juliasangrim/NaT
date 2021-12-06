package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.view.IView;

public interface IModel {
    void startGame() throws GameException;
    void updateModel() throws GameException;
    void notifyListeners() throws GameException;
    void addListener(IListener listener) throws GameException;
    void removeListener(IListener listener);
}
