package main.java.gamelogic;

import main.java.gamelogic.ai.SimpleAI;
import main.java.gui.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class GameController {

    private Game game;
    private Map<Integer, GameClient> clients;
    private GUIMain mainGUI;
    public GameSettings gameSettings;
    private String name;

    public GameController(GUIMain gui) {
        this.mainGUI = gui;
        gameSettings = new GameSettings(5000, 50, 25, 2, 10);
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
        assert numPlayers == 2 : "Number of players MUST be 2";

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
        game = new Game(gamesettings, this);
        if (!(game.getError() == null)) {
            //TODO: Tell GUI to display error-message that settings are not valid
            mainGUI.displayErrorMessageToLobby(game.getError());
            return;
        }

        //Empty map of clients
        clients = new HashMap<Integer, GameClient>();

        //Init GUIGameClient
        GameClient guiClient = mainGUI.displayGameScreen(gamesettings, 0); //0 --> playerID
        clients.put(0, guiClient);
        game.addPlayer(this.name, 0);

        //AIGameClient
        GameClient aiClient = new SimpleAI(1, 1.0);
        clients.put(1, aiClient);
        game.addPlayer("SimpleAI-player", 1);

        //Should maybe be called by game
        initClients(gamesettings);

        //TODO: add all players to GUI
        mainGUI.insertPlayer(0, this.name, gamesettings.getStartStack());
        mainGUI.insertPlayer(1, "SimpleAI-player", gamesettings.getStartStack());

        Thread gameThread = new Thread("GameThread") {
            @Override
            public void run() {
                game.playGame();
            }
        };

        gameThread.start();
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
            assert stackSizes.size() == 2;
            c.setAmountOfPlayers(stackSizes.size());
            c.setStackSizes(stackSizes);
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

    public void setPositions(Map<Integer, Integer> positions) {
        for (Integer clientID : clients.keySet()) {
            GameClient client = clients.get(clientID);
            client.setPositions(positions);
        }
    }
}
