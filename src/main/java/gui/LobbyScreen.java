package gui;

import gamelogic.GameController;
import gamelogic.ServerGameCommunicator;
import gamelogic.ServerLobbyCommunicator;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds all the information about the lobby.
 * @author André Dyrstad
 */
public class LobbyScreen {

    static String styling = "-fx-border-color: black; -fx-background-color: #362626";

    GameController gameController;

    static VBox settings;
    static VBox sideMenu = new VBox();
    static Pane fullLayout = new Pane();
    static Pane gameInfo;
    private ServerLobbyCommunicator serverLobbyCommunicator;
    private Map<Integer, LobbyTable> tables;
    private Map<Integer, VBox> tableBoxes; //Map from the VBoxes in the left side menu to table IDs
    private int ID;

    public LobbyScreen(GameController gameController, String name) {
        this.gameController = gameController;
        this.tables = new HashMap<>();
        this.tableBoxes = new HashMap<>();

        Pane pane = new Pane();
        Button newLobby = ObjectStandards.makeButtonForLobbyScreen("Make lobby");
        newLobby.setOnAction(event -> makeNewLobbyButtonListener());

        sideMenu.getChildren().addAll(newLobby);
        sideMenu.setLayoutX(1000);
        sideMenu.setLayoutY(100);
        sideMenu.setMinHeight(500);
        sideMenu.setMinWidth(150);
        sideMenu.setAlignment(Pos.TOP_CENTER);
        sideMenu.setStyle("-fx-background-color: #241414");

        gameInfo = new Pane();
        gameInfo.setLayoutX(150);
        gameInfo.setLayoutY(110);
        gameInfo.setMinHeight(500);
        gameInfo.setMinWidth(850);

        fullLayout.setStyle("-fx-background-color: #602121");
        fullLayout.getChildren().addAll(sideMenu, pane, gameInfo);

        SceneBuilder.showCurrentScene(fullLayout, "Lobby Screen");

        try {
            serverLobbyCommunicator = new ServerLobbyCommunicator(name, this);
            GUIMain.debugPrintln("Connected successfully to server!");
        } catch (IOException e) {
            GUIMain.debugPrintln("Error: Could not connect to server");
            e.printStackTrace();
        }
    }

    /**
     * Add more games to the game list
     * @return VBox with a new game
     */
    public VBox makeTableBox(LobbyTable table){

        VBox vBox = new VBox();
        HBox hBox = new HBox();

        Label names = ObjectStandards.makeStandardLabelWhite("Table " + table.id,"");
        Label player = ObjectStandards.makeStandardLabelWhite(table.playerIds.size() +"/"+table.settings.getMaxNumberOfPlayers(), "");
        Button moreInfo = ObjectStandards.makeStandardButton("Info");

        vBox.setStyle(styling);
        hBox.getChildren().addAll(names, player);

        hBox.setMinWidth(150);
        vBox.setMinHeight(75);

        vBox.getChildren().addAll(hBox, moreInfo);

        moreInfo.setOnAction(event -> moreInfoButtonListener(table));

        vBox.setStyle(styling);
        vBox.setAlignment(Pos.CENTER);
        hBox.setAlignment(Pos.CENTER);

        return vBox;
    }

    /**
     * Show the game info on the screen
     */
    public void displayGameInfo(LobbyTable table) {
        gameInfo.getChildren().clear();

        CheckBox privateGameCheckbox = new CheckBox("Private game");
        privateGameCheckbox.setFont(new Font("Areal", 15));
        privateGameCheckbox.setStyle("-fx-text-fill: white");
        privateGameCheckbox.setLayoutX(660);
        privateGameCheckbox.setLayoutY(350);
        privateGameCheckbox.setOnAction(e -> privateGameCheckboxListener(privateGameCheckbox.isSelected()));

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
        takeASeat.setOnAction(e -> takeASeatButtonListener(table));

        Button changeSettings = ObjectStandards.makeButtonForLobbyScreen("Change settings");

        changeSettings.setLayoutX(670);
        changeSettings.setLayoutY(400);
        changeSettings.setMinWidth(150);
        changeSettings.setOnAction(event -> settingsButtonListener(table));

        Button startGame = ObjectStandards.makeButtonForLobbyScreen("Start game");
        startGame.setLayoutX(50);
        startGame.setLayoutY(425);
        startGame.setOnAction(e -> startGameButtonListener(table));

        settings = displayTableSettings(table);

        settings.setLayoutX(650);
        settings.setLayoutY(150);

        gameInfo.getChildren().addAll(settings, privateGameCheckbox, takeASeat, imageView, changeSettings, gameName, startGame);
        seatPlayersOnTable(table, gameInfo);
    }

    /**
     *  Get all the seated players and paint them in the GUI based on their seat
     */
    public void seatPlayersOnTable(LobbyTable table, Pane gameInfo){
        int seat = 0;

        for (Integer playerID : table.playerIds) {
            Label nameLabel = new Label(serverLobbyCommunicator.getName(playerID));
            nameLabel.setStyle("-fx-text-fill: white");
            nameLabel.setFont(new Font("Areal", 20));

            switch (seat) {
                case 0:
                    nameLabel.setLayoutX(270);
                    nameLabel.setLayoutY(367);
                    break;
                case 1:
                    nameLabel.setLayoutX(0);
                    nameLabel.setLayoutY(300);
                    break;
                case 2:
                    nameLabel.setLayoutX(0);
                    nameLabel.setLayoutY(175);
                    break;
                case 3:
                    nameLabel.setLayoutX(270);
                    nameLabel.setLayoutY(113);
                    break;
                case 4:
                    nameLabel.setLayoutX(510);
                    nameLabel.setLayoutY(175);
                    break;
                case 5:
                    nameLabel.setLayoutX(510);
                    nameLabel.setLayoutY(300);
                    break;
                default:
                    GUIMain.debugPrint("Lobby is full");
                    break;
            }
            gameInfo.getChildren().add(nameLabel);
            seat++;
        }
    }

    /**
     *  A VBox with all the settings of the given table
     *
     * @param table The table to display the settings for
     * @return A VBox with all the settings
     */
    public VBox displayTableSettings(LobbyTable table){
        VBox vBox = new VBox();

        Label stackSize = ObjectStandards.makeLobbyLabelWhite("Stack size: ",table.settings.getStartStack()+"");
        Label numberOfPlayers = ObjectStandards.makeLobbyLabelWhite("Number of players: ",table.settings.getMaxNumberOfPlayers()+"");
        Label bigBlind = ObjectStandards.makeLobbyLabelWhite("Big blind: ",table.settings.getBigBlind()+"");
        Label smallBlind = ObjectStandards.makeLobbyLabelWhite("Small blind: ", table.settings.getSmallBlind()+"");
        Label levelDuration = ObjectStandards.makeLobbyLabelWhite("Level duration: ",table.settings.getLevelDuration()+"");
        Label aIDifficulty = ObjectStandards.makeLobbyLabelWhite("AI difficulty: ",table.settings.getAiType()+"");

        vBox.getChildren().addAll(stackSize, numberOfPlayers, bigBlind, smallBlind, levelDuration, aIDifficulty);
        return vBox;
    }

    //Button listeners//
    private void startGameButtonListener(LobbyTable table) {
        serverLobbyCommunicator.startGame(table.id);
    }

    private void makeNewLobbyButtonListener() {
        serverLobbyCommunicator.makeNewTable();
    }

    private void takeASeatButtonListener(LobbyTable table) {
        serverLobbyCommunicator.takeSeat(table.id);
    }

    private void moreInfoButtonListener(LobbyTable table) {
        this.displayGameInfo(table);
    }

    private void privateGameCheckboxListener(boolean selected) {

    }

    /**
     * Add a new table to the GUI
     * @param table The table to add
     */
    public void addTable(LobbyTable table) {
        assert table != null : "Table was null";
        assert tables != null : "Tables was null";

        tables.put(table.id, table);
        VBox tableBox = makeTableBox(table);
        tableBoxes.put(table.id, tableBox);
        sideMenu.getChildren().add(0, tableBox);
        //displayGameInfo(table);

        GUIMain.debugPrintln("Added new table, id " + table.id);
    }

    public LobbyTable getTable(int tableID) {
        assert tables.get(tableID) != null : "Tried to get table from LobbyScreen, but table " + tableID + " did not exist";
        return tables.get(tableID);
    }

    public void refreshTableSettings(int tableID) {
        displayTableSettings(tables.get(tableID));
    }

    public void settingsButtonListener(LobbyTable table) {
        //Implement
    }

    public void setID(int ID) {
        this.ID = ID;
    }
    public int getID() { return ID; }

    public void addPlayer(int tableID, int playerID) {
        getTable(tableID).addPlayer(playerID);
        displayGameInfo(tables.get(tableID));
    }

    public void removePlayer(int tableID, int playerID) {
        getTable(tableID).removePlayer(playerID);
    }
}