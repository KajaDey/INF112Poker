package gamelogic;

import gui.GUIMain;
import gui.GameSettings;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

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

    public synchronized void addNewClientSocket(Socket socket) {
        lobbyPlayers.add(new LobbyPlayer(socket, playerIdCounter++));
    }


    /**
     * Removes a client from the lobby, closing sockets and broadcasting their leaving to the other clients
     * @param id Id of the client to remove
     */
    private synchronized void removeClient(int id) {
        Optional<LobbyPlayer> op = lobbyPlayers.stream().filter(client -> client.id == id).findAny();
        if (op.isPresent()) {
            LobbyPlayer player = op.get();
            lobbyPlayers.remove(player);
            ClientBroadcasts.playerLeftLobby(this, player);
            if (!player.socket.isClosed()) {
                try {
                    player.socket.close();
                } catch (IOException e) {
                    System.out.println("Failed to close socket for " + player + "");
                }
            }
            else {
                System.out.println("Warning: Tried to remove player " + player + ", but socket was already closed");
            }
        }
        else {
            System.out.println("Warning: Tried to remove player id " + id + ", but player was not found");
        }
    }

    /**
     * Removes a client from the lobby and broadcasts their leaving to the other client,
     * but does not close the socket
     * @param id
     */
    private synchronized void playerStartedGame(int id) {
        Optional<LobbyPlayer> op = lobbyPlayers.stream().filter(client -> client.id == id).findAny();
        if (op.isPresent()) {
            LobbyPlayer player = op.get();
            lobbyPlayers.remove(player);
            ClientBroadcasts.playerLeftLobby(this, player);
        }
        else {
            System.out.println("Warning: Player id " + id + " tried to start game, but player was not found in lobby");
        }
    }

    private void addNewTable(GameSettings settings, LobbyPlayer host) {
        int tableID = tableIdCounter++;
        LobbyTable table = new LobbyTable(tableID, settings, host);
        lobbyTables.put(tableID, table);

        lobbyPlayers.forEach(p -> {
            p.write("tableCreated " + tableID);
            p.write("tableSettings " + tableID + " " + UpiUtils.settingsToString(table.settings));
            p.write("playerJoinedTable " + host.id + " " + tableID);
        });
    }

    /**
     * Like Integer.parseInt,except that it throws a checked exception if it fails
     * @param input
     * @return
     * @throws PokerProtocolException
     */
    public static int parseIntToken(String input) throws PokerProtocolException {
        try {
            return Integer.parseInt(input);
        }
        catch (NumberFormatException e) {
            throw new PokerProtocolException("Error parsing " + input + " to int");
        }
    }

    /**
     * Like Long.parseLong,except that it throws a checked exception if it fails
     * @param input
     * @return
     * @throws PokerProtocolException
     */
    public static long parseLongToken(String input) throws PokerProtocolException {
        try {
            return Long.parseLong(input);
        }
        catch (NumberFormatException e) {
            throw new PokerProtocolException("Error parsing " + input + " to long");
        }
    }

    class LobbyPlayer {
        final int id;
        final Socket socket;
        final Thread listener;
        String playerName;
        BufferedReader reader;
        BufferedWriter writer;
        private boolean readyToStartGame = false; // Whether the player's table has been started

        public LobbyPlayer(Socket s, int id) {
            this.socket = s;
            this.id = id;

            Runnable task = () -> {
                try {
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                } catch (IOException e) {
                    failedToReadFromPlayer(this);
                    return;
                }

                String input;
                try {
                    input = reader.readLine();
                } catch (IOException e) {
                    failedToReadFromPlayer(this);
                    return;
                }

                if (input.startsWith("lobby")) {
                    write("lobbyok");
                } else {
                    write("lobbynotok");
                    removeClient(id);
                    return;
                }

                playerName = input.substring("lobby".length()).trim();

                write("yourId " + id);
                sendLobbyInfo();
                ClientBroadcasts.playedJoinedLobby(Server.this, this);

                while(true) {
                    String line;
                    try {
                        line = reader.readLine();
                    } catch (IOException e) {
                        failedToReadFromPlayer(this);
                        return;
                    }

                    if (line == null)
                        break;
                    String [] tokens = UpiUtils.tokenize(line).get();
                    if (tokens.length <= 0) {
                        receivedIllegalCommandFrom(this, line);
                        continue;
                    }
                    System.out.println("Client " + this.id + ": " + line);
                    try {
                        switch (tokens[0]) {
                            case "quit":
                                removeClient(id);
                                return;
                            case "upi":
                                if (readyToStartGame) {
                                    System.out.println("Lobby received upi from #" + this.id + " (" + this.playerName + ")");
                                    return;
                                }
                                else {
                                    System.out.println("Untimely upi command, " + line);
                                    break;
                                }
                            case "takeseat": {
                                if (tokens.length <= 1) {
                                    receivedIllegalCommandFrom(this, line);
                                    break;
                                }
                                int tableID = parseIntToken(tokens[1]);
                                synchronized (Server.this) {
                                    if (lobbyTables.containsKey(tableID)) {
                                        if (lobbyTables.get(tableID).seatPlayer(this)) {
                                            ClientBroadcasts.playerJoinedTable(Server.this, this, lobbyTables.get(tableID));
                                        }
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
                                try {
                                    synchronized (Server.this) {
                                        changeSetting(tokens);
                                    }
                                } catch (PokerProtocolException e) {
                                    receivedIllegalCommandFrom(this, line);
                                }
                                break;
                            case "startgame": {
                                if (tokens.length <= 1) {
                                    receivedIllegalCommandFrom(this, line);
                                    break;
                                }
                                int tableID = parseIntToken(tokens[1]);
                                synchronized (Server.this) {
                                    if (lobbyTables.containsKey(tableID)) {
                                        LobbyTable t = lobbyTables.get(tableID);
                                        new Thread(() -> t.startGame()).start();
                                        // t.delete(); //TODO should not delete table, but rather do special "gameStarted" logic
                                    }
                                }
                                // Do not return. Only return when you receive upi handshake
                                break;
                            }
                            case "deletetable":
                                if (tokens.length <= 1) {
                                    receivedIllegalCommandFrom(this, line);
                                    break;
                                }
                                int tableID = parseIntToken(tokens[1]);
                                synchronized (Server.this) {
                                    if (lobbyTables.containsKey(tableID)) {
                                        lobbyTables.get(tableID).delete();
                                        lobbyTables.remove(tableID);
                                    }
                                }
                                break;
                            default:
                                System.out.println("Unknown command, " + line);
                        }
                    }
                    catch (PokerProtocolException e) {
                        receivedIllegalCommandFrom(this, input);
                    }
                }
            };
            listener = new Thread(task);
            listener.start();
        }

        /**
         * Send lobby info to client (specified in Lobby Protocol)
         */
        private void sendLobbyInfo() {
            //Send all player names: playerNames <id1 name1> osv.
            String allPlayerNames = "playerNames ";
            for (LobbyPlayer p : lobbyPlayers)
                allPlayerNames += p.id + " \"" + p.playerName + "\" ";

            //Send all tables: table <id> settings <<setting1, value1> ...> players <<id1><id2>..>
            ArrayList<String> allTables = new ArrayList<>();
            lobbyTables.forEach((id, table) -> allTables.add(table.toString()));

            //Send lobbySent
            write(allPlayerNames);
            allTables.forEach(this::write);
            write("lobbySent");
        }

        private void changeSetting(String [] tokens) throws PokerProtocolException {
            if (tokens.length <= 1) {
                throw new PokerProtocolException();
            }
            int tableID = parseIntToken(tokens[1]);
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
            }
            catch (IOException e) {
                failedToWriteToPlayer(this, msg + "\n");
            }
            try {
                writer.flush();
            } catch (IOException e) {
                System.out.println("Failed to flush socket of " + this + ", removing player");
                removeClient(this.id);
            }
        }
    }

    /**
     * Called whenever a write to a player fails. For now, this drops the player from the server,
     * but in the future this should probably buffer up outstanding writes, and try to send them
     * again for a while.
     * @param player
     * @param message
     */
    private void failedToWriteToPlayer(LobbyPlayer player, String message) {
        System.out.println("Failed to write to client " + player + ", dropping player.");
        this.removeClient(player.id);
    }

    /**
     * Called whenever a read from a player fails. This usually means that the socket has been closed,
     * and the client will be disconnected
     * @param player
     */
    private void failedToReadFromPlayer(LobbyPlayer player) {
        System.out.println("Failed to read from client " + player + ", dropping player.");
        this.removeClient(player.id);
    }

    /**
     * Called whenever the server receives an illegally formatted command from a client
     * For now, this just skips the command in question, and keeps the client
     * @param player
     * @param command
     */
    private void receivedIllegalCommandFrom(LobbyPlayer player, String command) {
        System.out.println("Received illegal command from client " + player + ", ignoring command \"" + command + "\"");
    }

    static class ClientBroadcasts {
        private static void broadCast(Server server, String string) {
            synchronized (server) {
                for (LobbyPlayer player : server.lobbyPlayers) {
                    player.write(string);
                }
            }
        }
        public static void playedJoinedLobby(Server server, LobbyPlayer player) {
            broadCast(server, "playerJoinedLobby " + player.id + " \"" + player.playerName + "\"");
        }
        public static void playerLeftLobby(Server server, LobbyPlayer player) {
            broadCast(server, "playerLeftLobby " + player.id);
        }
        public static void playerJoinedTable(Server server, LobbyPlayer player, LobbyTable table) {
            broadCast(server, "playerJoinedTable " + player.id + " " + table.tableID);
        }
        public static void playerLeftTable(Server server, LobbyPlayer player, LobbyTable table) {
            broadCast(server, "playerJoinedTable " + player.id + " " + table.tableID);
        }
        public static void tableCreated(Server server, LobbyTable table) {
            broadCast(server, "tableCreated " + table.tableID);
        }
        public static void tableDeleted(Server server, LobbyTable table) {
            broadCast(server, "tableDeleted " + table.tableID);
        }
        public static void tableSettings(Server server, LobbyTable table) {
            broadCast(server, "tableSettings " + table + UpiUtils.settingsToString(table.settings));
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
                GUIMain.debugPrintln(player.playerName + " tried to join full table, id " + tableID);
                return false;
            } else if (seatedPlayers.contains(player)) {
                GUIMain.debugPrintln(player.playerName + " is already seated at table " + tableID);
                return false;
            } else {
                seatedPlayers.add(player);
                return true;
            }
        }

        public void startGame() {
            System.out.println("Warning: Forcing default settings");
            this.settings = new GameSettings(GameSettings.DEFAULT_SETTINGS);
            GameController gameController = new GameController(this.settings);

            List<Socket> sockets = seatedPlayers.stream().map(p -> p.socket).collect(Collectors.toList());
            System.out.println("Starting game for " + seatedPlayers.toString());
            seatedPlayers.forEach(p -> {
                p.readyToStartGame = true;
            });

            this.seatedPlayers.forEach(player -> player.write("startGame"));
            seatedPlayers.forEach(p -> {
                try {
                    // Wait for all lobby listeners to get upi handshake
                    p.listener.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            try {
                gameController.initGame(false, sockets);
                this.delete();
            } catch (Game.InvalidGameSettingsException e) {
                System.out.println("Error while starting game");
                System.out.println(e.getMessage());
                System.out.println("Game was not started");
                return;
            }
        }

        public void delete() {
            for (LobbyPlayer player : seatedPlayers) {
                ClientBroadcasts.playerLeftTable(Server.this, player, this);
            }
            ClientBroadcasts.tableDeleted(Server.this, this);
            Server.this.lobbyTables.remove(this);
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
            return ("table " + tableID + " settings " + UpiUtils.settingsToString(this.settings) + " players " + playerIDsString()).trim();
        }
    }

    /**
     * Exception for when illegally formatted commands are received
     */
    public static class PokerProtocolException extends IOException {
        public PokerProtocolException() {
            super();
        }
        public PokerProtocolException(String message) {
            super(message);
        }
        public PokerProtocolException(String message, Throwable cause) {
            super (message, cause);
        }
        public PokerProtocolException(Throwable cause) {
            super(cause);
        }
    }

}
