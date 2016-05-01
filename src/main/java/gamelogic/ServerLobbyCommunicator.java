package gamelogic;

import gui.GameLobby;
import gui.GameSettings;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kristian Rosland on 27.04.2016.
 */
public class ServerLobbyCommunicator {

    final Socket clientSocket;
    final private BufferedReader socketReader;
    final private BufferedWriter socketWriter;
    final private Map<Integer, String> names;
    final private Map<Integer, Table> lobbies;

    /**
     * Initializes the ServerLobbyCommunicator, handshakes with the server and
     * receives informations about all the players from the server
     * @param name
     */
    public ServerLobbyCommunicator(String name) throws IOException {

        //Handshake

        clientSocket = new Socket(InetAddress.getLocalHost(), 39100);
        socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
        socketWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));

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
        lobbies = new HashMap<>();
        // Receive all information about the lobby
        getInit: while (true) {
            String input = socketReader.readLine();
            String[] tokens = input.split("\\r+");

            switch (tokens[0]) {
                case "lobbySent":
                    break getInit;
                case "playerNames":
                    for (int i = 1; i < tokens.length; i++) {
                        names.put(Integer.parseInt(tokens[i]), tokens[i + 1]);
                    }
                    break;
                case "table":
                    int id = Integer.parseInt(tokens[1]);
                    Table table = new Table(id);
                    lobbies.put((id), table);
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
                    default:
                        System.out.println("Unknown command " + tokens[0] + ", ignoring...");
                    //TODO: Implement more commands
                }
            }
        };
        new Thread(serverListener).start();

        GameLobby lobby = new GameLobby();
        lobby.createMultiPlayerLobbyScreen();

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
                    settings.setSmallBlind(Integer.parseInt(value));
                    break;
                //TODO: Implement more settings
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

    private void writeToSocket(String output) {
        try {
            socketWriter.write(output + "\n");
            socketWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
