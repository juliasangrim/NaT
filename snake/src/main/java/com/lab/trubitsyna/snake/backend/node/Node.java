package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;

public class Node {
    @Getter
    HashMap<SnakesProto.GameMessage.AnnouncementMsg, Long> availableGames;
    protected static final int PORT_UNICAST = 8000;
    @Getter
    private int seqNum;

    @Setter
    private SnakesProto.NodeRole nodeRole;

    public Node() {
        this.availableGames = null;
        this.seqNum = 0;
    }

    public void changeListAvailableServer(SnakesProto.GameMessage.AnnouncementMsg server) {
        //TODO: add check
        availableGames.put(server, System.currentTimeMillis());
    }


}
