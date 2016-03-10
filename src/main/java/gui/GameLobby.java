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

/**
 * Created by ady on 07/03/16.
 */
public class GameLobby {

    public static void createScreenForGameLobby(){

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

        Label amountOfChips = ObjectStandards.makeStandardLabelWhite("Chips: ", client.getStartChips() + "$");
        Label numberOfPlayers = ObjectStandards.makeStandardLabelWhite("Number of players: ", client.getAmountOfPlayers()+"");
        Label bigBlind = ObjectStandards.makeStandardLabelWhite("Big blind: ", client.getBigBlind() + "$");
        Label smallBlind = ObjectStandards.makeStandardLabelWhite("Small blind: ", client.getSmallBlind() + "$");
        Label levelDuration = ObjectStandards.makeStandardLabelWhite("Level duration: ", client.getLevelDuration() + "min");
        Label joinedPlayers = ObjectStandards.makeStandardLabelWhite("Players:\n - Jostein\n - André", "");


        //ActionListeners
        settings.setOnAction(e -> ButtonListeners.settingsButtonListener());
        startGame.setOnAction(e -> {
            window.close();
            ButtonListeners.startGameButtonListener();
        });

        leaveLobby.setOnAction(e -> {
            window.close();
            ButtonListeners.leaveLobbyButtonListener();
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

    public static HBox createScreenForSettings(Stage window,GUIClient client){

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



        Button accept = ObjectStandards.makeStandardButton("Accept");
        Button cancel = ObjectStandards.makeStandardButton("Cancel");

        accept.setOnAction(e -> ButtonListeners.acceptSettingsButtonListener(amountOfChipsTF.getText(), numberOfPlayersTF.getText(),
                bigBlindTF.getText(), smallBlindTF.getText(), levelDurationTF.getText(), window));
        cancel.setOnAction(e -> ButtonListeners.cancelSettingsButtonListener(window));

        levelDurationTF.setOnAction(e -> client.acceptSettingsButtonListener(amountOfChipsTF.getText(), numberOfPlayersTF.getText(),
                bigBlindTF.getText(), smallBlindTF.getText(), levelDurationTF.getText(), window));

        labelBox.getChildren().addAll(amountOfChips, numberOfPlayers, bigBlind, smallBlind, levelDuration, accept);
        textFieldBox.getChildren().addAll(amountOfChipsTF, numberOfPlayersTF, bigBlindTF, smallBlindTF, levelDurationTF, cancel);

        labelBox.setAlignment(Pos.CENTER);
        textFieldBox.setAlignment(Pos.CENTER);

        fullBox.getChildren().addAll(labelBox, textFieldBox);

        return fullBox;
    }

}
