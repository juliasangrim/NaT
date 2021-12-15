package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.backend.protocol.SocketWrap;
import com.lab.trubitsyna.snake.view.IView;
import com.lab.trubitsyna.snake.view.StateSystem;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetNode implements INetHandler {
    public static final int TIMEOUT_MS = 10000;
    public final ExecutorService communicationThreadPool = Executors.newCachedThreadPool();
    @Setter
    private IView gameView;
    @Getter
    private long seqNum;
    @Setter
    private SnakesProto.NodeRole nodeRole;
    private SocketWrap socket;
    private SnakesProto.NodeRole role;
    private ConcurrentHashMap<Long, SnakesProto.GameMessage> sentMessages = new ConcurrentHashMap<>();
    private SnakesProto.GameConfig config;
    private int myId;
    private String name;

    private StateSystem state = StateSystem.JOIN_GAME;


    private int serverPort;
    private String serverAddr;

    public NetNode(IView gameView, SnakesProto.GameConfig config, SnakesProto.NodeRole role, String serverAddr, int serverPort, String name) {
        this.config = config;
        this.seqNum = 0;
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.role = role;
        this.gameView = gameView;
        this.name = name;
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
            case ACK -> {
                socket.send(message, serverAddr, serverPort);
            }
            case JOIN -> {
                sentMessages.put(message.getMsgSeq(), message);
                while (sentMessages.containsKey(message.getMsgSeq())) {
                    MyLogger.getLogger().info("SIZE AFTER WHILE" + sentMessages.size());
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
            case STEER -> {
                sentMessages.put(message.getMsgSeq(), message);
                while (sentMessages.containsKey(message.getMsgSeq())) {
                    MyLogger.getLogger().info("SIZE AFTER WHILE" + sentMessages.size());
                    MyLogger.getLogger().info(String.format("Sending steer message to %s %s...", serverAddr, serverPort));
                    socket.send(message, serverAddr, serverPort);
                    MyLogger.getLogger().info("Send steer message successfully! SeqNum : " + message.getMsgSeq());
                    try {
                        MyLogger.getLogger().info("SLEEP");
                        Thread.sleep(config.getPingDelayMs());
                        MyLogger.getLogger().info("AWAKE!");
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            case STATE -> {}
            case ROLE_CHANGE -> {}
            case ANNOUNCEMENT -> {}
        }
    }

    @Override
    public void changeState(StateSystem state) {
        this.state = state;
    }

    @Override
    public SnakesProto.GameMessage getSteerMessage(SnakesProto.Direction direction) {
        var steerMessage = SnakesProto.GameMessage.SteerMsg.newBuilder()
                .setDirection(direction)
                .build();
        var gameMessage = SnakesProto.GameMessage.newBuilder()
                .setSteer(steerMessage)
                .setSenderId(myId)
                .setMsgSeq(seqNum)
                .build();
        incrementSeqNum();
        return gameMessage;
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
                        sentMessages.remove(seqNumRecv);
                        MyLogger.getLogger().info("SIZE AFTER RECEIVE" + sentMessages.size());
                        MyLogger.getLogger().info("Client get ack message with seqNum: " + seqNumRecv);
                    }
                    case ROLE_CHANGE -> {

                    }
                    case JOIN -> {
                        sentMessages.remove(seqNumRecv);
                        MyLogger.getLogger().info("SIZE AFTER RECEIVE" + sentMessages.size());
                        myId = receivedMessage.getMessage().getReceiverId();
                        MyLogger.getLogger().info("Client get ack message with seqNum: " + seqNumRecv);
                        Platform.runLater(()->gameView.render(StateSystem.ERROR_LOAD_GAME, "CONNECT!"));
                    }

                    case PING -> {

                    }
                    case STATE -> {

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
            communicationThreadPool.submit(this::receiver);
            communicationThreadPool.submit(() -> sender(null, getJoinMessage(name)));

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
        communicationThreadPool.shutdown();
        MyLogger.getLogger().info("Shutdown thread pool on client.");
        socket.getSocket().close();

        MyLogger.getLogger().info("Close socket on client.");
        //close socket and smth
    }

    public SnakesProto.GameMessage getJoinMessage(String name) {
        var joinMsg = SnakesProto.GameMessage.JoinMsg.newBuilder().setName(name).build();
        var sendMessage = SnakesProto.GameMessage.newBuilder().setJoin(joinMsg).setMsgSeq(seqNum).build();
        incrementSeqNum();
        return sendMessage;
    }
}
