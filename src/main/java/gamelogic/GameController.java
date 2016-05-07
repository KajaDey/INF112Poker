package gamelogic;

import gamelogic.ai.MCTSAI;
import gamelogic.ai.SimpleAI;
import gui.*;

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

    /**
     * Used by GUI to create a new single player game
     */
    public GameController(GameSettings settings, GUIMain guiMain) {
        this.guiMain = Optional.of(guiMain);
        this.gameSettings = settings;
        this.game = new Game(settings, this);

        clients = new HashMap<>();
        names = new HashMap<>();
    }

    /**
     * Used by Server to create a new GameController for a network game
     */
    public GameController(GameSettings settings) {
        this.guiMain = Optional.empty();
        this.gameSettings = settings;
        this.game = new Game(settings, this);

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
     * @param name
     */
    public void enterButtonClicked(String name, InetAddress IPAddress, MainScreen.GameType gameType) {
        //Tell GUI to display Lobby
        gameSettings = new GameSettings(GameSettings.DEFAULT_SETTINGS);

        if(gameType == MainScreen.GameType.SINGLE_PLAYER)
            guiMain.get().displaySinglePlayerScreen(name, gameSettings);
        else
            guiMain.get().displayMultiPlayerScreen(name, IPAddress, gameSettings);

        this.name = name;
    }

    /**
     * Called when the start tournament button is clicked.
     * Creates a new game, creates clients and starts the game.
     *
     * @param showCards If all players hole cards should be visible or not
     */
    public Thread initGame(boolean showCards, List<Socket> clientSockets) throws Game.InvalidGameSettingsException {
        //Make a new Game object and validate
        game = new Game(gameSettings, this);
        this.showAllPlayerCards = showCards;

        GUIMain.replayLogPrint("SETTINGS\n" + gameSettings.toString());

        if ((game.getError() != null)) {
            throw new Game.InvalidGameSettingsException(game.getError());
        }
        NameGenerator.readNewSeries();

        //Create GUI-GameClient
        if (guiMain.isPresent()) {
            createGUIClient(gameSettings);
        }

        System.out.println("Creating " + clientSockets.size() + " network clients");
        // Create network clients
        createNetworkClients(clientSockets);

        System.out.println("Creating ai clients");
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
     * Create a GUI-client with initial values
     */
    private void createGUIClient(GameSettings settings) {
        GameClient guiClient = guiMain.get().displayGameScreen(settings, 0);
        clients.put(0, guiClient);
        game.addPlayer(this.name, 0);
        guiMain.get().insertPlayer(0, this.name, settings.getStartStack());
        guiClient.setAmountOfPlayers(settings.getMaxNumberOfPlayers());
        names.put(0, name);
        GUIMain.debugPrintln("Initialized " + guiClient.getClass().getSimpleName() + " " + names.get(0));
    }

    private void createNetworkClients(List<Socket> clientSockets) {
        ArrayList<Thread> clientThreads = new ArrayList<>();
        for (int i = clients.size(); i < clientSockets.size(); i++) {
            int id = i;
            Runnable r = () -> {
                GameClient networkClient;
                try {
                    Socket socket = clientSockets.get(guiMain.isPresent() ? id - 1 : id);
                    System.out.println("Creating network client");
                    networkClient = new NetworkClient(socket, id);
                } catch (IOException e) {
                    System.out.println("Failed to connect to a client, dropping client");
                    return;
                }
                System.out.println("Connected to network client");
                networkClient.setAmountOfPlayers(gameSettings.getMaxNumberOfPlayers());
                String name = networkClient.getName();
                if (name.equals("")) {
                    name = NameGenerator.getRandomName();
                }
                synchronized (this) {
                    clients.put(id, networkClient);

                    game.addPlayer(name, id);
                    assert !names.containsKey(id);
                    names.put(id, name);
                    GUIMain.debugPrintln("Initialized " + networkClient.getClass().getSimpleName() + " " + names.get(id));
                }

            };
            clientThreads.add(new Thread(r));
            System.out.println("Connecting to client...");
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
                    aiClient = new MCTSAI(AI_id, contemptFactor);
                    break;
                case SIMPLE_AI:
                    aiClient = new SimpleAI(AI_id, contemptFactor);
                    break;
                case MIXED:
                    aiClient = Math.random() > 0.5 ? new MCTSAI(AI_id) : new SimpleAI(AI_id, contemptFactor);
                    break;
                default: throw new IllegalStateException(); // Exception to please our java overlords
            }

            aiClient.setAmountOfPlayers(settings.getMaxNumberOfPlayers());
            clients.put(AI_id, aiClient);
            game.addPlayer(aiName, AI_id);
            if (guiMain.isPresent()) {
                guiMain.get().insertPlayer(AI_id, aiName, settings.getStartStack());
            }
            assert !names.containsKey(AI_id) : "Name list already has a name for " + AI_id;
            names.put(AI_id, aiName);
            GUIMain.debugPrintln("Initialized " + aiClient.getClass().getSimpleName() + " " + names.get(AI_id));
        }
    }

    /**
     * @param id The id of this client
     * @param client Network or AI client
     * @param name Name of the client
     */
    public void addClient(int id, GameClient client, String name) {
        this.clients.put(id, client);
        game.addPlayer(name, id);
        client.setAmountOfPlayers(gameSettings.getMaxNumberOfPlayers());
        names.put(id, name);
    }


    /**
     * Informs each client about the small and big blind amount
     */
    private void initClients() {
        setBlinds();
        for (Integer clientID : clients.keySet()) {
            clients.get(clientID).setPlayerNames(new HashMap<>(names));
        }
        game.refreshAllStackSizes();
        //setPositions(game.);

    }

    /**
     * Sends the blinds to all the clients
     */
    public void setBlinds() {
        for (int clientID : clients.keySet()) {
            clients.get(clientID).setBigBlind(gameSettings.getBigBlind());
            clients.get(clientID).setSmallBlind(gameSettings.getSmallBlind());
        }
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
            return client.getDecision(20000L);

    }

    /**
     *  Get a decision from an AI-client
     * @param aiClient
     * @return
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
     * @param delayTime
     */
    public void delay(long delayTime) {
        System.out.println("Delay");
        try { Thread.sleep(Math.max(0, delayTime)); }
        catch (InterruptedException e) {
            System.out.println("Sleeping thread interrupted");
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
     *
     * @param showdownStats Information about pot (and side pots) and who won
     */
    public void showdown(ShowdownStats showdownStats) {
        String[] tokens = UpiUtils.tokenize("\"" + showdownStats.getWinnerText().replace("\n", "\" \"") + "\"").get();
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.showdown(tokens);
        }
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
            clients.forEach((id, client) -> client.setHandForClient(id, card1, card2));
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
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.playerMadeDecision(userID, decision);
        }
    }

    /**
     * Informs each player about the flop cards and current pot size
     *
     * @param card1 Card one in the flop
     * @param card2 Card two in the flop
     * @param card3 Card three in the flop
     */
    public void setFlop(Card card1, Card card2, Card card3) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setFlop(card1, card2, card3);
        }
    }

    /**
     * Informs each client about the turn card and current pot size
     *
     * @param turn Card displayed in the turn
     */
    public void setTurn(Card turn) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setTurn(turn);
        }
    }

    /**
     * Informs each client about the river card and current pot size
     *
     * @param river Card displayed in the river
     */
    public void setRiver(Card river) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setRiver(river);
        }
    }

    /**
     * Informs each client about the stack sizes of all players
     *
     * @param stackSizes Map of player ID mapped to his stack size
     */
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setAmountOfPlayers(stackSizes.size());
            c.setStackSizes(new HashMap<>(stackSizes));
        }
    }

    /**
     * Makes each client start a new hand.
     */
    public void startNewHand() {
        for (Integer clientID : clients.keySet()) {
            GameClient client = clients.get(clientID);
            client.startNewHand();
        }
    }

    /**
     * Tells each client that the game is over.
     *
     * @param stats The statistics of players
     */
    public void gameOver(Statistics stats) {
        for (Integer clientID : clients.keySet()) {
            GameClient client = clients.get(clientID);
            client.gameOver(stats);
        }
    }

    /**
     * Sends the position of each player to all clients
     * @param positions Positions of the players, indexed by their IDs
     */
    public void setPositions(Map<Integer, Integer> positions) {
        for (Integer clientID : clients.keySet()) {
            GameClient client = clients.get(clientID);
            client.setPositions(positions);
        }
    }

    /**
     * Print a message to all clients' logs
     */
    public void printToLogField(String message) {
        clients.forEach((id, client) -> client.printToLogField(message));
    }

    /**
     *  Called every time a hand is won before showdown (everyone but 1 player folded)
     *  Prints a text showing who won the pot and how much it was. Also prints to logfield
     */
    public void preShowdownWinner(int winnerID) {
        clients.forEach((id, client) -> client.preShowdownWinner(winnerID));
    }
    /**
     * Called every time a player is bust to inform all clients
     * @param rank Place the busted player finished in
     */
    public void bustClient(int bustPlayerID, int rank) {
        for (Integer clientID : clients.keySet())
            clients.get(clientID).playerBust(bustPlayerID, rank);

        GameClient bustedClient = clients.get(bustPlayerID);
        if (!(bustedClient instanceof GUIClient)) {
            clients.remove(bustPlayerID);
        } else {
            showAllPlayerCards = true;
        }
    }

    /**
     * Tell the GUI to show player cards
     * @param holeCards Hole cards of the players to show, indexed by playerIDs
     */
    public void showHoleCards(Map<Integer, Card[]> holeCards) {
        //guiClient.showHoleCards(holeCards); This method has been removed
        clients.forEach((clientId, client) -> {
            holeCards.forEach((id, cards) -> client.setHandForClient(id, cards[0], cards[1]));
        });
    }
}
