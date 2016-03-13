package main.java.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import main.java.gamelogic.GameController;

/**
 * Created by Jostein on 07.03.2016.
 */
public class MainScreen {

    /**
     * Creates the layout for the main screen, containing buttons, text fields, labels and choicebox.
     * @param window The "old" window that needs to be refreshed
     * @param gameController
     * @return The horizontal box containing all the information of the screen.
     */
    public static HBox makeLayout(Stage window,GameController gameController){
        Font standardFont = new Font("Areal",15);
        Font infoFont = new Font("Monaco", 30);
        Insets standardPadding = new Insets(5,5,5,5);
        Insets largePadding = new Insets(15);
        int standardButton = 75;

        HBox horisontalFull = new HBox();
        VBox verticalButtonAndChoiceBox = new VBox();

        //Top-text
        String title = "Heads Up Poker!";
        String info = "This is an implementation of heads up Texas hold'em.\n" +
                "Since the program isn't fully implemented yet, you can only choose \"Single player\" and play against one AI\n" +
                "Enter your name, and start playing!";

        Label titleText = ObjectStandards.makeStandardLabelWhite(title, "");
        titleText.setPadding(largePadding);
        titleText.setFont(infoFont);

        Label infoText = ObjectStandards.makeStandardLabelWhite(info, "");
        infoText.setPadding(largePadding);
        infoText.setFont(new Font("Areal", 15));

        verticalButtonAndChoiceBox.getChildren().addAll(titleText,infoText);
        verticalButtonAndChoiceBox.setAlignment(Pos.CENTER);

        horisontalFull.setAlignment(Pos.CENTER);
        horisontalFull.getChildren().addAll(verticalButtonAndChoiceBox);

        TextField nameIn = new TextField();
        nameIn.setPromptText("Enter Name");
        nameIn.setFont(standardFont);
        nameIn.setPadding(standardPadding);
        nameIn.setMaxWidth(2*standardButton);

        TextField playersIn = new TextField();
        playersIn.setPromptText("Number of Players");
        playersIn.setFont(standardFont);
        playersIn.setPadding(standardPadding);
        playersIn.setMaxWidth(2*standardButton);

        Button enter = ObjectStandards.makeStandardButton("Enter");
        enter.setMinWidth(2*standardButton);
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.setMinWidth(2*standardButton);
        choiceBox.getItems().addAll("Single Player", "Multi Player");
        choiceBox.setValue("Single Player");

        enter.setOnAction(e ->{
            window.close();
            ButtonListeners.mainScreenEnterListener(nameIn.getText(), playersIn.getText(), choiceBox.getValue(), gameController);
        });

        playersIn.setOnAction(e -> {
            window.close();
            ButtonListeners.mainScreenEnterListener(nameIn.getText(), playersIn.getText(), choiceBox.getValue(), gameController);
        });

        verticalButtonAndChoiceBox.getChildren().addAll(choiceBox, nameIn, playersIn, enter);

        return horisontalFull;
    }
}
