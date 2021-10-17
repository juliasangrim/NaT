package ru.nsu.ccfit.trubitsyna.server;
import picocli.CommandLine;

public class MainServer {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Server()).execute(args);
    }
}
