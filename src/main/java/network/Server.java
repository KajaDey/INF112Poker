package network;

import com.sun.deploy.util.SessionState;
import gamelogic.AIType;
import gamelogic.Game;
import gamelogic.GameController;
import gamelogic.Logger;
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
    private Thread server;
    private final Logger lobbyLogger;

    public Server() {
        lobbyLogger = new Logger("Lobby", "");
        try {
            serverSocket = new ServerSocket(39100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        start();
    }

    public static void main(String ... args){
        new Server();
    }

    private void start() {
        server = new Thread("ConnectionListener"){
            @Override
            public void run() {
                while(true) {
                    try {
                        lobbyLogger.println("Server listening for connection..", Logger.MessageType.NETWORK);
                        Socket socket = serverSocket.accept();
                        lobbyLogger.println("Connection established with " + socket.getInetAddress(), Logger.MessageType.NETWORK);

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
                    lobbyLogger.println("Failed to close socket for " + player + "", Logger.MessageType.WARNINGS, Logger.MessageType.NETWORK);
                }
            }
            else {
                lobbyLogger.println("Warning: Tried to remove player " + player + ", but socket was already closed", Logger.MessageType.NETWORK);
            }
        }
        else {
            lobbyLogger.println("Warning: Tried to remove player id " + id + ", but player was not found", Logger.MessageType.NETWORK);
        }
    }

    private void addNewTable(GameSettings settings, LobbyPlayer host) {
        int tableID = tableIdCounter++;
        LobbyTable table = new LobbyTable(tableID, settings, host);
        table.seatPlayer(host);
        lobbyTables.put(tableID, table);

        //Broadcast new table
        ClientBroadcasts.tableCreated(this, table);
        ClientBroadcasts.tableSettings(this, table);
        ClientBroadcasts.playerJoinedTable(this, host, table);
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
                    lobbyLogger.println("Client " + this.id + ": " + line, Logger.MessageType.NETWORK);
                    try {
                        tokenSwitch:
                        switch (tokens[0]) {
                            case "quit":
                                removeClient(id);
                                return;
                            case "upi":
                                if (readyToStartGame) {
                                    lobbyLogger.println("Lobby received upi from #" + this.id + " (" + this.playerName + ")", Logger.MessageType.NETWORK);
                                    return;
                                }
                                else {
                                    lobbyLogger.println("Untimely upi command, " + line, Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
                                    break;
                                }
                            case "takeSeat": {
                                if (tokens.length <= 1) {
                                    receivedIllegalCommandFrom(this, line);
                                    break;
                                }
                                int tableID = UpiUtils.parseIntToken(tokens[1]);
                                synchronized (Server.this) {
                                    if (!lobbyTables.containsKey(tableID)) {
                                        write("errorMessage \"Table " + tableID + " does not exist\"");
                                        break;
                                    }
                                    if (seatedAtAnyTable(this))
                                        write("errorMessage \"You are already seated at a table\"");
                                    else if (lobbyTables.get(tableID).seatPlayer(this))
                                        ClientBroadcasts.playerJoinedTable(Server.this, this, lobbyTables.get(tableID));
                                    else
                                        write("errorMessage \"This table is full\"");
                                }
                                break;
                            }
                            case "leaveSeat":
                                if (tokens.length <= 1){
                                    receivedIllegalCommandFrom(this, line);
                                    break;
                                }
                                int tID = UpiUtils.parseIntToken(tokens[1]);
                                if (!lobbyTables.containsKey(tID)) {
                                    write("errorMessage \"Table " + tID + " does not exist\"");
                                    break;
                                }

                                if (lobbyTables.get(tID).unseatPlayer(this))
                                    ClientBroadcasts.playerLeftTable(Server.this, this, lobbyTables.get(tID));
                                else
                                    write("errorMessage \"You are not seated at table " + tID + "\"");

                                break;
                            case "createTable":
                                if (seatedAtAnyTable(this)) {
                                    write("errorMessage \"You are already seated at a table\"");
                                    break;
                                }
                                GameSettings settings = new GameSettings(GameSettings.DEFAULT_SETTINGS);
                                try {
                                    for (int i = 1; i < tokens.length; i += 2)
                                        UpiUtils.parseSetting(settings, tokens[i], tokens[i+1]);
                                } catch (PokerProtocolException ppe) {
                                    ppe.printStackTrace();
                                    settings = new GameSettings(GameSettings.DEFAULT_SETTINGS);
                                }

                                addNewTable(settings, this);
                                break;
                            case "changeSettings":
                                try {
                                    synchronized (Server.this) {
                                        changeSetting(tokens);
                                    }
                                } catch (PokerProtocolException e) {
                                    receivedIllegalCommandFrom(this, line);
                                }
                                break;
                            case "startGame": {
                                if (tokens.length <= 1) {
                                    receivedIllegalCommandFrom(this, line);
                                    break;
                                }
                                int tableID = UpiUtils.parseIntToken(tokens[1]);
                                synchronized (Server.this) {
                                    if (lobbyTables.containsKey(tableID)) {
                                        LobbyTable t = lobbyTables.get(tableID);
                                        new Thread(t::startGame).start();
                                    }
                                }
                                // Do not return. Only return when you receive upi handshake
                                break;
                            }
                            case "deleteTable":
                                if (tokens.length <= 1) {
                                    receivedIllegalCommandFrom(this, line);
                                    break;
                                }
                                int tableID = UpiUtils.parseIntToken(tokens[1]);
                                synchronized (Server.this) {
                                    if (lobbyTables.containsKey(tableID)) {
                                        if(lobbyTables.get(tableID).host != this)
                                            write("errorMessage \"You are not the host of this table\"");
                                        else {
                                            lobbyTables.get(tableID).delete();
                                            lobbyTables.remove(tableID);
                                        }
                                    }
                                }
                                break;
                            default:
                                lobbyLogger.println("Unknown command from client: " + line, Logger.MessageType.NETWORK, Logger.MessageType.DEBUG);
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
         * @return true if the given player is seated at any table
         */
        private boolean seatedAtAnyTable(LobbyPlayer player) {
            return lobbyTables.values().stream().filter(t -> t.isSeated(player)).findAny().isPresent();
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
            int tableID = UpiUtils.parseIntToken(tokens[1]);
            if (lobbyTables.containsKey(tableID) && lobbyTables.get(tableID).host.equals(this)) {
                LobbyTable t = lobbyTables.get(tableID);
                GameSettings oldSettings = new GameSettings(t.settings);
                String settingsString = tokens[2];
                Optional<String []> settingsTokens = UpiUtils.tokenize(settingsString);
                if (settingsTokens.isPresent()) {
                    for (int i = 0; i < settingsTokens.get().length; i += 2)
                        UpiUtils.parseSetting(t.settings, settingsTokens.get()[i], settingsTokens.get()[i+1]);

                    if (t.settings.valid())
                        ClientBroadcasts.tableSettings(Server.this, t);
                    else {
                        write("errorMessage \"" + t.settings.getErrorMessage() + "\"");
                        t.settings = oldSettings;
                    }
                }
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
                lobbyLogger.println("Failed to flush socket of " + this + ", removing player", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
                removeClient(this.id);
            }
        }
    }

    /**
     * Called whenever a write to a player fails. For now, this drops the player from the server,
     * but in the future this should probably buffer up outstanding writes, and try to send them
     * again for a while.
     */
    private void failedToWriteToPlayer(LobbyPlayer player, String message) {
        lobbyLogger.println("Failed to write to client " + player + ", dropping player.", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
        this.removeClient(player.id);
    }

    /**
     * Called whenever a read from a player fails. This usually means that the socket has been closed,
     * and the client will be disconnected
     */
    private void failedToReadFromPlayer(LobbyPlayer player) {
        lobbyLogger.println("Failed to read from client " + player + ", dropping player.", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
        this.removeClient(player.id);
    }

    /**
     * Called whenever the server receives an illegally formatted command from a client
     * For now, this just skips the command in question, and keeps the client
     */
    private void receivedIllegalCommandFrom(LobbyPlayer player, String command) {
        lobbyLogger.println("Received illegal command from client " + player + ", ignoring command \"" + command + "\"", Logger.MessageType.NETWORK, Logger.MessageType.DEBUG);
    }

    /**
     * Gets a handle to the thread listening for connection
     */
    public Thread getServerThread() {
        return server;
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
            broadCast(server, "playerLeftTable " + player.id + " " + table.tableID);
        }
        public static void tableCreated(Server server, LobbyTable table) {
            broadCast(server, "tableCreated " + table.tableID);
        }
        public static void tableDeleted(Server server, LobbyTable table) {
            broadCast(server, "tableDeleted " + table.tableID);
        }
        public static void tableSettings(Server server, LobbyTable table) {
            broadCast(server, "tableSettings " + table.tableID + " " + UpiUtils.settingsToString(table.settings));
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

        /**
         *  Seat a player at this table
         * @return True if the player was seated
         */
        public boolean seatPlayer(LobbyPlayer player) {
            if (seatedPlayers.size() > settings.getMaxNumberOfPlayers()) {
                lobbyLogger.println(player.playerName + " tried to join full table, id " + tableID, Logger.MessageType.NETWORK);
                return false;
            } else if (seatedPlayers.contains(player)) {
                lobbyLogger.println(player.playerName + " is already seated at table " + tableID, Logger.MessageType.NETWORK);
                return false;
            } else {
                seatedPlayers.add(player);
                return true;
            }
        }

        /**
         * @return True if the player is seated at this table
         */
        public boolean isSeated(LobbyPlayer player) {
            return seatedPlayers.contains(player);
        }

        public void startGame() {
            lobbyLogger.println("Warning: Forcing default settings", Logger.MessageType.DEBUG);
            this.settings = new GameSettings(GameSettings.DEFAULT_SETTINGS);
            GameController gameController = new GameController(this.settings);

            List<Socket> sockets = seatedPlayers.stream().map(p -> p.socket).collect(Collectors.toList());
            lobbyLogger.println("Starting game for " + seatedPlayers.toString(), Logger.MessageType.INIT, Logger.MessageType.NETWORK);
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
                lobbyLogger.println("Error while starting game", Logger.MessageType.WARNINGS, Logger.MessageType.NETWORK, Logger.MessageType.INIT);
                lobbyLogger.println(e.getMessage(), Logger.MessageType.WARNINGS, Logger.MessageType.NETWORK, Logger.MessageType.INIT);
                lobbyLogger.println("Game was not started", Logger.MessageType.WARNINGS, Logger.MessageType.NETWORK, Logger.MessageType.INIT);
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

        /**
         * Remove player from this table
         */
        public boolean unseatPlayer(LobbyPlayer lobbyPlayer) {
            return seatedPlayers.remove(lobbyPlayer);
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
