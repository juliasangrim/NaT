package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.backend.protocol.SocketWrap;
import com.lab.trubitsyna.snake.model.GameModel;
import com.lab.trubitsyna.snake.model.Player;
import com.lab.trubitsyna.snake.model.Snake;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ServerInfo {

    @Getter@Setter
    private ConcurrentHashMap<Integer, Player> players;

    @Getter@Setter
    private ConcurrentHashMap <Integer, Snake> snakes;

    @Getter@Setter
    private ConcurrentHashMap<Long, SnakesProto.GameMessage> sentMessages;
    @Getter@Setter
    private SnakesProto.GameConfig config;
    @Getter@Setter
    private int currId;


    public ServerInfo(SnakesProto.GameConfig config, ConcurrentHashMap<Integer, Player> players,
                      ConcurrentHashMap<Integer, Snake> snakes) {
        this.config = config;
        this.players = players;
        this.currId = 0;
        this.snakes = snakes;
    }
}
