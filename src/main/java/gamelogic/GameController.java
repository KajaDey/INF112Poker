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

    public void enterButtonClicked(String name, int numPlayers, String gameType) {
        //TODO: Validate input
        assert numPlayers == 2 : "Number of players MUST be 2";

        //Tell GUI to display Lobby
        mainGUI.displayLobbyScreen(name, numPlayers, gameType, gameSettings);
        this.name = name;

    }

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
        mainGUI.insertPlayer(0, this.name, gamesettings.getStartStack(), "Dealer");
        mainGUI.insertPlayer(1, "SimpleAI-player", gamesettings.getStartStack(), "Big blind");

        Thread gameThread = new Thread("GameThread") {
            @Override
            public void run() {
                game.playGame();
            }
        };

        gameThread.start();
    }

    public void initClients(GameSettings gamesettings) {
        for (Integer clientID : clients.keySet()) {
            GameClient client = clients.get(clientID);
            client.setBigBlind(gamesettings.getBigBlind());
            client.setSmallBlind(gamesettings.getSmallBlind());
        }
    }

    public Decision getDecisionFromClient(int ID) {
        GameClient client = clients.get(ID);
        if (client == null) {
            return null;
        }
        return client.getDecision();
    }

    public void setGameSettings(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
    }

    public void showDown(List<Integer> playersStillPlaying, int winnerID, Map<Integer, Card[]> holeCards, long pot) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.showdown(playersStillPlaying, winnerID, holeCards, pot);
        }
    }

    public void setHandForClient(int clientID, Card card1, Card card2) {
        GameClient c = clients.get(clientID);
        c.setHandForClient(clientID, card1, card2);
    }

    public void setDecisionForClient(int userID, Decision decision) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.playerMadeDecision(userID, decision);
        }
    }

    public void setFlop(Card card1, Card card2, Card card3, long currentPotSize) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setFlop(card1, card2, card3, currentPotSize);
        }
    }

    public void setTurn(Card turn, long currentPotSize) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setTurn(turn, currentPotSize);
        }
    }

    public void setRiver(Card river, long currentPotSize) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setRiver(river, currentPotSize);
        }
    }

    public void setStackSizes(Map<Integer, Long> stackSizes) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            assert stackSizes.size() == 2;
            c.setAmountOfPlayers(stackSizes.size());
            c.setStackSizes(stackSizes);
        }
    }

    public void startNewHand() {
        for (Integer clientID : clients.keySet()) {
            GameClient client = clients.get(clientID);
            client.startNewHand();
        }
    }

    public void gameOver(int winnerID) {
        for (Integer clientID : clients.keySet()) {
            GameClient client = clients.get(clientID);
            client.gameOver(winnerID);
        }
    }
}
