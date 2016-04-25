package gui;

import gamelogic.AIType;
import gamelogic.Statistics;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import gamelogic.Decision;
import gamelogic.GameController;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by ady on 07/03/16.
 */
public class ButtonListeners {

    private static GameSettings gameSettings;
    private static GUIClient client;

    private static String savedName, savedNumOfPlayers, savedChoiceBox;
    private static GameController savedGameController;

    /**
     * What happens when the betButton is pushed
     */
    public static void betButtonListener(String betAmount, String buttonText){
        try {
            if (buttonText.equalsIgnoreCase("Raise to")) {
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
     * What happens when the foldButton is pushed
     */
    public static void foldButtonListener(){
        client.setDecision(Decision.Move.FOLD);
    }

    /**
     * What happens when the settingsButton is pushed
     */
    public static void settingsButtonListener(GameController gameController) {
        Stage settings = new Stage();
        settings.initModality(Modality.APPLICATION_MODAL);
        settings.setTitle("Settings");
        Scene scene = new Scene(GameLobby.createScreenForSettings(settings,gameController),270,250);
        settings.setScene(scene);
        settings.show();
    }
    /**
     * What happens when the acceptSettingsButton is pushed
     */
    public static void acceptSettingsButtonListener(String amountOfChips, String numberOfPlayersText, String bigBlindText,
                                             String smallBlindText, String levelDurationText, Stage window, GameController gameController,String aiChoice) {

        AIType aiType = AIType.fromString(aiChoice);
        try {
            gameSettings = new GameSettings(Long.valueOf(amountOfChips),Integer.valueOf(bigBlindText),
                    Integer.valueOf(smallBlindText),(Integer.valueOf(numberOfPlayersText)),Integer.valueOf(levelDurationText),aiType);

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
    public static void leaveLobbyButtonListener() throws IOException {
        File[] files = new File(System.getProperty("user.dir")).listFiles();
        ObjectStandards.startNewInstanceOfGame(files);
    }
    /**
     * Listener for the button on the enter button on the main screen
     */
    public static void mainScreenEnterListener(String name, String numOfPlayers, String choiceBox, GameController gameController){
        savedName = name;
        savedChoiceBox = choiceBox;
        savedNumOfPlayers = numOfPlayers;
        savedGameController = gameController;
        try {
            if (!name.isEmpty() && Integer.valueOf(numOfPlayers) != null) {
                AIType type = AIType.fromString(choiceBox);

                gameController.enterButtonClicked(name, Integer.parseInt(numOfPlayers), type);
                gameSettings = gameController.gameSettings;
            }
            else MainScreen.createSceneForMainScreen("PokerTable", gameController);
        }catch (NumberFormatException e){

        }
    }

    /**
     *
     * When you click the errorButton, you open a new settings window
     *
     * @param gameController
     */
    public static void errorButtonListener(GameController gameController) {
        settingsButtonListener(gameController);
    }

    /**
     * sets the client
     */
    public static void setClient(GUIClient c) {
        client = c;
    }

    /**
     * Closes the lobby screen and returns to the main screen
     */
    public static void returnToMainMenuButtonListener(){
        MainScreen.refreshSceneForMainScreen();
    }

    public static void saveToFile(Statistics stats) {
        stats.printStatisticsToFile();
    }


    public static void moreInfoButtonListener() {
        LobbyScreen.displayGameInfo();
    }

    public static void MakeNewLobbyButtonListener() {
        LobbyScreen.makeNewLobby();
    }

    public static void takeASeatButtonListener() {

    }
}
