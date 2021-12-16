package com.lab.trubitsyna.snake.view;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;

public interface IView {
    void render(StateSystem stateSystem, String message);
    void sendServerInfoToGameController(String message);
    void sendConfigToGameController(SnakesProto.GameConfig config);
}
