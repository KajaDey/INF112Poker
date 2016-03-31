package gui;

import javafx.application.Application;
import javafx.stage.Stage;
import gamelogic.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

/**
 * Created by ady on 05/03/16.
 */
public class GUIMain extends Application{

    private static final boolean PRINT_DEBUG_TO_STDOUT = true;
    private static final boolean PRINT_DEBUG_LOG = true;
    private static Optional<PrintWriter> logWriter = Optional.empty();

    private GameController gamecontroller;
    private GameScreen gameScreen;
    private GUIClient client;

    public GUIMain() {
            this.gamecontroller= new  GameController(this);
    }

    public static void main(String[] args) {
        GUIMain gui = new GUIMain();
        launch(args);
    }
    public void displayErrorMessageToLobby(String message){
        GameLobby.displayErrorMessage(message,gamecontroller);
    }

    /**
     * Displays the lobby screen
     * @param gameSettings The settings to be displayed
     */
    public void displayLobbyScreen(String name, int numberOfPlayers, String gameType, GameSettings gameSettings){
        GameLobby.createScreenForGameLobby(gameSettings, gamecontroller, name);
    }

    /**
     * Starts the displayment of initial screen.
     * @param window The window to be displayed
     */
    public void start(Stage window){
        MainScreen.createSceneForMainScreen("PokerTable", gamecontroller);

    }

    /**
     * Displays the game screen
     * @param settings The settings to use
     * @param userID The id of the user
     * @return The GUIClient to display
     */
    public GUIClient displayGameScreen(GameSettings settings, int userID) {
        this.gameScreen = new GameScreen(userID);
        this.client = new GUIClient(userID, gameScreen);
        ButtonListeners.setClient(client);

        //Create initial screen, empty
        SceneBuilder.showCurrentScene(gameScreen.createSceneForGameScreen(settings),"GameScreen");

        return client;
    }

    /**
     * Inserts a player into the game.
     * @param userID The ID of the player
     * @param name The name of the player
     * @param stackSize The player's stack size
     * @return The game screen containing the new player
     */
    public boolean insertPlayer(int userID, String name, long stackSize) {
        return gameScreen.insertPlayer(userID, name, stackSize);
    }

    /**
     * Prints a debug message to sysout and/or a lazily initialized log file
     * Constants PRINT_DEBUG_TO_STDOUT and PRINT_DEBUG_LOG control where the output is printed
     */
    public static void printDebugMessage(String message) {
        if (PRINT_DEBUG_TO_STDOUT) {
            System.out.println(message);
        }
        if (PRINT_DEBUG_LOG) {
            if (logWriter.isPresent()) {
                logWriter.get().println(message);
                logWriter.get().flush();
            }
            else {
                try {
                    logWriter = logWriter.of(new PrintWriter("poker" + System.currentTimeMillis() / 1000 + ".log", "UTF-8"));
                    logWriter.get().println(message);
                    logWriter.get().flush();
                } catch (FileNotFoundException e) {
                    // If creating the log file fails, do not write to it
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }
}
