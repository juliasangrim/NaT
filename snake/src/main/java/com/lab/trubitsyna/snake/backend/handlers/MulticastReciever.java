package com.lab.trubitsyna.snake.backend.handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import com.lab.trubitsyna.snake.backend.node.NetNode;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

public class MulticastReciever {
    private final static Logger logger = LoggerFactory.getLogger("APP");
    private final static int SIZE = 8192;
    private final static int TIMEOUT_MS = 2000;

    private final NetNode client;
    @Getter
    private MulticastSocket socket;
    private final int mcPort;
    private final String mcAddr;
    private final byte[] buf = new byte[SIZE];

    public MulticastReciever(NetNode client, int mcPort, String mcAddr) {
        this.client = client;
        this.mcPort = mcPort;
        this.mcAddr = mcAddr;
        logger.info("Create a multicast receiver.");
    }

    public void init() {
        try {
            logger.info("Creation MC socket...");
            this.socket = new MulticastSocket(mcPort);
            logger.info("Create MC socket!");
            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress group = InetAddress.getByName(mcAddr);
            logger.info("Joining to group...");
            socket.joinGroup(group);
            logger.info("Join to group successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            logger.info("Receiving announcement message...");
            socket.receive(packet);
        } catch (SocketTimeoutException ignore) {
            logger.info("Socket timeout!");
            client.checkAliveServers();
            return;
        } catch (IOException unknownHostException) {
            unknownHostException.printStackTrace();
        }

        //get the message from array(bc packet.getData() return all bytes(include zero bytes))
        byte[] receivedBytes = new byte[packet.getLength()];
        System.arraycopy(buf, 0, receivedBytes, 0, packet.getLength());
        logger.info("Receive message successfully!");
        try {
            client.changeListAvailableServer(SnakesProto.GameMessage.parseFrom(receivedBytes).getAnnouncement(), packet.getPort(), packet.getAddress());
            client.checkAliveServers();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    public void close() {
        try {
            InetAddress group = InetAddress.getByName(mcAddr);
            logger.info("Leaving group....");
            socket.leaveGroup(group);
            logger.info("Leave group successfully");
            socket.close();
            logger.info("Close MC socket!");
            } catch (IOException e) {
            socket.close();
            e.printStackTrace();
        }

    }
}
