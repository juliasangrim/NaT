package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.backend.protocol.SocketWrap;
import com.lab.trubitsyna.snake.model.GameModel;
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

    private final ExecutorService communicationThreadPool = Executors.newCachedThreadPool();

    private final ConcurrentHashMap<Long, SnakesProto.GameMessage> sentMessages = new ConcurrentHashMap<>();
    private final SnakesProto.GameConfig config;
    private final String name;

    @Setter
    private IView gameView;
    @Getter
    private long seqNum;
    @Setter
    private SnakesProto.NodeRole nodeRole;
    private SocketWrap socket;
    private SnakesProto.NodeRole role;
    private int myId;
    private int lastReceivedSeqNum;
    private GameModel model;
    private StateSystem state = StateSystem.JOIN_GAME;

    private int serverPort;
    private String serverAddr;

    public NetNode(IView gameView, SnakesProto.GameConfig config, SnakesProto.NodeRole role,
                   String serverAddr, int serverPort, String name, GameModel model) {
        this.config = config;
        this.seqNum = 0;
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.role = role;
        this.gameView = gameView;
        this.name = name;
        this.lastReceivedSeqNum = 0;
        this.model = model;
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
            case JOIN -> sendJoinMessage(message);
            case PING -> {}
            case STEER -> sendSteerMessage(message);
            case ROLE_CHANGE -> {}
        }
    }

    private void sendJoinMessage(SnakesProto.GameMessage message) {
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

    private void sendSteerMessage(SnakesProto.GameMessage message) {
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
                    case ROLE_CHANGE -> {}
                    case JOIN -> {
                        sentMessages.remove(seqNumRecv);
                        MyLogger.getLogger().info("SIZE AFTER RECEIVE" + sentMessages.size());
                        myId = receivedMessage.getMessage().getReceiverId();
                        MyLogger.getLogger().info("Client get ack message with seqNum: " + seqNumRecv);
                        Platform.runLater(()->gameView.render(StateSystem.ERROR_LOAD_GAME, "CONNECT!"));
                    }

                    case PING -> {}
                }
            }
            case JOIN -> {}
            case PING -> {}
            case ERROR -> {
                sentMessages.remove(receivedMessage.getMessage().getMsgSeq());
                Platform.runLater(()->gameView.render(StateSystem.ERROR_LOAD_GAME, receivedMessage.getMessage().getError().getErrorMessage()));
            }
            case STATE -> {
                MyLogger.getLogger().info("Get STATE MESSAGE ... ");
                var stateMessage = receivedMessage.getMessage().getState();
                sender(null, getAckMessage(receivedMessage.getMessage().getMsgSeq(), receivedMessage.getMessage().getReceiverId()));
                model.addConfig(stateMessage.getState().getConfig());
                MyLogger.getLogger().info("FILL");
                if (stateMessage.getState().getStateOrder() > model.getStateOrder()) {
                    MyLogger.getLogger().info("STATE ORDER IS COOL");
                    model.updateClientModel(receivedMessage.getMessage().getState().getState());
                }
            }
            case ROLE_CHANGE -> {}
        }
    }

    private void openSocket() {
        MyLogger.getLogger().info("Creation client unicast socket...");
        try {
            socket = new SocketWrap(new DatagramSocket());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        MyLogger.getLogger().info("Create client unicast socket successfully!");
    }

    private void startReceiver() {
        communicationThreadPool.submit(() -> {
            while (state == StateSystem.JOIN_GAME) {
                receiver();
            }
            socket.getSocket().close();
        });
    }

    private void sendFirstJoin() {
        communicationThreadPool.submit(() -> sender(null, getJoinMessage(name)));
    }

    @Override
    public void start() {
            openSocket();
            startReceiver();
            sendFirstJoin();
    }

    @Override
    public void end() {
        if (socket == null) {
            MyLogger.getLogger().error("SOCKET IS NULL!!!");
            return;
        }
        communicationThreadPool.shutdown();
        MyLogger.getLogger().info("Shutdown thread pool on client.");

        MyLogger.getLogger().info("Close socket on client.");
        //close socket and smth
    }

    public SnakesProto.GameMessage getJoinMessage(String name) {
        var joinMsg = SnakesProto.GameMessage.JoinMsg.newBuilder().setName(name).build();
        var sendMessage = SnakesProto.GameMessage.newBuilder().setJoin(joinMsg).setMsgSeq(seqNum).build();
        incrementSeqNum();
        return sendMessage;
    }

    private SnakesProto.GameMessage getAckMessage(long msgSeq, int receiverId) {
        var ackMessage = SnakesProto.GameMessage.AckMsg.newBuilder().build();
        return SnakesProto.GameMessage.newBuilder().setAck(ackMessage).setMsgSeq(msgSeq).setSenderId(myId).setReceiverId(receiverId).build();
    }
}
