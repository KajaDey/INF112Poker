package network;

import gamelogic.Logger;
import gui.*;
import javafx.application.Platform;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kristian Rosland on 27.04.2016.
 * <p>
 * This class acts as a communicator between the LobbyScreen and the Server.
 */
public class ServerLobbyCommunicator {

    private Socket clientSocket;
    private BufferedReader socketReader;
    private BufferedWriter socketWriter;
    private Map<Integer, String> names;
    private final LobbyScreen lobbyScreen;
    private final Logger logger;
    private Thread listeningThread;
    private InetAddress serverAddress;
    private String name;

    /**
     * Initializes the ServerLobbyCommunicator, handshakes with the server and
     * receives information about all the players from the server
     *
     * @param name Name of the player
     * @param lobbyScreen Lobby screen made by the player
     * @param serverAddress IP-address of the server
     * @throws IOException
     */
    public ServerLobbyCommunicator(String name, LobbyScreen lobbyScreen,
                                   InetAddress serverAddress, Logger logger) throws IOException {
        this.name = name;
        this.logger = logger;
        this.lobbyScreen = lobbyScreen;
        this.serverAddress = serverAddress;
    }

    private void connectClientToServer() throws IOException {
        Socket tempSocket = new Socket();
        // Attempt to connect to server up to 5 times before giving up
        for (int i = 0; i < 5; i++) {
            try {
                tempSocket = new Socket(); // New socket must be created on every iteration, for some reason
                tempSocket.connect(new InetSocketAddress(serverAddress, 39100), 1000);
                break;
            }
            catch (IOException e) {
                logger.println("Failed to connect to " + serverAddress + ". Retrying...", Logger.MessageType.NETWORK);
                try {
                    Thread.sleep(1500L);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        if (tempSocket.isConnected()) {
            clientSocket = tempSocket;
        } else {
            throw new IOException("Failed to connect to " + serverAddress);
        }
    }

    public void start() throws IOException {
        connectClientToServer();

        socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
        socketWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));

        //Establish handshake with server
        writeToSocket("lobby " + name);
        {
            String input = readFromServer();
            if (input.equals("lobbyok")) {
                logger.println("Received handshake from client", Logger.MessageType.NETWORK);
            } else {
                throw new IOException("Received handshake " + input + " from server, expected \"lobby\"");
            }
        }

        names = new HashMap<>();
        // Receive all information about the lobby
        getInit: while (true) {
            String input = readFromServer();
            String[] tokens = UpiUtils.tokenize(input).get();
            logger.println("Server: " + input, Logger.MessageType.NETWORK_DEBUG);

            switch (tokens[0]) {
                case "lobbySent":
                    break getInit;
                case "yourId":
                    assert tokens.length > 1 : "Couldn't parse " + input;
                    logger.println("Received id " + tokens[1] + " from server", Logger.MessageType.NETWORK);
                    this.lobbyScreen.setID(Integer.parseInt(tokens[1]));
                    break;
                case "playerNames":
                    for (int i = 1; i < tokens.length; i += 2)
                        names.put(Integer.parseInt(tokens[i]), tokens[i + 1]);
                    break;
                case "table":
                    int id = Integer.parseInt(tokens[1]);
                    LobbyTable table = new LobbyTable(id, logger);
                    if (!tokens[2].equals("settings")) {
                        throw new IOException();
                    }
                    int i = 3;
                    while (!tokens[i].equals("players")) {
                        table.parseSetting(tokens[i], tokens[i + 1]);
                        i += 2;
                    }
                    i++;
                    while (i < tokens.length)
                        table.addPlayer(Integer.parseInt(tokens[i++]));

                    Platform.runLater(() -> lobbyScreen.addTable(table));
                    break;
                default:
                    logger.println("Received unknown init command " + tokens[0], Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
            }
        }

        listeningThread = listenForInputsFromServer();
    }

    private Thread listenForInputsFromServer() {
        Runnable serverListener = () -> {
            while (true) {
                String input;
                try {
                    input = readFromServer();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                String[] tokens = UpiUtils.tokenize(input).get();

                if (tokens.length < 1)
                    continue;
                switch (tokens[0]) {
                    case "startGame":
                        goToGameScreen();
                        return;
                    case "playerJoinedLobby":
                        names.put(Integer.parseInt(tokens[1]), tokens[2]);
                        logger.println("Player joined lobby, id: " + tokens[1] + " name" + tokens[2], Logger.MessageType.NETWORK);
                        break;
                    case "playerLeftLobby":
                        names.remove(Integer.parseInt(tokens[1]));
                        Platform.runLater(() ->lobbyScreen.playerQuit(Integer.parseInt(tokens[1])));
                        logger.println("Player left lobby, p.id: " + tokens[1], Logger.MessageType.NETWORK);
                        break;
                    case "playerJoinedTable":
                        Platform.runLater(()->lobbyScreen.addPlayer(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2])));
                        logger.println("Player joined table, p.id:" + tokens[1] + " t.id:" + tokens[2], Logger.MessageType.NETWORK);
                        break;
                    case "playerLeftTable":
                        Platform.runLater(()->lobbyScreen.removePlayer(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2])));
                        logger.println("Player left table, p.id:" + tokens[1] + " t.id:" + tokens[2], Logger.MessageType.NETWORK);
                        break;
                    case "tableCreated":
                        //Make new table with default settings, tableSettings will follow shortly after this command anyway
                        logger.println("New table created, tableID: " + tokens[1], Logger.MessageType.NETWORK);
                        Platform.runLater(()->lobbyScreen.addTable(new LobbyTable(Integer.parseInt(tokens[1]), logger)));
                        break;
                    case "tableSettings":
                        int tableID = Integer.parseInt(tokens[1]);
                        Platform.runLater(() -> updateSettings(tableID, tokens[2]));
                        break;
                    case "tableDeleted":
                        logger.println("Table deleted, tableID: " + tokens[1], Logger.MessageType.NETWORK);
                        Platform.runLater(()->lobbyScreen.removeTable(Integer.parseInt(tokens[1])));
                        break;
                    case "errorMessage":
                        Platform.runLater(() -> lobbyScreen.displayErrorMessage(tokens[1]));
                        break;
                    default:
                        logger.println("Unknown command \"" + tokens[0] + "\", ignoring...", Logger.MessageType.NETWORK);
                }
            }
        };
        Thread listening = new Thread(serverListener);
        listening.start();
        return listening;
    }

    public String readFromServer() throws IOException{
        return socketReader.readLine();
    }

    public void startGame(int tableID) {
        writeToSocket("startGame " + tableID);
    }

    public void setNewSettings(GameSettings newSettings, int tableID) {
        writeToSocket("changeSettings " + tableID + " \"" + UpiUtils.settingsToString(newSettings) + "\"");
    }

    public void deleteTable(int tableID) {
        writeToSocket("deleteTable " + tableID);
    }

    private void updateSettings(int tableID, String tokensString) {
        assert lobbyScreen.getTable(tableID) != null : "Table with id " + tableID + " does not exist. " + tokensString;

        String [] tokens = UpiUtils.tokenize(tokensString).get();
        LobbyTable table = lobbyScreen.getTable(tableID);
        for (int i = 0; i < tokens.length; i++) {
            switch (tokens[i]) {
                case "maxNumberOfPlayers":
                case "startStack":
                case "smallBlind":
                case "bigBlind":
                case "levelDuration":
                case "playerClock":
                case "aiType":
                    table.parseSetting(tokens[i], tokens[i + 1]);
                    break;
            }
        }

        Platform.runLater(() -> lobbyScreen.refreshTableSettings(tableID));

    }

    public void quit() {
        writeToSocket("quit");
        try {
            clientSocket.close();
        } catch (IOException e) {
            logger.println("Closing socket failed. Shutting down anyway.", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
            e.printStackTrace();
        }
    }

    /**
     * Write to socket (adds new line)
     *
     * @param output Message to write
     */
    private void writeToSocket(String output) {
        try {
            socketWriter.write(output + "\n");
            socketWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName(Integer playerID) {
        return names.get(playerID);
    }

    public void takeSeat(int tableID) {
        writeToSocket("takeSeat " + tableID);
    }

    public void leaveSeat(int tableID) { writeToSocket("leaveSeat " + tableID); }

    public void makeNewTable() {
        writeToSocket("createTable \"" + UpiUtils.settingsToString(new GameSettings()) + "\"");
    }

    /**
     * Called when the server tell the client to start a game
     */
    public void goToGameScreen() {
        int id = lobbyScreen.getID();
        logger.println("Client " + id + ": Going to game screen", Logger.MessageType.NETWORK);
        ServerGameCommunicator serverGameCommunicator = new ServerGameCommunicator(socketWriter, socketReader, names.get(id), logger);

        logger.println("Client " + id + ": Starting upi communication", Logger.MessageType.NETWORK);
        try {
            serverGameCommunicator.startUpi();
        } catch (IOException e) {
            Platform.runLater(() -> lobbyScreen.displayErrorMessage(e.getMessage()));
        }
    }

    public Thread getListeningThread() {
        return listeningThread;
    }
}
