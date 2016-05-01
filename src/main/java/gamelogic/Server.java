package gamelogic;

import gui.GameSettings;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Kristian Rosland on 27.04.2016.
 *
 */
public class Server {

    private int playerIdCounter = 0, tableIdCounter = 0;
    public ServerSocket serverSocket;
    public ArrayList<LobbyPlayer> lobbyPlayers = new ArrayList<>();
    public Map<Integer, LobbyTable> lobbyTables = new HashMap<>();

    public Server() {
        try {
            serverSocket = new ServerSocket(39100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        start();
    }

    public static void main(String ... args){
        Server server = new Server();
    }

    public void start() {
        Thread server = new Thread("ConnectionListener"){
            @Override
            public void run() {
                while(true) {
                    try {
                        System.out.println("Server listening for connection..");
                        Socket socket = serverSocket.accept();
                        System.out.println("Connection established with " + socket.getInetAddress());

                        addNewClientSocket(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        server.start();
    }

    public void addNewClientSocket(Socket socket) {
        lobbyPlayers.add(new LobbyPlayer(socket, playerIdCounter++));
    }

    private synchronized void removeClient(int id) {
        Optional<LobbyPlayer> op = lobbyPlayers.stream().filter(client -> client.id == id).findAny();
        if (op.isPresent()) {
            try {
                op.get().socket.close();
                lobbyPlayers.remove(op.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //TODO: If player is seated, remove from table
    }

    private void addNewTable(GameSettings settings, LobbyPlayer host) {
        LobbyTable table = new LobbyTable(tableIdCounter, settings, host);
        lobbyTables.put(tableIdCounter, table);
        tableIdCounter++;
    }

    class LobbyPlayer {
        final int id;
        final Socket socket;
        final Thread listener;
        String playerName;
        BufferedReader reader;
        BufferedWriter writer;

        public LobbyPlayer(Socket s, int id) {
            this.socket = s;
            this.id = id;

            Runnable task = () -> {
                    try {
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                        String input = reader.readLine();

                        if (input.startsWith("lobby")) {
                            writer.write("lobbyok\n");
                        } else {
                            writer.write("lobbynotok\n");
                            removeClient(id);
                            return;
                        }

                        playerName = input.substring("lobby".length());

                        while(true) {
                            String [] tokens = reader.readLine().split("\\s+");

                            switch (tokens[0]) {
                                case "quit":
                                    removeClient(id);
                                    return;
                                case "takeseat": {
                                    int tableID = Integer.parseInt(tokens[1]);
                                    if (lobbyTables.containsKey(tableID))
                                        lobbyTables.get(tableID).seatPlayer(this);
                                    break;
                                }
                                case "createtable":
                                    long stack = Long.parseLong(tokens[1]),
                                            smallBlind = Long.parseLong(tokens[2]),
                                            bigBlind = Long.parseLong(tokens[3]);
                                    int maxPlayers = Integer.parseInt(tokens[4]),
                                            levelDuration = Integer.parseInt(tokens[5]);

                                    GameSettings settings = new GameSettings(stack, bigBlind, smallBlind, maxPlayers, levelDuration, AIType.MCTS_AI);
                                    addNewTable(settings, this);
                                    break;
                                case "changesetting":
                                    changeSetting(tokens);
                                    break;
                                case "":
                                    break;
                            }
                        }

                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        removeClient(id);
                        return;
                    }

            };
            listener = new Thread(task);
            listener.start();
        }

        private void changeSetting(String [] tokens) {
            int tableID = Integer.parseInt(tokens[1]);
            if (!lobbyTables.containsKey(tableID) || !lobbyTables.get(tableID).host.equals(this)) {
                //If the table does not exist, or the player is not the host, don't change the settings
                return;
            } else {
                //TODO: Change settings for this table based on the tokens[2] value
                GameSettings s = lobbyTables.get(tableID).settings;
            }
        }
    }

    class LobbyTable {
        final int tableID;
        ArrayList<LobbyPlayer> seatedPlayers;
        LobbyPlayer host;
        GameSettings settings;

        public LobbyTable(int id, GameSettings settings, LobbyPlayer host) {
            this.tableID = id;
            this.settings = settings;
            this.seatedPlayers = new ArrayList<>();
            this.seatedPlayers.add(host);
            this.host = host;
        }

        public boolean seatPlayer(LobbyPlayer player) {
            if (seatedPlayers.size() > settings.getMaxNumberOfPlayers()) {
                return false;
            } else {
                seatedPlayers.add(player);
                return true;
            }
        }
    }


}
