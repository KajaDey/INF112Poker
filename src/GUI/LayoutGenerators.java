package GUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Created by ady on 05/03/16.
 */
public class LayoutGenerators {

    /**
     * A method for making a playerLayout
     *
     * @return A VBox with the player layout
     */

    public static VBox makePlayerLayout(){

        //Setting standards i want to use
        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(5,5,5,5);
        int standardButton = 75;

        //Make ALL the boxes
        HBox fullBox = new HBox();
        VBox fullBoxWithLastMove = new VBox();
        VBox stats = new VBox();
        VBox inputAndButtons = new VBox();
        HBox twoButtonsUnderInput = new HBox();
        twoButtonsUnderInput.setMaxWidth(50);
        VBox twoButtonsLeft = new VBox();
        VBox twoButtonsRight = new VBox();


        //////Make all the elements i want to add to the playerLayout//////////
        Label amountOfChipsText = makeStandardLabel("Amount of chips:","900");
        Label positionsText = makeStandardLabel("Positions:","BB");
        Label lastMove = makeStandardLabel("Fold","");

        ImageView imageView1 = ImageViewer.setCardImage("player","Spades 12");
        ImageView imageView2 = ImageViewer.setCardImage("player","Clubs 5");

        //Amount to bet
        TextField betAmount = new TextField();
        betAmount.setPromptText("Amount to bet");
        betAmount.setFont(standardFont);
        betAmount.setPadding(standardPadding);
        betAmount.setMaxWidth(standardButton * 2);

        //Buttons in the VBox

        Button bet = makeStandardButton("Bet");
        Button check = makeStandardButton("Check");
        Button fold = makeStandardButton("Fold");
        Button pot = makeStandardButton("Pot");
        Button doubleB = makeStandardButton("Double");
        Button max = makeStandardButton("Max");

        //Actions
        bet.setOnAction(e -> ButtonListeners.betButtonListener());
        check.setOnAction(e -> ButtonListeners.checkButtonListener());
        doubleB.setOnAction(e -> ButtonListeners.doubleButtonListener());
        fold.setOnAction(e -> ButtonListeners.foldButtonListener());
        max.setOnAction(e -> ButtonListeners.maxButtonListener());
        pot.setOnAction(e -> ButtonListeners.potButtonListener());

        //Add objects to the boxes

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

        return fullBoxWithLastMove;
    }

    /**
     * Generates a boardLayout
     * @return a boardLayout
     */
    public static HBox makeBoardLayout(){

        ImageView card1 = ImageViewer.setCardImage("player","Diamonds 10");
        ImageView card2 = ImageViewer.setCardImage("player","Diamonds 11");
        ImageView card3 = ImageViewer.setCardImage("player","Diamonds 12");
        ImageView card4 = ImageViewer.setCardImage("player","Diamonds 13");
        ImageView card5 = ImageViewer.setCardImage("player","Diamonds 1");

        HBox horizontalLayout = new HBox();
        VBox verticalLayout = new VBox();

        verticalLayout.setSpacing(10);
        horizontalLayout.getChildren().addAll(card1, card2, card3, card4, card5, verticalLayout);
        horizontalLayout.setSpacing(10);
        horizontalLayout.setAlignment(Pos.CENTER);

        return horizontalLayout;
    }

    public static VBox makeOpponentLayout(String card1, String card2){

        ImageView imageViewOpponentLeft = ImageViewer.setCardImage("opponent", card1);
        ImageView imageViewOpponentRight = ImageViewer.setCardImage("opponent", card2);

        Label name = makeStandardLabel("Name:","Kake");
        Label chips = makeStandardLabel("Chips:","1000");
        Label position = makeStandardLabel("Position:","SB");
        Label status = makeStandardLabel("Bet","100");

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

    /**
     * A template for a button
     *
     * @param name
     * @return a new button
     */
    public static Button makeStandardButton(String name){
        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(5,5,5,5);
        int standardMinWidth = 75;

        Button button = new Button(name);
        button.setFont(standardFont);
        button.setPadding(standardPadding);
        button.setMinWidth(standardMinWidth);

        return button;
    }

    /**
     * A template for a label
     *
     * @param name
     * @param value
     * @return a new label
     */
    public static Label makeStandardLabel(String name, String value){
        Label label = new Label(name + " " + value);
        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(5,5,5,5);

        label.setFont(standardFont);
        label.setPadding(standardPadding);

        return label;
    }
}
