package GUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Created by ady on 05/03/16.
 */
public class LayoutGenerators {

    public static VBox makePlayerLayout(){

        //Setting standards i want to use
        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(5,5,5,5);
        int standardButton = 75;

        /*************************************/

        HBox fullBox = new HBox();
        VBox fullBoxWithLastMove = new VBox();
        VBox stats = new VBox();
        VBox inputAndButtons = new VBox();
        HBox twoButtonsUnderInput = new HBox();
        twoButtonsUnderInput.setMaxWidth(50);
        VBox twoButtonsLeft = new VBox();
        VBox twoButtonsRight = new VBox();

        /***********************************/

        //////Make all the elements i want to add to the playerLayout//////////
        Label amountOfChipsText = new Label("Amount of chips: 900");
        amountOfChipsText.setFont(standardFont);
        amountOfChipsText.setPadding(standardPadding);

        //Positions
        Label positionsText = new Label("Position: BB");
        positionsText.setFont(standardFont);
        positionsText.setPadding(standardPadding);

        //LastMove
        Label lastMove = new Label("Fold");
        lastMove.setFont(standardFont);
        positionsText.setPadding(standardPadding);

        //Cards
        Image image1 = new Image("file:CardSprites/Diamonds 12.png");

        ImageView imageView1 = new ImageView();
        imageView1.setImage(image1);
        imageView1.setPreserveRatio(true);
        imageView1.setFitHeight(130);

        //Image image2 = new Image(card2);

        ImageView imageView2 = new ImageView();
        imageView2.setImage(image1);
        imageView2.setPreserveRatio(true);
        imageView2.setFitHeight(130);


        //Amount to bet
        TextField betAmount = new TextField();
        betAmount.setPromptText("Amount to bet");
        betAmount.setFont(standardFont);
        betAmount.setPadding(standardPadding);
        betAmount.setMaxWidth(standardButton * 2);

        //Buttons in the VBox

        //Bet
        Button bet = new Button("Place bet");
        bet.setFont(standardFont);
        bet.setPadding(standardPadding);
        bet.setMinWidth(standardButton);

        //Check
        Button check = new Button("Check/Call");
        check.setFont(standardFont);
        check.setPadding(standardPadding);
        check.setMinWidth(standardButton);

        //Fold
        Button fold = new Button("Fold");
        fold.setFont(standardFont);
        fold.setPadding(standardPadding);
        fold.setMinWidth(standardButton);

        //Pot
        Button pot = new Button("Pot");
        pot.setFont(standardFont);
        pot.setPadding(standardPadding);
        pot.setMinWidth(standardButton);

        //Double
        Button doubleB = new Button("Double");
        doubleB.setFont(standardFont);
        doubleB.setPadding(standardPadding);
        doubleB.setMinWidth(standardButton);

        //Max
        Button max = new Button("Max");
        max.setFont(standardFont);
        max.setPadding(standardPadding);
        max.setMinWidth(standardButton);

        /////////////////////////////////////////

        //Actions
        bet.setOnAction(e -> System.out.println("betClick"));
        check.setOnAction(e -> System.out.println("checkClick"));
        fold.setOnAction(e -> System.out.println("foldClick"));
        pot.setOnAction(e -> System.out.println("potClick"));
        doubleB.setOnAction(e -> System.out.println("doubleClick"));
        max.setOnAction(e -> System.out.println("maxClick"));


        /**************/

        stats.getChildren().addAll(amountOfChipsText, positionsText);
        stats.setAlignment(Pos.CENTER);
        twoButtonsUnderInput.getChildren().addAll(check, fold);
        inputAndButtons.getChildren().addAll(betAmount, twoButtonsUnderInput);
        inputAndButtons.setAlignment(Pos.CENTER);
        twoButtonsLeft.getChildren().addAll(bet, max);
        twoButtonsLeft.setAlignment(Pos.CENTER);
        twoButtonsRight.getChildren().addAll(doubleB, pot);
        twoButtonsRight.setAlignment(Pos.CENTER);
        fullBox.getChildren().addAll(stats, imageView1, imageView2, inputAndButtons, twoButtonsLeft, twoButtonsRight);
        fullBox.setAlignment(Pos.BOTTOM_CENTER);
        fullBoxWithLastMove.getChildren().addAll(lastMove, fullBox);
        fullBoxWithLastMove.setAlignment(Pos.BOTTOM_CENTER);

        /*************/



        return fullBoxWithLastMove;
    }

    public static GridPane makeBoardLayout(){
        //TODO: Design and implement board-layout.
        return null;
    }

    public static VBox makeOpponentLayout(String card1, String card2){

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
        Label status = new Label("Bet 10000");

        HBox horizontalLayout = new HBox();
        VBox verticalLayout = new VBox();
        VBox fullBox = new VBox();

        verticalLayout.getChildren().addAll(name, chips, position);
        verticalLayout.setSpacing(10);
        verticalLayout.setAlignment(Pos.CENTER);
        horizontalLayout.getChildren().addAll(imageViewOpponentLeft, imageViewOpponentRight, verticalLayout);
        horizontalLayout.setSpacing(10);
        horizontalLayout.setAlignment(Pos.TOP_CENTER);
        fullBox.getChildren().addAll(horizontalLayout,status);
        fullBox.setAlignment(Pos.BOTTOM_CENTER);


        return fullBox;

    }

    public static HBox makeOpponentLayoutWithTheRightAmout(){
        //TODO: Fix
        return null;
    }

    public static Scene makeSceneForOpponentCards(String card1, String card2){
        Stage window = new Stage();
        BorderPane completeLayout = new BorderPane();
        completeLayout.setPadding(new Insets(10,10,10,10));

        //Construct a new scene
        completeLayout.setBottom(LayoutGenerators.makePlayerLayout());
        completeLayout.setTop(LayoutGenerators.makeOpponentLayout(card1, card2));
        /*Image image = new Image("CardSprites/_Info.png");
        BackgroundImage backgroundImage = new BackgroundImage(image, null, null, null, null);
        Background background = new Background(backgroundImage);
        completeLayout.setBackground(background);*/

        Scene scene = new Scene(completeLayout,1000,1000);

        window.setScene(scene);
        window.show();


        return scene;
    }

    /*public static Scene makeSceneForPlayerCards(String card1, String card2){
        Stage window = new Stage();
        BorderPane completeLayout = new BorderPane();
        completeLayout.setPadding(new Insets(10,10,10,10));

        //Construct a new scene
        completeLayout.setBottom(LayoutGenerators.makePlayerLayout(card1, card2));
        //completeLayout.setTop(LayoutGenerators.makeOpponentLayout(card1, card2));
        Scene scene = new Scene(completeLayout,1000,1000);

        window.setScene(scene);
        window.show();

        return scene;
    }*/

}
