package com.lab.trubitsyna.snake.backend.protocol;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import lombok.Getter;

import java.net.InetAddress;

public class GameMessageWrap {
    @Getter
    private final String senderAddr;
    @Getter
    private final int port;
    @Getter
    private final SnakesProto.GameMessage message;




    public GameMessageWrap(SnakesProto.GameMessage message, String senderAddr, int port) {
        this.senderAddr = senderAddr;
        this.message = message;
        this.port = port;
    }

}
