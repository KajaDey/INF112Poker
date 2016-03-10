package main.java.gui;

import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.LongBinding;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceBox;
import main.java.gamelogic.GameClient;
import main.java.gamelogic.GameController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ady on 07/03/16.
 */
public class ButtonListeners {

   static GameSettings gameSettings;

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

    public static void settingsButtonListener() {
        Stage settings = new Stage();
        settings.initModality(Modality.APPLICATION_MODAL);
        settings.setTitle("Settings");
        Scene scene = new Scene(GameLobby.createScreenForSettings(settings),260,200);
        settings.setScene(scene);
        settings.show();
    }

    public static void acceptSettingsButtonListener(String amountOfChips, String numberOfPlayersText, String bigBlindText,
                                             String smallBlindText, String levelDurationText, Stage window) {
        try {
            /*client.setStartChips(Long.valueOf(amountOfChips));
            client.setAmountOfPlayers(Integer.valueOf(numberOfPlayersText));
            client.setBigBlind(Integer.valueOf(bigBlindText));
            client.setSmallBlind(Integer.valueOf(smallBlindText));
            client.setLevelDuration(Integer.valueOf(levelDurationText));*/

            gameSettings = new GameSettings(Long.valueOf(amountOfChips),Integer.valueOf(numberOfPlayersText),
                    Integer.valueOf(bigBlindText),(Integer.valueOf(smallBlindText)),Integer.valueOf(levelDurationText));

            SceneBuilder.updateLobbyScreen();
            window.close();

        }catch (NumberFormatException e){

        }

    }

    public static void cancelSettingsButtonListener(Stage window) {
        window.close();
    }

    public static void startGameButtonListener() {

        GameController.StartTournamentButtonClicked(gameSettings);
    }

    public static void leaveLobbyButtonListener() {
        SceneBuilder.showCurrentScene(SceneBuilder.createSceneForInitialScreen("PokerTable"), "Main Screen");
    }

    /**
     * Listener for the button on the enter button on the main screen
     */
    public static void mainScreenEnterListener(String name, String numOfPlayers, String choiceBox,){
        try {
            if (!name.isEmpty() && Integer.valueOf(numOfPlayers) != null && choiceBox.equals("Single Player")) {
                GameController.EnterButtonClicked(name,Integer.parseInt(numOfPlayers),choiceBox);

            }
            else SceneBuilder.createSceneForInitialScreen("PokerTable");
        }catch (NumberFormatException e){

        }
    }

}
