package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.backend.handlers.MulticastSender;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.backend.protocol.SocketWrap;
import com.lab.trubitsyna.snake.model.CustomGameConfig;
import com.lab.trubitsyna.snake.model.GameModel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MasterNetNode implements INetHandler {
    private final Logger logger = LoggerFactory.getLogger("APP");
    private final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
    @Getter
    private long seqNum;
    @Getter
    @Setter
    private SnakesProto.GameMessage.AnnouncementMsg announcementMsg;

    private String name;
    private ArrayList<SnakesProto.GamePlayer> players;
    private MulticastSender multicastSender;
    private SnakesProto.NodeRole role;
    private SocketWrap socket;
    private SnakesProto.GameConfig config;
    private int myId;
    private int currId;
    private GameModel model;

    public MasterNetNode(CustomGameConfig config, SnakesProto.NodeRole role, GameModel model, int id) {
        this.name = config.getLogin();
        this.config = config.convertToProto();
        this.seqNum = 0;
        this.role = role;
        this.model = model;
        this.myId = id;
        this.currId = id;
        this.players = new ArrayList<>();
    }

    private synchronized void incrementSeqNum() {
        ++seqNum;
    }

    @Override
    public void sender(SnakesProto.GamePlayer player, SnakesProto.GameMessage message) {
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
            socket.send(message,  player.getIpAddress(), player.getPort());
    }

    @Override
    public void receiver() {
        var message = socket.receive();
        switch (message.getMessage().getTypeCase()) {
            case ACK -> {
            }
            case JOIN -> {
                var newPlayer = addNewPlayer(message.getMessage().getJoin().getName(),
                        message.getSenderAddr(), message.getPort());
                logger.info("Sending ack message on server ...");
                socket.send(getAckMessage(message.getMessage().getMsgSeq(), newPlayer.getId()),
                        newPlayer.getIpAddress(), newPlayer.getPort());
                logger.info("Send ack message on server successfully!");
            }
            case PING -> {}
            case ERROR -> {}
            case STEER -> {}
            case STATE -> {}
            case ROLE_CHANGE -> {}
            case ANNOUNCEMENT -> {}
        }
    }

    @Override
    public SnakesProto.GameMessage getJoinMessage(String name) {
        return null;
    }

    @Override
    public void start() {
        try {
            logger.info("Creation server unicast socket...");
            this.socket = new SocketWrap(new DatagramSocket());
            logger.info("Create server unicast socket successfully!");
        } catch (SocketException e) {
            e.printStackTrace();
        }
        var multicastSender = new MulticastSender(this, socket.getSocket(), MULTICAST_ADDR, MULTICAST_PORT);
        multicastSender.init();
        threadPool.scheduleAtFixedRate(multicastSender::run, 1, 1, TimeUnit.SECONDS);

        var mainPlayer = getMainPlayer();
        model.addNewPlayer(mainPlayer);
        players.add(mainPlayer);
        createAnnouncementMessage();
    }

    public void end() {
        threadPool.shutdown();
        logger.info("Thread pool for McSender shutdown!");
        socket.getSocket().close();
        logger.info("Close server unicast socket!");
    }

    public SnakesProto.GameMessage getAnnouncementMessage() {
        logger.info("Getting message ...");
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

    private SnakesProto.GamePlayer addNewPlayer(String name, String ipAddress, int port) {
        var player = SnakesProto.GamePlayer.newBuilder().setName(name)
                .setId(++currId).setIpAddress(ipAddress).setPort(port)
                .setRole(SnakesProto.NodeRole.NORMAL).setScore(0).build();
        players.add(player);
        model.addNewPlayer(player);
        return player;
    }

    private SnakesProto.GamePlayer getMainPlayer() {
        return SnakesProto.GamePlayer.newBuilder().setName(name)
                .setId(myId).setIpAddress("").setPort(socket.getSocket().getPort())
                .setRole(SnakesProto.NodeRole.MASTER).setScore(0).build();

    }
}
