package main.java.gamelogic;

import main.java.gui.GUIMain;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class GameController {

    public static void Main (String [] args) {
        //TODO: Display the first screen (login screen)
        // new GUI(this)
    }

    public void LoginButtonClicked() { //TODO: Set the parameters here
        //TODO: Validate input
        //TODO: Tell GUI to set screen to Lobby screen
    }

    public void StartTournamentButtonClicked() { //TODO: Set the parameters we need here
        //TODO: Validate the data, if invalid, report back to GUI
        //TODO: Make AIs for remaining players (1 AI in first iteration)
        //TODO: Make a new Game object

        //GUIGameClient
        //TODO: Tell GUI to set the screen to Table screen. ** This must return a GameClient-object! **
        //TODO: Tell GUIGameClientObject what to display in the table screen, using the init-method from GameClient-interface

        //AIGameClient
        //TODO: Make an AIGameClient-object
        //TODO: Tell the AIGameClient-object whats up with the table using the init-method from GC-interface
    }
}
