package gamelogic;

import gamelogic.ai.SimpleAI;
import gui.*;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kristianrosland on 07.03.2016.
 *
 * A game controller to connect GUI and back end. GUI informs the controller that something happened, and the controller
 * asks back end to execute the requested move.
 */
public class GameController {

    private Game game;
    private Map<Integer, GameClient> clients;
    private GUIClient guiClient;
    private GUIMain mainGUI;
    public GameSettings gameSettings;
    private String name;

    public GameController(GUIMain gui) {
        this.mainGUI = gui;
        gameSettings = new GameSettings(5000, 50, 25, 2, 10, "Simple AI");
    }

    /**
     * Called when the enter button is clicked.
     * Checks valid number of players, then makes GUI show the lobby screen
     *
     * @param name
     * @param numPlayers
     * @param gameType
     */
    public void enterButtonClicked(String name, int numPlayers, String gameType) {
        //TODO: Validate input
        //assert numPlayers == 2 : "Number of players MUST be 2";

        //Tell GUI to display Lobby
        mainGUI.displayLobbyScreen(name, numPlayers, gameType, gameSettings);
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
        Runnable task = () -> {
            guiClient.setAmountOfPlayers(gamesettings.getMaxNumberOfPlayers());
        };
        Platform.runLater(task);
        game = new Game(gamesettings, this);

        String error;
        if (((error = game.getError()) != null)) {
            mainGUI.displayErrorMessageToLobby(error);
            return;
        }

        //Empty map of clients
        clients = new HashMap<>();

        //Init GUIGameClient
        guiClient = mainGUI.displayGameScreen(gamesettings, 0);
        clients.put(0, guiClient);
        game.addPlayer(this.name, 0);
        mainGUI.insertPlayer(0, this.name, gamesettings.getStartStack());
        guiClient.setAmountOfPlayers(gamesettings.getMaxNumberOfPlayers());

        //Init AIClients
        int numOfAIs = gamesettings.getMaxNumberOfPlayers() - 1;
        for (int i = 0; i < numOfAIs; i++) {
            String aiName = NameGenerator.getRandomName();
            int AI_id = i+1;
            GameClient aiClient = new SimpleAI(AI_id, 0.75);
            clients.put(AI_id, aiClient);
            game.addPlayer(aiName, AI_id);
            mainGUI.insertPlayer(AI_id, aiName, gamesettings.getStartStack());
        }

        //Set initial values for clients
        initClients(gamesettings);

        Thread gameThread = new Thread("GameThread") {
            @Override
            public void run() {
                game.playGame();
            }
        };
        gameThread.start();

        //Print to on screen log
        this.printToLogfield("Game with " + gameSettings.getMaxNumberOfPlayers() + " players started!");
    }

    /**
     * Informs each client about the small and big blind amount
     *
     * @param gamesettings
     */
    public void initClients(GameSettings gamesettings) {
        for (Integer clientID : clients.keySet()) {
            GameClient client = clients.get(clientID);
            client.setBigBlind(gamesettings.getBigBlind());
            client.setSmallBlind(gamesettings.getSmallBlind());
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
        if (client == null) {
            return null;
        }
        return client.getDecision();
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
     * Tells each client that it is time for show down, and who is the winner of the hand
     *
     * @param playersStillPlaying Players still in the hand
     * @param winnerID ID of the winning player
     * @param holeCards Map of player IDs and their hole cards
     * @param pot Total amount in the pot
     */
    public void showDown(List<Integer> playersStillPlaying, int winnerID, Map<Integer, Card[]> holeCards, long pot) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.showdown(playersStillPlaying, winnerID, holeCards, pot);
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
     * @param decision The dicision that was made
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
     * Informs each client about the stacksizes of all players
     *
     * @param stackSizes Map of player ID mapped to his stacksize
     */
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            //assert stackSizes.size() == 2;
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
     * @param winnerID ID of the winning player
     */
    public void gameOver(int winnerID) {
        for (Integer clientID : clients.keySet()) {
            GameClient client = clients.get(clientID);
            client.gameOver(winnerID);
        }
    }

    /**
     * TODO write javadoc
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
}
