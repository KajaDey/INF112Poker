package main.java.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import main.java.gamelogic.Card;
import main.java.gamelogic.Game;
import main.java.gamelogic.GameController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ady on 05/03/16.
 */
public class GUIMain extends Application{

    private GameController gamecontroller;

    public GUIMain() {
        this.gamecontroller = new GameController(this);
    }

    public GameController getGamecontroller(){
        return gamecontroller;
    }

    public void displayLobbyScreen(String name, int numberOfPlayers, String gameType,GameSettings gameSettings){
        GameLobby.createScreenForGameLobby(gameSettings,gamecontroller);
    }

    public static void main(String[] args) {
        GUIMain gui = new GUIMain();
        launch(args);
    }

    //TODO: DEPRECATED
    public static void run(String[] args){
        launch(args);
    }

    public void start(Stage window) throws Exception {
        //TODO: Stop using fucking kake and katt.........
        GUIClient kake = new GUIClient(0);

        //TODO: Get this to take gamecontroller as a parameter instead of GUIClient
        SceneBuilder.showCurrentScene(SceneBuilder.createSceneForInitialScreen("PokerTable",gamecontroller), "Main Screen");

    }

    public void displayGameScreen(GameSettings settings, GUIClient client) {
        SceneBuilder.showCurrentScene(GameScreen.createSceneForGameScreen(client),"GameScreen");
    }
}
