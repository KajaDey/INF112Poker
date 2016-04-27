package gamelogic;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Created by morten on 27.04.16.
 */
public class NetworkClient implements GameClient {

    final int playerId;
    Socket socket;
    BufferedReader socketInput;
    BufferedWriter socketOutput;


    public NetworkClient(Socket socket, int playerId) throws IOException {
        this.socket = socket;
        this.playerId = playerId;

        socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        socketOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

        String input = socketInput.readLine();
        if (input.startsWith("upi")) {
            System.out.println("Received handshake upi");
            writeToSocket("upiok\n");
        }
        else {
            System.out.println("Wrong handshake " + input);
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
        return Decision.fold;
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

    private void writeToSocket(String output) {
        try {
            socketOutput.write(output + "\n");
            socketOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String cardsToString(Card ... cards) {
        return Arrays.stream(cards)
                .map(card -> card.suit.name().toLowerCase() + card.rank + " ")
                .reduce("", String::concat);
    }

    public static <K, V> String mapToString(Map<K, V> map) {
        return map.keySet().stream()
                    .map(key -> key.toString() + " " + map.get(key).toString() + " ")
                    .reduce("", String::concat);
    }
}
