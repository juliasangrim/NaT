package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.model.Player;

public interface INetHandler {
    int MULTICAST_PORT = 9192;
    String MULTICAST_ADDR = "239.192.0.4";

    void start();
    void end();
    void sender(Player player, SnakesProto.GameMessage message) ;
    void receiver() throws GameException;
    SnakesProto.GameMessage getSteerMessage(SnakesProto.Direction direction);
    void openSocket();
    void startReceiver();

}
