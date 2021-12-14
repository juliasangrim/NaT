package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.MyLogger;
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
    public static final int TIMEOUT_MS = 10000;

    //private final Logger logger = MyLogger.getLogger();
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
        if (socket == null) {
            MyLogger.getLogger().error("SOCKET IS NULL!!!");
            return;
        }
        switch (message.getTypeCase()) {
            case ACK -> {}
            case JOIN -> {
                sentMessages.put(message.getMsgSeq(), message);
                while (sentMessages.containsKey(message.getMsgSeq())) {
                    System.out.println("SIZE " + sentMessages.size());
                    MyLogger.getLogger().info(String.format("Sending join message to %s %s...", serverAddr, serverPort));
                    socket.send(message, serverAddr, serverPort);
                    MyLogger.getLogger().info("Send join message successfully! SeqNum : " + message.getMsgSeq());
                    try {
                        MyLogger.getLogger().info("SLEEP");
                        Thread.sleep(TIMEOUT_MS);
                        MyLogger.getLogger().info("AWAKE!");
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
        if (socket == null) {
            MyLogger.getLogger().error("SOCKET IS NULL!!!");
            return;
        }
        //TODO: socket timeout????
        MyLogger.getLogger().info("Receiving message on client ....");
        var receivedMessage = socket.receive();
        MyLogger.getLogger().info("Receive message on client successfully!");
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
                        sentMessages.remove(seqNumRecv);
                        System.out.println(sentMessages.size());
                        myId = receivedMessage.getMessage().getReceiverId();
                        MyLogger.getLogger().info("Client get ack message with seqNum: " + seqNumRecv);
                        Platform.runLater(()->gameView.render(StateSystem.ERROR_LOAD_GAME, "CONNECT!"));
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
                Platform.runLater(()->gameView.render(StateSystem.ERROR_LOAD_GAME, receivedMessage.getMessage().getError().getErrorMessage()));
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
            MyLogger.getLogger().info("Creation client unicast socket...");
            socket = new SocketWrap(new DatagramSocket());
            MyLogger.getLogger().info("Create client unicast socket successfully!");
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void end() {
        if (socket == null) {
            MyLogger.getLogger().error("SOCKET IS NULL!!!");
            return;
        }
        socket.getSocket().close();
        //close socket and smth
    }

    public void checkAliveServers() {
        //logger.info("Check available servers.");
        for (var game: availableGames.keySet()) {
            if (System.currentTimeMillis() - availableGames.get(game).getValue() > TIMEOUT_MS) {
                MyLogger.getLogger().info("Disconnect " + game.getKey() + " " + game.getValue());
                availableGames.remove(game);
                isMapChange = true;
            }
        }
    }

    public void changeListAvailableServer(SnakesProto.GameMessage.AnnouncementMsg server, int port, InetAddress ip) {
//        isMapChange =  availableGames.put(new Pair<>(port, ip), new Pair<>(server, System.currentTimeMillis())) == null;
        if (!availableGames.containsKey(new Pair<>(port, ip))) {
            availableGames.put(new Pair<>(port, ip), new Pair<>(server, System.currentTimeMillis()));
            MyLogger.getLogger().info("Connect " + port + " " + ip);
            isMapChange = true;
        } else {
            availableGames.put(new Pair<>(port, ip), new Pair<>(server, System.currentTimeMillis()));
            isMapChange = false;
        }
    }



    @Override
    public SnakesProto.GameMessage getJoinMessage(String name) {
        var joinMsg = SnakesProto.GameMessage.JoinMsg.newBuilder().setName(name).build();
        var sendMessage = SnakesProto.GameMessage.newBuilder().setJoin(joinMsg).setMsgSeq(seqNum).build();
        incrementSeqNum();
        return sendMessage;
    }
}
