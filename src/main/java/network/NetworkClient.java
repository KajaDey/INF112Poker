package network;

import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.GameClient;
import gamelogic.Statistics;

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * An implementation of upi (universal poker interface) (Specification on the docs)
 * wrapped around the GameClient interface.
 *
 * Although this object is technically on the server-side of the network,
 * the GameController sees it as another client, hence the name NetworkClient
 */
public class NetworkClient implements GameClient {

    private final int playerId;
    private Socket socket;
    private BufferedReader socketInput;
    private BufferedWriter socketOutput;
    Queue<String> outstandingWrites = new LinkedList<>();

    /**
     * Initializes the network client, and does the upi handshake with the remote client
     * @param socket A TCP socket to the client, which is expected to already have an open connection
     * @param playerId ID of the player
     * @throws IOException
     */
    public NetworkClient(Socket socket, int playerId) throws IOException {
        this.socket = socket;
        this.playerId = playerId;

        socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        socketOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

        System.out.println("Waiting for upi handshake");
        String input = "upi"; // Pretend the upi handshake was received by the lobby

        if (input != null && input.startsWith("upi")) {
            System.out.println("Got upi from player " + playerId);
            writeToSocket("upiok");
        }
        else {
            throw new IOException("Wrong handshake " + input);
        }
        writeToSocket("clientId " + playerId);
    }

    @Override
    public Decision getDecision(long timeToThink) {

        if (!writeToSocket("getDecision " + timeToThink)) {
            System.out.println("Failed to ask " + this + " for decision, folding...");
            return Decision.fold;
        }

        try {
            String input = socketInput.readLine();
            Optional<Decision> decision = ServerGameCommunicator.parseDecision(input);
            if (!decision.isPresent()) {
                System.out.println("Server received incorrectly formatted decision " + input + ", folding");
                return Decision.fold;
            } else {
                return decision.get();
            }
        } catch (IOException e) {
            System.out.println("Failed to read decision from socket of " + this + ", folding...");
            return Decision.fold;
        }
    }

    @Override
    public String getName() {
        writeToSocket("getName");
        String input;
        try {
            input = socketInput.readLine();
        } catch (IOException e) {
            System.out.println("Failed to get name from client, returning blank");
            return "";
        }
        Optional<String[]> tokens = UpiUtils.tokenize(input);
        System.out.println("Got name command " + input + " from client, tokens: " + Arrays.toString(tokens.get()));
        if (!tokens.isPresent() || tokens.get().length <= 1 || !tokens.get()[0].equals("playerName")) {
            System.out.println("Got illegal name command \"" + input + "\" from client, returning blank");
            return "";
        }
        return tokens.get()[1];
    }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        Map<Integer, String> namesForSending = new HashMap<>(names);
        for (Integer key : namesForSending.keySet()) {
            if (namesForSending.get(key).contains(" ")) {
                System.out.println("Found name containing space, wrapping in quotation marks");
                namesForSending.put(key, "\"" + namesForSending.get(key) + "\"");
            }
        }
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
                + ServerGameCommunicator.decisionToString(decision));
    }

    @Override
    public void showdown(String[] winnerStrings) {
        System.out.println("Gamecontroller sent winnerStrings " + Arrays.toString(winnerStrings));
        String winnerString = Arrays.stream(winnerStrings).map(s -> " \"" + s + "\"").reduce("", String::concat).trim();
        System.out.println("sending winnerString to clients: --" + winnerString + "--");
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
    public void gameOver(Statistics winnerID) {

    }

    @Override
    public void printToLogField(String output) {
        //TODO: Implement
    }

    @Override
    public void preShowdownWinner(int winnerID) {
        //TODO: Maybe implement
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Failed to close socket " + socket + " of " + this);
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
        System.out.println("Failed to write to client " + this + ", dropping player.");
        this.removeClient(player.id);
    }*/

    /**
     * Writes the output to the socket, terminating the line and flushing the socket
     * If the write fails, it will try to re-write for 5 seconds, before giving up
     * @param output
     * @return True if the write succeeded, false if it failed.
     */
    private boolean writeToSocket(String output) {
        outstandingWrites.add(output);
        if (socket.isClosed() || !socket.isConnected()) {
            System.out.println("Socket for " + this + " is not connected, cannot do write");
            return false;
        }
        int attempts = 5;
        for (int i = 0; i < attempts; i++) {
            try {
                while (!outstandingWrites.isEmpty()) {
                    socketOutput.write(outstandingWrites.peek() + "\n");
                    outstandingWrites.poll();
                }
            }
            catch (IOException e) {
                System.out.println("Failed to write \"" + outstandingWrites.peek() + "\" to " + this + ", retry #" + i + " (" + outstandingWrites.size() + " outstand writes waiting");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                }
                continue;
            }
            try {
                socketOutput.flush();
                return true;
            } catch (IOException e) {
                System.out.println("Failed to flush socket after writing \"" + output + "\" to " + this + ". (" + outstandingWrites.size() + " outstand writes waiting)");
                return false;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "{ NetworkClient, id " + playerId + " }";
    }
}
