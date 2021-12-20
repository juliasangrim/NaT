package com.lab.trubitsyna.snake.backend.node;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.backend.mcHandlers.MulticastSender;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.backend.protocol.GameMessageWrap;
import com.lab.trubitsyna.snake.backend.protocol.SocketWrap;
import com.lab.trubitsyna.snake.gameException.GameException;
import com.lab.trubitsyna.snake.model.CustomGameConfig;
import com.lab.trubitsyna.snake.model.GameModel;
import com.lab.trubitsyna.snake.model.Player;
import com.lab.trubitsyna.snake.view.StateSystem;
import lombok.Getter;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

public class MasterNetNode implements INetHandler {
    private final ScheduledExecutorService announcementThreadPool = Executors.newScheduledThreadPool(1);
    private final ExecutorService communicateThreadPool = Executors.newCachedThreadPool();

    @Getter
    private final ConcurrentHashMap<Integer, Player> players;
    private final ConcurrentHashMap<Long, SnakesProto.GameMessage> sentMessages;
    private final ConcurrentHashMap<Integer, Long> lastReceivedSteer;
    private ConcurrentHashMap<Integer, Integer> lastMessageFromPlayer;
    private Player currDeputy;

    private final GameModel model;
    private final SnakesProto.GameConfig config;

    private Player master;
    private SnakesProto.GameMessage.AnnouncementMsg announcementMsg;
    private SocketWrap socket;
    private int currId;
    @Getter
    private long seqNum;
    private boolean isError;


    private StateSystem state = StateSystem.NEW_GAME;


    public MasterNetNode(CustomGameConfig config, GameModel model) {
        this.config = config.convertToProto();
        this.seqNum = 0;
        this.model = model;
        this.currDeputy = null;
        this.master = new Player(config.getLogin(), 0, 0, "127.0.0.1", SnakesProto.NodeRole.MASTER);
        this.model.addNewPlayer(master);
        this.currId = 0;
        this.players = new ConcurrentHashMap<>();
        players.put(master.getId(), master);
        this.isError = false;
        this.sentMessages = new ConcurrentHashMap<>();
        this.lastReceivedSteer = new ConcurrentHashMap<>();
        this.lastMessageFromPlayer = new ConcurrentHashMap<>();
    }

    public MasterNetNode(ServerInfo info, Player player, GameModel model, long seqNum) {
        this.config = info.getConfig();
        this.seqNum = seqNum;
        this.model = model;
        this.master = player;
        this.model.addNewPlayer(master);
        this.currId = info.getCurrId();
        this.players = info.getPlayers();
        this.isError = false;
        this.sentMessages = info.getSentMessages();
        this.lastReceivedSteer = new ConcurrentHashMap<>();
        this.lastMessageFromPlayer = new ConcurrentHashMap<>();
    }

    private synchronized void incrementSeqNum() {
        ++seqNum;
    }

    @Override
    public void sender(Player player, SnakesProto.GameMessage message) {
        if (socket == null) {
            MyLogger.getLogger().error("SOCKET IS NULL!!!");
            //TODO: gameException
            return;
        }
        switch (message.getTypeCase()) {
            case ACK, ERROR -> sendAckMessage(message, player.getIpAddr(), player.getPort());
            case PING -> {
            }
            case STEER -> sendSteerMessage(message);
            case STATE -> {
                if (player.getId() != master.getId()) {
                    sendStateMessage(message,  player.getIpAddr(), player.getPort());
                }
            }
            case ROLE_CHANGE -> {
                MyLogger.getLogger().info("IM SWITCH");
                sendRoleChangeMessage(message, player.getIpAddr(), player.getPort());}
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
//            MyLogger.getLogger().info("SIZE AFTER WHILE" + sentMessages.size());
            MyLogger.getLogger().info("Sending steer server message to %s %s...");

            socket.send(message, master.getIpAddr(), master.getPort());

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
//            MyLogger.getLogger().info("SIZE AFTER WHILE" + sentMessages.size());
//            MyLogger.getLogger().info("Sending state server message to %s %s...");
            socket.send(message, receiverIpAddr, receiverPort);
//            MyLogger.getLogger().info("Send state message server successfully! SeqNum : " + message.getMsgSeq());
            try {
 //               MyLogger.getLogger().info("SLEEP");
                Thread.sleep(config.getPingDelayMs());
 //               MyLogger.getLogger().info("AWAKE!");
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void receiver() throws GameException {
        if (socket == null) {
            throw new GameException("Socket is null.");
        }
//        MyLogger.getLogger().info("Receiving message from client...");

        var message = socket.receive();

        if (message == null) {
            return;
        }

        var seqNumRecv = message.getMessage().getMsgSeq();
//        MyLogger.getLogger().info("Receive message on server with seqNum " + message.getMessage().getMsgSeq());

        switch (message.getMessage().getTypeCase()) {
            case ACK -> {
                var receivedMessage = sentMessages.get(seqNumRecv);
                switch (receivedMessage.getTypeCase()) {
                    case STEER, STATE, ROLE_CHANGE -> {
                        sentMessages.remove(seqNumRecv);
                        MyLogger.getLogger().info("SIZE AFTER RECEIVE" + sentMessages.size());
                        MyLogger.getLogger().info("Client get ack message with seqNum: " + seqNumRecv);
                    }
                    case PING -> {}
                }
            }
            case JOIN -> receiveJoinMessage(message);
            case PING -> {}
            case STEER -> receiveSteerMessage(message);
            case ROLE_CHANGE -> {}
            default -> {
                //TODO GAME EXCEPTION
                MyLogger.getLogger().info("Message of unknown type");
            }
        }
    }

    private void sendRoleChangeMessage(SnakesProto.GameMessage message, String receiverIp, int receiverPort) {
        sentMessages.put(message.getMsgSeq(), message);

        while (sentMessages.containsKey(message.getMsgSeq())) {
//            MyLogger.getLogger().info("SIZE AFTER WHILE" + sentMessages.size());
            MyLogger.getLogger().info("Sending role change message server message to %s %s...");

            socket.send(message, receiverIp, receiverPort);

            MyLogger.getLogger().info("Send role change message server successfully! SeqNum : " + message.getMsgSeq());
            try {
                MyLogger.getLogger().info("SLEEP");
                Thread.sleep(config.getPingDelayMs());
                MyLogger.getLogger().info("AWAKE!");
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void choseNewDeputy() {
        if (players.size() > 1) {
            for (var player : players.values()) {
                if (player.getId() != master.getId()) {
                    communicateThreadPool.submit(() -> sender(player, getRoleChangeMessage(SnakesProto.NodeRole.DEPUTY, player.getId())));
                    break;
                }
            }
        }
    }

    private void receiveSteerMessage(GameMessageWrap message) {
        var steerMessage = message.getMessage();
        MyLogger.getLogger().info("Get last receive ack" + lastReceivedSteer.get(message.getMessage().getSenderId()));
        if (players.get(message.getMessage().getSenderId()).getRole() != SnakesProto.NodeRole.VIEWER) {
            if (lastReceivedSteer.get(message.getMessage().getSenderId()) == null ||
                    lastReceivedSteer.get(message.getMessage().getSenderId()) < steerMessage.getMsgSeq()) {
                var currPlayer = players.get(steerMessage.getSenderId());
                model.changeSnakesDirection(currPlayer, steerMessage.getSteer().getDirection());
                MyLogger.getLogger().info("Sending ack message on server ...");
                sender(currPlayer, getAckMessage(message.getMessage().getMsgSeq(), currPlayer.getId()));
                lastReceivedSteer.put(message.getMessage().getSenderId(), message.getMessage().getMsgSeq());
//        MyLogger.getLogger().info("Send ack message on server successfully!");
            }
        }
    }



    private void receiveJoinMessage(GameMessageWrap message) {
        var newPlayer = addNewPlayer(message.getMessage().getJoin().getName(),
                message.getSenderAddr(), message.getPort());
        //can add player
        if (!isError) {
//            MyLogger.getLogger().info("Sending ack message on server ...");
            sender(newPlayer, getAckMessage(message.getMessage().getMsgSeq(), newPlayer.getId()));
//            MyLogger.getLogger().info("Send ack message on server successfully!");
            model.updateModel();
            //choose deputy
            if (currDeputy == null) {
                communicateThreadPool.submit(() -> sender(newPlayer, getRoleChangeMessage(SnakesProto.NodeRole.DEPUTY, newPlayer.getId())));
                currDeputy = newPlayer;
            }
            MyLogger.getLogger().info("Role change message end");
        } else {
            //can't add player
//            MyLogger.getLogger().info("Sending error message on server ...");
            sender(newPlayer, getErrorMessage(message.getMessage().getMsgSeq(), "SORRY, BUT NUMBER OF PLAYERS EXCEEDED. PLEASE CONNECT LATER...."));
//            MyLogger.getLogger().info("Send error message on server successfully!");
        }
    }


    private void createMulticastSender() {
        var multicastSender = new MulticastSender(this, socket.getSocket(), MULTICAST_ADDR, MULTICAST_PORT);
        multicastSender.init();
        announcementThreadPool.scheduleAtFixedRate(multicastSender::run, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void openSocket() {
        try {
            this.socket = new SocketWrap(new DatagramSocket());
            socket.getSocket().setSoTimeout(500);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startReceiver() {
        communicateThreadPool.submit(() -> {
            while (state == StateSystem.NEW_GAME) {
           //     MyLogger.getLogger().info("Starting receiver on server!");
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
        master.setPort(socket.getSocket().getLocalPort());
        createMulticastSender();
        createAnnouncementMessage();
        startReceiver();
        startGame();
    }


    @Override
    public synchronized void end() {
        state = StateSystem.EXIT;
        announcementThreadPool.shutdown();
        communicateThreadPool.shutdown();
        MyLogger.getLogger().info("Close node!");
    }

    public SnakesProto.GameMessage getAnnouncementMessage() {
        var sendMessage = SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(announcementMsg)
                .setMsgSeq(seqNum)
                .build();
        incrementSeqNum();
        return sendMessage;
    }

    private SnakesProto.GameMessage getRoleChangeMessage(SnakesProto.NodeRole newRole, int receiverId) {
        var roleChangeMessage = SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                .setSenderRole(SnakesProto.NodeRole.MASTER)
                .setReceiverRole(newRole)
                .build();
        var message = SnakesProto.GameMessage.newBuilder()
                .setRoleChange(roleChangeMessage)
                .setMsgSeq(seqNum)
                .setSenderId(master.getId())
                .setReceiverId(receiverId)
                .build();
        incrementSeqNum();
        return message;
    }


    private void createAnnouncementMessage() {
        ArrayList<SnakesProto.GamePlayer> listPlayers = new ArrayList<>();
        for (var player : players.values()) {
            listPlayers.add(player.convertToProto());
        }
        SnakesProto.GamePlayers clients = SnakesProto.GamePlayers.newBuilder().addAllPlayers(listPlayers).build();
        announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder().
                setPlayers(clients).setConfig(config).build();
    }


    private SnakesProto.GameMessage getAckMessage(long msgSeq, int receiverId) {
        var ackMessage = SnakesProto.GameMessage.AckMsg.newBuilder().build();
        return SnakesProto.GameMessage.newBuilder().setAck(ackMessage).setMsgSeq(msgSeq).setSenderId(master.getId()).setReceiverId(receiverId).build();
    }

    private SnakesProto.GameMessage getErrorMessage(long msgSeq, String error) {
        var errorMsg = SnakesProto.GameMessage.ErrorMsg.newBuilder().setErrorMessage(error).build();
        return SnakesProto.GameMessage.newBuilder().setError(errorMsg).setMsgSeq(msgSeq).setSenderId(master.getId()).build();
    }

    @Override
    public SnakesProto.GameMessage getSteerMessage(SnakesProto.Direction direction) {
        var steerMessage = SnakesProto.GameMessage.SteerMsg.newBuilder()
                .setDirection(direction)
                .build();
        var gameMessage = SnakesProto.GameMessage.newBuilder()
                .setSteer(steerMessage)
                .setSenderId(master.getId())
                .setMsgSeq(seqNum)
                .build();
        incrementSeqNum();
        return gameMessage;
    }

    private Player addNewPlayer(String name, String ipAddress, int port) {
        var player = new Player(name, ++currId, port, ipAddress, SnakesProto.NodeRole.NORMAL);
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
        for (var snake : model.getSnakes().values()) {
            protoSnakes.add(snake.convertToProtoSnake());
        }
        for (var singleFood : model.getFood()) {
            var point = singleFood.getPlace();
            foodProto.add(point.convertPointToCoord());
        }
        var gamePLayers = SnakesProto.GamePlayers.newBuilder();
        for (var player : model.getPlayers().values()) {
            gamePLayers.addPlayers(players.get(player.getId()).convertToProto());
            if (!model.getSnakes().containsKey(player.getId()) && player.getId() != master.getId()) {
                communicateThreadPool.submit(() -> sender(player, getRoleChangeMessage(SnakesProto.NodeRole.VIEWER, player.getId())));
            }
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
