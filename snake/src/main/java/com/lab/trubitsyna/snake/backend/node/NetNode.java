package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.backend.protocol.SocketWrap;
import com.lab.trubitsyna.snake.model.CustomGameConfig;
import com.lab.trubitsyna.snake.view.IView;
import com.lab.trubitsyna.snake.view.StateSystem;
import javafx.application.Platform;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class NetNode implements INetHandler {
    public static final int TIMEOUT_MS = 2000;

    private final Logger logger = LoggerFactory.getLogger("APP");
    //sorry for that
    @Setter
    private IView gameView;
    @Getter
    private ConcurrentHashMap<Pair<Integer, InetAddress>, Pair<SnakesProto.GameMessage.AnnouncementMsg, Long>> availableGames;
    @Getter
    private boolean isMapChange = true;
    @Getter
    private long seqNum;
    @Setter
    private SnakesProto.NodeRole nodeRole;
    private SocketWrap socket;
    private SnakesProto.NodeRole role;
    private ConcurrentHashMap<Long, SnakesProto.GameMessage> sentMessages = new ConcurrentHashMap<>();
    private SnakesProto.GameConfig config;
    private int myId;

    private int serverPort;
    private String serverAddr;

    public NetNode() {
        this.availableGames = new ConcurrentHashMap<>();
    }
    public NetNode(IView gameView, SnakesProto.GameConfig config, SnakesProto.NodeRole role, String serverAddr, int serverPort) {
        this.config = config;
        this.seqNum = 0;
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.role = role;
        this.gameView = gameView;
    }

    public synchronized void incrementSeqNum() {
        ++seqNum;
    }

    @Override
    public void sender(SnakesProto.GamePlayer player, SnakesProto.GameMessage message) {
        switch (message.getTypeCase()) {
            case ACK -> {}
            case JOIN -> {
                sentMessages.put(message.getMsgSeq(), message);
                while (sentMessages.containsKey(message.getMsgSeq())) {
                    logger.info(String.format("Sending join message to %s %s...", serverAddr, serverPort));
                    socket.send(message, serverAddr, serverPort);
                    logger.info("Send join message successfully!");
                    try {
                        Thread.sleep(config.getPingDelayMs());
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            case PING -> {}
            case ERROR -> {}
            case STEER -> {}
            case STATE -> {}
            case ROLE_CHANGE -> {}
            case ANNOUNCEMENT -> {}
        }
        socket.send(message, serverAddr, serverPort);
    }

    @Override
    public void receiver() {
        //TODO: socket timeout????
        logger.info("Receiving message on client ....");
        var receivedMessage = socket.receive();
        logger.info("Receive message on client successfully!");
        long seqNumRecv = receivedMessage.getMessage().getMsgSeq();
        switch (receivedMessage.getMessage().getTypeCase()) {
            case ACK -> {
                var sentMessage = sentMessages.get(seqNumRecv);
                switch (sentMessage.getTypeCase()) {
                    case STEER -> {

                    }
                    case ROLE_CHANGE -> {

                    }
                    case JOIN -> {
                        myId = receivedMessage.getMessage().getReceiverId();
                        sentMessages.remove(seqNumRecv);
                        Platform.runLater(()->gameView.render(StateSystem.ERROR_LOAD_GAME));
                    }

                    case PING -> {

                    }
                }
            }
            case JOIN -> {

            }
            case PING -> {}
            case ERROR -> {
                sentMessages.remove(receivedMessage.getMessage().getMsgSeq());
                Platform.runLater(()->gameView.render(StateSystem.ERROR_LOAD_GAME));
            }
            case STEER -> {

            }
            case STATE -> {}
            case ROLE_CHANGE -> {}
            case ANNOUNCEMENT -> {}
        }
    }

    @Override
    public void start() {
        try {
            logger.info("Creation client unicast socket...");
            socket = new SocketWrap(new DatagramSocket());
            logger.info("Create client unicast socket successfully!");
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void end() {
        socket.getSocket().close();
        //close socket and smth
    }

    public void checkAliveServers() {
        logger.info("Check available servers.");
        for (var game: availableGames.keySet()) {
            if (System.currentTimeMillis() - availableGames.get(game).getValue() > TIMEOUT_MS) {
                availableGames.remove(game);
                isMapChange = true;
            }
        }
    }

    public void changeListAvailableServer(SnakesProto.GameMessage.AnnouncementMsg server, int port, InetAddress ip) {
        isMapChange = availableGames.put(new Pair<>(port, ip), new Pair<>(server, System.currentTimeMillis())) == null;
    }



    @Override
    public SnakesProto.GameMessage getJoinMessage(String name) {
        var joinMsg = SnakesProto.GameMessage.JoinMsg.newBuilder().setName(name).build();
        var sendMessage = SnakesProto.GameMessage.newBuilder().setJoin(joinMsg).setMsgSeq(seqNum).build();
        incrementSeqNum();
        return sendMessage;
    }
}
