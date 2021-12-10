package com.lab.trubitsyna.snake.backend.protocol;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import lombok.Getter;

import java.net.InetAddress;

public class GameMessageWrap implements MsgWrap {
    @Getter
    private InetAddress senderAddr;
    @Getter
    private SnakesProto.GameMessage message;

    public GameMessageWrap(InetAddress senderAddr, SnakesProto.GameMessage message) {
        this.senderAddr = senderAddr;
        this.message = message;
    }

    @Override
    public void parseMessage() {
    }
}
