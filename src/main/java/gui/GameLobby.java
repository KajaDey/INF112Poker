package main.java.gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
        BorderPane playerImage = new BorderPane();
        VBox gameRules = new VBox();
        HBox startLeave = new HBox();
        HBox layoutNoStartButton = new HBox();
        VBox fullLayout = new VBox();

        //Objects
        Button settings = ObjectStandards.makeStandardButton("Settings");
        Button startGame = ObjectStandards.makeStandardButton("Start game");
        startGame.setFont(new Font("Areal", 30));
        Button leaveLobby = ObjectStandards.makeStandardButton("Leave lobby");
        leaveLobby.setFont(new Font("Areal",30));

        Label amountOfChips = ObjectStandards.makeStandardLabel("Chips: ", "1000" + "$");
        Label numberOfPlayers = ObjectStandards.makeStandardLabel("Number of players: ", "5");
        Label bigBlind = ObjectStandards.makeStandardLabel("Big blind: ", "50" + "$");
        Label smallBlind = ObjectStandards.makeStandardLabel("Small blind: ", "25" + "$");
        Label levelDuration = ObjectStandards.makeStandardLabel("Level duration: ", "10" + "min");

        //Add image
        Image table = new Image(ImageViewer.returnURLPathForImages("PokerTable"));
        ImageView imageView = new ImageView();
        imageView.setImage(table);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(250);

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
        playerImage.setCenter(imageView);
        gameRules.getChildren().addAll(settings, amountOfChips, numberOfPlayers, bigBlind, smallBlind, levelDuration);
        gameRules.setAlignment(Pos.TOP_LEFT);
        layoutNoStartButton.getChildren().addAll(playerImage, gameRules);
        layoutNoStartButton.setAlignment(Pos.TOP_CENTER);

        startLeave.getChildren().addAll(startGame, leaveLobby);
        startLeave.setAlignment(Pos.TOP_CENTER);

        fullLayout.getChildren().addAll(layoutNoStartButton,startLeave);

        //set scene
            Scene scene = new Scene(fullLayout,900,400);

            SceneBuilder.showCurrentScene(scene);
    }

    public static HBox createScreenForSettings(Stage window){

        HBox fullBox = new HBox();
        VBox labelBox = new VBox();
        VBox textFieldBox = new VBox();

        Label amountOfChips = ObjectStandards.makeStandardLabel("Chips:", "");
        Label numberOfPlayers = ObjectStandards.makeStandardLabel("Number of players:", "");
        Label bigBlind = ObjectStandards.makeStandardLabel("Big blind:", "");
        Label smallBlind = ObjectStandards.makeStandardLabel("Small blind:", "");
        Label levelDuration = ObjectStandards.makeStandardLabel("Level duration:", "");

        TextField amountOfChipsTF = ObjectStandards.makeStandardTextField();
        TextField numberOfPlayersTF = ObjectStandards.makeStandardTextField();
        TextField bigBlindTF = ObjectStandards.makeStandardTextField();
        TextField smallBlindTF = ObjectStandards.makeStandardTextField();
        TextField levelDurationTF = ObjectStandards.makeStandardTextField();

        Button accept = ObjectStandards.makeStandardButton("Accept");
        Button cancel = ObjectStandards.makeStandardButton("Cancel");

        accept.setOnAction(e -> ButtonListeners.acceptSettingsButtonListener());
        cancel.setOnAction(e -> ButtonListeners.cancelSettingsButtonListener(window));

        labelBox.getChildren().addAll(amountOfChips, numberOfPlayers, bigBlind, smallBlind, levelDuration, accept);
        textFieldBox.getChildren().addAll(amountOfChipsTF, numberOfPlayersTF, bigBlindTF, smallBlindTF, levelDurationTF, cancel);

        labelBox.setAlignment(Pos.CENTER);
        textFieldBox.setAlignment(Pos.CENTER);

        fullBox.getChildren().addAll(labelBox, textFieldBox);

        return fullBox;
    }
}
