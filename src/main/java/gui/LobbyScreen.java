package gui;

import gamelogic.GameController;
import gamelogic.Hand;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

/**
 * Created by ady on 18/04/16.
 */
public class LobbyScreen {

    //static Label name,name2,players,players2;

    static String styling = "-fx-border-color: darkgreen ";

    static GameController gameController;
    static GameSettings gameSettings;

    static VBox sideMenu = new VBox();
    static Pane fullLayout = new Pane();

    public static void createScreenForGameLobby(GameSettings gs, GameController gc, String names){

        gameController = gc;
        gameSettings = gs;

        Pane pane = new Pane();

        sideMenu.getChildren().addAll(addGames("Ady's lobby", "2/4"), addGames("Jos's lobby", "3/6"));
        sideMenu.setLayoutX(1000);
        sideMenu.setLayoutY(100);
        sideMenu.setStyle(styling);
        sideMenu.setMinHeight(500);
        sideMenu.setMinWidth(150);
        sideMenu = ImageViewer.setBackground(("listbackground"),sideMenu,150,500);

        fullLayout.setStyle("-fx-background-color: #890418");
        fullLayout.getChildren().addAll(sideMenu, pane);


        SceneBuilder.showCurrentScene(fullLayout, "Lobby Screen");

    }

    public static VBox addGames(String name, String players){

        VBox vBox = new VBox();
        HBox hBox = new HBox();

        Label names = ObjectStandards.makeStandardLabelWhite(name,"");
        Label player = ObjectStandards.makeStandardLabelWhite(players,"");
        Button moreInfo = ObjectStandards.makeStandardButton("Info");

        names.setAlignment(Pos.TOP_LEFT);
        player.setAlignment(Pos.TOP_RIGHT);
        moreInfo.setAlignment(Pos.CENTER);

        vBox.setStyle(styling);
        hBox.getChildren().addAll(names, player);

        hBox.setMinWidth(150);
        vBox.setMinHeight(75);

        vBox.getChildren().addAll(hBox, moreInfo);

        moreInfo.setOnAction(event -> ButtonListeners.moreInfoButtonListener());

        return vBox;
    }

    public static void displayGameInfo() {
        Pane pane = new Pane();
        pane.setLayoutX(150);
        pane.setLayoutY(110);
        pane.setMinHeight(500);
        pane.setMinWidth(850);

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

        Button changeSettings = ObjectStandards.makeButtonForLobbyScreen("Change settings");

        changeSettings.setLayoutX(670);
        changeSettings.setLayoutY(350);
        changeSettings.setMinWidth(150);
        changeSettings.setOnAction(event -> ButtonListeners.settingsButtonListener(gameController));

        VBox settings = generateSettingsBox(gameSettings);

        settings.setLayoutX(650);
        settings.setLayoutY(150);

        pane.getChildren().addAll(settings, takeASeat, imageView, changeSettings, gameName);
        fullLayout.getChildren().remove(1);
        fullLayout.getChildren().add(pane);

    }

    /*public static void addPlayerOnBoard(){
        switch (numberOfPlayers){
            case 1:
        }
    }*/

    public static VBox generateSettingsBox(GameSettings gameSettings){
        VBox vBox = new VBox();

        Label stackSize = ObjectStandards.makeLobbyLabelWhite("Stack size: ",gameSettings.getStartStack()+"");
        Label numberOfPlayers = ObjectStandards.makeLobbyLabelWhite("Number of players: ",gameSettings.getMaxNumberOfPlayers()+"");
        Label bigBlind = ObjectStandards.makeLobbyLabelWhite("Big blind: ",gameSettings.getBigBlind()+"");
        Label smallBlind = ObjectStandards.makeLobbyLabelWhite("Small blind: ", gameSettings.getSmallBlind()+"");
        Label levelDuration = ObjectStandards.makeLobbyLabelWhite("Level duration: ",gameSettings.getLevelDuration()+"");
        Label aIDifficulty = ObjectStandards.makeLobbyLabelWhite("AI difficulty: ",gameSettings.getAiType()+"");

        vBox.getChildren().addAll(stackSize,numberOfPlayers,bigBlind,smallBlind,levelDuration, aIDifficulty);
        return vBox;
    }

}
