package main.java.gamelogic;

import main.java.gui.GUIClient;
import main.java.gui.GUIMain;
import main.java.gui.GameSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class GameController {

    public Game game;
    public Map<Integer, GameClient> clients;
    public GUIMain mainGUI;

    public GameController(GUIMain gui) {
        this.mainGUI = gui;
    }

    public void enterButtonClicked(String name, int numPlayers, String gameType) {
        //TODO: Validate input


        //TODO: Tell GUI to set screen to Lobby screen
        System.out.println("Touchdown");

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

        //GUIGameClient
        GameClient guiClient = new GUIClient(0); //0 --> playerID
        clients.put(0, guiClient);
        game.addPlayer("Kristian", 0);

        //AIGameClient
        AI aiClient = new AI(1);
        clients.put(1, aiClient);
        game.addPlayer("AI-player", 1);

        //TODO: Tell GUIGameClient to got to table scene

        //Start the pokergame
        game.start();

        //TODO: Tell GUIGameClientObject what to display in the table screen, using the init-method from GameClient-interface
        //TODO: Tell the AIGameClient-object whats up with the table using the init-method from GC-interface
    }

    public Decision getDecisionFromClient(int ID) {
        GameClient client = clients.get(ID);
        if (client == null) { return null; }
        return client.getDecision();
    }
}
