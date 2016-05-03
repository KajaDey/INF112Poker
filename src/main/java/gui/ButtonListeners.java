package gui;

import gamelogic.*;
import gui.layouts.BoardLayout;
import gui.layouts.PlayerLayout;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Created by ady on 07/03/16.
 */
public class ButtonListeners {

    private static GameSettings gameSettings;
    private static GUIClient client;

    private static String savedName, savedNumOfPlayers, savedChoiceBox;
    private static GameController savedGameController;

    private static long lastSpaceTap = 0;

    /**
     * What happens when the betButton is pushed
     */
    public static void betButtonListener(TextField amountTextField, String buttonText) {
        String betAmount = amountTextField.getText();
        try {
            if (betAmount.equalsIgnoreCase("all in"))
                client.setDecision(Decision.Move.ALL_IN);
            else if (buttonText.equalsIgnoreCase("Raise to"))
                client.setDecision(Decision.Move.RAISE, Long.valueOf(betAmount));
            else if (buttonText.equalsIgnoreCase("Bet"))
                client.setDecision(Decision.Move.BET, Long.valueOf(betAmount));
        } catch (NumberFormatException nfe){
            amountTextField.clear();
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

            //GameLobby.updateLabels(gameSettings);
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
    public static void startGameButtonListener(GameController gameController, CheckBox showAllPlayerCards) {
        boolean showCards = false;
        gameController.startTournamentButtonClicked(gameSettings, showCards);
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
            if (!name.isEmpty()) {

                gameController.enterButtonClicked(name, choiceBox);
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

    /**
     * Acts upon button released events.
     *      Single space tap: Call (if possible)
     *      Double space tap: Check (if possible)
     *      Single enter tap: Bet/raise
     *      Arrow up/down tap: Increase/decrease the amount field by 1 BB-amount
     *      Back space tap: Fold
     *
     * @param ke
     * @param playerLayout
     */
    public static void keyReleased(KeyEvent ke, PlayerLayout playerLayout, BoardLayout boardLayout) {
        TextField tf = playerLayout.getAmountTextField();
        long currentBB = boardLayout.getBB(), stackSize = playerLayout.getStackSize();

        if (tf.focusedProperty().getValue())
            return;

        try {
            switch(ke.getCode()) {
                case SPACE:
                    if (System.currentTimeMillis() - lastSpaceTap <= 1000 || playerLayout.getCheckCallButtonText().equalsIgnoreCase("Call"))
                        checkButtonListener(playerLayout.getCheckCallButtonText());

                    lastSpaceTap = System.currentTimeMillis();
                    break;

                case ENTER:
                    betButtonListener(tf, playerLayout.getBetRaiseButtonText());
                    break;

                case UP:case DOWN:
                    if (ke.isShiftDown()) currentBB *= 10;
                    long currentAmount = Long.parseLong(tf.getText());
                    currentAmount = (ke.getCode() == KeyCode.UP) ? currentAmount+currentBB : currentAmount-currentBB;
                    currentAmount = Math.max(currentAmount, boardLayout.getBB());
                    currentAmount = Math.min(currentAmount, stackSize);

                    playerLayout.setAmountTextField(currentAmount+"");
                    break;

                case BACK_SPACE:
                    foldButtonListener();
                    break;
            }
        } catch (NumberFormatException nfe) {
            tf.setText("" + currentBB);
        }
    }
}
