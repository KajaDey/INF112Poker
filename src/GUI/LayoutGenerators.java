package GUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

/**
 * Created by ady on 05/03/16.
 */
public class LayoutGenerators {

    public static HBox makePlayerLayout(){

        //Setting standards i want to use
        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(5,5,5,5);
        int standardButton = 100;

        /*************************************/

        HBox fullBox = new HBox();
        VBox stats = new VBox();
        VBox inputAndButtons = new VBox();
        HBox twoButtonsUnderInput = new HBox();
        VBox twoButtonsLeft = new VBox();
        VBox twoButtonsRight = new VBox();

        /***********************************/

        //////Make all the elements i want to add to the playerLayout//////////
        Label amountOfChipsText = new Label("Amount of chips: 900");
        amountOfChipsText.setFont(standardFont);
        amountOfChipsText.setPadding(standardPadding);

        //Bet this round
        Label betThisRound = new Label("Bet this round: 100");
        betThisRound.setFont(standardFont);
        betThisRound.setPadding(standardPadding);

        //Positions
        Label positionsText = new Label("Position: BB");
        positionsText.setFont(standardFont);
        positionsText.setPadding(standardPadding);

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

        /**************/

        stats.getChildren().addAll(amountOfChipsText, positionsText, betThisRound);
        stats.setAlignment(Pos.CENTER);
        twoButtonsUnderInput.getChildren().addAll(check, fold);
        inputAndButtons.getChildren().addAll(betAmount,twoButtonsUnderInput);
        inputAndButtons.setAlignment(Pos.CENTER);
        twoButtonsLeft.getChildren().addAll(bet,max);
        twoButtonsLeft.setAlignment(Pos.CENTER);
        twoButtonsRight.getChildren().addAll(doubleB,pot);
        twoButtonsRight.setAlignment(Pos.CENTER);
        fullBox.getChildren().addAll(stats,imageView1,imageView2,inputAndButtons,twoButtonsLeft,twoButtonsRight);

        /*************/

        fullBox.setAlignment(Pos.BOTTOM_CENTER);

        return fullBox;
    }

    public static GridPane makeBoardLayout(){
        //TODO: Design and implement board-layout.
        return null;
    }

    public static HBox makeOpponentLayout(String card1, String card2){

        //Branch test

        Image image1 = new Image(card1);
        ImageView imageViewOpponentLeft = new ImageView();
        imageViewOpponentLeft.setImage(image1);
        imageViewOpponentLeft.setPreserveRatio(true);
        imageViewOpponentLeft.setFitHeight(100);

        Image image2 = new Image(card2);
        ImageView imageViewOpponentRight = new ImageView();
        imageViewOpponentRight.setImage(image2);
        imageViewOpponentRight.setPreserveRatio(true);
        imageViewOpponentRight.setFitHeight(100);

        Label name = new Label("Name: Kake");
        Label chips = new Label("Chips: 1000");
        Label position = new Label("Position: SB");
        Label status = new Label("Status: Fold");

        HBox horizontalLayout = new HBox();
        VBox verticalLayout = new VBox();

        verticalLayout.getChildren().addAll(name, chips, position, status);
        verticalLayout.setSpacing(10);
        verticalLayout.setAlignment(Pos.CENTER);
        horizontalLayout.getChildren().addAll(imageViewOpponentLeft, imageViewOpponentRight, verticalLayout);
        horizontalLayout.setSpacing(10);
        horizontalLayout.setAlignment(Pos.TOP_CENTER);

        return horizontalLayout;

    }

    public static HBox makeOpponentLayoutWithTheRightAmout(){
        //TODO: Fix
        return null;
    }

    public static Scene makeScene(String card1, String card2){
        Stage window = new Stage();
        BorderPane completeLayout = new BorderPane();
        completeLayout.setPadding(new Insets(10,10,10,10));

        //Construct a new scene
        completeLayout.setBottom(LayoutGenerators.makePlayerLayout());
        completeLayout.setTop(LayoutGenerators.makeOpponentLayout(card1, card2));
        Scene scene = new Scene(completeLayout,1000,1000);

        window.setScene(scene);
        window.show();

        return scene;
    }

}
