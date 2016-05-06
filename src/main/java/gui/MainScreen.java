package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import gamelogic.GameController;
import gamelogic.AIType;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Supplier;

/**
 * This purpose of this class is to create the full screen that is seen when the application is started.
 * The class will add the welcome text, input field for name and number of players, as well as the button
 * for entering the lobby screen.
 *
 * @author Jostein Kringlen
 * @author André Dyrstad
 */
public class MainScreen {

    private static String imgName;
    private static GameController gc;
    private static Stage window;
    private static File chosenFile;
    private static GameType gameType;

    /**
     * Creates the sceen for the initial (main) screen
     * @param imageName The name of the background that will be used
     * @param gameController
     * @return The scene to be shown
     */
    public static void createSceneForMainScreen(String imageName, GameController gameController){
        window = new Stage();
        imgName = imageName;
        gc = gameController;

        BorderPane mainScreenLayout = new BorderPane();
        mainScreenLayout.setPadding(new Insets(10,10,10,10));
        mainScreenLayout.setCenter(MainScreen.makeLayout(window, gameController));
        mainScreenLayout.setTop(ObjectStandards.createMenuBar());
        SceneBuilder.showCurrentScene(mainScreenLayout, "Welcome to The Game!");
    }

    public static void refreshSceneForMainScreen() {
        createSceneForMainScreen(imgName, gc);
    }

    /**
     * Creates the layout for the main screen, containing buttons, text fields, labels and choicebox.
     * @param window The "old" window that needs to be refreshed
     * @param gameController
     * @return The horizontal box containing all the information of the screen.
     */
    public static HBox makeLayout(Stage window,GameController gameController){
        Insets largePadding = new Insets(15);
        int standardButton = 75;

        HBox horizontalFull = new HBox();
        VBox verticalButtonAndChoiceBox = new VBox();

        //Top-text
        String title = "Texas Hold'em!";
        String info = "In this game, you can play the popular \nversion of poker called " +
                "Texas Hold'em against 1-5 players.\n" +
               "Enter your name, and start playing!";


        VBox playGameBox = new VBox();
        VBox watchGameBox = new VBox();

        Button singlePlayer = ObjectStandards.makeMainScreenButton("Single Player");
        Button multiPlayer = ObjectStandards.makeMainScreenButton("Multi player");
        Button watchReplay = ObjectStandards.makeMainScreenButton("Watch game");
        Button exit = ObjectStandards.makeMainScreenButton("Exit");
        Button selectFile = ObjectStandards.makeButtonForLobbyScreen("Select file");
        Button watchNow = ObjectStandards.makeButtonForLobbyScreen("Watch now");
        Label selectedFile = ObjectStandards.makeStandardLabelWhite("No file chosen", "");
        selectedFile.setFont(new Font("Areal",20));

        Label titleText = ObjectStandards.makeLabelForHeadLine(title);

        Label infoText = ObjectStandards.makeStandardLabelWhite(info,"");
        infoText.setPadding(largePadding);
        infoText.setFont(new Font("Areal", 15));

        verticalButtonAndChoiceBox.getChildren().addAll(titleText);
        verticalButtonAndChoiceBox.setAlignment(Pos.CENTER);

        horizontalFull.setAlignment(Pos.CENTER);
        horizontalFull.getChildren().addAll(verticalButtonAndChoiceBox);

        TextField nameIn = ObjectStandards.makeTextFieldForMainScreen("Name");
        TextField IPIn = ObjectStandards.makeTextFieldForMainScreen("IP addres");
        IPIn.setText("127.0.0.1");
        TextField numOfPlayersIn = ObjectStandards.makeTextFieldForMainScreen("Number of players");

        Button enter = ObjectStandards.makeStandardButton("Enter");
        enter.setMinWidth(2 * standardButton);

        Supplier<Void> enterGameScreen = () -> {
            InetAddress inetAddress;
            try {
                if (IPIn.getText() == null || IPIn.getText().isEmpty())
                    inetAddress = InetAddress.getLocalHost();
                else
                    inetAddress = InetAddress.getByName(IPIn.getText());
            } catch (UnknownHostException ex) {
                // TODO show error message next to textfield
                return null;
            }
            window.close();
            ButtonListeners.mainScreenEnterListener(nameIn.getText(), inetAddress, numOfPlayersIn.getText(), gameType, gameController);
            return null;
        };

        enter.setOnAction(e ->{
            System.out.println("enter clicked. gameType = "+gameType.name());
            enterGameScreen.get();
        });

        nameIn.setOnAction(e -> { enterGameScreen.get(); });

        numOfPlayersIn.setOnAction(e -> { enterGameScreen.get(); });

        singlePlayer.setOnAction(e -> {
            gameType = GameType.SINGLE_PLAYER;
            playGameBox.getChildren().clear();
            playGameBox.getChildren().addAll(nameIn, enter);
            verticalButtonAndChoiceBox.getChildren().clear();
            verticalButtonAndChoiceBox.getChildren().addAll(titleText, singlePlayer, playGameBox, multiPlayer, watchReplay, exit);
        });

        multiPlayer.setOnAction(e -> {
            gameType = GameType.MULTI_PLAYER;
            playGameBox.getChildren().clear();
            playGameBox.getChildren().addAll(nameIn, IPIn, enter);
            verticalButtonAndChoiceBox.getChildren().clear();
            verticalButtonAndChoiceBox.getChildren().addAll(titleText, singlePlayer, multiPlayer, playGameBox, watchReplay, exit);
        });

        watchReplay.setOnAction(e -> {
            gameType = GameType.WATCH_GAME;
            verticalButtonAndChoiceBox.getChildren().clear();
            verticalButtonAndChoiceBox.getChildren().addAll(titleText, singlePlayer, multiPlayer, watchGameBox, watchReplay, exit);

        });

        selectFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File fileSelected = fileChooser.showOpenDialog(null);

            if(fileSelected != null) {
                selectedFile.setText(fileSelected.getName());
                chosenFile = fileSelected;
            }
            else
                selectedFile.setText("File is not valid");

        });

        watchNow.setOnAction(event -> ButtonListeners.watchNowButtonListener(chosenFile));

        exit.setOnAction(e -> System.exit(0));

        playGameBox.getChildren().addAll(nameIn, enter);
        playGameBox.setAlignment(Pos.CENTER);
        playGameBox.setPadding(new Insets(10, 10, 10, 10));
        watchGameBox.getChildren().addAll(selectFile, selectedFile, watchNow);
        watchGameBox.setAlignment(Pos.CENTER);
        watchGameBox.setPadding(new Insets(10, 10, 10, 10));

        verticalButtonAndChoiceBox.getChildren().addAll(singlePlayer, multiPlayer, watchReplay, exit);
        verticalButtonAndChoiceBox.setAlignment(Pos.CENTER);

        return horizontalFull;
    }

    public static Stage getStage(){
        return window;
    }
    public static GameController getGameController(){
        return gc;
    }

    public static enum GameType { SINGLE_PLAYER, MULTI_PLAYER, WATCH_GAME }
}
