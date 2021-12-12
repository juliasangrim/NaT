package com.lab.trubitsyna.snake.backend.protocol;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import lombok.Getter;

import java.net.InetAddress;

public class GameMessageWrap {
    @Getter
    private String senderAddr;
    @Getter
    private int port;
    @Getter
    private SnakesProto.GameMessage message;

    @Getter
    private SnakesProto.GameMessage.AnnouncementMsg announcementMsg;



    public GameMessageWrap(SnakesProto.GameMessage message, String senderAddr, int port) {
        this.senderAddr = senderAddr;
        this.message = message;
        this.port = port;
    }


    public void parseMessage() {
    }
}
