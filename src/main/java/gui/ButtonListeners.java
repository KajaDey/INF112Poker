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
    public static void betButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the checkButton is pushed
     */
    public static void checkButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the doubleButton is pushed
     */
    public static void doubleButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the foldButton is pushed
     */
    public static void foldButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the maxButton is pushed
     */
    public static void maxButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the potButton is pushed
     */
    public static void potButtonListener(){
        //TODO: Implement method
    }

    public static void settingsButtonListener() {
        Stage settings = new Stage();
        settings.initModality(Modality.APPLICATION_MODAL);
        settings.setTitle("Settings");
        Scene scene = new Scene(GameLobby.createScreenForSettings(settings),260,200);
        settings.setScene(scene);
        settings.show();
    }

    public static void acceptSettingsButtonListener() {
        //TODO: Implement method

    }

    public static void cancelSettingsButtonListener(Stage window) {
        //TODO: Implement method
        window.close();
    }

    public static void startGameButtonListener() {
        GameScreen.createSceneForGameScreen(LayoutGenerators.makeOpponentLayout("Clubs 1","Clubs 2"),LayoutGenerators.makePlayerLayout(),LayoutGenerators.makeBoardLayout());

    }

    public static void leaveLobbyButtonListener() {
        MainScreen.createSceneForMainScreen("kai-pus");
    }

    /**
     * Listener for the button on the enter button on the main screen
     */
    public static void mainScreenEnterListener(String name, String numOfPlayers, ChoiceBox<String> choiceBox){
        if (!name.isEmpty() && !numOfPlayers.isEmpty() && choiceBox.getValue().equals("Single Player"))
            GameLobby.createScreenForGameLobby();
        else MainScreen.createSceneForMainScreen("kai-pus");
    }

}
