package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.backend.mcHandlers.MulticastSender;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.backend.protocol.SocketWrap;
import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.model.CustomGameConfig;
import com.lab.trubitsyna.snake.model.GameModel;
import com.lab.trubitsyna.snake.view.StateSystem;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.FileSystems;
import java.util.concurrent.*;

public class MasterNetNode implements INetHandler {
    //private final Logger logger = LoggerFactory.getLogger("APP");
    private final ScheduledExecutorService announcementThreadPool = Executors.newScheduledThreadPool(1);
    private final ExecutorService communicateThreadPool = Executors.newCachedThreadPool();


    private ConcurrentHashMap<Long, SnakesProto.GameMessage> sentMessages = new ConcurrentHashMap<>();


    @Getter
    private long seqNum;
    @Getter
    @Setter
    private SnakesProto.GameMessage.AnnouncementMsg announcementMsg;

    private String name;
    @Getter
    private ConcurrentHashMap<Integer, SnakesProto.GamePlayer> players;
    private SnakesProto.NodeRole role;
    private SocketWrap socket;
    private SnakesProto.GameConfig config;
    private int myId;
    private int currId;
    private GameModel model;
    private boolean isError;

    private StateSystem state = StateSystem.NEW_GAME;


    public MasterNetNode(CustomGameConfig config, SnakesProto.NodeRole role, GameModel model, int id) {
        this.name = config.getLogin();
        this.config = config.convertToProto();
        this.seqNum = 0;
        this.role = role;
        this.model = model;
        this.myId = id;
        this.currId = id;
        this.players = new ConcurrentHashMap<>();
        this.isError = false;
        this.sentMessages = new ConcurrentHashMap<>();
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
            case ACK, ERROR -> {
                socket.send(message, player.getIpAddress(), player.getPort());
            }
            case PING -> {
            }
            case STEER -> {
                sentMessages.put(message.getMsgSeq(), message);
                while (sentMessages.containsKey(message.getMsgSeq())) {
                    MyLogger.getLogger().info("SIZE AFTER WHILE" + sentMessages.size());
                    MyLogger.getLogger().info("Sending steer server message to %s %s...");
                    socket.send(message, players.get(myId).getIpAddress(), players.get(myId).getPort());
                    MyLogger.getLogger().info("Send steer message server successfully! SeqNum : " + message.getMsgSeq());
                    try {
                        MyLogger.getLogger().info("SLEEP");
                        Thread.sleep(config.getPingDelayMs());
                        MyLogger.getLogger().info("AWAKE!");
                    } catch (InterruptedException ignored) {
                    }
                }

            }
            case STATE -> {

            }
            case ROLE_CHANGE -> {
            }
            case ANNOUNCEMENT -> {
            }
        }
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
        var seqNumRecv = message.getMessage().getMsgSeq();
        MyLogger.getLogger().info("Receive message on server with seqNum " + message.getMessage().getMsgSeq());
        switch (message.getMessage().getTypeCase()) {
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
                    case PING -> {

                    }
                }
            }
            case JOIN -> {
                var newPlayer = addNewPlayer(message.getMessage().getJoin().getName(),
                        message.getSenderAddr(), message.getPort());
                //can add player
                if (!isError) {
                    MyLogger.getLogger().info("Sending ack message on server ...");
                    sender(newPlayer, getAckMessage(message.getMessage().getMsgSeq(), newPlayer.getId()));
                    MyLogger.getLogger().info("Send ack message on server successfully!");
                    model.updateModel();

                } else {
                    //can't add player
                    MyLogger.getLogger().info("Sending error message on server ...");
                    sender(newPlayer, getErrorMessage(message.getMessage().getMsgSeq(), "SORRY, BUT NUMBER OF PLAYERS EXCEEDED. PLEASE CONNECT LATER...."));
                    MyLogger.getLogger().info("Send error message on server successfully!");
                }
            }
            case PING -> {
            }
            case ERROR -> {
            }
            case STEER -> {
                var steerMessage = message.getMessage();
                var currPlayer = players.get(steerMessage.getSenderId());
                model.changeSnakesDirection(currPlayer, steerMessage.getSteer().getDirection());
                MyLogger.getLogger().info("Sending ack message on server ...");
                sender(currPlayer, getAckMessage(message.getMessage().getMsgSeq(), currPlayer.getId()));
                MyLogger.getLogger().info("Send ack message on server successfully!");

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
    public void changeState(StateSystem state) {
        this.state = state;
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
        announcementThreadPool.scheduleAtFixedRate(multicastSender::run, 1, 1, TimeUnit.SECONDS);
        var mainPlayer = getMainPlayer();
        model.addNewPlayer(mainPlayer);
        players.put(mainPlayer.getId(), mainPlayer);
        createAnnouncementMessage();

        MyLogger.getLogger().info("Make thread for game!!");
        communicateThreadPool.submit(() -> {
            while (state == StateSystem.NEW_GAME) {
                MyLogger.getLogger().info("Starting receiver on server!");
                receiver();
            }
            socket.getSocket().close();
            MyLogger.getLogger().info("CLOSE SOCKET SUCCESS");
        });
        communicateThreadPool.submit(() -> {
            try {
                while (state == StateSystem.NEW_GAME) {
                    model.oneTurnGame();
                }

                MyLogger.getLogger().info("FINISH THREADPOOL WITH STARTGAME AND UPDATEMODEL");
            } catch (GameException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized void end() {
        state = StateSystem.EXIT;
        announcementThreadPool.shutdown();
        MyLogger.getLogger().info("Thread pool for McSender shutdown!");
        communicateThreadPool.shutdown();
        MyLogger.getLogger().info("Thread pool for communication shutdown!");
        MyLogger.getLogger().info("Close server unicast socket!");
    }

    public SnakesProto.GameMessage getAnnouncementMessage() {
        MyLogger.getLogger().info("Getting message ...");
        var sendMessage = SnakesProto.GameMessage.newBuilder().setAnnouncement(announcementMsg).setMsgSeq(seqNum).build();
        incrementSeqNum();
        return sendMessage;
    }


    private void createAnnouncementMessage() {
        SnakesProto.GamePlayers clients = SnakesProto.GamePlayers.newBuilder().addAllPlayers(players.values()).build();
        announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder().
                setPlayers(clients).setConfig(config).build();
    }



    private SnakesProto.GameMessage getAckMessage(long msgSeq, int receiverId) {
        var ackMessage = SnakesProto.GameMessage.AckMsg.newBuilder().build();
        return SnakesProto.GameMessage.newBuilder().setAck(ackMessage).setMsgSeq(msgSeq).setSenderId(myId).setReceiverId(receiverId).build();
    }

    private SnakesProto.GameMessage getErrorMessage(long msgSeq, String error) {
        var errorMsg = SnakesProto.GameMessage.ErrorMsg.newBuilder().setErrorMessage(error).build();
        return SnakesProto.GameMessage.newBuilder().setError(errorMsg).setMsgSeq(msgSeq).setSenderId(myId).build();
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

    private SnakesProto.GamePlayer addNewPlayer(String name, String ipAddress, int port) {
        var player = SnakesProto.GamePlayer.newBuilder().setName(name)
                .setId(++currId).setIpAddress(ipAddress).setPort(port)
                .setRole(SnakesProto.NodeRole.NORMAL).setScore(0).build();
        if (model.addNewPlayer(player)) {
            players.put(player.getId(), player);
            createAnnouncementMessage();
            isError = false;
        } else {
            isError = true;
        }
        return player;
    }

    private SnakesProto.GamePlayer getMainPlayer() {
        return SnakesProto.GamePlayer.newBuilder().setName(name)
                .setId(myId).setIpAddress("127.0.0.1").setPort(socket.getSocket().getLocalPort())
                .setRole(SnakesProto.NodeRole.MASTER).setScore(0).build();

    }
}
