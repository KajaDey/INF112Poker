package main.java.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import main.java.gamelogic.*;

import java.util.HashMap;
import java.util.Map;

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

    public void displayLobbyScreen(String name, int numberOfPlayers, String gameType,GameSettings gameSettings){
        GameLobby.createScreenForGameLobby(gameSettings,gamecontroller);
    }

    public static void main(String[] args) {
        GUIMain gui = new GUIMain();
        launch(args);
    }

    public void start(Stage window) throws Exception {
        SceneBuilder.showCurrentScene(SceneBuilder.createSceneForInitialScreen("PokerTable",gamecontroller), "Main Screen");
    }

    public GUIClient displayGameScreen(GameSettings settings, int userID) {
        this.gameScreen = new GameScreen(userID);
        this.client = new GUIClient(userID, gameScreen);
        ButtonListeners.setClient(client);

        //Create initial screen, empty
        SceneBuilder.showCurrentScene(gameScreen.createSceneForGameScreen(settings),"GameScreen");

        return client;
    }

    public boolean insertPlayer(int userID, String name, long stackSize, String pos) {
        return gameScreen.insertPlayer(userID, name, stackSize, pos);
    }

}
