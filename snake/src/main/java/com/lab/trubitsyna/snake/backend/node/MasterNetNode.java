package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.backend.mcHandlers.MulticastSender;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.backend.protocol.GameMessageWrap;
import com.lab.trubitsyna.snake.backend.protocol.SocketWrap;
import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.model.CustomGameConfig;
import com.lab.trubitsyna.snake.model.GameModel;
import com.lab.trubitsyna.snake.view.StateSystem;
import lombok.Getter;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class MasterNetNode implements INetHandler {
    private final ScheduledExecutorService announcementThreadPool = Executors.newScheduledThreadPool(1);
    private final ExecutorService communicateThreadPool = Executors.newCachedThreadPool();

    @Getter
    private final ConcurrentHashMap<Integer, SnakesProto.GamePlayer> players;
    private final ConcurrentHashMap<Long, SnakesProto.GameMessage> sentMessages;
    private final ConcurrentHashMap<Integer, Long> lastReceivedAck;

    private final String name;
    private final GameModel model;
    private final SnakesProto.NodeRole role;
    private final SnakesProto.GameConfig config;
    private final int myId;

    private SnakesProto.GameMessage.AnnouncementMsg announcementMsg;
    private SocketWrap socket;
    private int currId;
    @Getter
    private long seqNum;
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
        this.lastReceivedAck = new ConcurrentHashMap<>();
    }

    private synchronized void incrementSeqNum() {
        ++seqNum;
    }

    @Override
    public void sender(SnakesProto.GamePlayer player, SnakesProto.GameMessage message) {
        if (socket == null) {
            MyLogger.getLogger().error("SOCKET IS NULL!!!");
            //TODO: gameException
            return;
        }
        switch (message.getTypeCase()) {
            case ACK, ERROR -> sendAckMessage(message, player.getIpAddress(), player.getPort());
            case PING -> {
            }
            case STEER -> sendSteerMessage(message);
            case STATE -> {
                if (player.getId() != myId) {
                    sendStateMessage(message,  player.getIpAddress(), player.getPort() );
                }
            }
            case ROLE_CHANGE -> {}
            default -> {
                //TODO GAME EXCEPTION
                MyLogger.getLogger().info("Message of unknown type");
            }
        }
    }


    private void sendAckMessage(SnakesProto.GameMessage message, String receiverIpAddr, int receiverPort) {
        socket.send(message, receiverIpAddr, receiverPort);
    }

    private void sendSteerMessage(SnakesProto.GameMessage message) {
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

    private void sendStateMessage(SnakesProto.GameMessage message,  String receiverIpAddr, int receiverPort) {
        sentMessages.put(message.getMsgSeq(), message);
        while (sentMessages.containsKey(message.getMsgSeq())) {
            MyLogger.getLogger().info("SIZE AFTER WHILE" + sentMessages.size());
            MyLogger.getLogger().info("Sending state server message to %s %s...");
            socket.send(message, receiverIpAddr, receiverPort);
            MyLogger.getLogger().info("Send state message server successfully! SeqNum : " + message.getMsgSeq());
            try {
                MyLogger.getLogger().info("SLEEP");
                Thread.sleep(config.getPingDelayMs());
                MyLogger.getLogger().info("AWAKE!");
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void receiver() throws GameException {
        if (socket == null) {
            throw new GameException("Socket is null.");
        }
        MyLogger.getLogger().info("Receiving message from client...");

        var message = socket.receive();

        if (message == null) {
            return;
        }

        var seqNumRecv = message.getMessage().getMsgSeq();
        MyLogger.getLogger().info("Receive message on server with seqNum " + message.getMessage().getMsgSeq());

        switch (message.getMessage().getTypeCase()) {
            case ACK -> {
                var sentMessage = sentMessages.get(seqNumRecv);
                switch (sentMessage.getTypeCase()) {
                    case STEER, STATE -> {
                        sentMessages.remove(seqNumRecv);
                        MyLogger.getLogger().info("SIZE AFTER RECEIVE" + sentMessages.size());
                        MyLogger.getLogger().info("Client get ack message with seqNum: " + seqNumRecv);
                    }
                    case ROLE_CHANGE -> {}
                    case PING -> {}
                }
            }
            case JOIN -> receiveJoinMessage(message);
            case PING -> {}
            case ERROR -> {}
            case STEER -> receiveSteerMessage(message);
            case STATE -> {}
            case ROLE_CHANGE -> {}
            case ANNOUNCEMENT -> {}
            default -> {
                //TODO GAME EXCEPTION
                MyLogger.getLogger().info("Message of unknown type");
            }
        }
    }

    private void receiveSteerMessage(GameMessageWrap message) {
        var steerMessage = message.getMessage();
        var currPlayer = players.get(steerMessage.getSenderId());
        model.changeSnakesDirection(currPlayer, steerMessage.getSteer().getDirection());
        MyLogger.getLogger().info("Sending ack message on server ...");
        sender(currPlayer, getAckMessage(message.getMessage().getMsgSeq(), currPlayer.getId()));
        MyLogger.getLogger().info("Send ack message on server successfully!");
    }

    private void receiveJoinMessage(GameMessageWrap message) {
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


    private void createMulticastSender() {
        var multicastSender = new MulticastSender(this, socket.getSocket(), MULTICAST_ADDR, MULTICAST_PORT);
        multicastSender.init();
        announcementThreadPool.scheduleAtFixedRate(multicastSender::run, 1, 1, TimeUnit.SECONDS);
    }

    private void openSocket() {
        try {
            this.socket = new SocketWrap(new DatagramSocket());
            socket.getSocket().setSoTimeout(500);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void startReceiver() {
        communicateThreadPool.submit(() -> {
            while (state == StateSystem.NEW_GAME) {
                MyLogger.getLogger().info("Starting receiver on server!");
                try {
                    receiver();
                } catch (GameException e) {
                    e.printStackTrace();
                }
            }
            socket.getSocket().close();
            MyLogger.getLogger().info("CLOSE SOCKET SUCCESS");
        });
    }

    private void startGame() {
        communicateThreadPool.submit(() -> {
            gameLoop();
            MyLogger.getLogger().info("FINISH THREADPOOL WITH STARTGAME AND UPDATEMODEL");
        });
    }

    private void gameLoop() {
        while (state == StateSystem.NEW_GAME) {
            System.out.println("");
            try {
                model.oneTurnGame();
            } catch (GameException e) {
                e.printStackTrace();
            }

            var stateMessage = getGameStateMessage();
            communicateThreadPool.submit(() -> {
                for (var player : players.values()) {
                    sender(player, stateMessage);
                }
            });

            try {
                Thread.sleep(config.getStateDelayMs());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void start() {
        openSocket();
        createMulticastSender();

        var mainPlayer = getMainPlayer();
        model.addNewPlayer(mainPlayer);
        players.put(mainPlayer.getId(), mainPlayer);
        createAnnouncementMessage();

        startReceiver();

        startGame();
    }

    private SnakesProto.GamePlayer getMainPlayer() {
        return SnakesProto.GamePlayer.newBuilder().setName(name)
                .setId(myId).setIpAddress("127.0.0.1").setPort(socket.getSocket().getLocalPort())
                .setRole(SnakesProto.NodeRole.MASTER).setScore(0).build();

    }

    public synchronized void end() {
        state = StateSystem.EXIT;
        announcementThreadPool.shutdown();
        communicateThreadPool.shutdown();
        MyLogger.getLogger().info("Close node!");
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

    private SnakesProto.GameMessage getGameStateMessage() {
        ArrayList<SnakesProto.GameState.Snake> protoSnakes = new ArrayList<>();
        ArrayList<SnakesProto.GameState.Coord> foodProto = new ArrayList<>();
        for (var snake : model.getPlayers().values()) {
            protoSnakes.add(snake.convertToProtoSnake());

        }

        for (var singleFood : model.getFood()) {
            var point = singleFood.getPlace();
            foodProto.add(point.convertPointToCoord());
        }
        var gamePLayers = SnakesProto.GamePlayers.newBuilder();
        for (var player : model.getPlayers().keySet()) {
            gamePLayers.addPlayers(SnakesProto.GamePlayer.newBuilder()
                    .setId(player.getId())
                    .setRole(player.getRole())
                    .setIpAddress(player.getIpAddress())
                    .setName(player.getName())
                    .setPort(player.getPort())
                    .setType(player.getType())
                    .setScore(model.getPlayers().get(player).getScore()));
        }

        var updatedPlayers = gamePLayers.build();


        var gameState = SnakesProto.GameState.newBuilder()
                .setStateOrder(model.getStateOrder())
                .setConfig(config)
                .addAllSnakes(protoSnakes)
                .setPlayers(updatedPlayers)
                .addAllFoods(foodProto)
                .build();
        var stateMessage = SnakesProto.GameMessage.StateMsg.newBuilder()
                .setState(gameState)
                .build();
        var gameMessage = SnakesProto.GameMessage.newBuilder()
                .setState(stateMessage)
                .setMsgSeq(seqNum)
                .build();
        incrementSeqNum();
        return gameMessage;
    }
}
