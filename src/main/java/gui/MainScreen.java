package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import gamelogic.GameController;
import gamelogic.AIType;

/**
 * This purpose of this class is to create the full screen that is seen when the application is started.
 * The class will add the welcome text, input field for name and number of players, as well as the button
 * for entering the lobby screen.
 *
 * @author Jostein Kringlen
 */
public class MainScreen {

    private static String imgName;
    private static GameController gc;
    private static Stage window;

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
        SceneBuilder.showCurrentScene(mainScreenLayout,"Welcome to The Game!");
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

        HBox horisontalFull = new HBox();
        VBox verticalButtonAndChoiceBox = new VBox();

        //Top-text
        String title = "Texas Hold'em!";
        String info = "In this game, you can play the popular \nversion of poker called " +
                "Texas Hold'em against 1-5 players.\n" +
               "Enter your name, and start playing!";


        Label titleText = ObjectStandards.makeLabelForHeadLine(title);

        Label infoText = ObjectStandards.makeStandardLabelWhite(info,"");
        infoText.setPadding(largePadding);
        infoText.setFont(new Font("Areal", 15));

        verticalButtonAndChoiceBox.getChildren().addAll(titleText,infoText);
        verticalButtonAndChoiceBox.setAlignment(Pos.CENTER);

        horisontalFull.setAlignment(Pos.CENTER);
        horisontalFull.getChildren().addAll(verticalButtonAndChoiceBox);

        TextField nameIn = ObjectStandards.makeTextFieldForMainScreen("Name");
        TextField numOfPlayersIn = ObjectStandards.makeTextFieldForMainScreen("Number of players");

        Button enter = ObjectStandards.makeStandardButton("Enter");
        enter.setMinWidth(2 * standardButton);
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.setMinWidth(2 * standardButton);
        choiceBox.getItems().addAll(AIType.SIMPLE_AI.toString(), AIType.MCTS_AI.toString(), AIType.MIXED.toString());
        choiceBox.setValue(AIType.MCTS_AI.toString());
        choiceBox.setTooltip(new Tooltip("Pick a game mode"));


        choiceBox.setStyle("-fx-background-color:#090a0c, " +
                "linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%), " +
                "linear-gradient(#20262b, #191d22), " +
                "radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0)); " +
                "-fx-background-radius: 5,4,3,5; " +
                "-fx-background-insets: 0,1,2,0; " +
                "-fx-text-fill: linear-gradient(white, #d0d0d0) ; ");
        choiceBox.getStylesheets().addAll("file:resources/choiceBoxStyling.css");

        enter.setOnAction(e ->{
            window.close();
            ButtonListeners.mainScreenEnterListener(nameIn.getText(), numOfPlayersIn.getText(), choiceBox.getValue(), gameController);
        });

        numOfPlayersIn.setOnAction(e -> {
            window.close();
            ButtonListeners.mainScreenEnterListener(nameIn.getText(), numOfPlayersIn.getText(), choiceBox.getValue(), gameController);
        });

        verticalButtonAndChoiceBox.getChildren().addAll(choiceBox, nameIn, numOfPlayersIn, enter);

        return horisontalFull;
    }

    public static Stage getStage(){
        return window;
    }
    public static GameController getGameController(){
        return gc;
    }
}
