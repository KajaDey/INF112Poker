package gui;

import gamelogic.GameController;
import gamelogic.ServerLobbyCommunicator;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.ArrayList;

/**
 * This class holds all the information about the lobby.
 * @author André Dyrstad
 */
public class LobbyScreen {

    static String styling = "-fx-border-color: black; -fx-background-color: #362626";

    static ArrayList<Integer> emptyPositions = new ArrayList<>();

    static GameController gameController;
    static GameSettings gameSettings;

    static VBox settings;
    static VBox sideMenu = new VBox();
    static Pane fullLayout = new Pane();
    private static ServerLobbyCommunicator serverLobbyCommunicator;

    /**
     * Displays the lobbyScreen.
     *
     * @param gs
     * @param gc
     * @param names
     */
    public static void createScreenForGameLobby(GameSettings gs, GameController gc, String names){
        //serverLobbyCommunicator = new ServerLobbyCommunicator(names, this);
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

    /**
     * Add more games to the game list
     *
     * @param name
     * @param players
     * @return VBox with a new game
     */
    public static VBox addGames(String name, String players){

        VBox vBox = new VBox();
        HBox hBox = new HBox();

        Label names = ObjectStandards.makeStandardLabelWhite(name,"");
        Label player = ObjectStandards.makeStandardLabelWhite(players, "");
        Button moreInfo = ObjectStandards.makeStandardButton("Info");

        vBox.setStyle(styling);
        hBox.getChildren().addAll(names, player);

        hBox.setMinWidth(150);
        vBox.setMinHeight(75);

        vBox.getChildren().addAll(hBox, moreInfo);

        moreInfo.setOnAction(event -> ButtonListeners.moreInfoButtonListener());

        vBox.setStyle(styling);
        vBox.setAlignment(Pos.CENTER);
        hBox.setAlignment(Pos.CENTER);

        return vBox;
    }

    /**
     * Show the game info on the screen
     */
    public static void displayGameInfo() {

        for(int i=0;i<6;i++)
            emptyPositions.add(i);

        Pane gameInfo = new Pane();
        gameInfo.setLayoutX(150);
        gameInfo.setLayoutY(110);
        gameInfo.setMinHeight(500);
        gameInfo.setMinWidth(850);

        CheckBox privateGameCheckbox = new CheckBox("Private game");
        privateGameCheckbox.setFont(new Font("Areal", 15));
        privateGameCheckbox.setStyle("-fx-text-fill: white");
        privateGameCheckbox.setLayoutX(660);
        privateGameCheckbox.setLayoutY(350);
        privateGameCheckbox.setOnAction(e -> ButtonListeners.privateGameCheckboxListener(privateGameCheckbox.isSelected()));

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
        changeSettings.setLayoutY(400);
        changeSettings.setMinWidth(150);
        changeSettings.setOnAction(event -> ButtonListeners.settingsButtonListener(gameController));

        Button startGame = ObjectStandards.makeButtonForLobbyScreen("Start game");
        startGame.setLayoutX(50);
        startGame.setLayoutY(425);
        startGame.setOnAction(e -> ButtonListeners.startGameButtonListener(gameController, null));

        settings = generateSettingsBox(gameSettings);

        settings.setLayoutX(650);
        settings.setLayoutY(150);

        gameInfo.getChildren().addAll(settings, privateGameCheckbox, takeASeat, imageView, changeSettings, gameName, startGame, addPlayerOnBoard(),addPlayerOnBoard());
        fullLayout.getChildren().remove(1);
        fullLayout.getChildren().add(gameInfo);

    }

    /**
     * Add a player to the board in the lobby
     * @return Label with the players name
     */
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

    /**
     * Generates a setting box that displays the right settings.
     *
     * @param gameSettings
     * @return
     */
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

    /**
     * Makes a new lobby and adds it to the layout
     */
    public static void makeNewLobby() {
        sideMenu.getChildren().add(0, addGames("default", "1/6"));
        displayGameInfo();
    }

    /**
     * updates all the labels in in the game info
     *
     * @param newSettings
     */
    public static void updateLabels(GameSettings newSettings){
        gameSettings = newSettings;
        displayGameInfo();

    }

}
