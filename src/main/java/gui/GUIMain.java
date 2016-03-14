package main.java.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import main.java.gamelogic.*;

/**
 * Created by ady on 05/03/16.
 */
public class GUIMain extends Application{

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

}
