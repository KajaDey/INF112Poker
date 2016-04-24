package gui;

import gamelogic.AIType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import gamelogic.GameController;

import java.io.IOException;

/**
 * TODO: Add class description
 *
 * @author André Dyrstad
 */
public class GameLobby {

    private static Label amountOfChips, numberOfPlayers, bigBlind, smallBlind, levelDuration, headLine;
    private static ChoiceBox<String> choiceBox;

    /**
     * Creates the screen for the gameLobby and shows it on the screen
     *
     * @param gameSettings
     * @param gameController
     */
    public static void createScreenForGameLobby(GameSettings gameSettings,GameController gameController, String name){

        Stage window = new Stage();

        //Boxes
        VBox gameRules = new VBox();
        VBox players = new VBox();
        VBox buttons = new VBox();
        HBox layoutNoHeadline = new HBox();
        VBox fullLayout = new VBox();

        //Objects
        Button settings = ObjectStandards.makeButtonForLobbyScreen("Settings");
        Button startGame = ObjectStandards.makeButtonForLobbyScreen("Start game");
        Button leaveLobby = ObjectStandards.makeButtonForLobbyScreen("Leave lobby");
        CheckBox showAllPlayerCards = new CheckBox("Show all players cards");

        amountOfChips = ObjectStandards.makeLobbyLabelWhite("Chips: ", gameSettings.getStartStack() + "$");
        numberOfPlayers = ObjectStandards.makeLobbyLabelWhite("Number of players: ", gameSettings.getMaxNumberOfPlayers()+"");
        bigBlind = ObjectStandards.makeLobbyLabelWhite("Big blind: ", gameSettings.getBigBlind() + "$");
        smallBlind = ObjectStandards.makeLobbyLabelWhite("Small blind: ", gameSettings.getSmallBlind() + "$");
        levelDuration = ObjectStandards.makeLobbyLabelWhite("Level duration: ", gameSettings.getLevelDuration() + "min");
        headLine = ObjectStandards.makeLabelForHeadLine("Game Lobby");

        //ActionListeners
        settings.setOnAction(e -> ButtonListeners.settingsButtonListener(gameController));
        startGame.setOnAction(e -> {
            window.close();
            ButtonListeners.startGameButtonListener(gameController, showAllPlayerCards);
        });

        leaveLobby.setOnAction(e -> {
            window.close();
            try {
                ButtonListeners.leaveLobbyButtonListener();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        //Put objects in boxes

        gameRules.getChildren().addAll(amountOfChips, numberOfPlayers, bigBlind, smallBlind, levelDuration);
        gameRules.setAlignment(Pos.CENTER_LEFT);

        buttons.getChildren().addAll(startGame, leaveLobby, settings, showAllPlayerCards);
        buttons.setAlignment(Pos.CENTER_LEFT);

        layoutNoHeadline.getChildren().addAll(players, gameRules, buttons);
        layoutNoHeadline.setAlignment(Pos.CENTER);

        fullLayout.getChildren().addAll(headLine, layoutNoHeadline);
        fullLayout.setAlignment(Pos.CENTER);

        BorderPane pane = new BorderPane();
        MenuBar menuBar = ObjectStandards.createMenuBar();
        menuBar.setLayoutY(0);
        menuBar.setLayoutX(0);
        pane.setCenter(fullLayout);
        pane.setTop(menuBar);
        SceneBuilder.showCurrentScene(pane, "Lobby Screen");

    }

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

        TextField amountOfChipsTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField numberOfPlayersTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField bigBlindTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField smallBlindTF = ObjectStandards.makeTextFieldForSettingsScreen();
        TextField levelDurationTF = ObjectStandards.makeTextFieldForSettingsScreen();

        amountOfChipsTF.setText(String.valueOf(gameController.gameSettings.getStartStack()));
        numberOfPlayersTF.setText(String.valueOf(gameController.gameSettings.getMaxNumberOfPlayers()));
        bigBlindTF.setText(String.valueOf(gameController.gameSettings.getBigBlind()));
        smallBlindTF.setText(String.valueOf(gameController.gameSettings.getSmallBlind()));
        levelDurationTF.setText(String.valueOf(gameController.gameSettings.getLevelDuration()));

        choiceBox = new ChoiceBox<>();
        choiceBox.setValue(gameController.gameSettings.getAiType().toString());
        choiceBox.setMinWidth(100);
        choiceBox.setMaxWidth(100);
        choiceBox.setMinHeight(30);
        choiceBox.setMaxWidth(30);
        choiceBox.getItems().addAll(AIType.SIMPLE_AI.toString(), AIType.MCTS_AI.toString(), AIType.MIXED.toString());
        choiceBox.setValue(gameController.gameSettings.getAiType().toString());
        choiceBox.setTooltip(new Tooltip("Pick a difficulty"));
        choiceBox.setPadding(new Insets(5,5,8,5));


        Button accept = ObjectStandards.makeStandardButton("Accept");
        Button cancel = ObjectStandards.makeStandardButton("Cancel");


        accept.setOnAction(e -> ButtonListeners.acceptSettingsButtonListener(amountOfChipsTF.getText(), numberOfPlayersTF.getText(),
                bigBlindTF.getText(), smallBlindTF.getText(), levelDurationTF.getText(), window, gameController,choiceBox.getValue()));
        cancel.setOnAction(e -> ButtonListeners.cancelSettingsButtonListener(window));

        levelDurationTF.setOnAction(e -> ButtonListeners.acceptSettingsButtonListener(amountOfChipsTF.getText(), numberOfPlayersTF.getText(),
                bigBlindTF.getText(), smallBlindTF.getText(), levelDurationTF.getText(), window, gameController,choiceBox.getValue()));

        labelBox.getChildren().addAll(amountOfChips, numberOfPlayers, bigBlind, smallBlind, levelDuration,aIDifficulty ,accept);
        textFieldBox.getChildren().addAll(amountOfChipsTF, numberOfPlayersTF, bigBlindTF, smallBlindTF, levelDurationTF,choiceBox,cancel);

        labelBox.setAlignment(Pos.CENTER);
        textFieldBox.setAlignment(Pos.CENTER);

        fullBox.getChildren().addAll(labelBox, textFieldBox);

        return fullBox;
    }

    /**
     *
     * Uppdates all the setting in game lobby
     *
     * @param gameSettings
     */

    public static void updateLabels(GameSettings gameSettings){

        amountOfChips.setText("Chips:  " + gameSettings.getStartStack() + "$");
        numberOfPlayers.setText("Number of players:  " + gameSettings.getMaxNumberOfPlayers() + "");
        bigBlind.setText("Big blind:  " + gameSettings.getBigBlind() + "$");
        smallBlind.setText("Small blind:  " + gameSettings.getSmallBlind() + "$");
        levelDuration.setText("Level duration:  " + gameSettings.getLevelDuration() + "min");
    }

    /**
     *
     * Displays an error message if the settings are wrong
     *
     * @param message
     * @param gameController
     */

    public static void displayErrorMessage(String message,GameController gameController){

        System.err.println("Illegal settings. Please insert valid numbers.");

        Stage errorMessage = new Stage();

        VBox layout = new VBox();
        layout.setPadding(new Insets(10, 10, 10, 10));

        Label label = new Label(message);
        label.setFont(new Font("Areal", 25));

        Button backToSettings = ObjectStandards.makeStandardButton("Alright. Take me back to the settings menu");
        backToSettings.setOnAction(e -> {
            ButtonListeners.errorButtonListener(gameController);
            errorMessage.close();
        });

        layout.getChildren().addAll(label, backToSettings);
        layout.setAlignment(Pos.CENTER);

        layout.setStyle("-fx-background-color:#42b43d, " +
                "linear-gradient(#309e2a 0%, #2bbd24 20%, #42b43d 100%), " +
                "linear-gradient(#218a0f, #42b43d), " +
                "radial-gradient(center 50% 0%, radius 100%, rgba(63,191,63,0.9), rgba(51,151,51,1)); " +
                "-fx-text-fill: linear-gradient(white, #d0d0d0) ; ");

        errorMessage.initModality(Modality.APPLICATION_MODAL);
        errorMessage.setTitle("Settings");
        Scene scene = new Scene(layout);
        errorMessage.setScene(scene);
        errorMessage.show();

    }
}
