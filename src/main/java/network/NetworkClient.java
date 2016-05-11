package network;

import gamelogic.*;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

/**
 * An implementation of upi (universal poker interface) (Specification on the docs)
 * wrapped around the GameClient interface.
 *
 * Although this object is technically on the server-side of the network,
 * the GameController sees it as another client, hence the name NetworkClient
 */
public class NetworkClient implements GameClient {

    private final int playerID;
    private Socket socket;
    private BufferedReader socketInput;
    private BufferedWriter socketOutput;
    private Optional<Decision> decision = Optional.empty();
    private Queue<String> outstandingWrites = new LinkedList<>();

    //Blocking queues for locking while reading
    private ArrayBlockingQueue<String> nameBlockingQueue = new ArrayBlockingQueue<>(2);
    private ArrayBlockingQueue<Decision> decisionBlockingQueue = new ArrayBlockingQueue<>(2);
    private Consumer<String> chatListener;
    private final Logger logger;

    private volatile boolean isDropped = false;

    /**
     * Initializes the network client, and does the upi handshake with the remote client
     * @param socket A TCP socket to the client, which is expected to already have an open connection
     * @param playerID ID of the player
     * @throws IOException
     */
    public NetworkClient(Socket socket, int playerID, Logger logger) throws IOException {
        this.logger = logger;
        this.socket = socket;
        this.playerID = playerID;

        socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        socketOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

        //Sending upi-handshake
        writeToSocket("upiok");
        writeToSocket("clientId " + playerID);

        new Thread(this::readFromSocket).start();
    }

    /**
     * Read from socket and make appropriate action
     */
    public void readFromSocket(){
            int consecutiveAttempts = 0;
            while (!isDropped) {
                try {
                    String input;
                    try {
                        input = socketInput.readLine();
                        if (input == null) {
                            throw new IOException("Got input null");
                        }
                        consecutiveAttempts = 0;
                    } catch (IOException e) {
                        logger.println("Failed to read from client " + this + ", retrying...", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
                        consecutiveAttempts++;
                        if (consecutiveAttempts > 10) {
                            dropClient();
                            return; // Give up after 10 attempts
                        }
                        else {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) { }
                        }
                        continue;
                    }

                    logger.println("Client #" + playerID + ": " + input, Logger.MessageType.NETWORK);
                    Optional<String[]> tokens = UpiUtils.tokenize(input);
                    if (tokens.isPresent() && tokens.get().length != 0) {
                        switch (tokens.get()[0]) {
                            case "chat":
                                if (tokens.get().length > 1)
                                    chatListener.accept(tokens.get()[1]);
                                break;
                            case "decision":
                                //Parse decision and queue it. If it can't be parsed, queue FOLD
                                decision = UpiUtils.parseDecision(input.substring("decision ".length()));
                                decisionBlockingQueue.add(decision.isPresent() ? decision.get() : Decision.fold);
                                break;
                            case "playerName":
                                //Queue name so that getName will be unlocked
                                nameBlockingQueue.add(tokens.get()[1]);
                                break;
                            default:
                                logger.println("Unrecognized input", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
                        }
                    }
                }
                catch (IllegalStateException ise) {
                    logger.println("WARNING: Illegal state exception, " + ise.getMessage(), Logger.MessageType.WARNINGS, Logger.MessageType.NETWORK);
                }
            }
        }

    public void dropClient() {
        isDropped = true;
        logger.println("Gave up connecting to " + this + ", dropping...", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);

        // Add dummy data to the blocking queues, in case someone is waiting for them
        decisionBlockingQueue.add(Decision.fold);
        nameBlockingQueue.add("");
        this.closeSocket();
    }

    @Override
    /**
     *  Get decision from this client.
     *  Writes get decision + timeToThink to client and waits for decisionBlockingQueue to unlock.
     *
     *  @return The decision the player made if it is parsed correctly, fold if not
     */
    public synchronized Decision getDecision(long timeToThink) {
        if (!isDropped) {
            //Write getDecision to client
            if (!writeToSocket("getDecision " + timeToThink)) {
                logger.println("Failed to ask " + this + " for decision, folding...", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
                return Decision.fold;
            }

            //Wait for decision to be added to decision queue (by the readingThread)
            try {
                return decisionBlockingQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return Decision.fold;
            }
        }
        else {
            return Decision.fold;
        }
    }

    @Override
    public synchronized String getName() {
        if (!isDropped) {
            writeToSocket("getName");

            //Wait for name to be added to name queue (by the reading thread)
            try {
                return nameBlockingQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return "";
            }
        }
        else {
            return "";
        }
    }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        Map<Integer, String> namesForSending = new HashMap<>(names);
        namesForSending.keySet().stream()
                .filter(key -> namesForSending.get(key).contains(" "))
                .forEach(key -> {
                    logger.println("Found name containing space, wrapping in quotation marks", Logger.MessageType.WARNINGS, Logger.MessageType.NETWORK);
                    namesForSending.put(key, "\"" + namesForSending.get(key) + "\"");
                });
        writeToSocket("playerNames " + UpiUtils.mapToString(namesForSending));
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        writeToSocket("setHand " + userID + " " + UpiUtils.cardsToString(card1, card2));
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        writeToSocket("stackSizes " + UpiUtils.mapToString(stackSizes));
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        writeToSocket("playerMadeDecision " + playerId + " "
                + UpiUtils.decisionToString(decision));
    }

    @Override
    public void showdown(String[] winnerStrings) {
        String winnerString = Arrays.stream(winnerStrings).map(s -> " \"" + s + "\"").reduce("", String::concat).trim();
        writeToSocket("showdown " + winnerString);
    }

    @Override
    public void setBigBlind(long bigBlind) {
        writeToSocket("bigBlind " + bigBlind);
    }

    @Override
    public void setSmallBlind(long smallBlind) {
        writeToSocket("smallBlind " + smallBlind);
    }

    @Override
    public void setPositions(Map<Integer, Integer> setPositions) {
        writeToSocket("playerPositions " + UpiUtils.mapToString(setPositions));
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        writeToSocket("amountOfPlayers " + amountOfPlayers);
    }

    @Override
    public void setLevelDuration(int levelDuration) {
        writeToSocket("levelDuration " + levelDuration);
    }

    @Override
    public void setFlop(Card card1, Card card2, Card card3) {
        writeToSocket("setFlop " + UpiUtils.cardsToString(card1, card2, card3));
    }

    @Override
    public void setTurn(Card turn) {
        writeToSocket("setTurn " + UpiUtils.cardsToString(turn));
    }

    @Override
    public void setRiver(Card river) {
        writeToSocket("setRiver " + UpiUtils.cardsToString(river));
    }

    @Override
    public void startNewHand() {
        writeToSocket("newHand");
    }

    @Override
    public void playerBust(int playerID, int rank) {
        writeToSocket("playerBust " + playerID + " " + rank);
    }

    @Override
    public void gameOver(Statistics statistics) {
        writeToSocket(statistics.toUPIString());
    }

    @Override
    public void printToLogField(String output) {
        writeToSocket("logPrint \"" + output + "\"");
    }

    @Override
    public void preShowdownWinner(int winnerID) {
        writeToSocket("preShowdownWinner " + winnerID);
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.println("Failed to close socket " + socket + " of " + this, Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
        }
    }

    /**
     * Called whenever a write to a player fails. For now, this drops the player from the server,
     * but in the future this should probably buffer up outstanding writes, and try to send them
     * again for a while.
     * @param player
     * @param message
     */
    /*private void failedToWriteToPlayer(String message) {
        logger.println("Failed to write to client " + this + ", dropping player.");
        this.removeClient(player.id);
    }*/

    /**
     * Writes the output to the socket, terminating the line and flushing the socket
     * If the write fails, it will try to re-write for 5 seconds, before giving up
     * @param output
     * @return True if the write succeeded, false if it failed.
     */
    private boolean writeToSocket(String output) {
        if (isDropped) {
            return false;
        }
        outstandingWrites.add(output);
        if (socket.isClosed() || !socket.isConnected()) {
            logger.println("Socket for " + this + " is not connected, cannot do write", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
            return false;
        }
        int attempts = 10;
        for (int i = 0; i < attempts; i++) {
            try {
                while (!outstandingWrites.isEmpty()) {
                    socketOutput.write(outstandingWrites.peek() + "\n");
                    logger.println("Server to #" + playerID + ": " + outstandingWrites.poll(), Logger.MessageType.NETWORK);
                }
            }
            catch (IOException e) {
                logger.println("Failed to write \"" + outstandingWrites.peek() + "\" to " + this + ", retry #" + i + " (" + outstandingWrites.size() + " outstand writes waiting", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) { }
                continue;
            }
            try {
                socketOutput.flush();
                return true;
            } catch (IOException e) {
                logger.println("Failed to flush socket after writing \"" + output + "\" to " + this + ". (" + outstandingWrites.size() + " outstand writes waiting)", Logger.MessageType.WARNINGS, Logger.MessageType.NETWORK);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) { }
            }
        }
        dropClient();
        return false;
    }

    @Override
    public String toString() {
        return "{ NetworkClient, id " + playerID + " }";
    }

    /**
     *  Set the chat listener
     * @param chatListener Listener to be fired when a client sends a chat message
     */
    public void setChatListener(Consumer<String> chatListener) {
        this.chatListener = chatListener;
    }
}
