package main.java.gamelogic;

import main.java.gui.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class GameController {

    public Game game;
    public Map<Integer, GameClient> clients;
    public GUIMain mainGUI;
    public GameSettings gameSettings;


    public GameController(GUIMain gui) {
        this.mainGUI = gui;
        gameSettings = new GameSettings(1000,50,25,2,10);
    }


    public void enterButtonClicked(String name, int numPlayers, String gameType) {
        //TODO: Validate input


        //TODO: Tell GUI to set screen to Lobby screen
        System.out.println("Touchdown");
        //SceneBuilder.showCurrentScene(GameLobby.createScreenForGameLobby());

        mainGUI.displayLobbyScreen(name, numPlayers, gameType, gameSettings);

    }

    public void startTournamentButtonClicked(GameSettings gamesettings) {
        //Make a new Game object and validate
        game = new Game(gamesettings);
        if (!game.isValid()) {
            //TODO: Tell GUI to display error-message that settings are not valid
            return;
        }

        //Empty map of clients
        clients = new HashMap<Integer, GameClient>();

        //Init GUIGameClient
        GameClient guiClient = mainGUI.displayGameScreen(gamesettings, 0); //0 --> playerID
        clients.put(0, guiClient);
        game.addPlayer("Kristian", 0);

        //AIGameClient
        AI aiClient = new AI(1);
        clients.put(1, aiClient);
        game.addPlayer("AI-player", 1);

        //TODO: add all players to GUI
        mainGUI.insertPlayer(0, "Kristian", gamesettings.getStartStack(), "Dealer");
        mainGUI.insertPlayer(1, "AI-player", gamesettings.getStartStack(), "Big blind");


        tests();

        Thread thread = new Thread() {
            @Override
            public void run() {
                Decision d = guiClient.getDecision();
            }
        };

        thread.start();


        // System.out.println("TOUCHDOWN!!!!!");


        //Start the pokergame
        // game.playGame();

        //TODO: Tell GUIGameClientObject what to display in the table screen, using the init-method from GameClient-interface
        //TODO: Tell the AIGameClient-object whats up with the table using the init-method from GC-interface

    }

    public Decision getDecisionFromClient(int ID) {
        GameClient client = clients.get(ID);
        if (client == null) { return null; }
        return client.getDecision();
    }

    public void setGameSettings(GameSettings gameSettings){
        this.gameSettings = gameSettings;
    }

    public void setHandForClient(int userID, Card card1, Card card2) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setHandForClient(userID, card1, card2);
        }
    }

    public void setFlop(Card card1, Card card2, Card card3) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setFlop(card1, card2, card3);
        }
    }

    public void setTurn(Card turn) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setTurn(turn);
        }
    }

    public void setRiver(Card river) {
        for (Integer clientID : clients.keySet()) {
            GameClient c = clients.get(clientID);
            c.setRiver(river);
        }
    }

    public void tests() {
        Deck deck = new Deck();

        this.setHandForClient(0, deck.draw().get(), deck.draw().get());
        this.setHandForClient(1, deck.draw().get(), deck.draw().get());

        this.setFlop(deck.draw().get(), deck.draw().get(), deck.draw().get());
        this.setTurn(deck.draw().get());
        this.setRiver(deck.draw().get());


    }
}
