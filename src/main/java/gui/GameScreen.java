package main.java.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Created by ady on 07/03/16.
 */
public class GameScreen {

    public static Scene createSceneForGameScreen(GUIClient client) {

        BorderPane borderPane = new BorderPane();

        borderPane.setTop(makeOpponentLayout(client));
        borderPane.setCenter(makeBoardLayout(client));
        borderPane.setBottom(makePlayerLayout(client));

        Scene scene = new Scene(ImageViewer.setBackground("PokerTable", borderPane, 1920, 1080), 1280, 720);

        SceneBuilder.showCurrentScene(scene, "GameScreen");

        return scene;
    }

        /**
         * A method for making a playerLayout
         *
         * @return A VBox with the player layout
         */

        public static VBox makePlayerLayout(GUIClient client){

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
            Label amountOfChipsText = ObjectStandards.makeStandardLabelWhite("Amount of chips:", client.getStackSizes().get(client.getId())+"");
            Label positionsText = ObjectStandards.makeStandardLabelWhite("Positions:", client.getPosition().get(client.getId())+"");
            Label lastMove = ObjectStandards.makeStandardLabelWhite("Fold", "");
            Label name = ObjectStandards.makeStandardLabelWhite("Name: ",client.getName().get(client.getId()));

            ImageView imageView1 = ImageViewer.setCardImage("player",client.getCard1().getCardNameForGui());
            ImageView imageView2 = ImageViewer.setCardImage("player",client.getCard2().getCardNameForGui());

            //Amount to bet
            TextField betAmount = new TextField();
            betAmount.setPromptText("Amount to bet");
            betAmount.setFont(standardFont);
            betAmount.setPadding(standardPadding);
            betAmount.setMaxWidth(standardButton * 2);

            //Buttons in the VBox
            Button bet = ObjectStandards.makeStandardButton("Bet");
            Button check = ObjectStandards.makeStandardButton("Check");
            Button fold = ObjectStandards.makeStandardButton("Fold");
            Button pot = ObjectStandards.makeStandardButton("Pot");
            Button doubleB = ObjectStandards.makeStandardButton("Double");
            Button max = ObjectStandards.makeStandardButton("Max");

            //Actions
            bet.setOnAction(e -> client.betButtonListener());
            check.setOnAction(e -> client.checkButtonListener());
            doubleB.setOnAction(e -> client.doubleButtonListener());
            fold.setOnAction(e -> client.foldButtonListener());
            max.setOnAction(e -> client.maxButtonListener());
            pot.setOnAction(e -> client.potButtonListener());

            //Add objects to the boxes

            stats.getChildren().addAll(name,amountOfChipsText, positionsText);
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
        public static HBox makeBoardLayout(GUIClient client){

            ImageView card1 = ImageViewer.setCardImage("player","_Back");
            ImageView card2 = ImageViewer.setCardImage("player","_Back");
            ImageView card3 = ImageViewer.setCardImage("player","_Back");
            ImageView card4 = ImageViewer.setCardImage("player","_Back");
            ImageView card5 = ImageViewer.setCardImage("player","_Back");

            HBox horizontalLayout = new HBox();
            VBox verticalLayout = new VBox();

            Label currentBB = ObjectStandards.makeStandardLabelWhite("Current BB:",client.getBigBlind() + "$");
            Label currentSB = ObjectStandards.makeStandardLabelWhite("Current SM:",client.getSmallBlind() + "$");
            Label nextBB = ObjectStandards.makeStandardLabelWhite("Next BB: ",client.getBigBlind()*1.5 + "$");
            Label nextSB = ObjectStandards.makeStandardLabelWhite("Next SB: ",client.getSmallBlind()*1.5 + "$");
            Label pot = ObjectStandards.makeStandardLabelWhite("Pot: ", client.getPot() + "$");

            verticalLayout.getChildren().addAll(currentBB, currentSB, nextBB, nextSB, pot);
            verticalLayout.setSpacing(10);
            verticalLayout.setAlignment(Pos.CENTER);
            horizontalLayout.getChildren().addAll(card1, card2, card3, card4, card5, verticalLayout);
            horizontalLayout.setSpacing(10);
            horizontalLayout.setAlignment(Pos.CENTER);

            return horizontalLayout;
        }

        public static VBox makeOpponentLayout(GUIClient client){

            ImageView imageViewOpponentLeft = ImageViewer.setCardImage("opponent", client.getCard1().getCardNameForGui());
            ImageView imageViewOpponentRight = ImageViewer.setCardImage("opponent", client.getCard2().getCardNameForGui());

            Label name = ObjectStandards.makeStandardLabelWhite("Name:", client.getName().get(1));
            Label chips = ObjectStandards.makeStandardLabelWhite("Chips:", client.getStackSizes().get(1)+"");
            Label position = ObjectStandards.makeStandardLabelWhite("Position:", client.getPosition().get(1) + "");
            Label status = ObjectStandards.makeStandardLabelWhite("Bet","100");

            HBox horizontalLayout = new HBox();
            VBox verticalLayout = new VBox();
            VBox fullBox = new VBox();

            verticalLayout.getChildren().addAll(name, chips, position);
            verticalLayout.setSpacing(5);
            verticalLayout.setAlignment(Pos.CENTER);
            horizontalLayout.getChildren().addAll(imageViewOpponentLeft, imageViewOpponentRight, verticalLayout);
            horizontalLayout.setSpacing(10);
            horizontalLayout.setAlignment(Pos.TOP_CENTER);
            fullBox.getChildren().addAll(horizontalLayout,status);
            fullBox.setAlignment(Pos.BOTTOM_CENTER);

            return fullBox;

        }

        /*public static Scene makeSceneForOpponentCards(GUIClient client){
            BorderPane completeLayout = new BorderPane();
            completeLayout.setPadding(new Insets(10, 10, 10, 10));

            //Construct a new scene
            completeLayout.setBottom(makePlayerLayout(client));
            completeLayout.setTop(makeOpponentLayout(client));

            Scene scene = new Scene(completeLayout,1000,1000);


            return scene;
        }*/

}
