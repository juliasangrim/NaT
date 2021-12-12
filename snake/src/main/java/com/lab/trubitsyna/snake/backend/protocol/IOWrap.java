package com.lab.trubitsyna.snake.backend.protocol;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;

public interface IOWrap {


    void send(SnakesProto.GameMessage message, String receiver, int receiverPort);
    GameMessageWrap receive();
}
