package network;

import gamelogic.Game;
import gamelogic.GameController;
import gamelogic.Logger;
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

    private static final String CURRENT_UPI_VERSION = "1.0";

    private int playerIDCounter = 0, tableIDCounter = 0;
    public ServerSocket serverSocket;
    public List<LobbyPlayer> lobbyPlayers = new Vector<>();
    public Map<Integer, LobbyTable> lobbyTables = new HashMap<>();
    private final Logger lobbyLogger;

    public Server(int port) {
        lobbyLogger = new Logger("Lobby", "");
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        start();
    }

    public Server() {
        this(39100);
    }

    public static void main(String ... args){
        new Server();
    }

    private void start() {
        Thread server = new Thread("ConnectionListener"){
            @Override
            public void run() {
                while(true) {
                    try {
                        lobbyLogger.println("Server listening for connection on port " + serverSocket.getLocalPort(), Logger.MessageType.NETWORK);
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
        lobbyPlayers.add(new LobbyPlayer(socket, playerIDCounter++));
    }


    /**
     * Removes a client from the lobby, closing sockets and broadcasting their leaving to the other clients
     * @param ID Id of the client to remove
     */
    private synchronized void removeClient(int ID) {
        Optional<LobbyPlayer> op = lobbyPlayers.stream().filter(client -> client.id == ID).findAny();
        if (op.isPresent()) {
            lobbyLogger.println("Removing player " + op.get() + " from lobby", Logger.MessageType.NETWORK);
            //If this player was host for a table, this table is removed. Client side handles this
            Optional<LobbyTable> opTable = lobbyTables.values().stream().filter(t -> t.host == op.get()).findAny();
            opTable.ifPresent(t -> lobbyTables.remove(t.tableID));

            LobbyPlayer player = op.get();
            if (!lobbyPlayers.remove(player)) {
                lobbyLogger.println("Attempted to remove player " + player + ", but couldn't find it in lobbyPlayers: " + lobbyPlayers, Logger.MessageType.NETWORK, Logger.MessageType.NETWORK_DEBUG, Logger.MessageType.WARNINGS);
            }
            ClientBroadcasts.playerLeftLobby(this, player);
            if (!player.socket.isClosed()) {
                try {
                    player.socket.close();
                } catch (IOException e) {
                    lobbyLogger.println("Failed to close socket for " + player + "", Logger.MessageType.WARNINGS, Logger.MessageType.NETWORK);
                }
            }
            else {
                lobbyLogger.println("Warning: Tried to close socket for " + player + ", but socket was already closed", Logger.MessageType.NETWORK);
            }
        }
        else {
            lobbyLogger.println("Warning: Tried to remove player ID " + ID + ", but player was not found", Logger.MessageType.NETWORK);
        }
    }

    private void addNewTable(GameSettings settings, LobbyPlayer host) {
        int tableID = tableIDCounter++;
        LobbyTable table = new LobbyTable(tableID, settings, host);
        assert table.isSeated(host) : "Host was not seated at table.";
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
                    lobbyLogger.println("Client #" + id + ": " + input, Logger.MessageType.NETWORK_DEBUG, Logger.MessageType.NETWORK);
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
                        lobbyLogger.println("Client #" + id + ": " + line, Logger.MessageType.NETWORK_DEBUG);
                    } catch (IOException e) {
                        failedToReadFromPlayer(this);
                        return;
                    }

                    if (line == null) {
                        Server.this.removeClient(id);
                        return;
                    }
                    String [] tokens = UpiUtils.tokenize(line).get();
                    if (tokens.length <= 0) {
                        receivedIllegalCommandFrom(this, line);
                        continue;
                    }
                    try {
                        switch (tokens[0]) {
                            case "quit":
                                removeClient(id);
                                return;
                            case "upi":
                                if (tokens.length < 1) {
                                    write("errorMessage \"Handshake error, no upi version submitted\"");
                                    receivedIllegalCommandFrom(this, line);
                                    return;
                                }
                                if (!tokens[1].equalsIgnoreCase(CURRENT_UPI_VERSION)) {
                                    lobbyLogger.println("Client " + this.id + "s upi version("+ tokens[1]+") does not match server version (" +
                                            CURRENT_UPI_VERSION + ")", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS );
                                    write("errorMessage \"You are running the wrong upi version. Server version is " + CURRENT_UPI_VERSION + "\"");
                                    receivedIllegalCommandFrom(this, line);
                                    return;
                                }
                                if (readyToStartGame) {
                                    lobbyLogger.println("Lobby received upi from #" + this.id + " (" + this.playerName + ")", Logger.MessageType.NETWORK);
                                    return;
                                }
                                else {
                                    lobbyLogger.println("Untimely upi command, " + line, Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
                                    break;
                                }
                            case "takeSeat": { //takeSeat <tableID>
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
                            case "leaveSeat": //leaveSeat <tableID>
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
                            case "createTable": //createTable <setting1, value1> ...
                                if (seatedAtAnyTable(this)) {
                                    write("errorMessage \"You are already seated at a table\"");
                                    break;
                                }
                                GameSettings settings = new GameSettings();
                                try {
                                    if (tokens.length < 2)
                                        throw new PokerProtocolException("Missing settings for new table");
                                    String [] settingsTokens = UpiUtils.tokenize(tokens[1]).get();
                                    for (int i = 0; i < settingsTokens.length; i += 2)
                                        UpiUtils.parseSetting(settings, settingsTokens[i], settingsTokens[i+1]);
                                } catch (PokerProtocolException e) {
                                    settings = new GameSettings();
                                    lobbyLogger.println("Missing settings for new table to be created", Logger.MessageType.WARNINGS);
                                }

                                addNewTable(settings, this);
                                break;
                            case "changeSettings": //changeSettings <tableID> <setting1, value1> osv..
                                try {
                                    synchronized (Server.this) {
                                        changeSetting(tokens);
                                    }
                                } catch (PokerProtocolException e) {
                                    lobbyLogger.println(e.getMessage(), Logger.MessageType.NETWORK);
                                    receivedIllegalCommandFrom(this, line);
                                }
                                break;
                            case "startGame": { //startGame <tableID>
                                if (tokens.length <= 1) {
                                    receivedIllegalCommandFrom(this, line);
                                    break;
                                }
                                int tableID = UpiUtils.parseIntToken(tokens[1]);
                                synchronized (Server.this) {
                                    if (lobbyTables.containsKey(tableID)) {
                                        if (lobbyTables.get(tableID).host != this) {
                                            write("errorMessage \"You are not the host of this table\"");
                                            break;
                                        } else {
                                            LobbyTable t = lobbyTables.get(tableID);
                                            new Thread(t::startGame).start();
                                        }
                                    }
                                }
                                // Do not return. Only return when you receive upi handshake
                                break;
                            }
                            case "deleteTable": //deleteTable <tableID>
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
                                            System.out.println("Legal table"); // TODO REMOVE
                                            lobbyTables.get(tableID).delete();
                                            lobbyTables.remove(tableID);
                                        }
                                    } else write("errorMessage \"There is no table with id "+ tableID +"\"");
                                }
                                break;
                            default:
                                write("errorMessage \"Unknown command\"");
                                lobbyLogger.println("Unknown command from client: " + line, Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
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
        public String toString() {
            return "Player " + playerName + ", id " + id;
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
            if (tokens.length <= 2) {
                throw new PokerProtocolException();
            }
            int tableID = UpiUtils.parseIntToken(tokens[1]);
            if (lobbyTables.containsKey(tableID) && lobbyTables.get(tableID).host.equals(this)) {
                LobbyTable t = lobbyTables.get(tableID);
                GameSettings oldSettings = new GameSettings(t.settings);
                String settingsString = tokens[2];
                Optional<String []> settingsTokens = UpiUtils.tokenize(settingsString);
                if (settingsTokens.isPresent()) {
                    try {
                        for (int i = 0; i < settingsTokens.get().length; i += 2)
                            UpiUtils.parseSetting(t.settings, settingsTokens.get()[i], settingsTokens.get()[i + 1]);
                    } catch (PokerProtocolException ppe) {
                        lobbyLogger.print(ppe.getMessage());
                        write("errorMessage \"" + t.settings.getErrorMessage() + "\"");
                        t.settings = oldSettings;
                        return;
                    }

                    if (!t.settings.valid()) {
                        write("errorMessage \"" + t.settings.getErrorMessage() + "\"");
                        t.settings = oldSettings;
                    }

                    if (t.settings.getMaxNumberOfPlayers() < t.seatedPlayers.size()) {
                        write("errorMessage \"Too many players already seated, cannot set to " + t.settings.getMaxNumberOfPlayers() + "\"");
                        t.settings.setMaxNumberOfPlayers(oldSettings.getMaxNumberOfPlayers());
                    }

                    assert t.settings.valid();

                    ClientBroadcasts.tableSettings(Server.this, t);
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
                lobbyLogger.println("Server to #" + this.id + ": " + msg, Logger.MessageType.NETWORK_DEBUG);
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

    static class ClientBroadcasts {
        private static void broadCast(Server server, String string) {
            synchronized (server) {
                LobbyPlayer [] hackArray = new LobbyPlayer[server.lobbyPlayers.size()];
                for (int i = 0; i < hackArray.length; i++)
                    hackArray[i] = server.lobbyPlayers.get(i);

                for (LobbyPlayer p : hackArray)
                    p.write(string);
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
            broadCast(server, "tableSettings " + table.tableID + " \"" + UpiUtils.settingsToString(table.settings)+"\"");
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
            if (seatedPlayers.size() >= settings.getMaxNumberOfPlayers()) {
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
            GameController gameController = new GameController(this.settings);

            List<Socket> sockets = seatedPlayers.stream().map(p -> p.socket).collect(Collectors.toList());
            lobbyLogger.println("Starting game for " + seatedPlayers.toString(), Logger.MessageType.INIT, Logger.MessageType.NETWORK);
            seatedPlayers.forEach(p -> p.readyToStartGame = true);

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
            }
        }

        public void delete() {
            for (LobbyPlayer player : seatedPlayers) {
                ClientBroadcasts.playerLeftTable(Server.this, player, this);
            }
            ClientBroadcasts.tableDeleted(Server.this, this);
            Server.this.lobbyTables.remove(this.tableID);
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
