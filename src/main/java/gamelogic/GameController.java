package main.java.gamelogic;

import main.java.gamelogic.ai.SimpleAI;
import main.java.gui.*;

import java.util.HashMap;
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
        gameSettings = new GameSettings(1000, 50, 25, 2, 10);
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
        if (!game.isValid()) {
            //TODO: Tell GUI to display error-message that settings are not valid
            mainGUI.displayErrorMessageToLobby("Illegal settings!");
            return;
        }

        //Empty map of clients
        clients = new HashMap<Integer, GameClient>();

        //Init GUIGameClient
        GameClient guiClient = mainGUI.displayGameScreen(gamesettings, 0); //0 --> playerID
        clients.put(0, guiClient);
        game.addPlayer(this.name, 0);

        //AIGameClient
        GameClient aiClient = new SimpleAI(1, 2.0);
        clients.put(1, aiClient);
        game.addPlayer("SimpleAI-player", 1);

        //Should maybe be called by game
        initClients(gamesettings);

        //TODO: add all players to GUI
        mainGUI.insertPlayer(0, this.name, gamesettings.getStartStack(), "Dealer");
        mainGUI.insertPlayer(1, "SimpleAI-player", gamesettings.getStartStack(), "Big blind");

        Thread thread = new Thread("GameThread") {
            @Override
            public void run() {
                game.playGame();
            }
        };

        thread.start();
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

    public void setHandForClient(int userID, Card card1, Card card2) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setHandForClient(userID, card1, card2);
        }
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
}
