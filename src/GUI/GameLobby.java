package GUI;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * Created by ady on 07/03/16.
 */
public class GameLobby {

    public static void createScreenForGameLobby(){

        BorderPane playerImage = new BorderPane();
        HBox fullLayout = new HBox();

        Button settings = LayoutGenerators.makeStandardButton("Settings");
        Label amountOfChips = LayoutGenerators.makeStandardLabel("Chips", "1000" + "$");
        Label numberOfPlayers = LayoutGenerators.makeStandardLabel("Number of players","5");
        Label bigBlind = LayoutGenerators.makeStandardLabel("Big blind","50" + "$");
        Label smallBlind = LayoutGenerators.makeStandardLabel("Small blind","25" + "%");
        Label levelDuration = LayoutGenerators.makeStandardLabel("Level duration","10" + "min");



    }
}
