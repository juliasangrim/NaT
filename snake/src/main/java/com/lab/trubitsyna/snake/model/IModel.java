package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.view.IView;

public interface IModel {
    void oneTurnGame() throws GameException;
    void updateModel() throws GameException;
    boolean addNewPlayer(Player player);
    void notifyListeners() throws GameException;
    void addListener(IListener listener) throws GameException;
    void removeListener(IListener listener);
}
