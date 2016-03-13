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
    private static Label joinedPlayers;

    /**
     * Creates the screen for the gamelobby and shows it on the screen
     *
     * @param gameSettings
     * @param gameController
     */
    public static void createScreenForGameLobby(GameSettings gameSettings,GameController gameController, String name){

        Stage window = new Stage();

        //Boxes
        VBox gameRules = new VBox();
        HBox startLeave = new HBox();
        HBox layoutNoStartButton = new HBox();
        VBox fullLayout = new VBox();

        //Objects
        Button settings = ObjectStandards.makeStandardButton("Settings");
        Button startGame = ObjectStandards.makeStandardButton("Start game");
        startGame.setFont(new Font("Areal", 30));
        startLeave.setSpacing(30);
        Button leaveLobby = ObjectStandards.makeStandardButton("Leave lobby");
        leaveLobby.setFont(new Font("Areal",30));

        amountOfChips = ObjectStandards.makeStandardLabelWhite("Chips: ", gameSettings.getStartStack() + "$");
        numberOfPlayers = ObjectStandards.makeStandardLabelWhite("Number of players: ", gameSettings.getMaxNumberOfPlayers()+"");
        bigBlind = ObjectStandards.makeStandardLabelWhite("Big blind: ", gameSettings.getBigBlind() + "$");
        smallBlind = ObjectStandards.makeStandardLabelWhite("Small blind: ", gameSettings.getSmallBlind() + "$");
        levelDuration = ObjectStandards.makeStandardLabelWhite("Level duration: ", gameSettings.getLevelDuration() + "min");
        joinedPlayers = ObjectStandards.makeStandardLabelWhite("Players:\n -" + name, "");


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
        gameRules.getChildren().addAll(settings, amountOfChips, numberOfPlayers, bigBlind, smallBlind, levelDuration);
        gameRules.setAlignment(Pos.CENTER_LEFT);
        layoutNoStartButton.getChildren().addAll(joinedPlayers, gameRules);
        layoutNoStartButton.setAlignment(Pos.CENTER);

        startLeave.getChildren().addAll(startGame, leaveLobby);
        startLeave.setAlignment(Pos.CENTER);

        fullLayout.getChildren().addAll(layoutNoStartButton,startLeave);
        fullLayout.setAlignment(Pos.CENTER);

        //set scene

        BorderPane gameScreenLayout = new BorderPane();
        gameScreenLayout.setPadding(new Insets(10,10,10,10));
        gameScreenLayout.setCenter(fullLayout);

        Scene scene = new Scene(ImageViewer.setBackground("PokerTable", gameScreenLayout, 1920, 1080), 1280, 720);
        SceneBuilder.showCurrentScene(scene, "GameLobby");

    }

    /**
     *
     * Creates a layout for the settingsScreen and displays it
     *
     * @param window
     * @param gameController
     * @return A settingScreen
     */

    public static HBox createScreenForSettings(Stage window,GameController gameController){

        HBox fullBox = new HBox();
        VBox labelBox = new VBox();
        VBox textFieldBox = new VBox();

        Label amountOfChips = ObjectStandards.makeStandardLabelBlack("Chips:", "");
        Label numberOfPlayers = ObjectStandards.makeStandardLabelBlack("Number of players:", "");
        Label bigBlind = ObjectStandards.makeStandardLabelBlack("Big blind:", "");
        Label smallBlind = ObjectStandards.makeStandardLabelBlack("Small blind:", "");
        Label levelDuration = ObjectStandards.makeStandardLabelBlack("Level duration:", "");

        TextField amountOfChipsTF = ObjectStandards.makeStandardTextField();
        TextField numberOfPlayersTF = ObjectStandards.makeStandardTextField();
        TextField bigBlindTF = ObjectStandards.makeStandardTextField();
        TextField smallBlindTF = ObjectStandards.makeStandardTextField();
        TextField levelDurationTF = ObjectStandards.makeStandardTextField();

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
        amountOfChips.setText("Chips: "+ gameSettings.getStartStack() + "$");
        numberOfPlayers.setText("Number of players: "+ gameSettings.getMaxNumberOfPlayers()+"");
        bigBlind.setText("Big blind: "+ gameSettings.getBigBlind() + "$");
        smallBlind.setText("Small blind: "+ gameSettings.getSmallBlind() + "$");
        levelDuration.setText("Level duration: "+ gameSettings.getLevelDuration() + "min");
        //joinedPlayers = ObjectStandards.makeStandardLabelWhite("Players:\n -" + , "");
    }

}
