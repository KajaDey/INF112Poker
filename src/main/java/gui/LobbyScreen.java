package gui;

import gamelogic.GameController;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by ady on 18/04/16.
 */
public class LobbyScreen {

    //static Label name,name2,players,players2;

    static String styling = "-fx-border-color: red ";// + "-fx-border-width 2px, " + "-fx-padding 10, "+  "-fx-spacing 8";

    static VBox sideMenu = new VBox();
    static Pane fullLayout = new Pane();

    public static void createScreenForGameLobby(GameSettings gameSettings, GameController gameController, String names){

        Pane pane = new Pane();

        sideMenu.getChildren().addAll(addGames("Ady's lobby", "2/4"), addGames("Jos's lobby", "3/6"));
        sideMenu.setLayoutX(1000);
        sideMenu.setLayoutY(100);
        sideMenu.setStyle(styling);
        sideMenu.setMinHeight(500);
        sideMenu.setMinWidth(150);
        fullLayout.getChildren().addAll(sideMenu, pane);

        SceneBuilder.showCurrentScene(fullLayout, "Lobby Screen");

    }

    public static VBox addGames(String name, String players){

        VBox vBox = new VBox();

        Label names = ObjectStandards.makeStandardLabelWhite(name,"");
        Label player = ObjectStandards.makeStandardLabelWhite(players,"");
        Button moreInfo = ObjectStandards.makeStandardButton("Info");

        vBox.setStyle(styling);
        vBox.getChildren().addAll(names, player, moreInfo);

        moreInfo.setOnAction(event -> ButtonListeners.moreInfoButtonListener());

        return vBox;
    }

    public static void displayGameInfo() {
        Pane pane = new Pane();
        Label players = ObjectStandards.makeStandardLabelBlack("Player1\nPlayer2\nPlayer3\nPlayer4", "");

        players.setLayoutX(100);
        players.setLayoutY(100);

        Label settings = ObjectStandards.makeStandardLabelBlack("This is \n where the \n stats are\n supposed to \n be", "");

        settings.setLayoutX(200);
        settings.setLayoutY(200);

        ImageView imageView = new ImageView(new Image(ImageViewer.returnURLPathForImages("PokerTable")));

        imageView.setLayoutX(200);
        imageView.setLayoutY(100);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(550);

        pane.getChildren().addAll(players, settings);
        pane.setLayoutX(200);
        pane.setLayoutY(100);
        pane.setMinHeight(500);
        pane.setMinWidth(800);
        pane.setStyle("-fx-background-color: white");
        //pane.setStyle(styling);
        fullLayout.getChildren().remove(1);
        fullLayout.getChildren().add(pane);

    }

    /*public static void addPlayerOnBoard(){
        switch (numberOfPlayers){
            case 1:
        }
    }*/
}
