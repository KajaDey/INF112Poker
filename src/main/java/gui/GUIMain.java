package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import gamelogic.*;
import replay.ReplayClient;
import replay.ReplayReader;

import java.net.InetAddress;

/**
 * This is the main method of the game and contains prints to file
 *
 * @author André Dyrstad
 * @author Kristian Rosland
 * @author Morten Lohne
 */
public class GUIMain extends Application{

    public static GUIMain guiMain;

    private GameController gameController;
    private GameScreen gameScreen;
    private GUIClient client;

    public final Logger logger;

    public GUIMain() {
        this.logger = new Logger("Client", "");
        this.gameController = new GameController(new GameSettings(), this);
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

    public void displayMultiPlayerScreen(String name, InetAddress IPAddress) {
        new LobbyScreen(gameController, name, IPAddress, logger);
    }

    /**
     * Starts the display of initial screen.
     * @param window The window to be displayed
     */
    public void start(Stage window){
        MainScreen.createSceneForMainScreen("PokerTable", gameController);
    }

    /**
     * Displays the game screen
     * @param userID The id of the user
     * @return The GUIClient to display
     */
    public GUIClient displayGameScreen(int userID) {
        this.gameScreen = new GameScreen(userID, logger);
        this.client = new GUIClient(userID, gameScreen, logger);
        ButtonListeners.setClient(client);

        //Create initial screen, empty
        Platform.runLater(() -> SceneBuilder.showCurrentScene(gameScreen.createSceneForGameScreen(), "GameScreen"));

        return client;
    }

    public GUIClient displayReplayScreen(int userID, ReplayReader reader) {
        this.gameScreen = new GameScreen(userID, logger);
        this.client = new ReplayClient(userID, gameScreen, reader, logger);

        Platform.runLater(() -> SceneBuilder.showCurrentScene(gameScreen.createSceneForGameScreen(), "GameScreen"));
        return client;
    }
}
