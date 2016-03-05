package GUI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;


/**
 * Created by ady on 05/03/16.
 */
public class GUIMain extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage window) throws Exception{

        window.setTitle("Texas Hold'em");

        BorderPane completeLayout = new BorderPane();

        //Setting standards i want to use
        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(20,10,20,10);
        int standardButton = 100;

        //Make a new playerLayout
        HBox playerLayout = new HBox();
        completeLayout.setPadding(standardPadding);
        VBox buttonLayout1 = new VBox();
        VBox buttonLayout2 = new VBox();
        VBox textLayout1 = new VBox();
        HBox textLayout2 = new HBox();

        //////Make all the elements i want to add to the playerLayout//////////
        Label amountOfChips = new Label("Amount of chips: 1000");
        amountOfChips.setFont(standardFont);
        amountOfChips.setPadding(standardPadding);

        //Positions
        Label positions = new Label("Position: BB");
        positions.setFont(standardFont);
        positions.setPadding(standardPadding);

        //Cards
        Label temp1 = new Label("Insert card here!");
        temp1.setFont(standardFont);
        temp1.setPadding(standardPadding);

        Label temp2 = new Label("Insert card here!");
        temp2.setFont(standardFont);
        temp2.setPadding(standardPadding);

        //Amount to bet
        TextField betAmount = new TextField();
        betAmount.setPromptText("Amount to bet");
        betAmount.setFont(standardFont);
        betAmount.setPadding(standardPadding);

        //Buttons in the VBox

        //Bet
        Button bet = new Button("Place bet");
        bet.setFont(standardFont);
        bet.setPadding(standardPadding);
        bet.setMaxWidth(standardButton);

        //Check
        Button check = new Button("Check");
        check.setFont(standardFont);
        check.setPadding(standardPadding);
        check.setMaxWidth(standardButton);

        //Fold
        Button fold = new Button("Fold");
        fold.setFont(standardFont);
        fold.setPadding(standardPadding);
        fold.setMaxWidth(standardButton);

        //Pot
        Button pot = new Button("Pot");
        pot.setFont(standardFont);
        pot.setPadding(standardPadding);
        pot.setMaxWidth(standardButton);

        //Double
        Button doubleB = new Button("Double");
        doubleB.setFont(standardFont);
        doubleB.setPadding(standardPadding);
        doubleB.setMaxWidth(standardButton);

        //Max
        Button max = new Button("Max");
        max.setFont(standardFont);
        max.setPadding(standardPadding);
        max.setMaxWidth(standardButton);


        ////////////////TEMP FIX/////////////////

        Label tempLabel = new Label("");
        tempLabel.setFont(standardFont);
        tempLabel.setPadding(standardPadding);
        tempLabel.setMaxWidth(standardButton);

        /////////////////////////////////////////

        //Add buttons to the buttonLayout
        buttonLayout1.getChildren().addAll(bet,check,fold);
        buttonLayout2.getChildren().addAll(pot,doubleB,max);
        textLayout2.getChildren().addAll(positions, amountOfChips, temp1, temp2, betAmount);

        //Add all the elements to the playerLayout
        textLayout1.getChildren().addAll(tempLabel,textLayout2);
        playerLayout.getChildren().addAll(textLayout1,buttonLayout1,buttonLayout2);

        //Construct a new scene
        completeLayout.setBottom(playerLayout);
        Scene scene = new Scene(completeLayout,1000,200);

        window.setScene(scene);
        window.show();

    }
}
