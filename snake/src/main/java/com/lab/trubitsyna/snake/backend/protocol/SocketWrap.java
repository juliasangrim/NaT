package com.lab.trubitsyna.snake.backend.protocol;

import com.lab.trubitsyna.snake.MyLogger;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

public class SocketWrap implements IOWrap {
   // private final Logger logger = LoggerFactory.getLogger("APP");
    private static final int MAX_SIZE = 8192;
    @Getter
    private DatagramSocket socket;


    public SocketWrap(DatagramSocket socket) throws SocketException {
        this.socket = socket;
    }

    @Override
    public void send(SnakesProto.GameMessage message, String receiver, int receiverPort) {
        try {
         //   logger.info("Creating packet...");
            InetAddress addr = InetAddress.getByName(receiver);
            var packet = new DatagramPacket(message.toByteArray(), message.getSerializedSize(), addr, receiverPort);
            MyLogger.getLogger().info("Send to " + addr + ", "+ receiverPort +  " " + message.getTypeCase());
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GameMessageWrap receive() {
        if (socket == null) {
            MyLogger.getLogger().info("SOCKET IS NULL!!!");
            return null;
        }
        try {
            byte[] receiveRowByte = new byte[MAX_SIZE];
            DatagramPacket packet = new DatagramPacket(receiveRowByte, receiveRowByte.length);
            MyLogger.getLogger().info(String.format("Trying receive message... on port=%d, addr=%s", socket.getLocalPort(), socket.getLocalAddress()));
            socket.receive(packet);
            MyLogger.getLogger().info(String.format("Receive message... on port=%d, addr=%s", socket.getLocalPort(), socket.getLocalAddress()));
            //get the message from array(bc packet.getData() return all bytes(include zero bytes))
            byte[] receivedBytes = new byte[packet.getLength()];
            System.arraycopy(receiveRowByte, 0, receivedBytes, 0, packet.getLength());
            var message = SnakesProto.GameMessage.parseFrom(receivedBytes);
            MyLogger.getLogger().info("Get message from " + packet.getAddress() + " " + packet.getPort() + " " + message.getTypeCase());
            return new GameMessageWrap(message, packet.getAddress().getHostAddress(), packet.getPort());
        } catch (SocketTimeoutException e) {
            MyLogger.getLogger().info("Socket timeout");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
