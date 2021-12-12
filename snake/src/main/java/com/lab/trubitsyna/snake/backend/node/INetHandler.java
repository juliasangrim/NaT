package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;

import java.util.ArrayList;

public interface INetHandler {
    int MULTICAST_PORT = 9192;
    String MULTICAST_ADDR = "239.192.0.4";
    void start();
    void end();
    void sender(SnakesProto.GamePlayer player, SnakesProto.GameMessage message);
    void receiver();
    SnakesProto.GameMessage getJoinMessage(String name);

}
