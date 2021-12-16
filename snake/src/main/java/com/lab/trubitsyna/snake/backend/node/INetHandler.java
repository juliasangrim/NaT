package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.gameException.GameException;

public interface INetHandler {
    int MULTICAST_PORT = 9192;
    String MULTICAST_ADDR = "239.192.0.4";

    void start();
    void end();
    void sender(SnakesProto.GamePlayer player, SnakesProto.GameMessage message) ;
    void receiver() throws GameException;
    SnakesProto.GameMessage getSteerMessage(SnakesProto.Direction direction);


}
