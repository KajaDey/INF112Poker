package main.java.gui.mariahgui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

/**
 * Created by ady on 05/03/16.
 */
public class LayoutGeneratorsMariah {

    public static HBox makePlayerLayout(){

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
        VBox textLayoutVertical = new VBox();
        HBox textLayoutHorizontallyTop = new HBox();

        //////Make all the elements i want to add to the playerLayout//////////
        Label amountOfChipsText = new Label("Amount of chips:");
        amountOfChipsText.setFont(standardFont);
        amountOfChipsText.setPadding(standardPadding);

        Label amountOfChipsNumber = new Label("1000");
        amountOfChipsNumber.setFont(standardFont);
        amountOfChipsNumber.setPadding(standardPadding);

        //Positions
        Label positionsText = new Label("Position:");
        positionsText.setFont(standardFont);
        positionsText.setPadding(standardPadding);

        Label positions = new Label("BB");
        positions.setFont(standardFont);
        positions.setPadding(standardPadding);

        //Cards
        Image image = new Image("file:CardSprites/_Back.png");

        ImageView imageView1 = new ImageView();
        imageView1.setImage(image);
        imageView1.setPreserveRatio(true);
        imageView1.setFitHeight(150);

        ImageView imageView2 = new ImageView();
        imageView2.setImage(image);
        imageView2.setPreserveRatio(true);
        imageView2.setFitHeight(150);


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
        Button check = new Button("Check/Call");
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

        //Actions
        bet.setOnAction(e -> System.out.println("SetBet"));

        //Add buttons to the buttonLayout
        buttonLayout1.getChildren().addAll(bet, check, fold);
        buttonLayout2.getChildren().addAll(pot,doubleB,max);
        textLayoutHorizontallyTop.getChildren().addAll(positionsText,positions,amountOfChipsText,amountOfChipsNumber,imageView1,imageView2,betAmount);

        //Add all the elements to the playerLayout
        textLayoutVertical.getChildren().addAll(tempLabel,textLayoutHorizontallyTop);
        playerLayout.getChildren().addAll(textLayoutVertical,buttonLayout1,buttonLayout2);

        playerLayout.setAlignment(Pos.BASELINE_CENTER);

        return playerLayout;
    }

    public static GridPane makeBoardLayout(){
        //TODO: Design and implement board-layout.
        return null;
    }

    public static HBox makeOpponentLayout(){

        Image image = new Image("file:CardSprites/_Back.png");
        ImageView imageViewOpponentLeft = new ImageView();
        imageViewOpponentLeft.setImage(image);
        imageViewOpponentLeft.setPreserveRatio(true);
        imageViewOpponentLeft.setFitHeight(150);

        ImageView imageViewOpponentRight = new ImageView();
        imageViewOpponentRight.setImage(image);
        imageViewOpponentRight.setPreserveRatio(true);
        imageViewOpponentRight.setFitHeight(150);

        Label name = new Label("Name: Kake");
        Label chips = new Label("Chips: 1000");
        Label position = new Label("Position: SB");
        Label status = new Label("Status: Fold");

        HBox horizontalLayout = new HBox();
        VBox verticalLayout = new VBox();

        verticalLayout.getChildren().addAll(name, chips, position, status);
        verticalLayout.setSpacing(10);
        horizontalLayout.getChildren().addAll(imageViewOpponentLeft, imageViewOpponentRight, verticalLayout);
        horizontalLayout.setSpacing(10);
        horizontalLayout.setAlignment(Pos.BASELINE_CENTER);

        return horizontalLayout;

    }

    public static HBox makeOpponentLayoutWithTheRightAmout(){

        return null;
    }

}
