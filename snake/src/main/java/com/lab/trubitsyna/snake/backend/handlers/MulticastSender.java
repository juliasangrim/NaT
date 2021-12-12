package com.lab.trubitsyna.snake.backend.handlers;

import com.lab.trubitsyna.snake.backend.node.MasterNetNode;
import com.lab.trubitsyna.snake.backend.protoClass.SnakesProto;
import com.lab.trubitsyna.snake.backend.protocol.SocketWrap;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MulticastSender {
    private final static Logger logger = LoggerFactory.getLogger("APP");

    @Setter
    private MasterNetNode master;
    private final String mcAddr;
    private final int mcPort;
    private SocketWrap socket;
    private final DatagramSocket datagramSocket;

    public MulticastSender(MasterNetNode master, DatagramSocket socket, String mcAddr, int mcPort) {
        this.master = master;
        this.datagramSocket = socket;
        this.mcAddr = mcAddr;
        this.mcPort = mcPort;
        logger.info("Create multicast sender.");
    }

    public void init() {
        try {
            this.socket = new SocketWrap(datagramSocket);
            logger.info("Create socket wrap");
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        var message = master.getAnnouncementMessage();
        logger.info("Sending MC message....");
        socket.send(message, mcAddr, mcPort);
        logger.info("Send MC message successfully!");
    }

}
