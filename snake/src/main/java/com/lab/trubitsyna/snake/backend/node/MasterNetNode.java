package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.backend.handlers.MulticastSender;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.backend.protocol.SocketWrap;
import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.model.CustomGameConfig;
import com.lab.trubitsyna.snake.model.GameModel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MasterNetNode implements INetHandler {
    //private final Logger logger = LoggerFactory.getLogger("APP");
    private final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
    @Getter
    private long seqNum;
    @Getter
    @Setter
    private SnakesProto.GameMessage.AnnouncementMsg announcementMsg;

    private String name;
    @Getter
    private CopyOnWriteArrayList<SnakesProto.GamePlayer> players;
    private MulticastSender multicastSender;
    private SnakesProto.NodeRole role;
    private SocketWrap socket;
    private SnakesProto.GameConfig config;
    private int myId;
    private int currId;
    private GameModel model;
    private boolean isError;

    public MasterNetNode(CustomGameConfig config, SnakesProto.NodeRole role, GameModel model, int id) {
        this.name = config.getLogin();
        this.config = config.convertToProto();
        this.seqNum = 0;
        this.role = role;
        this.model = model;
        this.myId = id;
        this.currId = id;
        this.players = new CopyOnWriteArrayList<>();
        this.isError = false;
    }

    private synchronized void incrementSeqNum() {
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
            }
            case PING -> {
            }
            case ERROR -> {
            }
            case STEER -> {
            }
            case STATE -> {
            }
            case ROLE_CHANGE -> {
            }
            case ANNOUNCEMENT -> {
            }
        }
        socket.send(message, player.getIpAddress(), player.getPort());
    }

    @Override
    public void receiver() {
        if (socket == null) {
            MyLogger.getLogger().error("SOCKET IS NULL!!!");
            return;
        }
        MyLogger.getLogger().info("Receiving message from client...");
        var message = socket.receive();
        if (message == null) {
            MyLogger.getLogger().info("Message is null");
            return;
        }
        MyLogger.getLogger().info("Receive message on server with seqNum " + message.getMessage().getMsgSeq());
        switch (message.getMessage().getTypeCase()) {
            case ACK -> {
            }
            case JOIN -> {
                var newPlayer = addNewPlayer(message.getMessage().getJoin().getName(),
                        message.getSenderAddr(), message.getPort());
                //can add player
                if (!isError) {
                    MyLogger.getLogger().info("Sending ack message on server ...");
                    socket.send(getAckMessage(message.getMessage().getMsgSeq(), newPlayer.getId()),
                            newPlayer.getIpAddress(), newPlayer.getPort());
                    MyLogger.getLogger().info("Send ack message on server successfully!");
                    model.updateModel();

                } else {
                    //can't add player
                    MyLogger.getLogger().info("Sending error message on server ...");
                    socket.send(getErrorMessage(message.getMessage().getMsgSeq(), "SORRY, BUT NUMBER OF PLAYERS EXCEEDED. PLEASE CONNECT LATER...."),
                            newPlayer.getIpAddress(), newPlayer.getPort());
                    MyLogger.getLogger().info("Send error message on server successfully!");
                }
            }
            case PING -> {
            }
            case ERROR -> {
            }
            case STEER -> {
            }
            case STATE -> {
            }
            case ROLE_CHANGE -> {
            }
            case ANNOUNCEMENT -> {
            }
            default -> {
                MyLogger.getLogger().info("Message of unknown type");
            }
        }
    }

    @Override
    public SnakesProto.GameMessage getJoinMessage(String name) {
        return null;
    }

    @Override
    public void start() {
        try {
            MyLogger.getLogger().info("Creation server unicast socket...");
            this.socket = new SocketWrap(new DatagramSocket());
            socket.getSocket().setSoTimeout(500);
            MyLogger.getLogger().info(String.format("Create server unicast socket successfully! Port=%d, Address=%s", socket.getSocket().getLocalPort(), socket.getSocket().getInetAddress()));
        } catch (SocketException e) {
            e.printStackTrace();
        }
        var multicastSender = new MulticastSender(this, socket.getSocket(), MULTICAST_ADDR, MULTICAST_PORT);
        multicastSender.init();
        threadPool.scheduleAtFixedRate(multicastSender::run, 1, 1, TimeUnit.SECONDS);

        var mainPlayer = getMainPlayer();
        model.addNewPlayer(mainPlayer);
        players.add(mainPlayer);
        model.updateModel();
        createAnnouncementMessage();
    }

    public void end() {
        threadPool.shutdown();
        MyLogger.getLogger().info("Thread pool for McSender shutdown!");
        socket.getSocket().close();
        MyLogger.getLogger().info("Close server unicast socket!");
    }

    public SnakesProto.GameMessage getAnnouncementMessage() {
        MyLogger.getLogger().info("Getting message ...");
        var sendMessage = SnakesProto.GameMessage.newBuilder().setAnnouncement(announcementMsg).setMsgSeq(seqNum).build();
        incrementSeqNum();
        return sendMessage;
    }


    public void createAnnouncementMessage() {
        SnakesProto.GamePlayers clients = SnakesProto.GamePlayers.newBuilder().addAllPlayers(players).build();
        announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder().
                setPlayers(clients).setConfig(config).build();
    }

    public SnakesProto.GameMessage getAckMessage(long msgSeq, int receiverId) {
        var ackMessage = SnakesProto.GameMessage.AckMsg.newBuilder().build();
        return SnakesProto.GameMessage.newBuilder().setAck(ackMessage).setMsgSeq(msgSeq).setSenderId(myId).setReceiverId(receiverId).build();
    }

    public SnakesProto.GameMessage getErrorMessage(long msgSeq, String error) {
        var errorMsg = SnakesProto.GameMessage.ErrorMsg.newBuilder().setErrorMessage(error).build();
        return SnakesProto.GameMessage.newBuilder().setError(errorMsg).setMsgSeq(msgSeq).setSenderId(myId).build();
    }

    private SnakesProto.GamePlayer addNewPlayer(String name, String ipAddress, int port) {
        var player = SnakesProto.GamePlayer.newBuilder().setName(name)
                .setId(++currId).setIpAddress(ipAddress).setPort(port)
                .setRole(SnakesProto.NodeRole.NORMAL).setScore(0).build();
        if (model.addNewPlayer(player)) {
            players.add(player);
            isError = false;
        } else {
            isError = true;
        }
        return player;
    }

    private SnakesProto.GamePlayer getMainPlayer() {
        return SnakesProto.GamePlayer.newBuilder().setName(name)
                .setId(myId).setIpAddress("").setPort(socket.getSocket().getPort())
                .setRole(SnakesProto.NodeRole.MASTER).setScore(0).build();

    }
}
