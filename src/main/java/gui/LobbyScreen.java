package gui;

import gamelogic.GameController;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.ArrayList;

/**
 * Created by ady on 18/04/16.
 */
public class LobbyScreen {

    static String styling = "-fx-border-color: black; -fx-background-color: #362626";

    static ArrayList<Integer> emptyPositions = new ArrayList<>();

    static GameController gameController;
    static GameSettings gameSettings;

    static VBox settings;
    static VBox sideMenu = new VBox();
    static Pane fullLayout = new Pane();

    public static void createScreenForGameLobby(GameSettings gs, GameController gc, String names){

        gameController = gc;
        gameSettings = gs;

        Pane pane = new Pane();
        Button newLobby = ObjectStandards.makeButtonForLobbyScreen("Make lobby");
        newLobby.setOnAction(event -> ButtonListeners.MakeNewLobbyButtonListener());

        sideMenu.getChildren().addAll(addGames("Ady's lobby", "2/4"), addGames("Jos's lobby", "3/6"),newLobby);
        sideMenu.setLayoutX(1000);
        sideMenu.setLayoutY(100);
        sideMenu.setMinHeight(500);
        sideMenu.setMinWidth(150);
        sideMenu.setAlignment(Pos.TOP_CENTER);
        sideMenu.setStyle("-fx-background-color: #241414");

        fullLayout.setStyle("-fx-background-color: #602121");
        fullLayout.getChildren().addAll(sideMenu, pane);

        SceneBuilder.showCurrentScene(fullLayout, "Lobby Screen");

    }

    public static VBox addGames(String name, String players){

        VBox vBox = new VBox();
        HBox hBox = new HBox();

        Label names = ObjectStandards.makeStandardLabelWhite(name,"");
        Label player = ObjectStandards.makeStandardLabelWhite(players,"");
        Button moreInfo = ObjectStandards.makeStandardButton("Info");

        vBox.setStyle(styling);
        hBox.getChildren().addAll(names, player);

        hBox.setMinWidth(150);
        vBox.setMinHeight(75);

        vBox.getChildren().addAll(hBox, moreInfo);

        moreInfo.setOnAction(event -> ButtonListeners.moreInfoButtonListener());

        vBox.setStyle(styling);

        return vBox;
    }

    public static void displayGameInfo() {

        for(int i=0;i<6;i++)
            emptyPositions.add(i);

        Pane gameInfo = new Pane();
        gameInfo.setLayoutX(150);
        gameInfo.setLayoutY(110);
        gameInfo.setMinHeight(500);
        gameInfo.setMinWidth(850);

        Label gameName = ObjectStandards.makeLabelForHeadLine("Andy's game!");
        gameName.setLayoutX(325);
        gameName.setLayoutY(0);

        ImageView imageView = new ImageView(new Image(ImageViewer.returnURLPathForImages("tablev2")));

        imageView.setLayoutX(25);
        imageView.setLayoutY(100);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(550);

        Button takeASeat = ObjectStandards.makeButtonForLobbyScreen("Take a seat");

        takeASeat.setLayoutX(200);
        takeASeat.setLayoutY(425);
        takeASeat.setOnAction(e -> ButtonListeners.takeASeatButtonListener());

        Button changeSettings = ObjectStandards.makeButtonForLobbyScreen("Change settings");

        changeSettings.setLayoutX(670);
        changeSettings.setLayoutY(350);
        changeSettings.setMinWidth(150);
        changeSettings.setOnAction(event -> ButtonListeners.settingsButtonListener(gameController));

        Button startGame = ObjectStandards.makeButtonForLobbyScreen("Start game");
        startGame.setLayoutX(50);
        startGame.setLayoutY(425);
        //startGame.setOnAction(e -> ButtonListeners.startGameButtonListener(gameController));

        settings = generateSettingsBox(gameSettings);

        settings.setLayoutX(650);
        settings.setLayoutY(150);

        gameInfo.getChildren().addAll(settings, takeASeat, imageView, changeSettings, gameName, startGame, addPlayerOnBoard(),addPlayerOnBoard());
        fullLayout.getChildren().remove(1);
        fullLayout.getChildren().add(gameInfo);

    }

    public static Label addPlayerOnBoard(){

        emptyPositions.sort(null);
        Label label = new Label("Simple AI");
        label.setStyle("-fx-text-fill: white");
        label.setFont(new Font("Areal",20));

        switch (emptyPositions.get(0)){
            case 0:
                label.setLayoutX(270);
                label.setLayoutY(367);
                emptyPositions.remove(0);
                break;
            case 1:
                label.setLayoutX(0);
                label.setLayoutY(300);
                emptyPositions.remove(0);
                break;
            case 2:
                label.setLayoutX(0);
                label.setLayoutY(175);
                emptyPositions.remove(0);
                break;
            case 3:
                label.setLayoutX(270);
                label.setLayoutY(113);
                emptyPositions.remove(0);
                break;
            case 4:
                label.setLayoutX(510);
                label.setLayoutY(175);
                emptyPositions.remove(0);
                break;
            case 5:
                label.setLayoutX(510);
                label.setLayoutY(300);
                emptyPositions.remove(0);
                break;
            default:
                GUIMain.debugPrint("Lobby is full");
                break;
        }
        return label;
    }

    public static VBox generateSettingsBox(GameSettings gameSettings){
        VBox vBox = new VBox();

        Label stackSize = ObjectStandards.makeLobbyLabelWhite("Stack size: ",gameSettings.getStartStack()+"");
        Label numberOfPlayers = ObjectStandards.makeLobbyLabelWhite("Number of players: ",gameSettings.getMaxNumberOfPlayers()+"");
        Label bigBlind = ObjectStandards.makeLobbyLabelWhite("Big blind: ",gameSettings.getBigBlind()+"");
        Label smallBlind = ObjectStandards.makeLobbyLabelWhite("Small blind: ", gameSettings.getSmallBlind()+"");
        Label levelDuration = ObjectStandards.makeLobbyLabelWhite("Level duration: ",gameSettings.getLevelDuration()+"");
        Label aIDifficulty = ObjectStandards.makeLobbyLabelWhite("AI difficulty: ",gameSettings.getAiType()+"");

        vBox.getChildren().addAll(stackSize, numberOfPlayers, bigBlind, smallBlind, levelDuration, aIDifficulty);
        return vBox;
    }

    public static void makeNewLobby() {
        sideMenu.getChildren().add(0, addGames("default", "1/6"));
        displayGameInfo();
    }

    public static void updateLabels(GameSettings newSettings){
        gameSettings = newSettings;
        displayGameInfo();

    }

}
