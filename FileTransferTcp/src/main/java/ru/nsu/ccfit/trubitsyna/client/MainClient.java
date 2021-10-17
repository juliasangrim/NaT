package ru.nsu.ccfit.trubitsyna.client;
import picocli.CommandLine;

public class MainClient {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Client()).execute(args);
    }
}
