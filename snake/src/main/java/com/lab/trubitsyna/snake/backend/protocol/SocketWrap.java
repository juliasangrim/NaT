package com.lab.trubitsyna.snake.backend.protocol;

import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;

import java.io.IOException;
import java.net.*;

public class SocketWrap implements IOWrap {
    private static final int MAX_SIZE = 8192;
    private DatagramSocket socket;
    private int port;

    public SocketWrap(DatagramSocket socket) throws SocketException {
        this.socket = socket;
        this.port = socket.getPort();
    }

    @Override
    public void send(SnakesProto.GameMessage message, String receiver) {
        try {
            InetAddress addr = InetAddress.getByName(receiver);
            var packet = new DatagramPacket(message.toByteArray(), message.getSerializedSize(), addr, port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GameMessageWrap receive() {
        try {
            byte[] receiveRowByte = new byte[MAX_SIZE];
            DatagramPacket packet = new DatagramPacket(receiveRowByte, receiveRowByte.length);
            socket.receive(packet);

            return new GameMessageWrap(packet.getAddress(), SnakesProto.GameMessage.parseFrom(packet.getData()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
