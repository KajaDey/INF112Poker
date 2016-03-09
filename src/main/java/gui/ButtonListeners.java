package main.java.gui;

import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.LongBinding;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceBox;
import main.java.gamelogic.GameController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ady on 07/03/16.
 */
public class ButtonListeners {

    GameSettings gameSettings;

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

    public void acceptSettingsButtonListener(String amountOfChips, String numberOfPlayersText, String bigBlindText,
                                             String smallBlindText, String levelDurationText, GUIClient client, Stage window) {
        try {
            /*client.setStartChips(Long.valueOf(amountOfChips));
            client.setAmountOfPlayers(Integer.valueOf(numberOfPlayersText));
            client.setBigBlind(Integer.valueOf(bigBlindText));
            client.setSmallBlind(Integer.valueOf(smallBlindText));
            client.setLevelDuration(Integer.valueOf(levelDurationText));*/

            gameSettings = new GameSettings(Long.valueOf(amountOfChips),Integer.valueOf(numberOfPlayersText),
                    Integer.valueOf(bigBlindText),(Integer.valueOf(smallBlindText)),Integer.valueOf(levelDurationText));

            SceneBuilder.updateLobbyScreen(client);
            window.close();

        }catch (NumberFormatException e){

        }

    }

    public void cancelSettingsButtonListener(Stage window) {
        window.close();
    }

    public void startGameButtonListener(GUIClient client) {

        GameController.StartTournamentButtonClicked(gameSettings);
    }

    public void leaveLobbyButtonListener(GUIClient client) {
        SceneBuilder.showCurrentScene(SceneBuilder.createSceneForInitialScreen("PokerTable",client), "Main Screen");
    }

    /**
     * Listener for the button on the enter button on the main screen
     */
    public void mainScreenEnterListener(String name, String numOfPlayers, ChoiceBox<String> choiceBox, GUIClient client){
        try {
            if (!name.isEmpty() && Integer.valueOf(numOfPlayers) != null && choiceBox.getValue().equals("Single Player")) {
                Map<Integer, String> map = client.getName();
                map.put(client.getId(), name);
                client.setName(map);
                GameLobby.createScreenForGameLobby(client);
            }
            else SceneBuilder.createSceneForInitialScreen("PokerTable", client);
        }catch (NumberFormatException e){

        }
    }

}
