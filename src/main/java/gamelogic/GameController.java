package main.java.gamelogic;

import main.java.gui.GUIMain;
import main.java.gui.GameSettings;

import java.util.List;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class GameController {

    public static Game game;
    public static GameClient [] clients;
    public static GUIMain mainGUI;

    public static void main (String [] args) {
        //TODO: Display the first screen (login screen)
        GUIMain.run(args);
    }

    public static void EnterButtonClicked(String name, int numPlayers, String gameType) {
        //TODO: Validate input

        //TODO: Tell GUI to set screen to Lobby screen
        System.out.println("Touchdown");

    }

    public static void StartTournamentButtonClicked(GameSettings gamesettings) {
        //TODO: Validate the data, if invalid, report back to GUI


        //TODO: Make a new Game object
        //game = new Game(maxPlayers, startstack, startSB, startBB, levelDuration);

        //TODO: Make clients array
        //clients = new GameClient[maxPlayers];

        //Create a GUIGameClient
        //GameClient guiClient = new GUIClient(0); //0 --> playerID

        //TODO: Tell GUIGameClient to got to table scene
        // guiClient.

        //TODO: Tell GUIGameClientObject what to display in the table screen, using the init-method from GameClient-interface

        //AIGameClient
        //TODO: Make an AIGameClient-object
        AI ai = new AI(1);



        //TODO: Tell the AIGameClient-object whats up with the table using the init-method from GC-interface




    }
}
