package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.backend.protocol.SocketWrap;
import com.lab.trubitsyna.snake.model.GameModel;
import com.lab.trubitsyna.snake.model.Player;
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
    public static final int TIMEOUT_MS = 1000;

    private final ExecutorService communicationThreadPool = Executors.newCachedThreadPool();


    private final ConcurrentHashMap<Long, SnakesProto.GameMessage> sentMessages = new ConcurrentHashMap<>();
    private final SnakesProto.GameConfig config;

    private ServerInfo serverInfo;
    private Player me;
    @Setter
    private IView gameView;
    @Getter
    private long seqNum;
    private SocketWrap socket;

    private GameModel model;
    private StateSystem state = StateSystem.JOIN_GAME;
    private int serverPort;
    private String serverAddr;

    public NetNode(IView gameView, SnakesProto.GameConfig config,
                   String serverAddr, int serverPort, String name, GameModel model) {
        this.config = config;
        this.seqNum = 0;
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.gameView = gameView;
        this.me = new Player(name, -1, 0, "", SnakesProto.NodeRole.NORMAL);
        this.model = model;
        this.serverInfo = new ServerInfo(null, null, null);
    }

    public synchronized void incrementSeqNum() {
        ++seqNum;
    }

    @Override
    public void sender(Player player, SnakesProto.GameMessage message) {
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
                .setSenderId(me.getId())
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
        var receivedMessage = socket.receive();

        long seqNumRecv = receivedMessage.getMessage().getMsgSeq();
        switch (receivedMessage.getMessage().getTypeCase()) {
            case ACK -> {
                var sentMessage = sentMessages.get(seqNumRecv);
                switch (sentMessage.getTypeCase()) {
                    case STEER -> {
                        for (var messageSeqNum : sentMessages.keySet()) {
                            if (messageSeqNum <= seqNumRecv && sentMessages.get(messageSeqNum).getTypeCase() == SnakesProto.GameMessage.TypeCase.STEER) {
                                sentMessages.remove(messageSeqNum);
                            }
                        }
                        MyLogger.getLogger().info("SIZE AFTER RECEIVE" + sentMessages.size());
                        MyLogger.getLogger().info("Client get ack message with seqNum: " + seqNumRecv);
                    }
                    case ROLE_CHANGE -> {}
                    case JOIN -> {
                        sentMessages.remove(seqNumRecv);
                        MyLogger.getLogger().info("SIZE AFTER RECEIVE" + sentMessages.size());
                        me.setId(receivedMessage.getMessage().getReceiverId());
                        MyLogger.getLogger().info("Client get ack message with seqNum: " + seqNumRecv);
                        //Platform.runLater(()->gameView.render(StateSystem.ERROR_LOAD_GAME, "CONNECT!"));
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
                var stateMessage = receivedMessage.getMessage().getState();
                sender(null, getAckMessage(receivedMessage.getMessage().getMsgSeq(), receivedMessage.getMessage().getReceiverId()));
                if (model.getConfig() == null) {
                    model.addConfig(stateMessage.getState().getConfig());
                }
                if (stateMessage.getState().getStateOrder() > model.getStateOrder()) {
                    model.updateClientModel(receivedMessage.getMessage().getState().getState());
                    updateServerInfo();
                }
            }
            case ROLE_CHANGE -> {
                var roleChangeMessage = receivedMessage.getMessage().getRoleChange();
                me.setRole(roleChangeMessage.getReceiverRole());
                MyLogger.getLogger().info(me.getRole().toString());
                sender(null, getAckMessage(receivedMessage.getMessage().getMsgSeq(), receivedMessage.getMessage().getReceiverId()));
                //if for master if you deputy
            }
        }



    }

    private void updateServerInfo() {
        serverInfo.setConfig(model.getConfig());
        serverInfo.setSnakes(model.getSnakes());
        serverInfo.setPlayers(model.getPlayers());
    }

    @Override
    public void openSocket() {
        MyLogger.getLogger().info("Creation client unicast socket...");
        try {
            socket = new SocketWrap(new DatagramSocket());
            socket.getSocket().setSoTimeout(TIMEOUT_MS);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        MyLogger.getLogger().info("Create client unicast socket successfully!");
    }

    @Override
    public void startReceiver() {
        communicationThreadPool.submit(() -> {
            while (state == StateSystem.JOIN_GAME) {
                receiver();
            }
            socket.getSocket().close();
        });
    }

    private void sendFirstJoin() {
        communicationThreadPool.submit(() -> sender(null, getJoinMessage(me.getName())));
    }

    @Override
    public void start() {
            me.setPort(socket.getSocket().getLocalPort());
            startReceiver();
            if (me.getRole() == SnakesProto.NodeRole.NORMAL) {
                sendFirstJoin();
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
        return SnakesProto.GameMessage.newBuilder().setAck(ackMessage).setMsgSeq(msgSeq).setSenderId(me.getId()).setReceiverId(receiverId).build();
    }
}
