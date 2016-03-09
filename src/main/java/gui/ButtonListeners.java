package main.java.gui;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceBox;

/**
 * Created by ady on 07/03/16.
 */
public class ButtonListeners {

    /**
     * What happens when the betButton is pushed
     */
    public void betButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the checkButton is pushed
     */
    public void checkButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the doubleButton is pushed
     */
    public void doubleButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the foldButton is pushed
     */
    public void foldButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the maxButton is pushed
     */
    public void maxButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the potButton is pushed
     */
    public void potButtonListener(){
        //TODO: Implement method
    }

    public void settingsButtonListener(GUIClient client) {
        Stage settings = new Stage();
        settings.initModality(Modality.APPLICATION_MODAL);
        settings.setTitle("Settings");
        Scene scene = new Scene(GameLobby.createScreenForSettings(settings,client),260,200);
        settings.setScene(scene);
        settings.show();
    }

    public void acceptSettingsButtonListener() {
        //TODO: Implement method

    }

    public void cancelSettingsButtonListener(Stage window) {
        window.close();
    }

    public void startGameButtonListener(GUIClient client) {
        GameScreen.createSceneForGameScreen(GameScreen.makeOpponentLayout(client), GameScreen.makePlayerLayout(client), GameScreen.makeBoardLayout(client),client);

    }

    public void leaveLobbyButtonListener(GUIClient client) {
        SceneBuilder.showCurrentScene(SceneBuilder.createSceneForInitialScreen("PokerTable",client), "Main Screen");
    }

    /**
     * Listener for the button on the enter button on the main screen
     */
    public void mainScreenEnterListener(String name, String numOfPlayers, ChoiceBox<String> choiceBox, GUIClient client){
        if (!name.isEmpty() && !numOfPlayers.isEmpty() && choiceBox.getValue().equals("Single Player"))
            GameLobby.createScreenForGameLobby(client);
        else SceneBuilder.createSceneForInitialScreen("PokerTable",client);
    }

}
