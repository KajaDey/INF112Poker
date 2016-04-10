package gamelogic;

import gamelogic.ai.MCTSAI;
import gamelogic.ai.SimpleAI;
import gui.*;

import java.util.*;

/**
 * Created by kristianrosland on 07.03.2016.
 *
 * A controller to function as a layer between the GUI and the back end.
 */
public class GameController {
    public enum AIType { MCTS_AI, SIMPLE_AI }

    private Game game;
    private Map<Integer, GameClient> clients;
    private GUIClient guiClient;
    private GUIMain mainGUI;
    public GameSettings gameSettings;
    private String name;
    private Map<Integer, String> names;

    public GameController(GUIMain gui) {
        this.mainGUI = gui;
    }

    /**
     * Called when the enter button is clicked.
     * Checks valid number of players, then makes GUI show the lobby screen
     *  @param name
     * @param numPlayers
     * @param aiType Type of AI (Simple or MCTS)
     */
    public void enterButtonClicked(String name, int numPlayers, AIType aiType) {
        //Tell GUI to display Lobby
        gameSettings = new GameSettings(5000, 50, 25, numPlayers, 10, aiType);
        mainGUI.displayLobbyScreen(name, gameSettings);
        this.name = name;
    }

    /**
     * Called when the start tournament button is clicked.
     * Creates a new game, creates clients and starts the game.
     *
     * @param gamesettings Game settings
     */
    public void startTournamentButtonClicked(GameSettings gamesettings) {
        //Make a new Game object and validate
        game = new Game(gamesettings, this);

        String error;
        if (((error = game.getError()) != null)) {
            mainGUI.displayErrorMessageToLobby(error);
            return;
        }

        //Empty maps of clients/names
        clients = new HashMap<>();
        names = new HashMap<>();

        //Create GUI-GameClient
        createGUIClient(gamesettings);

        //Create AI-GameClients
        int numberOfAIClients = gameSettings.getMaxNumberOfPlayers() - 1;
        createAIClients(numberOfAIClients, gamesettings);

        //Set initial blind values for clients
        initClients(gamesettings);

        //Print welcome message to log
        this.printToLogfield("Game with " + gameSettings.getMaxNumberOfPlayers() + " players started!");

        Thread gameThread = new Thread("GameThread") {
            @Override
            public void run() {
                game.playGame();
            }
        };
        gameThread.start();
    }

    /**
     *  Create a GUI-client with initial values
     * @param settings
     */
    private void createGUIClient(GameSettings settings) {
        guiClient = mainGUI.displayGameScreen(settings, 0);
        clients.put(0, guiClient);
        game.addPlayer(this.name, 0);
        mainGUI.insertPlayer(0, this.name, settings.getStartStack());
        guiClient.setAmountOfPlayers(settings.getMaxNumberOfPlayers());
        names.put(0, name);
    }

    /**
     * Create a given number of AI-clients to correspond to Player's in Game
     * @param numberOfAIs
     * @param settings
     */
    private void createAIClients(int numberOfAIs, GameSettings settings) {
        NameGenerator.readNewSeries();

        for (int i = 0; i < numberOfAIs; i++) {
            String aiName = NameGenerator.getRandomName();
            int AI_id = i+1;

            GameClient aiClient;
            if (gameSettings.getAIType() == AIType.MCTS_AI)
                aiClient = new MCTSAI(AI_id);
            else
                aiClient = new SimpleAI(AI_id, 0.9);

            aiClient.setAmountOfPlayers(settings.getMaxNumberOfPlayers());
            clients.put(AI_id, aiClient);
            game.addPlayer(aiName, AI_id);
            mainGUI.insertPlayer(AI_id, aiName, settings.getStartStack());
            names.put(AI_id, aiName);
        }
    }

    /**
     * Informs each client about the small and big blind amount
     *
     * @param gamesettings
     */
    private void initClients(GameSettings gamesettings) {
        for (Integer clientID : clients.keySet()) {
            GameClient client = clients.get(clientID);
            client.setBigBlind(gamesettings.getBigBlind());
            client.setSmallBlind(gamesettings.getSmallBlind());
            client.setPlayerNames(names);
        }
    }

    /**
     * Asks a client for a decision, if he exists
     *
     * @param ID Client ID
     * @return Decision made by the client
     */
    public Decision getDecisionFromClient(int ID) {
        GameClient client = clients.get(ID);
        if (client instanceof SimpleAI)
            addDelayTimeForDecision();
        return client.getDecision();
    }

    /**
     * Delays the execution 1-3 seconds (to make Simple-AI decision time look more realistic)
     */
    private void addDelayTimeForDecision() {
        Random rand = new Random();
        try { Thread.sleep(1000 + rand.nextInt(2000)); }
        catch (Exception e) {
            System.out.println("Thread " + Thread.currentThread() + " was interrupted");
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
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.showdown(showdownStats);
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
        GameClient c = clients.get(clientID);
        c.setHandForClient(clientID, card1, card2);
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
     * @param currentPotSize Current amount in the pot
     */
    public void setFlop(Card card1, Card card2, Card card3, long currentPotSize) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setFlop(card1, card2, card3, currentPotSize);
        }
    }

    /**
     * Informs each client about the turn card and current pot size
     *
     * @param turn Card displayed in the turn
     * @param currentPotSize Current amount in the pot
     */
    public void setTurn(Card turn, long currentPotSize) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setTurn(turn, currentPotSize);
        }
    }

    /**
     * Informs each client about the river card and current pot size
     *
     * @param river Card displayed in the river
     * @param currentPotSize Current amount in the pot
     */
    public void setRiver(Card river, long currentPotSize) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setRiver(river, currentPotSize);
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
     * @param positions
     */
    public void setPositions(Map<Integer, Integer> positions) {
        for (Integer clientID : clients.keySet()) {
            GameClient client = clients.get(clientID);
            client.setPositions(positions);
        }
    }

    /**
     * Print a message to the on screen log
     * @param message
     */
    public void printToLogfield(String message) {
        guiClient.printToLogfield(message);
    }

    /**
     *  Called every time a hand is won before showdown (everyone but 1 player folded)
     *  Prints a text showing who won the pot and how much it was. Also prints to logfield
     * @param winnerID
     * @param potSize
     */
    public void preShowdownWinner(int winnerID, long potSize) {
        guiClient.preShowdownWinner(winnerID, potSize);
    }
    /**
     * Called every time a player is bust to inform all clients
     * @param bustPlayerID
     * @param rank Place the busted player finished in
     */
    public void bustClient(int bustPlayerID, int rank) {
        for (Integer clientID : clients.keySet())
            clients.get(clientID).playerBust(bustPlayerID, rank);

        GameClient bustedClient = clients.get(bustPlayerID);
        if (!(bustedClient instanceof GUIClient)) {
            clients.remove(bustPlayerID);
        }
    }

    /**
     * Tell the GUI to show player cards
     * @param playerList
     * @param holeCards
     */
    public void showHoleCards(ArrayList<Integer> playerList, Map<Integer, Card[]> holeCards) {
        guiClient.showHoleCards(playerList, holeCards);
    }
}
