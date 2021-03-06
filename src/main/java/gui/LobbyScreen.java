package gui;

import gamelogic.GameController;
import gamelogic.Logger;
import network.ServerLobbyCommunicator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class holds all the information about the lobby.
 * @author André Dyrstad
 */
public class LobbyScreen {

    static String styling = "-fx-border-color: black; -fx-background-color: #362626";

    GameController gameController;

    private VBox settings;
    private VBox sideMenu = new VBox();
    private Pane fullLayout = new Pane();
    private Pane gameInfo;
    private Label numberOfPlayer;
    private Label names;
    private Button takeASeat;
    private ServerLobbyCommunicator serverLobbyCommunicator;
    private Map<Integer, LobbyTable> tables;
    private Map<Integer, VBox> tableBoxes; //Map from the VBoxes in the left side menu to table IDs
    private int ID;
    private Optional<LobbyTable> currentTable = Optional.empty();
    private final Logger logger;
    private String ipAddress = "";

    private String [] buttonTexts = {"Take seat", "Leave table", "Delete table", "Change settings", "Start game"};

    public LobbyScreen(GameController gameController, String name, InetAddress IPAddress, Logger logger) {
        this.ipAddress = IPAddress.getHostAddress();
        this.gameController = gameController;
        this.logger = logger;
        this.tables = new HashMap<>();
        this.tableBoxes = new HashMap<>();

        Pane pane = new Pane();
        Button newLobby = ObjectStandards.makeButtonForLobbyScreen("Make lobby");
        newLobby.setOnAction(event -> makeNewLobbyButtonListener());
        Label serverIPLabel = ObjectStandards.makeLobbyLabelWhite("Server IP:","\n"+ ipAddress);
        serverIPLabel.setLayoutX(1000);
        serverIPLabel.setLayoutY(50);


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
        fullLayout.getChildren().setAll(sideMenu, pane, gameInfo,serverIPLabel);

        SceneBuilder.showCurrentScene(fullLayout, "Lobby Screen");


        try {
            serverLobbyCommunicator = new ServerLobbyCommunicator(name, this, IPAddress, GUIMain.guiMain.logger);
            serverLobbyCommunicator.start();
            this.logger.println("Connected successfully to server!", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
        } catch (IOException e) {
            displayErrorMessage("Could not connect to server");
            this.logger.println("Error: Could not connect to server", Logger.MessageType.NETWORK, Logger.MessageType.WARNINGS);
        }
    }

    /**
     * Make a new table box (for the left hand menu)
     * @return VBox with a new game
     */
    public VBox makeTableBox(LobbyTable table){

        VBox vBox = new VBox();
        HBox hBox = new HBox();

        names = ObjectStandards.makeStandardLabelWhite("Table " + table.id,":");
        numberOfPlayer = ObjectStandards.makeStandardLabelWhite(table.playerIds.size() +"/"+table.settings.getMaxNumberOfPlayers(), "");
        Button moreInfo = ObjectStandards.makeStandardButton("Info");

        vBox.setStyle(styling);
        hBox.getChildren().setAll(names, numberOfPlayer);

        hBox.setMinWidth(150);
        vBox.setMinHeight(75);

        vBox.getChildren().setAll(hBox, moreInfo);

        moreInfo.setOnAction(event -> moreInfoButtonListener(table));

        vBox.setStyle(styling);
        vBox.setAlignment(Pos.CENTER);
        hBox.setAlignment(Pos.CENTER);

        return vBox;
    }

    /**
     * Display info for the selected game (table, players, settings and buttons)
     * @param table The table to display
     */
    public void displayGameInfo(LobbyTable table) {
        gameInfo.getChildren().clear();
        updateNumberOfPlayersSeated(table);

        Label gameName = ObjectStandards.makeLabelForHeadLine(serverLobbyCommunicator.getName(table.getHost()) + "'s game!");
        gameName.setLayoutX(325);
        gameName.setLayoutY(0);

        ImageView imageView = new ImageView(new Image(ImageViewer.returnURLPathForImages("tablev2")));

        imageView.setLayoutX(25);
        imageView.setLayoutY(100);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(550);

        if (table.getHost() == ID)
            takeASeat = ObjectStandards.makeButtonForLobbyScreen(buttonTexts[2]);
        else if(!table.isSeated(ID))
            takeASeat = ObjectStandards.makeButtonForLobbyScreen(buttonTexts[0]);
        else
            takeASeat = ObjectStandards.makeButtonForLobbyScreen(buttonTexts[1]);

        takeASeat.setLayoutX(200);
        takeASeat.setLayoutY(425);
        takeASeat.setOnAction(e -> takeASeatButtonListener(table));

        Button changeSettings = ObjectStandards.makeButtonForLobbyScreen(buttonTexts[3]);

        changeSettings.setLayoutX(670);
        changeSettings.setLayoutY(400);
        changeSettings.setMinWidth(150);
        changeSettings.setOnAction(event -> ButtonListeners.settingsButtonListener(serverLobbyCommunicator,table));

        Button startGame = ObjectStandards.makeButtonForLobbyScreen(buttonTexts[4]);
        startGame.setLayoutX(50);
        startGame.setLayoutY(425);
        startGame.setOnAction(e -> startGameButtonListener(table));

        if(table.getHost() != this.getID()){
            changeSettings.setVisible(false);
            startGame.setVisible(false);
        }

        settings = displayTableSettings(table);

        settings.setLayoutX(650);
        settings.setLayoutY(150);

        gameInfo.getChildren().addAll(settings, takeASeat, imageView, changeSettings, gameName, startGame);
        seatPlayersOnTable(table, gameInfo);
    }

    /**
     *  Get all the seated players and paint them in the GUI based on their seat.
     *  Called every time the displayGameInfo()-method is called
     *  @param table The selected table
     *  @param gameInfo A GUI pane that contains all the components for a table
     */
    private void seatPlayersOnTable(LobbyTable table, Pane gameInfo){
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
                    logger.println("Lobby is full", Logger.MessageType.NETWORK);
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
    private VBox displayTableSettings(LobbyTable table){
        VBox vBox = new VBox();

        Label stackSize = ObjectStandards.makeLobbyLabelWhite("Stack size: ","$" + table.settings.getStartStack());
        Label numberOfPlayers = ObjectStandards.makeLobbyLabelWhite("Number of players: ",table.settings.getMaxNumberOfPlayers()+"");
        Label bigBlind = ObjectStandards.makeLobbyLabelWhite("Big blind: ","$" + table.settings.getBigBlind());
        Label smallBlind = ObjectStandards.makeLobbyLabelWhite("Small blind: ", "$" + table.settings.getSmallBlind());
        Label levelDuration = ObjectStandards.makeLobbyLabelWhite("Level duration: ",table.settings.getLevelDuration()+"min");
        Label aIDifficulty = ObjectStandards.makeLobbyLabelWhite("AI difficulty: ",table.settings.getAiType()+"");
        Label playerClock = ObjectStandards.makeLobbyLabelWhite("Player clock: ",table.settings.getPlayerClock()+"sec");

        vBox.getChildren().addAll(stackSize, numberOfPlayers, bigBlind, smallBlind, levelDuration, playerClock, aIDifficulty);
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
        if(takeASeat.getText().equalsIgnoreCase(buttonTexts[0]))
            serverLobbyCommunicator.takeSeat(table.id);
        else if(takeASeat.getText().equalsIgnoreCase(buttonTexts[1]))
            serverLobbyCommunicator.leaveSeat(table.id);
        else if(takeASeat.getText().equalsIgnoreCase(buttonTexts[2]))
            serverLobbyCommunicator.deleteTable(table.id);
    }
    private void moreInfoButtonListener(LobbyTable table) {
        this.displayGameInfo(table);
        this.currentTable = Optional.of(table);
    }

    /**
     * Add a new table to the GUI.
     * Adds the table to the tables-map and puts it in the sideMenu
     * @param table The table to add
     */
    public void addTable(LobbyTable table) {
        assert table != null : "Table was null";
        assert tables != null : "Tables was null";

        tables.put(table.id, table);
        VBox tableBox = makeTableBox(table);
        tableBoxes.put(table.id, tableBox);
        sideMenu.getChildren().add(0, tableBox);

        if (!currentTable.isPresent())
            currentTable = Optional.of(table);

        logger.println("Added new table, id " + table.id, Logger.MessageType.NETWORK);
    }

    /**
     *  Remove a table from the GUI and the tables-map and re-paint.
     * @param tableID Id of the table to remove
     */
    public void removeTable(int tableID) {
        if (tables.get(tableID) == null) {
            logger.println("Tried to remove table " + tableID + ", but it was not found in tables", Logger.MessageType.WARNINGS, Logger.MessageType.NETWORK);
            return;
        }
        tables.remove(tableID);
        sideMenu.getChildren().remove(tableBoxes.get(tableID));
        tableBoxes.remove(tableID);

        if (currentTable.isPresent() && currentTable.get().id == tableID) {
            if (tables.isEmpty()) {
                currentTable = Optional.empty();
                gameInfo.getChildren().clear();
            } else {
                currentTable = tables.values().stream().findAny();
                if (currentTable.isPresent())
                    displayGameInfo(currentTable.get());
            }
        }
    }

    /**
     * Get a table (used by ServerLobbyCommunicator)
     * @param tableID Id of the table
     * @return The table
     */
    public LobbyTable getTable(int tableID) {
        assert tables.get(tableID) != null : "Tried to get table from LobbyScreen, but table " + tableID + " did not exist";
        return tables.get(tableID);
    }

    /**
     * Update the current table settings
     * @param tableID The table id
     */
    public void refreshTableSettings(int tableID) {
        if (currentTable.isPresent() && currentTable.get().id == tableID)
            displayGameInfo(currentTable.get());
    }

    public void setID(int ID) {
        this.ID = ID;
    }
    public int getID() { return ID; }

    /**
     * Add a numberOfPlayer to a given table and re-paint the table
     * @param tableID The table id
     * @param playerID The players id
     */
    public void addPlayer(int playerID, int tableID) {
        getTable(tableID).addPlayer(playerID);
        if (playerID == this.ID)
            currentTable = Optional.of(getTable(tableID));

        if (currentTable.isPresent())
            displayGameInfo(currentTable.get());
    }

    /**
     *  Remove a numberOfPlayer from the given table and re-paint the table
     * @param tableID The table id
     * @param playerID The players id
     */
    public void removePlayer(int playerID, int tableID ) {
        LobbyTable t = getTable(tableID);
        if (t.getHost() == playerID)
            removeTable(tableID);
        else
            t.removePlayer(playerID);

        if (currentTable.isPresent())
            displayGameInfo(currentTable.get());
    }

    /**
     *  Display an error message in a new window if client cannot connect to server
     * @param error The error message to display
     */
    public void displayErrorMessage(String error){

        Stage errorMessage = new Stage();

        VBox layout = new VBox();
        layout.setPadding(new Insets(10, 10, 10, 10));

        Label label = ObjectStandards.makeLobbyLabelWhite(error,"");
        label.setFont(new Font("Areal", 25));

        Button ok;

        if(error.contains("connect")) {
            ok = ObjectStandards.makeStandardButton("Return to the main menu");
            ok.setOnAction(e -> {
                ButtonListeners.returnToMainMenuButtonListener();
                errorMessage.close();
            });
        }
        else{
            ok = ObjectStandards.makeStandardButton("Return to lobby");
            ok.setOnAction(e -> {
                errorMessage.close();
            });
        }

        layout.getChildren().addAll(label, ok);
        layout.setAlignment(Pos.CENTER);

        layout.setStyle("-fx-background-color:#602121");

        errorMessage.initModality(Modality.APPLICATION_MODAL);
        errorMessage.setTitle("Error");
        Scene scene = new Scene(layout);
        errorMessage.setScene(scene);
        errorMessage.show();

    }

    public void updateNumberOfPlayersSeated(LobbyTable table){
        numberOfPlayer.setText(table.playerIds.size() +"/"+table.settings.getMaxNumberOfPlayers());
    }

    /**
     * Remove player from any table he is seated at
     */
    public void playerQuit(int playerID) {
        //If this player is the host of a table, remove this table
        Optional<LobbyTable> hostTable = tables.values().stream().filter(t -> t.getHost() == playerID).findAny();
        if (hostTable.isPresent())
            removeTable(hostTable.get().id);

        //Remove player from any other table (should not be any if the player was host of a game)
        tables.values().stream().forEach(t -> t.removePlayer(playerID));

        //Re-display table info
        if (currentTable.isPresent())
            displayGameInfo(currentTable.get());
    }

}
