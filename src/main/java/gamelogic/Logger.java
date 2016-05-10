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
        DEBUG, AI, GAMEPLAY, WARNINGS, NETWORK, INIT
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
            String logFolder = fileNamePrefix + "_" + System.currentTimeMillis() / 1000 + "-log";
            Files.createDirectories((Paths.get("logs", logFolder)));
            this.logFolder = Optional.of(logFolder);
        }
        Path path = Paths.get("logs", logFolder.get(), type.toString().toLowerCase() + ".log");
        Path logFile = Files.createFile(path);
        logWriters.put(type, Files.newBufferedWriter(logFile));
    }

    public void print(String message, MessageType ... messageTypes) {
        if (messageTypes.length == 0) {
            messageTypes = new MessageType[]{MessageType.DEBUG};
        }
        for (MessageType messageType : messageTypes) {
            if (!logWriters.containsKey(messageType)) {
                try {
                    createLogFor(messageType);
                } catch (IOException e) {
                    System.out.println("Failed to create log file for " + messageType + ": ");
                    System.err.println("Failed to create log file for " + messageType + ": ");
                    return;
                }
            }
            LocalTime time = LocalTime.now();
            String wholeMessage;
            if (message.startsWith("\n")) {
                if (message.equals("\n")) {
                    wholeMessage = "\n";
                }
                else {
                    print("\n", messageTypes);
                    wholeMessage = time.getHour() + ":" + time.getMinute() + ":" + time.getSecond() + " - " + logMessagePrefix + message.substring(1);
                }
            }
            else {
                wholeMessage = time.getHour() + ":" + time.getMinute() + ":" + time.getSecond() + " - " + logMessagePrefix + message;
            }
            try {
                logWriters.get(messageType).write(wholeMessage);
                logWriters.get(messageType).flush();
            } catch (IOException e) {
                System.out.println("Failed to write to " + messageType + " logfile");
            }
            System.out.print(wholeMessage);
        }
    }

    public void println(String message, MessageType ... messageTypes) {
        print(message + "\n", messageTypes);
    }

    public void println(String message) {
        print(message + "\n");
    }

    public void print(String message) { print(message, MessageType.DEBUG); }

    public void aiPrintln(String message) { aiPrint(message + "\n"); }

    public void aiPrint(String message) { print(message, MessageType.AI); }
}
