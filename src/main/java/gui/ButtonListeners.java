package main.java.gui;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceBox;
import main.java.gamelogic.Decision;
import main.java.gamelogic.GameClient;
import main.java.gamelogic.GameController;

import java.util.Map;

/**
 * Created by ady on 07/03/16.
 */
public class ButtonListeners {

    private static GameSettings gameSettings;
    private static GUIClient client;

    /**
     * What happens when the betButton is pushed
     */
    public static void betButtonListener(String betAmount, String buttonText){
        try {
            if (buttonText.equalsIgnoreCase("Raise")) {
                client.setDecision(Decision.Move.RAISE, Long.valueOf(betAmount));
            } else if (buttonText.equalsIgnoreCase("Bet")) {
                client.setDecision(Decision.Move.BET, Long.valueOf(betAmount));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * What happens when the checkButton is pushed
     */
    public static void checkButtonListener(String buttonText){
        if (buttonText.equals("Call")) {
            client.setDecision(Decision.Move.CALL);
        } else if (buttonText.equals("Check")) {
            client.setDecision(Decision.Move.CHECK);
        }
    }

    /**
     * What happens when the doubleButton is pushed
     */
    public static void doubleButtonListener(String betAmount){
        //Not yet implemented
    }

    /**
     * What happens when the foldButton is pushed
     */
    public static void foldButtonListener(){
        client.setDecision(Decision.Move.FOLD);
    }

    /**
     * What happens when the maxButton is pushed
     */
    public static void maxButtonListener(String betAmount){
    }

    /**
     * What happens when the potButton is pushed
     */
    public static void potButtonListener(String betAmount) {
    }

    /**
     * What happens when the settingsButton is pushed
     */
    public static void settingsButtonListener(GameController gameController) {
        Stage settings = new Stage();
        settings.initModality(Modality.APPLICATION_MODAL);
        settings.setTitle("Settings");
        Scene scene = new Scene(GameLobby.createScreenForSettings(settings,gameController),260,200);
        settings.setScene(scene);
        settings.show();
    }
    /**
     * What happens when the acceptSettingsButton is pushed
     */
    public static void acceptSettingsButtonListener(String amountOfChips, String numberOfPlayersText, String bigBlindText,
                                             String smallBlindText, String levelDurationText, Stage window, GameController gameController) {
        try {
            gameSettings = new GameSettings(Long.valueOf(amountOfChips),Integer.valueOf(bigBlindText),
                    Integer.valueOf(smallBlindText),(Integer.valueOf(numberOfPlayersText)),Integer.valueOf(levelDurationText));

            GameLobby.updateLabels(gameSettings);
            gameController.setGameSettings(gameSettings);
            window.close();

        }catch (NumberFormatException e){

        }

    }
    /**
     * What happens when the cancelSettingsButton is pushed
     */
    public static void cancelSettingsButtonListener(Stage window) {
        window.close();
    }
    /**
     * What happens when the startGameButton is pushed
     */
    public static void startGameButtonListener(GameController gameController) {
        gameController.startTournamentButtonClicked(gameSettings);
    }
    /**
     * What happens when the leaveLobbyButton is pushed
     */
    public static void leaveLobbyButtonListener(GameController gameController) {
        SceneBuilder.showCurrentScene(SceneBuilder.createSceneForInitialScreen("PokerTable", gameController), "Main Screen");
    }
    /**
     * Listener for the button on the enter button on the main screen
     */
    public static void mainScreenEnterListener(String name, String numOfPlayers, String choiceBox,GameController gameController){
        try {
            if (!name.isEmpty() && Integer.valueOf(numOfPlayers) != null && choiceBox.equals("Single Player")) {
                gameController.enterButtonClicked(name, Integer.parseInt(numOfPlayers), choiceBox);
                gameSettings = gameController.gameSettings;
            }
            else SceneBuilder.createSceneForInitialScreen("PokerTable",gameController);
        }catch (NumberFormatException e){

        }
    }

    /**
     * sets the client
     */
    public static void setClient(GUIClient c) {
        client = c;
    }

}
