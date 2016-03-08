package main.java.gui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by ady on 07/03/16.
 */
public class GameScreen {


    public static void createSceneForGameScreen(VBox opponent, VBox player, HBox board) {

        Stage window = new Stage();

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(opponent);
        borderPane.setCenter(board);
        borderPane.setBottom(player);

        Scene scene = new Scene(ImageViewer.setBackground("PokerTable", borderPane, 1920, 1080), 1280, 720);

        window.setScene(scene);
        window.show();

    }
}
