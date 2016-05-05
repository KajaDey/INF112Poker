package gamelogic;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

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
        String input = socketInput.readLine();
        if (input.startsWith("upi")) {
            writeToSocket("upiok");
        }
        else {
            throw new IOException("Wrong handshake " + input);
        }
        writeToSocket("clientId " + playerId);
    }

    @Override
    public Decision getDecision(long timeToThink) {
        long startTime = System.currentTimeMillis();
        while (startTime + timeToThink > System.currentTimeMillis()) {
            try {
                socketOutput.write("getDecision " + timeToThink + "\n");
                socketOutput.flush();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        while (startTime + timeToThink > System.currentTimeMillis()) {
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
                e.printStackTrace();
            }
        }
        System.out.println("Couldn't get decision from client, returning fold");
        return Decision.fold;
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
        String[] tokens = input.split("\\s+");
        if (tokens.length < 1 || !tokens[0].equals("name")) {
            System.out.println("Got illegal name command \"" + input + "\" from client, returning blank");
            return "";
        }
        return tokens[1];
    }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        writeToSocket("playerNames " + mapToString(names));

    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        writeToSocket("setHand " + userID + " " + cardsToString(card1, card2));
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        writeToSocket("stackSizes " + mapToString(stackSizes));
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        writeToSocket("playerMadeDecision " + playerId + " "
                + ServerGameCommunicator.decisionToString(decision));
    }

    @Override
    public void showdown(ShowdownStats showdownStats) {

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
        writeToSocket("playerPositions " + mapToString(setPositions));
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
        writeToSocket("setFlop " + cardsToString(card1, card2, card3));
    }

    @Override
    public void setTurn(Card turn) {
        writeToSocket("setTurn " + cardsToString(turn));
    }

    @Override
    public void setRiver(Card river) {
        writeToSocket("setRiver " + cardsToString(river));
    }

    @Override
    public void startNewHand() {
        writeToSocket("newHand");
    }

    @Override
    public void playerBust(int playerID, int rank) {

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
            e.printStackTrace();
        }
    }

    /**
     * Writes the output to the socket, terminating the line and flushing the socket
     */
    private void writeToSocket(String output) {
        try {
            socketOutput.write(output + "\n");
            socketOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts a list of cards into a upi-compatible string
     */
    public static String cardsToString(Card ... cards) {
        return Arrays.stream(cards)
                .map(card -> card.suit.name().toLowerCase() + card.rank + " ")
                .reduce("", String::concat);
    }

    /**
     * Converts a map into a upi-compatible string
     */
    public static <K, V> String mapToString(Map<K, V> map) {
        return map.keySet().stream()
                    .map(key -> key.toString() + " " + map.get(key).toString() + " ")
                    .reduce("", String::concat);
    }
}
