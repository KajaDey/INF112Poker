package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import gamelogic.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Optional;

/**
 * This is the main method of the game and contains prints to file
 *
 * @author André Dyrstad
 * @author Kristian Rosland
 * @author Morten Lohne
 */
public class GUIMain extends Application{

    private static final boolean PRINT_DEBUG_TO_STDOUT = true;
    private static final boolean PRINT_DEBUG_LOG = true;
    private static Optional<PrintWriter> logWriter = Optional.empty();
    private static Optional<PrintWriter> replayWriter = Optional.empty();
    public static GUIMain guiMain;

    private GameController gameController;
    private GameScreen gameScreen;
    private GUIClient client;

    public GUIMain() {
            this.gameController = new GameController(new GameSettings(GameSettings.DEFAULT_SETTINGS), this);
    }

    public static void main(String[] args) {
        guiMain = new GUIMain();
        launch(args);
    }
    public void displayErrorMessageToLobby(String message){
        GameLobby.displayErrorMessage(message, gameController);
    }

    /**
     * Displays the lobby screen
     * @param gameSettings The settings to be displayed
     */
    public void displaySinglePlayerScreen(String name, GameSettings gameSettings){
        GameLobby.createScreenForGameLobby(this, gameSettings, gameController, name);

    }

    public void displayMultiPlayerScreen(String name, InetAddress IPAddress, GameSettings gameSettings) {
        new LobbyScreen(gameController, name, IPAddress);
    }

    /**
     * Starts the displayment of initial screen.
     * @param window The window to be displayed
     */
    public void start(Stage window){
        MainScreen.createSceneForMainScreen("PokerTable", gameController);
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
        Platform.runLater(() -> SceneBuilder.showCurrentScene(gameScreen.createSceneForGameScreen(settings), "GameScreen"));

        return client;
    }

    /**
     * Prints a debug message to sysout and/or a lazily initialized log file, and terminates the line
     * Constants PRINT_DEBUG_TO_STDOUT and PRINT_DEBUG_LOG control where the output is printed
     */
    public static void debugPrintln(String message) {
        debugPrint(message + "\n");
    }

    /**
     * Prints a debug message to sysout and/or a lazily initialized log file
     * Constants PRINT_DEBUG_TO_STDOUT and PRINT_DEBUG_LOG control where the output is printed
     */
    public static void debugPrint(String message) {
        if (PRINT_DEBUG_TO_STDOUT) {
            System.out.print(message);
        }
        if (PRINT_DEBUG_LOG) {
            if (logWriter.isPresent()) {
                logWriter.get().print(message);
                logWriter.get().flush();
            }
            else {
                try {
                    File logFile = new File("logs/poker" + System.currentTimeMillis() / 1000 + ".log");
                    new File("logs").mkdir();
                    logWriter = logWriter.of(new PrintWriter(logFile, "UTF-8"));
                    logWriter.get().print(message);
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

    /**
     * This method will save all the information needed to make a complete replay file.
     *
     * @param message The message to add to the replay file.
     */
    public static void replayLogPrint(String message){

        if (replayWriter.isPresent()) {
            replayWriter.get().print(message);
            replayWriter.get().flush();
        }
        else {
            try {
                File replayFile = new File("replays/poker" + System.currentTimeMillis() / 1000 + ".log");
                new File("replays").mkdir();
                replayWriter = replayWriter.of(new PrintWriter(replayFile, "UTF-8"));
                replayWriter.get().print(message);
                replayWriter.get().flush();
            } catch (FileNotFoundException e) {
                // If creating the log file fails, do not write to it
                System.out.println(e);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static void debugPrintln() {
        debugPrint("\n");
    }
}
