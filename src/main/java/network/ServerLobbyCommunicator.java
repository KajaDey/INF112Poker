package network;

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
 *
 * This class acts as a communicator between the LobbyScreen and the Server.
 *
 */
public class ServerLobbyCommunicator {

    private final Socket clientSocket;
    private final BufferedReader socketReader;
    private final BufferedWriter socketWriter;
    private final Map<Integer, String> names;
    private final LobbyScreen lobbyScreen;

    /**
     * Initializes the ServerLobbyCommunicator, handshakes with the server and
     * receives information about all the players from the server
     * @param name Name of the player
     */
    public ServerLobbyCommunicator(String name, LobbyScreen lobbyScreen, InetAddress serverAddress) throws IOException {
        this.lobbyScreen = lobbyScreen;
        Socket tempSocket = new Socket();
        // Attempt to connect to server up to 20 times before giving up
        for (int i = 0; i < 20; i++) {
            try {
                tempSocket = new Socket(); // New socket must be created on every iteration, for some reason
                tempSocket.connect(new InetSocketAddress(serverAddress, 39100), 1000);
                break;
            }
            catch (IOException e) {
                System.out.println("Failed to connect to " + serverAddress + ". Retrying...");
                try {
                    Thread.sleep(1500L);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        if (tempSocket.isConnected()) {
            clientSocket = tempSocket;
        }
        else {
            throw new IOException("Failed to connect to " + serverAddress);
        }
        socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
        socketWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));

        //Establish handshake with server
        writeToSocket("lobby " + name);
        {
            String input = socketReader.readLine();
            if (input.equals("lobbyok")) {
                System.out.println("Received handshake from client");
            } else {
                throw new IOException("Received handshake " + input + " from server, expected \"lobby\"");
            }
        }

        names = new HashMap<>();
        // Receive all information about the lobby
        getInit: while (true) {
            String input = socketReader.readLine();
            String[] tokens = UpiUtils.tokenize(input).get();
            System.out.println(input);

            switch (tokens[0]) {
                case "lobbySent":
                    break getInit;
                case "yourId":
                    assert tokens.length > 1 : "Couldn't parse " + input;
                    System.out.println("Received id " + tokens[1] + " from server");
                    this.lobbyScreen.setID(Integer.parseInt(tokens[1]));
                    break;
                case "playerNames":
                    for (int i = 1; i < tokens.length; i+=2)
                        names.put(Integer.parseInt(tokens[i]), tokens[i + 1]);
                    break;
                case "table":
                    int id = Integer.parseInt(tokens[1]);
                    LobbyTable table = new LobbyTable(id);
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

                    Platform.runLater(()->lobbyScreen.addTable(table));
                    break;
                default:
                    System.out.println("Received unknown init command " + tokens[0]);
            }
        }

        Runnable serverListener = () -> {
            while (true) {
                String input;
                try {
                    input = socketReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                String[] tokens = UpiUtils.tokenize(input).get();
                switch (tokens[0]) {
                    case "startGame":
                        goToGameScreen();
                        // Stop listening for the server here
                        return;
                    case "playerJoinedLobby":
                        names.put(Integer.parseInt(tokens[1]), tokens[2]);
                        System.out.println("Player joined lobby, id: " + tokens[1] + " name" + tokens[2]);
                        break;
                    case "playerLeftLobby":
                        names.remove(Integer.parseInt(tokens[1]));
                        System.out.println("Player left lobby, p.id: " + tokens[1]);
                        break;
                    case "playerJoinedTable":
                        Platform.runLater(()->lobbyScreen.addPlayer(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2])));
                        System.out.println("Player joined table, p.id:" + tokens[1] + " t.id:" + tokens[2]);
                        break;
                    case "playerLeftTable":
                        Platform.runLater(()->lobbyScreen.removePlayer(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2])));
                        System.out.println("Player left table, p.id:" + tokens[1] + " t.id:" + tokens[2]);
                        break;
                    case "tableCreated":
                        //Make new table with default settings, tableSettings will follow shortly after this command anyway
                        System.out.println("New table created, tableID: " + tokens[1]);
                        Platform.runLater(()->lobbyScreen.addTable(new LobbyTable(Integer.parseInt(tokens[1]))));
                        break;
                    case "tableSettings":
                        int tableID = Integer.parseInt(tokens[1]);
                        Platform.runLater(() -> updateSettings(tableID, tokens));
                        break;
                    case "tableDeleted":
                        System.out.println("Table deleted, tableID: " + tokens[1]);
                        Platform.runLater(()->lobbyScreen.removeTable(Integer.parseInt(tokens[1])));
                        break;
                    default:
                        System.out.println("Unknown command \"" + tokens[0] + "\", ignoring...");
                }
            }
        };
        new Thread(serverListener).start();
    }

    public void startGame(int tableID) {
        writeToSocket("startgame " + tableID);
    }

    public void setNewSettings(GameSettings newSettings, int tableID) {
        writeToSocket("changesettings " + tableID + " " + UpiUtils.settingsToString(newSettings));
    }


    private void updateSettings(int tableID, String[] tokens) {
        assert lobbyScreen.getTable(tableID) != null : "Table with id " + tableID + " does not exist. " + tokens.toString();

        LobbyTable table = lobbyScreen.getTable(tableID);
        for (int i = 0; i < tokens.length; i++) {
            switch(tokens[i]) {
                case "maxNumberOfPlayers":
                case "startStack":
                case "smallBlind":
                case "bigBlind":
                case "levelDuration":
                    table.parseSetting(tokens[i], tokens[i+1]);
                    break;
            }
        }

        Platform.runLater(()->lobbyScreen.refreshTableSettings(tableID));

    }

    public void quit() {
        writeToSocket("quit");
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Closing socket failed. Shutting down anyway.");
            e.printStackTrace();
        }
    }

    /**
     * Write to socket (adds new line)
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
        writeToSocket("takeseat " + tableID);
    }

    public void makeNewTable() {
        writeToSocket("createtable " + UpiUtils.settingsToString(new GameSettings(GameSettings.DEFAULT_SETTINGS)));
    }

    /**
     *  Called when the server tell the client to start a game
     */
    public void goToGameScreen() {
        int id = lobbyScreen.getID();
        System.out.println("Client " + id + ": Going to game screen");
        ServerGameCommunicator serverGameCommunicator = new ServerGameCommunicator(socketWriter, socketReader, names.get(id));

        System.out.println("Client " + id + ": Starting upi communication");
        try {
            serverGameCommunicator.startUpi();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}