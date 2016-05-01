package gamelogic;

import gamelogic.ai.MCTSAI;
import gui.GameScreen;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Created by morten on 27.04.16.
 */
public class ServerGameCommunicator {
    private final Socket socket;
    private final GameScreen gameScreen;
    private final String playerName;
    private Optional<GameClient> gameClient = Optional.empty();

    /**
     * * Creates a new ServerGameCommunicator to the given ip, but does not start communcation
     * @param socket A TCP connetion to the server. This is expected to already be open
     * @param playerName The player's chosen name
     * @param gameScreen The gameScreen of the GUI being played on
     */
    public ServerGameCommunicator(Socket socket, String playerName, GameScreen gameScreen) {
        this.socket = socket;
        this.gameScreen = gameScreen;
        this.playerName = playerName;
    }

    /**
     * Starts upi communcation with the server
     * Does not return until the game ends
     * @throws IOException
     */
    public void startUpi() throws IOException {

        assert socket.isConnected();

        BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        BufferedWriter socketOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

        socketOutput.write("upi 0.1\n");
        socketOutput.flush();

        String input = socketInput.readLine();
        if (!input.equals("upiok")) {
            throw new IOException("Received " + input + " from server, expected upiok");
        }

        while (true) {
            input = socketInput.readLine();
            String[] tokens = input.split("\\s+");
            if (tokens.length == 0) {
                throw new IOException("Received empty command \"" + input + "\" from server");
            }
            switch (tokens[0]) {
                case "getName":
                    socketOutput.write("name " + playerName + "\n");
                    break;
                case "newHand":
                    assert gameClient.isPresent();
                    gameClient.get().startNewHand();
                    break;
                case "newGame":
                    // TODO: Figure out what needs to be done here
                    break;
                case "amountOfPlayers":
                    assert gameClient.isPresent();
                    gameClient.get().setAmountOfPlayers(Integer.parseInt(tokens[1]));
                    break;
                case "clientId":
                    assert !gameClient.isPresent() : "Server sent clientId twice";
                    gameClient = Optional.of(new MCTSAI(Integer.parseInt(tokens[1]), 2.0));
                    break;
                case "playerNames":
                    assert gameClient.isPresent();
                    HashMap<Integer, String> playerNames = new HashMap<>();
                    for (int i = 1; i < tokens.length; i += 2) {
                        playerNames.put(Integer.parseInt(tokens[i]), tokens[i + 1]);
                    }
                    gameClient.get().setPlayerNames(playerNames);
                    break;
                case "playerPositions":
                    assert gameClient.isPresent();
                    HashMap<Integer, Integer> playerPositions = new HashMap<>();
                    for (int i = 1; i < tokens.length; i += 2) {
                        playerPositions.put(Integer.parseInt(tokens[i]), Integer.parseInt(tokens[i + 1]));
                    }
                    gameClient.get().setPositions(playerPositions);
                    break;
                case "stackSizes":
                    assert gameClient.isPresent();
                    HashMap<Integer, Long> stackSizes = new HashMap<>();
                    for (int i = 1; i < tokens.length; i += 2) {
                        stackSizes.put(Integer.parseInt(tokens[i]), Long.parseLong(tokens[i + 1]));
                    }
                    gameClient.get().setStackSizes(stackSizes);
                    break;
                case "smallBlind":
                    assert gameClient.isPresent();
                    gameClient.get().setSmallBlind(Long.parseLong(tokens[1]));
                    break;
                case "bigBlind":
                    assert gameClient.isPresent();
                    gameClient.get().setBigBlind(Long.parseLong(tokens[1]));
                    break;
                case "setHand":
                    assert gameClient.isPresent();
                    gameClient.get().setHandForClient(Integer.parseInt(tokens[1]), parseCard(tokens[2]), parseCard(tokens[3]));
                    break;
                case "setFlop":
                    assert gameClient.isPresent();
                    gameClient.get().setFlop(parseCard(tokens[1]), parseCard(tokens[2]), parseCard(tokens[3]));
                    break;
                case "setTurn":
                    assert gameClient.isPresent();
                    gameClient.get().setTurn(parseCard(tokens[1]));
                    break;
                case "setRiver":
                    assert gameClient.isPresent();
                    gameClient.get().setRiver(parseCard(tokens[1]));
                    break;
                case "getDecision": {
                    assert gameClient.isPresent();
                    Decision decision = gameClient.get().getDecision(Long.parseLong(tokens[1]));
                    socketOutput.write(decisionToString(decision) + "\n");
                    socketOutput.flush();
                    break;
                }
                case "playerMadeDecision":
                    assert gameClient.isPresent();
                    Optional<Decision> decision = parseDecision(tokens[2]);
                    if (!decision.isPresent()) {
                        System.out.println("Couldn't parse decision " + tokens[2]);
                    }
                    int id = Integer.parseInt(tokens[1]);
                    gameClient.get().playerMadeDecision(id,  decision.get());
                    break;
                default:
                    System.out.println("Received unrecognized command \"" + input + "\"");
            }
        }
    }

    public static String decisionToString(Decision decision) {
        if (decision.move == Decision.Move.RAISE || decision.move == Decision.Move.BET) {
            return decision.move.toString().toLowerCase() + decision.getSize();
        }
        else if (decision.move == Decision.Move.BIG_BLIND) {
            return "bigBlind";
        }
        else if (decision.move == Decision.Move.SMALL_BLIND) {
            return "smallBlind";
        }
        else {
            return decision.move.toString().toLowerCase();
        }
    }

    public static Optional<Decision> parseDecision(String string) {
        int firstDigitIndex = 0;
        for (int i = 0; i < string.length(); i++) {
            if (Character.isDigit(string.charAt(i))) {
                firstDigitIndex = i;
                break;
            }
        }
        if (firstDigitIndex == 0) {
            try {
                return Optional.of(new Decision(parseMove(string).get()));
            }
            catch (NoSuchElementException | IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        else {
            try {
                return Optional.of(new Decision(parseMove(string.substring(0, firstDigitIndex)).get(), Long.parseLong(string.substring(firstDigitIndex))));
            }
            catch (NoSuchElementException | IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }

    public static Optional<Decision.Move> parseMove(String string) {
        switch (string) {
            case "smallBlind":
                return Optional.of(Decision.Move.SMALL_BLIND);
            case "bigBlind":
                return Optional.of(Decision.Move.BIG_BLIND);
            default:
                try {
                    return Optional.of(Decision.Move.valueOf(string.toUpperCase()));
                }
                catch (IllegalArgumentException e) {
                    return Optional.empty();
                }
        }
    }

    public static Card parseCard(String input) {
        if (input.startsWith("spades")) {
            int rank = Integer.parseInt(input.substring("spades".length()));
            return Card.of(rank, Card.Suit.SPADES).get();
        }
        else if (input.startsWith("hearts")) {
            int rank = Integer.parseInt(input.substring("hearts".length()));
            return Card.of(rank, Card.Suit.HEARTS).get();
        }
        else if (input.startsWith("diamonds")) {
            int rank = Integer.parseInt(input.substring("diamonds".length()));
            return Card.of(rank, Card.Suit.DIAMONDS).get();
        }
        else if (input.startsWith("clubs")) {
            int rank = Integer.parseInt(input.substring("clubs".length()));
            return Card.of(rank, Card.Suit.CLUBS).get();
        }
        throw new IllegalArgumentException("Couldn't parse card " + input);
    }
}
