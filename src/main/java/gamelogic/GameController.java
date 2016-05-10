package gamelogic;

import gamelogic.ai.MCTSAI;
import gamelogic.ai.SimpleAI;
import gui.*;
import network.NetworkClient;
import network.UpiUtils;
import replay.ReplayClient;
import replay.ReplayReader;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

/**
 * Created by kristianrosland on 07.03.2016.
 *
 * A controller to function as a layer between the GUI and the back end.
 */
public class GameController {
    private Game game;
    private Map<Integer, GameClient> clients;

    private GameSettings gameSettings;
    private final Optional<GUIMain> guiMain;
    private String name;
    private Map<Integer, String> names;
    private boolean showAllPlayerCards;
    private MainScreen.GameType gameType;
    private final Logger logger;

    /**
     * Used by GUI to create a new single player game
     */
    public GameController(GameSettings settings, GUIMain guiMain) {
        this.guiMain = Optional.of(guiMain);
        this.gameSettings = settings;
        this.logger = guiMain.logger;
        this.game = new Game(settings, this, logger);

        clients = new HashMap<>();
        names = new HashMap<>();
    }

    /**
     * Used by Server to create a new GameController for a network game
     */
    public GameController(GameSettings settings) {
        this.guiMain = Optional.empty();
        this.gameSettings = settings;
        this.logger = new Logger("ServerGame", "");
        this.game = new Game(settings, this, logger);

        clients = new HashMap<>();
        names = new HashMap<>();
    }

    public GameController() {
        this(new GameSettings(GameSettings.DEFAULT_SETTINGS));
    }

    /**
     * @return a deep copy of the game's settings
     */
    public GameSettings getGameSettings() {
        return new GameSettings(gameSettings);
    }

    /**
     * Called when the enter button is clicked.
     * Checks valid number of players, then makes GUI show the lobby screen
     * @param name The player's chosen name
     */
    public void enterButtonClicked(String name, InetAddress IPAddress, MainScreen.GameType gameType) {
        //Tell GUI to display Lobby
        gameSettings = new GameSettings(GameSettings.DEFAULT_SETTINGS);
        this.gameType = gameType;

        if(gameType == MainScreen.GameType.SINGLE_PLAYER)
            guiMain.get().displaySinglePlayerScreen(name, gameSettings);
        else if (gameType == MainScreen.GameType.MULTI_PLAYER)
            guiMain.get().displayMultiPlayerScreen(name, IPAddress);

        this.name = name;
    }

    /**
     * Called when the start tournament button is clicked.
     * Creates a new game, creates clients and starts the game.
     *
     * @param showCards If all players hole cards should be visible or not
     */
    public Thread initGame(boolean showCards, List<Socket> clientSockets) throws Game.InvalidGameSettingsException {
        if (!gameSettings.valid())
            throw new Game.InvalidGameSettingsException(gameSettings.getErrorMessage());

        //Make a new Game object and validate
        game = new Game(gameSettings, this, logger);
        this.showAllPlayerCards = showCards;

        GUIMain.replayLogPrint("SETTINGS\n" + gameSettings.toString());

        NameGenerator.readNewSeries();

        //Create GUI-GameClient
        if (guiMain.isPresent())
            createGUIClient();

        logger.println("Creating " + clientSockets.size() + " network clients", Logger.MessageType.INIT);
        // Create network clients
        createNetworkClients(clientSockets);

        logger.println("Creating ai clients", Logger.MessageType.INIT);
        //Create AI-GameClients
        int numberOfAIClients = this.gameSettings.getMaxNumberOfPlayers();
        if (guiMain.isPresent()) {
            numberOfAIClients--;
        }
        numberOfAIClients -= clientSockets.size();
        createAIClients(numberOfAIClients, gameSettings);

        //Set initial blind values for clients
        initClients();

        //Print welcome message to log
        this.printToLogField("Game with " + this.gameSettings.getMaxNumberOfPlayers() + " players started!");
        logger.println("Game with " + this.gameSettings.getMaxNumberOfPlayers() + " players started!", Logger.MessageType.INIT);

        //Set chat listener for all clients
        clients.forEach((id, client) -> client.setChatListener(s -> printToLogField(names.get(id) + ": " + s)));

        //Print names to replay log
        GUIMain.replayLogPrint("\nNAMES");
        names.forEach((id, name) -> GUIMain.replayLogPrint("\n" + name));

        return startGame();
    }

    /**
     * Method to start the game (in a separate thread)
     */
    public Thread startGame() {
        Thread gameThread = new Thread("GameThread") {
            @Override
            public void run() {
                game.playGame();
            }
        };
        gameThread.start();
        return gameThread;
    }

    /**
     *  Called if a game replay is to be started
     * @param file File to read replay from
     */
    public void startReplay(File file) {
        ReplayReader replayReader = new ReplayReader(file);
        showAllPlayerCards = true;
        gameSettings = replayReader.getSettings();

        game = new Game(gameSettings, this, logger);

        //Initialize replay client with GUI
        createReplayGUIClient(replayReader);

        //Initialize the rest of the replay-clients
        createReplayClients(replayReader);

        //Set client initial values
        initClients();

        //Override the drawCard()-method in Game so that it draws cards from the replay queue instead of deck
        game.setReplayCardQueue(replayReader.getCardQueue());

        //Start the game replay
        startGame();
    }

    /**
     *  Create a ReplayClient with a GUI.
     */
    public void createReplayGUIClient(ReplayReader reader) {
        GameClient guiReplayClient = guiMain.get().displayReplayScreen(0, reader);
        this.name = reader.getNextName();
        clients.put(0, guiReplayClient);
        game.addPlayer(name, 0);
        guiReplayClient.setAmountOfPlayers(gameSettings.getMaxNumberOfPlayers());
        names.put(0, name);
        logger.println("Initialized " + guiReplayClient.getClass().getSimpleName() + " " + names.get(0), Logger.MessageType.INIT);
    }

    /**
     * Create a GUI-client with initial values
     */
    private void createGUIClient() {
        GameClient guiClient = guiMain.get().displayGameScreen(0);
        clients.put(0, guiClient);
        game.addPlayer(this.name, 0);
        guiClient.setAmountOfPlayers(gameSettings.getMaxNumberOfPlayers());
        names.put(0, name);
        logger.println("Initialized " + guiClient.getClass().getSimpleName() + " " + names.get(0), Logger.MessageType.INIT);
    }

    /**
     * Create all of the network clients and
     * @param clientSockets Sockets for each client
     */
    private void createNetworkClients(List<Socket> clientSockets) {
        ArrayList<Thread> clientThreads = new ArrayList<>();
        for (int i = clients.size(); i < clientSockets.size(); i++) {
            int id = i;
            Runnable r = () -> {
                GameClient networkClient;
                try {
                    Socket socket = clientSockets.get(guiMain.isPresent() ? id - 1 : id);
                    logger.println("Creating network client", Logger.MessageType.NETWORK);
                    networkClient = new NetworkClient(socket, id, logger);
                } catch (IOException e) {
                    logger.println("Failed to connect to a client, dropping client", Logger.MessageType.INIT, Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
                    return;
                }
                logger.println("Connected to network client", Logger.MessageType.INIT, Logger.MessageType.NETWORK);
                networkClient.setAmountOfPlayers(gameSettings.getMaxNumberOfPlayers());
                String name = networkClient.getName();
                if (name.equals(""))
                    name = NameGenerator.getRandomName();

                synchronized (this) {
                    clients.put(id, networkClient);

                    game.addPlayer(name, id);
                    assert !names.containsKey(id);
                    names.put(id, name);
                    logger.println("Initialized " + networkClient.getClass().getSimpleName() + " " + names.get(id), Logger.MessageType.INIT, Logger.MessageType.NETWORK);
                }

            };
            clientThreads.add(new Thread(r));
            logger.println("Connecting to client...", Logger.MessageType.INIT, Logger.MessageType.NETWORK);
            clientThreads.get(clientThreads.size() - 1).start();
        }
        // Wait for all clients to finish initialization
        for (Thread thread : clientThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create a given number of AI-clients to correspond to Player's in Game
     */
    private void createAIClients(int numberOfAIs, GameSettings settings) {

        for (int i = 0; i < numberOfAIs; i++) {
            String aiName = NameGenerator.getRandomName();
            int AI_id = clients.size();

            GameClient aiClient;
            double contemptFactor = 1.00;
            switch (gameSettings.getAiType()) {
                case MCTS_AI:
                    aiClient = new MCTSAI(AI_id, contemptFactor, logger);
                    break;
                case SIMPLE_AI:
                    aiClient = new SimpleAI(AI_id, contemptFactor);
                    break;
                case MIXED:
                    aiClient = Math.random() > 0.5 ? new MCTSAI(AI_id, logger) : new SimpleAI(AI_id, contemptFactor);
                    break;
                default: throw new IllegalStateException(); // Exception to please our java overlords
            }

            aiClient.setAmountOfPlayers(settings.getMaxNumberOfPlayers());
            clients.put(AI_id, aiClient);
            game.addPlayer(aiName, AI_id);
            assert !names.containsKey(AI_id) : "Name list already has a name for " + AI_id;
            names.put(AI_id, aiName);
            logger.println("Initialized " + aiClient.getClass().getSimpleName() + " " + names.get(AI_id), Logger.MessageType.AI, Logger.MessageType.INIT);
        }
    }

    /**
     * Create (max number of players - 1) replay clients with no GUI
     */
    public void createReplayClients(ReplayReader reader) {
        for (int i = 0; i < gameSettings.getMaxNumberOfPlayers() - 1; i++) {
            String replayName = reader.getNextName();
            int replayId = clients.size();

            GameClient replayClient = new ReplayClient(replayId, reader, logger);
            clients.put(replayId, replayClient);
            game.addPlayer(replayName, replayId);
            names.put(replayId, replayName);
            logger.println("Initialized " + replayClient.getClass().getSimpleName() + " " + names.get(replayId), Logger.MessageType.INIT);
        }
    }

    /**
     * Informs each client about the small and big blind amount
     */
    private void initClients() {
        setBlinds();
        clients.forEach((id, client) -> client.setPlayerNames(new HashMap<>(names)));
        game.refreshAllStackSizes();
    }

    /**
     * Sends the blinds to all the clients
     */
    public void setBlinds() {
        clients.forEach((id, client) -> {
            client.setSmallBlind(gameSettings.getSmallBlind());
            client.setBigBlind(gameSettings.getBigBlind());
        });
    }

    /**
     * Asks a client for a decision
     *
     * @param ID Client ID
     * @return Decision made by the client
     */
    public Decision getDecisionFromClient(int ID) {
        //Ask for decision from client
        GameClient client = clients.get(ID);
        if (client instanceof SimpleAI || client instanceof MCTSAI)
            return getAIDecision(client);
        else
            return client.getDecision(gameSettings.getPlayerClock() * 1000);
    }

    /**
     *  Get a decision from an AI-client
     *  @return Decision made by AI
     */
    private Decision getAIDecision(GameClient aiClient) {
        long startTime = System.currentTimeMillis();
        long timeToTake = 500L + (long)(Math.random() * 2000.0);
        Decision decision = aiClient.getDecision(timeToTake);
        long timeTaken = System.currentTimeMillis() - startTime;

        delay(timeToTake - timeTaken);

        return decision;
    }

    /**
     * Sleeps the thread for given amount of time
     * @param delayTime Time to delay for
     */
    public void delay(long delayTime) {
        try { Thread.sleep(Math.max(0, delayTime)); }
        catch (InterruptedException e) {
            logger.println("Sleeping thread interrupted", Logger.MessageType.WARNINGS);
        }
    }

    /**
     * Sets the game settings
     *
     * @param gameSettings Game settings
     */
    public void setGameSettings(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
    }

    /**
     * Tells each client that it is time for show down, and pass the necessary information about the showdown
     * @param showdownStats Information about pot (and side pots) and who won
     */
    public void showdown(ShowdownStats showdownStats) {
        String[] tokens = UpiUtils.tokenize("\"" + showdownStats.getWinnerText().replace("\n", "\" \"") + "\"").get();
        clients.forEach((id, client) -> client.showdown(tokens));
    }

    /**
     * Gives a client his hole cards
     *
     * @param clientID The client who gets the hand
     * @param card1 Card one in the hand
     * @param card2 Card two in the hand
     */
    public void setHandForClient(int clientID, Card card1, Card card2) {
        if (showAllPlayerCards) { // Send everyone's hole cards to everyone
            clients.forEach((id, client) -> client.setHandForClient(clientID, card1, card2));
        }
        else {
            GameClient c = clients.get(clientID);
            c.setHandForClient(clientID, card1, card2);
        }
    }

    /**
     * Informs each client about the decision that was made by a specific user
     *
     * @param userID The user who made the decision
     * @param decision The decision that was made
     */
    public void setDecisionForClient(int userID, Decision decision) {
        clients.forEach((id, client) -> client.playerMadeDecision(userID, decision));
    }

    /**
     * Informs each player about the flop cards and current pot size
     *
     * @param card1 Card one in the flop
     * @param card2 Card two in the flop
     * @param card3 Card three in the flop
     */
    public void setFlop(Card card1, Card card2, Card card3) {
        clients.forEach((id, client) -> client.setFlop(card1, card2, card3));
    }

    /**
     * Informs each client about the turn card and current pot size
     *
     * @param turn Card displayed in the turn
     */
    public void setTurn(Card turn) {
        clients.forEach((id, client) -> client.setTurn(turn));
    }

    /**
     * Informs each client about the river card and current pot size
     *
     * @param river Card displayed in the river
     */
    public void setRiver(Card river) {
        clients.forEach((id, client) -> client.setRiver(river));
    }

    /**
     * Informs each client about the stack sizes of all players
     *
     * @param stackSizes Map of player ID mapped to his stack size
     */
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        clients.forEach((id, client) -> {
            client.setAmountOfPlayers(stackSizes.size());
            client.setStackSizes(new HashMap<>(stackSizes));
        });
    }

    /**
     * Makes each client start a new hand.
     */
    public void startNewHand() {
        clients.forEach((id, client) -> client.startNewHand());
    }

    /**
     * Tell a client that the game is over.
     *
     * @param clientID id of the player
     * @param personalStats The statistics for the given player id
     */
    public void gameOver(int clientID, Statistics personalStats) {
        if (clients.get(clientID) != null)
            clients.get(clientID).gameOver(personalStats);
    }

    /**
     * Sends the position of each player to all clients
     * @param positions Positions of the players, indexed by their IDs
     */
    public void setPositions(Map<Integer, Integer> positions) {
        clients.forEach((id, client) -> client.setPositions(new HashMap<>(positions)));
    }

    /**
     * Print a message to all clients' logs
     */
    public void printToLogField(String message) {
        clients.forEach((id, client) -> client.printToLogField(message));
    }

    /**
     *  Called every time a hand is won before showdown (everyone but 1 player folded)
     *  Prints a text showing who won the pot and how much it was. Also prints to log field
     */
    public void preShowdownWinner(int winnerID) {
        clients.forEach((id, client) -> client.preShowdownWinner(winnerID));
    }
    /**
     * Called every time a player is bust to inform all clients
     * @param rank Place the busted player finished in
     */
    public void bustClient(int bustPlayerID, int rank) {
        clients.forEach((id, client) -> client.playerBust(bustPlayerID, rank));

        GameClient bustedClient = clients.get(bustPlayerID);
        if (!(bustedClient instanceof GUIClient || bustedClient instanceof NetworkClient)) {
            clients.remove(bustPlayerID);
        } else if (this.gameType == MainScreen.GameType.SINGLE_PLAYER){
            showAllPlayerCards = true;
        }
    }

    /**
     * Tell the GUI to show player cards for all players
     * @param holeCards Hole cards of the players to show, indexed by playerIDs
     */
    public void showHoleCards(Map<Integer, Card[]> holeCards) {
        clients.forEach((clientId, client) ->
                holeCards.forEach((id, cards) ->
                        client.setHandForClient(id, cards[0], cards[1]))
        );
    }
}
