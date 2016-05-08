package gamelogic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Optional;

/**
 * A logging instance for writing various debug logs
 */
public class Logger {
    private Optional<String> logFolder = Optional.empty();
    private final String fileNamePrefix;
    private final String logMessagePrefix;
    private final HashMap<MessageType, BufferedWriter> logWriters = new HashMap<>();

    public enum MessageType {
        DEBUG, AI, GAMEPLAY, WARNINGS, NETWORK
    }

    public Logger() {
        this("", "");
    }
    public Logger(String fileNamePrefix, String logMessagePrefix) {
        this.fileNamePrefix = fileNamePrefix;
        this.logMessagePrefix = logMessagePrefix;
    }

    private void createLogFor(MessageType type) throws IOException {
        assert !logWriters.containsKey(type);

        if (!logFolder.isPresent()) {
            String logFolder = System.currentTimeMillis() / 1000 + "-log";
            Files.createDirectories((Paths.get("logs", logFolder)));
            this.logFolder = Optional.of(logFolder);
        }

        Path logFile = Files.createFile(Paths.get(logFolder.get(), fileNamePrefix + type.toString().toLowerCase() + ".log"));
        logWriters.put(type, Files.newBufferedWriter(logFile));
    }

    public void print(String message, MessageType messageType) {
        if (!logWriters.containsKey(messageType)) {
            try {
                createLogFor(messageType);
            } catch (IOException e) {
                System.out.println("Failed to create log file for " + messageType);
                return;
            }
        }
        LocalTime time = LocalTime.now();
        String wholeMessage = time.getHour() + ":" + time.getMinute() + ":" + time.getSecond() + " - " + logMessagePrefix + message;
        try {
            logWriters.get(messageType).write(wholeMessage);
            logWriters.get(messageType).flush();
        } catch (IOException e) {
            System.out.println("Failed to write to " + messageType + " logfile");
        }
        System.out.print(wholeMessage);
    }

    public void println(String message, MessageType messageType) {
        print(message + "\n", messageType);
    }

    public void println(String message) {
        print(message + "\n");
    }

    public void print(String message) {
        print(message, MessageType.DEBUG);
    }
}
