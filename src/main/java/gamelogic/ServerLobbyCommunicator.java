package gamelogic;

import gui.GameLobby;
import gui.GameSettings;
import gui.LobbyScreen;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kristian Rosland on 27.04.2016.
 *
 * This class acts as a communicator between the LobbyScreen and the Server.
 *
 */
public class ServerLobbyCommunicator {

    final Socket clientSocket;
    final private BufferedReader socketReader;
    final private BufferedWriter socketWriter;
    final private Map<Integer, String> names;
    final private Map<Integer, Table> tables;
    final private LobbyScreen lobbyScreen;

    /**
     * Initializes the ServerLobbyCommunicator, handshakes with the server and
     * receives information about all the players from the server
     * @param name Name of the player
     */
    public ServerLobbyCommunicator(String name, LobbyScreen lobbyScreen) throws IOException {
        this.lobbyScreen = lobbyScreen;

        clientSocket = new Socket(InetAddress.getLocalHost(), 39100);
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
        tables = new HashMap<>();
        // Receive all information about the lobby
        getInit: while (true) {
            String input = socketReader.readLine();
            String[] tokens = input.split("\\r+");

            switch (tokens[0]) {
                case "lobbySent":
                    break getInit;
                case "playerNames":
                    for (int i = 1; i < tokens.length; i++)
                        names.put(Integer.parseInt(tokens[i]), tokens[i + 1]);
                    break;
                case "table":
                    int id = Integer.parseInt(tokens[1]);
                    Table table = new Table(id);
                    tables.put((id), table);
                    if (!tokens[2].equals("settings")) {
                        throw new IOException();
                    }
                    int i = 3;
                    while (!tokens[i].equals("players")) {
                        table.parseSetting(tokens[i], tokens[i + 1]);
                        i += 2;
                    }
                    while (i < tokens.length) {
                        table.playerIds.add(Integer.parseInt(tokens[i]));
                    }
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
                String[] tokens = input.split("\\r+");
                switch (tokens[0]) {
                    case "playerJoinedLobby":
                        names.put(Integer.parseInt(tokens[1]), tokens[2]);
                        break;
                    case "playerLeftLobby":
                        names.remove(Integer.parseInt(tokens[1]));
                        break;
                    case "playerJoinedTable":
                        break;
                    case "playerLeftTable":
                        break;
                    case "tableCreated":
                        //Make new table with default settings, tableSettings will follow shortly after this command anyway
                        break;
                    case "tableSettings":
                        int tableID = Integer.parseInt(tokens[1]);
                        updateSettings(tableID, tokens);

                        break;
                    case "tableDeleted":
                        break;

                    default:
                        System.out.println("Unknown command " + tokens[0] + ", ignoring...");
                }
            }
        };
        new Thread(serverListener).start();

        GameLobby lobby = new GameLobby();
        lobby.createMultiPlayerLobbyScreen();

    }

    public void startGame(int tableID) {
        writeToSocket("startgame " + tableID);
    }

    public void setNewSettings(GameSettings newSettings, int tableID) {
        writeToSocket("changesettings " + tableID + " " + settingsToString(newSettings));
    }


    private void updateSettings(int tableID, String[] tokens) {
        assert tables.containsKey(tableID) : "Trying to edit settings on table " + tableID + " that does not exist.";

        Table table = tables.get(tableID);
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
    }

    private class Table {
        final int id;
        final GameSettings settings;
        final ArrayList<Integer> playerIds = new ArrayList<>();

        private Table(int id) {
            this.id = id;
            settings = new GameSettings(GameSettings.DEFAULT_SETTINGS);
        }

        public void parseSetting(String name, String value) {
            switch (name) {
                case "smallBlind":
                    settings.setSmallBlind(Long.parseLong(value));
                    break;
                case "bigBlind":
                    settings.setBigBlind(Long.parseLong(value));
                    break;
                case "maxNumberOfPlayers":
                    settings.setMaxNumberOfPlayers(Integer.parseInt(value));
                    break;
                case "startStack":
                    settings.setStartStack(Long.parseLong(value));
                    break;
                case "levelDuration":
                    settings.setLevelDuration(Integer.parseInt(value));
                    break;
                default:
                    System.out.println("Received unknown table setting " + name + ", ignoring...");
            }
        }
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

    /**
     * Convert settings of this table to a string matching the Lobby Protocol
     * @return <<setting1, value1> <setting2, value2> ... >
     */
    public static String settingsToString(GameSettings settings) {
        return String.format("maxNumberOfPlayers %d startStack %d smallBlind %d bigBlind %d levelDuration %d",
                settings.getMaxNumberOfPlayers(), settings.getStartStack(), settings.getSmallBlind(), settings.getBigBlind(),
                settings.getLevelDuration()).trim();
    }
}
