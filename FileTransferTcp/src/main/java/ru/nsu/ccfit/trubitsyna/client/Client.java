package ru.nsu.ccfit.trubitsyna.client;
import picocli.CommandLine;
import ru.nsu.ccfit.trubitsyna.protocol.FTP;
import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

@CommandLine.Command(name = "startClient", mixinStandardHelpOptions = true,
        description = "Send file.", version = "1.0")
public class Client implements Runnable{

    private String filePath;
    private int port;
    private String host;

    private static final int TIMEOUT = 40000;
    private static final int MAX_NAME_SIZE = 4096;
    private static final int MAX_SOCKET_PORT = 65535;

    //PARSING
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-f", "--pathFile"}, required = true, description = "The path of file.")
    public void setFilePath(String value) {
        if (value.length() > MAX_NAME_SIZE) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    String.format("Invalid value '%s' for option '--pathFile': " +
                            "value is not a right path.", value));
        }
        filePath = value;
    }

    @CommandLine.Option(names = {"-p", "--port"}, required = true, description = "The number of port option. Usage between 0 and " + MAX_SOCKET_PORT)
    public void setPort(int value) {
        if (value < 0 || value >= MAX_SOCKET_PORT) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    String.format("Invalid value '%s' for option '--port': " +
                            "value is not a right port.", value));
        }
        port = value;
    }

    @CommandLine.Option(names = {"-H", "--host"}, required = true, description = "The host address or name.")
    public void setHost(String value) {
        if (value.isEmpty()) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    String.format("Invalid value '%s' for option '--host': " +
                            "value is not a right port.", value));
        }
        host = value;
    }

    @Override
    public void run()  {
        try (var client = new Socket(host, port)) {
            Path path = Paths.get(filePath);
            File file = path.toFile();
            client.setSoTimeout(TIMEOUT);
            System.out.println("Client started");
            var clientOutputSocket = client.getOutputStream();
            FTP.sendFile(file, clientOutputSocket);
            System.out.println(FTP.getStateMessage(client.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
