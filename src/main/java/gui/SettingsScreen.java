package gui;

import gamelogic.AIType;
import gamelogic.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import network.ServerLobbyCommunicator;

/**
 * Created by ady on 09/05/16.
 */
public class SettingsScreen {

    /**
     *
     * Creates a layout for the settingsScreen and displays it
     *
     * @param window
     * @param gameController
     * @return A settingScreen
     */

    public static HBox createScreenForSettings(Stage window, GameController gameController){

        HBox fullBox = new HBox();
        VBox labelBox = new VBox();
        VBox textFieldBox = new VBox();

        Label amountOfChips = ObjectStandards.makeLabelForSettingsScreen("Chips:");
        Label numberOfPlayers = ObjectStandards.makeLabelForSettingsScreen("Number of players:");
        Label bigBlind = ObjectStandards.makeLabelForSettingsScreen("Big blind:");
        Label smallBlind = ObjectStandards.makeLabelForSettingsScreen("Small blind:");
        Label levelDuration = ObjectStandards.makeLabelForSettingsScreen("Level duration:");
        Label aIDifficulty = ObjectStandards.makeLabelForSettingsScreen("AI difficulty:");
        Label playerClock = ObjectStandards.makeLabelForSettingsScreen("Player clock:");
        ChoiceBox<String> choiceBox = new ChoiceBox<>();

        TextField amountOfChipsTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField numberOfPlayersTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField bigBlindTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField smallBlindTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField levelDurationTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField playerClockTF = ObjectStandards.makeTextFieldForSettingsScreen();

        GameSettings gameSettings = gameController.getGameSettings();
        amountOfChipsTF.setText(String.valueOf(gameSettings.getStartStack()));
        numberOfPlayersTF.setText(String.valueOf(gameSettings.getMaxNumberOfPlayers()));
        bigBlindTF.setText(String.valueOf(gameSettings.getBigBlind()));
        smallBlindTF.setText(String.valueOf(gameSettings.getSmallBlind()));
        levelDurationTF.setText(String.valueOf(gameSettings.getLevelDuration()));
        playerClockTF.setText(String.valueOf(gameSettings.getPlayerClock()));


        choiceBox.setValue(gameSettings.getAiType().toString());
        choiceBox.setMinWidth(100);
        choiceBox.setMaxWidth(100);
        choiceBox.setMinHeight(30);
        choiceBox.setMaxWidth(30);
        choiceBox.getItems().addAll(AIType.SIMPLE_AI.toString(), AIType.MCTS_AI.toString(), AIType.MIXED.toString());
        choiceBox.setValue(gameSettings.getAiType().toString());
        choiceBox.setTooltip(new Tooltip("Pick a difficulty"));
        choiceBox.setPadding(new Insets(5,5,8,5));


        Button accept = ObjectStandards.makeStandardButton("Accept");
        Button cancel = ObjectStandards.makeStandardButton("Cancel");


        accept.setOnAction(e -> ButtonListeners.acceptSettingsButtonListener(amountOfChipsTF.getText(), numberOfPlayersTF.getText(),
                bigBlindTF.getText(), smallBlindTF.getText(), levelDurationTF.getText(), window, gameController,choiceBox.getValue(), playerClockTF.getText()));
        cancel.setOnAction(e -> ButtonListeners.cancelSettingsButtonListener(window));

        labelBox.getChildren().addAll(amountOfChips, numberOfPlayers, bigBlind, smallBlind, levelDuration ,playerClock ,aIDifficulty ,accept);
        textFieldBox.getChildren().addAll(amountOfChipsTF, numberOfPlayersTF, bigBlindTF, smallBlindTF, levelDurationTF ,playerClockTF ,choiceBox,cancel);

        labelBox.setAlignment(Pos.CENTER);
        textFieldBox.setAlignment(Pos.CENTER);

        fullBox.getChildren().addAll(labelBox, textFieldBox);

        return fullBox;
    }

    /**
     *
     * Creates a layout for the settingsScreen and displays it
     *
     * @param window
     * @param serverLobbyCommunicator
     * @return A settingScreen
     */

    public static HBox createScreenForSettings(Stage window, ServerLobbyCommunicator serverLobbyCommunicator, LobbyTable table){

        HBox fullBox = new HBox();
        VBox labelBox = new VBox();
        VBox textFieldBox = new VBox();

        Label amountOfChips = ObjectStandards.makeLabelForSettingsScreen("Chips:");
        Label numberOfPlayers = ObjectStandards.makeLabelForSettingsScreen("Number of players:");
        Label bigBlind = ObjectStandards.makeLabelForSettingsScreen("Big blind:");
        Label smallBlind = ObjectStandards.makeLabelForSettingsScreen("Small blind:");
        Label levelDuration = ObjectStandards.makeLabelForSettingsScreen("Level duration:");
        Label aIDifficulty = ObjectStandards.makeLabelForSettingsScreen("AI difficulty:");
        Label playerClock = ObjectStandards.makeLabelForSettingsScreen("Player clock:");
        ChoiceBox<String> choiceBox = new ChoiceBox<>();

        TextField amountOfChipsTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField numberOfPlayersTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField bigBlindTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField smallBlindTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField levelDurationTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField playerClockTF = ObjectStandards.makeTextFieldForSettingsScreen();

        GameSettings gameSettings = table.settings;
        amountOfChipsTF.setText(String.valueOf(gameSettings.getStartStack()));
        numberOfPlayersTF.setText(String.valueOf(gameSettings.getMaxNumberOfPlayers()));
        bigBlindTF.setText(String.valueOf(gameSettings.getBigBlind()));
        smallBlindTF.setText(String.valueOf(gameSettings.getSmallBlind()));
        levelDurationTF.setText(String.valueOf(gameSettings.getLevelDuration()));
        playerClockTF.setText(String.valueOf(gameSettings.getPlayerClock()));


        choiceBox.setValue(gameSettings.getAiType().toString());
        choiceBox.setMinWidth(100);
        choiceBox.setMaxWidth(100);
        choiceBox.setMinHeight(30);
        choiceBox.setMaxWidth(30);
        choiceBox.getItems().addAll(AIType.SIMPLE_AI.toString(), AIType.MCTS_AI.toString(), AIType.MIXED.toString());
        choiceBox.setValue(gameSettings.getAiType().toString());
        choiceBox.setTooltip(new Tooltip("Pick a difficulty"));
        choiceBox.setPadding(new Insets(5,5,8,5));


        Button accept = ObjectStandards.makeStandardButton("Accept");
        Button cancel = ObjectStandards.makeStandardButton("Cancel");


        accept.setOnAction(e -> ButtonListeners.acceptSettingsButtonListener(amountOfChipsTF.getText(), numberOfPlayersTF.getText(),
                bigBlindTF.getText(), smallBlindTF.getText(), levelDurationTF.getText(), window, serverLobbyCommunicator, table, choiceBox.getValue(), playerClockTF.getText()));
        cancel.setOnAction(e -> ButtonListeners.cancelSettingsButtonListener(window));

        labelBox.getChildren().addAll(amountOfChips, numberOfPlayers, bigBlind, smallBlind, levelDuration ,playerClock ,aIDifficulty ,accept);
        textFieldBox.getChildren().addAll(amountOfChipsTF, numberOfPlayersTF, bigBlindTF, smallBlindTF, levelDurationTF ,playerClockTF ,choiceBox,cancel);

        labelBox.setAlignment(Pos.CENTER);
        textFieldBox.setAlignment(Pos.CENTER);

        fullBox.getChildren().addAll(labelBox, textFieldBox);

        return fullBox;
    }

}
