package com.lab.trubitsyna.snake.backend.mcHandlers;

import com.lab.trubitsyna.snake.backend.node.MasterNetNode;
import com.lab.trubitsyna.snake.backend.protocol.SocketWrap;
import lombok.Setter;
import java.net.DatagramSocket;
import java.net.SocketException;

public class MulticastSender {

    private final String mcAddr;
    private final int mcPort;
    private final DatagramSocket datagramSocket;
    @Setter
    private MasterNetNode master;
    private SocketWrap socket;

    public MulticastSender(MasterNetNode master, DatagramSocket socket, String mcAddr, int mcPort) {
        this.master = master;
        this.datagramSocket = socket;
        this.mcAddr = mcAddr;
        this.mcPort = mcPort;
    }

    public void init() {
        try {
            this.socket = new SocketWrap(datagramSocket);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        var message = master.getAnnouncementMessage();
      //  logger.info("Sending MC message....");
        socket.send(message, mcAddr, mcPort);
      //  logger.info("Send MC message successfully!");
    }

}
