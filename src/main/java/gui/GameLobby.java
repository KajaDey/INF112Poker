package main.java.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.gamelogic.GameController;

/**
 * Created by ady on 07/03/16.
 */
public class GameLobby {

    private static Label amountOfChips;
    private static Label numberOfPlayers;
    private static Label bigBlind;
    private static Label smallBlind;
    private static Label levelDuration;
    private static Label headLine;

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
        Button settings = ObjectStandards.makeStandardButton("Settings");
        settings.setMinWidth(120);
        settings.setMinHeight(50);
        Button startGame = ObjectStandards.makeStandardButton("Start game");
        startGame.setMinWidth(120);
        startGame.setMinHeight(50);
        Button leaveLobby = ObjectStandards.makeStandardButton("Leave lobby");
        leaveLobby.setMinWidth(120);
        leaveLobby.setMinHeight(50);

        amountOfChips = ObjectStandards.makeStandardLabelWhite("Chips: ", gameSettings.getStartStack() + "$");
        numberOfPlayers = ObjectStandards.makeStandardLabelWhite("Number of players: ", gameSettings.getMaxNumberOfPlayers()+"");
        bigBlind = ObjectStandards.makeStandardLabelWhite("Big blind: ", gameSettings.getBigBlind() + "$");
        smallBlind = ObjectStandards.makeStandardLabelWhite("Small blind: ", gameSettings.getSmallBlind() + "$");
        levelDuration = ObjectStandards.makeStandardLabelWhite("Level duration: ", gameSettings.getLevelDuration() + "min");
        headLine = new Label("Game Lobby");
        headLine.setFont(new Font("Areal", 25));
        headLine.setPadding(new Insets(20, 20, 20, 20));

        //ActionListeners
        settings.setOnAction(e -> ButtonListeners.settingsButtonListener(gameController));
        startGame.setOnAction(e -> {
            window.close();
            ButtonListeners.startGameButtonListener(gameController);
        });

        leaveLobby.setOnAction(e -> {
            window.close();
            ButtonListeners.leaveLobbyButtonListener(gameController);
        });

        //Put objects in boxes
        gameRules.getChildren().addAll(amountOfChips, numberOfPlayers, bigBlind, smallBlind, levelDuration);
        gameRules.setAlignment(Pos.CENTER_LEFT);

        //players.getChildren().addAll(joinedPlayers);
        players.setAlignment(Pos.CENTER_LEFT);

        buttons.getChildren().addAll(startGame, leaveLobby, settings);
        buttons.setAlignment(Pos.CENTER_LEFT);

        layoutNoHeadline.getChildren().addAll(players, gameRules, buttons);
        layoutNoHeadline.setAlignment(Pos.CENTER);


        fullLayout.getChildren().addAll(headLine, layoutNoHeadline);
        fullLayout.setAlignment(Pos.CENTER);


        SceneBuilder.showCurrentScene(fullLayout,"Lobby Screen");

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

        Label amountOfChips = ObjectStandards.makeStandardLabelBlack("Chips:", "");
        Label numberOfPlayers = ObjectStandards.makeStandardLabelBlack("Number of players:", "");
        Label bigBlind = ObjectStandards.makeStandardLabelBlack("Big blind:", "");
        Label smallBlind = ObjectStandards.makeStandardLabelBlack("Small blind:", "");
        Label levelDuration = ObjectStandards.makeStandardLabelBlack("Level duration:", "");

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

        Button accept = ObjectStandards.makeStandardButton("Accept");
        Button cancel = ObjectStandards.makeStandardButton("Cancel");

        accept.setOnAction(e -> ButtonListeners.acceptSettingsButtonListener(amountOfChipsTF.getText(), numberOfPlayersTF.getText(),
                bigBlindTF.getText(), smallBlindTF.getText(), levelDurationTF.getText(), window, gameController));
        cancel.setOnAction(e -> ButtonListeners.cancelSettingsButtonListener(window));

        levelDurationTF.setOnAction(e -> ButtonListeners.acceptSettingsButtonListener(amountOfChipsTF.getText(), numberOfPlayersTF.getText(),
                bigBlindTF.getText(), smallBlindTF.getText(), levelDurationTF.getText(), window, gameController));

        labelBox.getChildren().addAll(amountOfChips, numberOfPlayers, bigBlind, smallBlind, levelDuration, accept);
        textFieldBox.getChildren().addAll(amountOfChipsTF, numberOfPlayersTF, bigBlindTF, smallBlindTF, levelDurationTF, cancel);

        labelBox.setAlignment(Pos.CENTER);
        textFieldBox.setAlignment(Pos.CENTER);

        fullBox.getChildren().addAll(labelBox, textFieldBox);

        return fullBox;
    }

    public static void updateLabels(GameSettings gameSettings){
        amountOfChips.setText("Chips:  " + gameSettings.getStartStack() + "$");
        numberOfPlayers.setText("Number of players:  " + gameSettings.getMaxNumberOfPlayers() + "");
        bigBlind.setText("Big blind:  " + gameSettings.getBigBlind() + "$");
        smallBlind.setText("Small blind:  " + gameSettings.getSmallBlind() + "$");
        levelDuration.setText("Level duration:  " + gameSettings.getLevelDuration() + "min");
    }

    public static void displayErrorMessage(String message,GameController gameController){

        System.err.println("Illegal settings. Please insert valid numbers.");

        Stage errorMessage = new Stage();

        VBox layout = new VBox();
        layout.setPadding(new Insets(10, 10, 10, 10));

        Label label = new Label(message);
        label.setFont(new Font("Areal", 50));

        Button backToSettings = new Button("Alright. Take me back to the settings menu");
        backToSettings.setFont((new Font("Areal",14)));
        backToSettings.setOnAction(e -> {
            ButtonListeners.errorButtonListener(gameController);
            errorMessage.close();
        });

        layout.getChildren().addAll(label, backToSettings);
        layout.setAlignment(Pos.CENTER);

        errorMessage.initModality(Modality.APPLICATION_MODAL);
        errorMessage.setTitle("Settings");
        Scene scene = new Scene(layout);
        errorMessage.setScene(scene);
        errorMessage.show();

    }
}
