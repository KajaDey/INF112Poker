package gamelogic;

import gui.GUIMain;
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
        int tableID = tableIdCounter++;
        LobbyTable table = new LobbyTable(tableID, settings, host);
        lobbyTables.put(tableID, table);

        lobbyPlayers.forEach(p -> {
            p.write("tableCreated " + tableID);
            p.write("tableSettings " + tableID + " " + table.settingsToString());
            p.write("playerJoinedTable " + tableID + " " + host.id);
        });
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
                            write("lobbyok");
                        } else {
                            write("lobbynotok");
                            removeClient(id);
                            return;
                        }

                        playerName = input.substring("lobby".length());

                        write("yourId " + id);
                        sendLobbyInfo();

                        broadcastMessage("playerJoinedLobby " + id + " " + playerName);

                        while(true) {
                            String line = reader.readLine();
                            if (line == null)
                                break;
                            String [] tokens = line.split("\\s+");

                            switch (tokens[0]) {
                                case "quit":
                                    removeClient(id);
                                    broadcastMessage("playerLeftLobby " + id);
                                    return;
                                case "takeseat": {
                                    int tableID = Integer.parseInt(tokens[1]);
                                    if (lobbyTables.containsKey(tableID)) {
                                        if(lobbyTables.get(tableID).seatPlayer(this)) {
                                            broadcastMessage("playerJoinedTable " + tableID + " " + this.id);
                                        }
                                    }
                                    break;
                                }
                                case "createtable":
                                    long stack = Long.parseLong(tokens[2]),
                                            smallBlind = Long.parseLong(tokens[4]),
                                            bigBlind = Long.parseLong(tokens[6]);
                                    int maxPlayers = Integer.parseInt(tokens[8]),
                                            levelDuration = Integer.parseInt(tokens[10]);
                                    //TODO: Add a token for playerClock when
                                    int playerClock = Integer.parseInt("30");

                                    GameSettings settings = new GameSettings(stack, bigBlind, smallBlind, maxPlayers, levelDuration, AIType.MCTS_AI, playerClock);
                                    addNewTable(settings, this);
                                    break;
                                case "changesettings":
                                    changeSetting(tokens);
                                    break;
                                case "startgame": {
                                    int tableID = Integer.parseInt(tokens[1]);
                                    if (lobbyTables.containsKey(tableID)) {
                                        LobbyTable t = lobbyTables.get(tableID);
                                        t.startGame();
                                        broadcastMessage("tableDeleted " + tableID);
                                        t.seatedPlayers.forEach(p -> broadcastMessage("playerLeftLobby " + p.id));
                                    }
                                    break;
                                }
                                case "deletetable":
                                    int tableID = Integer.parseInt(tokens[1]);
                                    if (lobbyTables.containsKey(tableID)) {
                                        lobbyTables.get(tableID).delete();
                                        lobbyTables.remove(tableID);
                                        broadcastMessage("tableDeleted " + tableID);
                                    }
                                    break;
                                default:
                                    System.out.println("Unknown command, " + tokens[0]);
                            }
                        }

                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        removeClient(id);
                    }

            };
            listener = new Thread(task);
            listener.start();
        }

        private void broadcastMessage(String message) {
            lobbyPlayers.forEach(p -> p.write(message));
        }

        /**
         * Send lobby info to client (specified in Lobby Protocol)
         */
        private void sendLobbyInfo() {
            //Send all player names: playerNames <id1 name1> osv.
            String allPlayerNames = "playerNames ";
            for (LobbyPlayer p : lobbyPlayers)
                allPlayerNames += p.id + " " + p.playerName + " ";

            //Send all tables: table <id> settings <<setting1, value1> ...> players <<id1><id2>..>
            ArrayList<String> allTables = new ArrayList<>();
            lobbyTables.forEach((id, table) -> allTables.add(table.toString()));

            //Send lobbySent
            write(allPlayerNames);
            allTables.forEach(this::write);
            write("lobbySent");
        }

        private void changeSetting(String [] tokens) {
            int tableID = Integer.parseInt(tokens[1]);
            if (lobbyTables.containsKey(tableID) && lobbyTables.get(tableID).host.equals(this)) {
                //TODO: Change settings for this table based on the tokens[2] value
                GameSettings s = lobbyTables.get(tableID).settings;
            }
        }

        /**
         * Use to write to socket (adds newline)
         * @param msg The message to write, without \n
         */
        public void write(String msg) {
            try {
                writer.write(msg + "\n");
                writer.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    class LobbyTable {
        final int tableID;
        ArrayList<LobbyPlayer> seatedPlayers;
        LobbyPlayer host;
        GameSettings settings;
        GameController gameController;

        public LobbyTable(int id, GameSettings settings, LobbyPlayer host) {
            this.tableID = id;
            this.settings = settings;
            this.seatedPlayers = new ArrayList<>();
            this.seatedPlayers.add(host);
            this.host = host;
            this.gameController = new GameController(settings);
        }

        public boolean seatPlayer(LobbyPlayer player) {
            if (seatedPlayers.size() > settings.getMaxNumberOfPlayers()) {
                GUIMain.debugPrintln(player.playerName + " tried to join full table, id " + tableID);
                return false;
            } else if (seatedPlayers.contains(player)) {
                GUIMain.debugPrintln(player.playerName + " is already seated at table " + tableID);
                return false;
            } else {
                seatedPlayers.add(player);
                /*
                try {
                    GameClient client = new NetworkClient(player.socket, player.id);
                    gameController.addClient(player.id, client, player.playerName);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }*/
                return true;
            }
        }

        public void startGame() {
            //TODO: Probably have to do way more stuff here
            this.gameController.startGame();
        }

        public void delete() {
            //TODO: Tell players that they were removed from this table
        }


        /**
         * Convert settings of this table to a string matching the Lobby Protocol
         * @return <<setting1, value1> <setting2, value2> ... >
         */
        public String settingsToString() {
            return String.format("maxNumberOfPlayers %d startStack %d smallBlind %d bigBlind %d levelDuration %d",
                    settings.getMaxNumberOfPlayers(), settings.getStartStack(), settings.getSmallBlind(), settings.getBigBlind(),
                    settings.getLevelDuration()).trim();
        }

        /**
         *  Return a string of all the players seated at this table, matching the Lobby Protocol
         * @return <<id1<id2> ... >
         */
        public String playerIDsString() {
            String allPlayerIDs = "";
            for (LobbyPlayer p : seatedPlayers)
                allPlayerIDs += p.id + " ";
            return allPlayerIDs.trim();
        }

        /**
         * @return Return a string confirming to the Lobby Protocol (used when sending table info to clients)
         */
        public String toString() {
            return ("table " + tableID + " settings " + settingsToString() + " players " + playerIDsString()).trim();
        }
    }


}
