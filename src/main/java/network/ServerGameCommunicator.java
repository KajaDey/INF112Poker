package network;

import gamelogic.Decision;
import gamelogic.GameClient;
import gui.GUIMain;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by morten on 27.04.16.
 *
 */
public class ServerGameCommunicator {
    private final String playerName;
    private Optional<GameClient> gameClient = Optional.empty();
    private final BufferedReader socketReader;
    private final BufferedWriter socketWriter;

    /**
     * * Creates a new ServerGameCommunicator to the given ip, but does not start communication
     * @param out An open output stream to the Server TCP socket
     * @param in An open input stream from the Server TCP socket
     * @param playerName The player's chosen name
     */
    public ServerGameCommunicator(BufferedWriter out, BufferedReader in, String playerName) {
        this.socketReader = in;
        this.socketWriter = out;
        this.playerName = playerName;
    }

    /**
     * Starts upi communication with the server
     * Does not return until the game ends
     * @throws IOException
     */
    public void startUpi() throws IOException {
        writeToSocket("upi 0.1");

        String input = socketReader.readLine();
        if (!input.equals("upiok"))
            throw new IOException("Received " + input + " from server, expected upiok");
         else
            System.out.println("Client " + playerName + " received upiok from server");

        while (true) {
            input = socketReader.readLine();

            String[] tokens = UpiUtils.tokenize(input).get();
            if (tokens.length == 0) {
                throw new IOException("Received empty command \"" + input + "\" from server");
            }

            switch (tokens[0]) {
                case "getName":
                    writeToSocket("playerName \"" + playerName + "\"");
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
                    gameClient = Optional.of(GUIMain.guiMain.displayGameScreen(Integer.parseInt(tokens[1])));
                    try { // Wait for a bit for the GUI to get ready
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        System.out.println("Thread " + Thread.currentThread() + " interrupted.");
                    }
                    //Set the chat listener
                    gameClient.get().setChatListener(this::chatListener);
                    break;
                case "playerNames":
                    assert gameClient.isPresent();
                    HashMap<Integer, String> playerNames = new HashMap<>();
                    try {
                        for (int i = 1; i < tokens.length; i += 2) {
                            playerNames.put(Integer.parseInt(tokens[i]), tokens[i + 1]);
                        }
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage());
                        System.out.println("Failed to parse " + input);
                        break;
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
                    gameClient.get().setHandForClient(Integer.parseInt(tokens[1]), UpiUtils.parseCard(tokens[2]), UpiUtils.parseCard(tokens[3]));
                    break;
                case "setFlop":
                    assert gameClient.isPresent();
                    gameClient.get().setFlop(UpiUtils.parseCard(tokens[1]), UpiUtils.parseCard(tokens[2]), UpiUtils.parseCard(tokens[3]));
                    break;
                case "setTurn":
                    assert gameClient.isPresent();
                    gameClient.get().setTurn(UpiUtils.parseCard(tokens[1]));
                    break;
                case "setRiver":
                    assert gameClient.isPresent();
                    gameClient.get().setRiver(UpiUtils.parseCard(tokens[1]));
                    break;
                case "playerBust":
                    assert gameClient.isPresent();
                    assert tokens.length >= 3;
                    gameClient.get().playerBust(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                    break;
                case "showdown":
                    assert gameClient.isPresent();
                    String[] lines = Arrays.copyOfRange(tokens, 1, tokens.length);
                    System.out.println("Sending " + Arrays.toString(lines) + " to the client");
                    gameClient.get().showdown(lines);
                    break;
                case "getDecision": {
                    assert gameClient.isPresent();
                    new Thread(() -> {
                        Decision decision = gameClient.get().getDecision(Long.parseLong(tokens[1]));
                        writeToSocket(UpiUtils.decisionToString(decision));
                    }).start();
                    break;
                }
                case "playerMadeDecision":
                    assert gameClient.isPresent();
                    Optional<Decision> decision = UpiUtils.parseDecision(tokens[3]);
                    if (!decision.isPresent()) {
                        System.out.println("Couldn't parse decision " + tokens[2]);
                    }
                    int id = Integer.parseInt(tokens[1]);
                    gameClient.get().playerMadeDecision(id, decision.get());
                    break;
                case "logPrint":
                    assert gameClient.isPresent();
                    if (gameClient.isPresent())
                        gameClient.get().printToLogField(tokens[1]);
                    break;
                default:
                    System.out.println("Received unrecognized command \"" + input + "\"");
            }
        }
    }

    /**
     *  Write to socket
     *  @param string to write
     */
    private boolean writeToSocket(String string) {
        try {
            socketWriter.write(string + "\n");
            socketWriter.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send a chat message to the server
     */
    public void chatListener(String chatMessage) {
        if (!writeToSocket("chat \"" + chatMessage + "\""))
            System.out.println("Failed to write chat message, " + chatMessage);
    }
}
