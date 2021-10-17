package ru.nsu.ccfit.trubitsyna.server;

import picocli.CommandLine;
import ru.nsu.ccfit.trubitsyna.protocol.FTP;
import ru.nsu.ccfit.trubitsyna.protocol.SpeedInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(name = "startServer", mixinStandardHelpOptions = true,
        description = "Download files.", version = "1.0")
public class Server implements Runnable {
    private int port;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private int lastClientId = 0;

    private static final int NUM_THREADS = 1;
    private static final int PERIOD_SEC = 3;
    private static final int MAX_SOCKET_PORT = 65535;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-p", "--port"}, required = true, description = "The number of port option. Usage between 0 and " + MAX_SOCKET_PORT)
    public void setPort(int value) {
        if (value < 0 || value >= MAX_SOCKET_PORT) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    String.format("Invalid value '%s' for option '--port': " +
                            "value is not a right port.", value));
        }
        port = value;
    }


    @Override
    public void run() {
        try {
            var servSocket = new ServerSocket(port);
            System.out.println("Server start working.");
            while (!servSocket.isClosed()) {
                //close this socket in downloadFile
                var newClient = servSocket.accept();
                int clientID = getClientID();
                System.out.println("                  Client connect with ID: " + clientID + "\n" +
                        "**********************************************");
                threadPool.submit(() -> downloadFile(newClient, clientID));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getClientID() {
        return lastClientId++;
    }

    private void printMessage(SpeedInfo speedInfo, int clientID, double interval) {
        System.out.println("-------------Speed of client with ID: " + clientID + "-------------\n" +
                "Moment speed: " + speedInfo.getMomentSpeed(interval) + " MB/s\n" +
                "Average speed: " + speedInfo.getAverageSpeed() + " MB/s\n");
    }

    private void downloadFile(Socket newClient, int id) {
        var speedInfo = new SpeedInfo();
        var scheduleThreadPool = Executors.newScheduledThreadPool(NUM_THREADS);

        //start schedule thread pool for printing speed information
        scheduleThreadPool.scheduleAtFixedRate(() -> {
            printMessage(speedInfo, id, PERIOD_SEC);
            speedInfo.flushCurrReadBytes();
        }, (long) (PERIOD_SEC * SpeedInfo.MILLS_IN_SEC), (long) (PERIOD_SEC * SpeedInfo.MILLS_IN_SEC), TimeUnit.MILLISECONDS);

        try (newClient) {
            //send message about upload success
            if (FTP.downloadFile(newClient.getInputStream(), speedInfo)) {
                FTP.sendStateMessage("File uploaded successfully.\n", newClient.getOutputStream());
            } else {
                FTP.sendStateMessage("File uploaded with errors.\n", newClient.getOutputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scheduleThreadPool.shutdown();
            //print message for connection less than 3 seconds
            double period = (double) (System.currentTimeMillis() - speedInfo.getStartTime()) / SpeedInfo.MILLS_IN_SEC;
            if (period < PERIOD_SEC) {
                printMessage(speedInfo, id, period);
                System.out.println("This client was connected less than 3 seconds.");
            }
        }
    }
}
