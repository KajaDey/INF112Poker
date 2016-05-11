package gamelogic;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.Arrays;
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
    public boolean printToSysout = true;

    public enum MessageType {
        DEBUG(true), AI(false), GAMEPLAY(true), WARNINGS(true), NETWORK(true), GUI(true), INIT(true), NETWORK_DEBUG(false), ALL(false);

        /* For message types with this enabled, all log prints will also be printed to sysout
         * Overriden by Logger.printToSysout
         */
        boolean printToSysoutByDefault;

        MessageType(boolean printToSysoutByDefault) {
            this.printToSysoutByDefault = printToSysoutByDefault;
        }
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
        if (Files.exists(path)) {
            if (Files.isWritable(path)) {
                logWriters.put(type, Files.newBufferedWriter(path, StandardOpenOption.APPEND));
                System.out.println("Started writing to an already open log");
            }
            else {
                System.out.println("Another logger is already writing to the file");
            }
        }
        else {
            logWriters.put(type, Files.newBufferedWriter(path));
        }

    }

    public void print(String message, MessageType ... messageTypes) {
        if (messageTypes.length == 0) {
            messageTypes = new MessageType[]{MessageType.DEBUG};
        }
        messageTypes = Arrays.stream(messageTypes).filter(type -> type !=  MessageType.ALL).toArray(MessageType[]::new);
        assert !Arrays.stream(messageTypes).filter(type -> type == MessageType.ALL).findFirst().isPresent() : "Cannot write directly to \"all\" log file (tried to write to logs " + Arrays.toString(messageTypes) + ")";

        // Add "all" to message types, so that it is always printed to
        messageTypes = Arrays.copyOf(messageTypes, messageTypes.length + 1);
        messageTypes[messageTypes.length - 1] = MessageType.ALL;

        boolean hasPrintedToSysout = false;
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
            if (printToSysout && !hasPrintedToSysout && messageType.printToSysoutByDefault) {
                System.out.print(wholeMessage);
                hasPrintedToSysout = true;
            }
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
