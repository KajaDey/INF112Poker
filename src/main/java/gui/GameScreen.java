package main.java.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import main.java.gamelogic.Card;
import main.java.gamelogic.Decision;

/**
 * Created by ady on 07/03/16.
 */
public class GameScreen {

    BorderPane borderPane;
    Scene scene;
    private int playerID;

    //Labels
    private Label playerStackLabel, playerPositionLabel, playerLastMoveLabel, playerNameLabel;
    private Label opponentNameLabel, opponentStackSizeLabel, opponentPositionLabel, opponentLastMoveLabel;
    private Label currentBBLabel, currentSBLabel, nextBBLabel, nextSBLabel, potLabel;

    //ImageViews
    private ImageView playerLeftCardImage, playerRightCardImage;
    private ImageView opponentLeftCardImage, opponentRightCardImage;
    private ImageView [] communityCards = new ImageView[5];

    //Buttons
    private Button betButton, checkButton, doubleButton, foldButton, maxButton, potButton;

    //Textfields
    private TextField amountTextfield;

    public GameScreen(int ID) {
        this.playerID = ID;
        borderPane = new BorderPane();
        scene = new Scene(ImageViewer.setBackground("PokerTable", borderPane, 1920, 1080), 1280, 720);
    }

    public Scene createSceneForGameScreen(GameSettings settings) {
        borderPane.setCenter(makeBoardLayout(settings.getSmallBlind(), settings.getBigBlind()));
        return scene;
    }

    public boolean insertPlayer(int userID, String name, long stackSize, String pos) {
        if (userID == playerID) {
            //Insert player
            borderPane.setBottom(makePlayerLayout(userID, name, stackSize, pos));
        } else {
            //insert opponent
            borderPane.setTop(makeOpponentLayout(userID, name, stackSize, pos));
        }
        return true;
    }

    public void setHandForUser(int userID, Card leftCard, Card rightCard) {
        if (userID == this.playerID) {
            //Set player hand
            Image leftImage = new Image(ImageViewer.returnURLPathForCardSprites(leftCard.getCardNameForGui()));
            Image rightImage = new Image(ImageViewer.returnURLPathForCardSprites(rightCard.getCardNameForGui()));
            playerLeftCardImage.setImage(leftImage);
            playerRightCardImage.setImage(rightImage);
        } else {
            //Set opponent hand
            Image backImage = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));
            opponentLeftCardImage.setImage(backImage);
            opponentRightCardImage.setImage(backImage);
        }
    }

    /**
     * A method for making a playerLayout
     *
     * @return A VBox with the player layout
     */

    public VBox makePlayerLayout(int userID, String name, long stackSize, String pos) {

        //Setting standards i want to use
        Font standardFont = new Font("Areal", 15);
        Insets standardPadding = new Insets(5, 5, 5, 5);
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
        playerStackLabel = ObjectStandards.makeStandardLabelWhite("Amount of chips:", stackSize + "");
        playerPositionLabel = ObjectStandards.makeStandardLabelWhite("Positions:", pos);
        playerLastMoveLabel = ObjectStandards.makeStandardLabelWhite("", "");
        playerNameLabel = ObjectStandards.makeStandardLabelWhite("Name: ", name);

        playerLeftCardImage = ImageViewer.getEmptyImageView("player");
        playerRightCardImage = ImageViewer.getEmptyImageView("player");

        //Amount to betButton
        amountTextfield = new TextField();
        amountTextfield.setPromptText("Amount to betButton");
        amountTextfield.setFont(standardFont);
        amountTextfield.setPadding(standardPadding);
        amountTextfield.setMaxWidth(standardButton * 2);

        //Buttons in the VBox
        betButton = ObjectStandards.makeStandardButton("Bet");
        checkButton = ObjectStandards.makeStandardButton("Check");
        foldButton = ObjectStandards.makeStandardButton("Fold");
        potButton = ObjectStandards.makeStandardButton("Pot");
        doubleButton = ObjectStandards.makeStandardButton("Double");
        maxButton = ObjectStandards.makeStandardButton("Max");

        //Actions
        betButton.setOnAction(e -> {
            ButtonListeners.betButtonListener(amountTextfield.getText());
        });
        checkButton.setOnAction(e -> ButtonListeners.checkButtonListener());
        //doubleButton.setOnAction(e -> ButtonListeners.doubleButtonListener(amountTextfield.getText()));
        foldButton.setOnAction(e -> ButtonListeners.foldButtonListener());
        maxButton.setOnAction(e -> ButtonListeners.maxButtonListener(amountTextfield.getText()));
        potButton.setOnAction(e -> ButtonListeners.potButtonListener(amountTextfield.getText()));

        //Add objects to the boxes

        stats.getChildren().addAll(playerNameLabel, playerStackLabel, playerPositionLabel);
        stats.setAlignment(Pos.CENTER);
        twoButtonsUnderInput.getChildren().addAll(checkButton, foldButton);
        inputAndButtons.getChildren().addAll(amountTextfield, twoButtonsUnderInput);
        inputAndButtons.setAlignment(Pos.CENTER);
        twoButtonsLeft.getChildren().addAll(betButton, maxButton);
        twoButtonsLeft.setAlignment(Pos.CENTER);
        twoButtonsRight.getChildren().addAll(doubleButton, potButton);
        twoButtonsRight.setAlignment(Pos.CENTER);
        fullBox.getChildren().addAll(stats, playerLeftCardImage, playerRightCardImage, inputAndButtons, twoButtonsLeft, twoButtonsRight);
        fullBox.setAlignment(Pos.BOTTOM_CENTER);
        fullBoxWithLastMove.getChildren().addAll(playerLastMoveLabel, fullBox);
        fullBoxWithLastMove.setAlignment(Pos.BOTTOM_CENTER);

        this.setActionsVisible(false);
        return fullBoxWithLastMove;
    }

    /**
     * Generates a boardLayout
     *
     * @return a boardLayout
     */
    public HBox makeBoardLayout(int smallBlind, int bigBlind) {

        for (int i = 0; i < communityCards.length; i++) {
            communityCards[i] = ImageViewer.getEmptyImageView("player");
        }

        HBox horizontalLayout = new HBox();
        VBox verticalLayout = new VBox();

        currentBBLabel = ObjectStandards.makeStandardLabelWhite("Current BB:", bigBlind + "$");
        currentSBLabel = ObjectStandards.makeStandardLabelWhite("Current SM:", smallBlind + "$");
        nextBBLabel = ObjectStandards.makeStandardLabelWhite("Next BB: ", bigBlind * 2 + "$");
        nextSBLabel = ObjectStandards.makeStandardLabelWhite("Next SB: ", smallBlind * 2 + "$");
        potLabel = ObjectStandards.makeStandardLabelWhite("", "");

        verticalLayout.getChildren().addAll(currentBBLabel, currentSBLabel, nextBBLabel, nextSBLabel, potLabel);
        verticalLayout.setSpacing(10);
        verticalLayout.setAlignment(Pos.CENTER);

        for (ImageView card : communityCards) {
            horizontalLayout.getChildren().add(card);
        }
        horizontalLayout.getChildren().add(verticalLayout);
        horizontalLayout.setSpacing(10);
        horizontalLayout.setAlignment(Pos.CENTER);

        return horizontalLayout;
    }

    public VBox makeOpponentLayout(int userID, String name, long stackSize, String pos) {

        opponentLeftCardImage = ImageViewer.getEmptyImageView("opponent");
        opponentRightCardImage = ImageViewer.getEmptyImageView("opponent");


        opponentNameLabel = ObjectStandards.makeStandardLabelWhite("Name:", name);
        opponentStackSizeLabel = ObjectStandards.makeStandardLabelWhite("Chips:", stackSize + "");
        opponentPositionLabel = ObjectStandards.makeStandardLabelWhite("Position:", pos);
        opponentLastMoveLabel = ObjectStandards.makeStandardLabelWhite("", "");

        HBox horizontalLayout = new HBox();
        VBox verticalLayout = new VBox();
        VBox fullBox = new VBox();

        verticalLayout.getChildren().addAll(opponentNameLabel, opponentStackSizeLabel, opponentPositionLabel);
        verticalLayout.setSpacing(5);
        verticalLayout.setAlignment(Pos.CENTER);
        horizontalLayout.getChildren().addAll(opponentLeftCardImage, opponentRightCardImage, verticalLayout);
        horizontalLayout.setSpacing(10);
        horizontalLayout.setAlignment(Pos.TOP_CENTER);
        fullBox.getChildren().addAll(horizontalLayout, opponentLastMoveLabel);
        fullBox.setAlignment(Pos.BOTTOM_CENTER);

        return fullBox;

    }

    public void displayFlop(Card card1, Card card2, Card card3) {

        Image card1Image = new Image(ImageViewer.returnURLPathForCardSprites(card1.getCardNameForGui()));
        Image card2Image = new Image(ImageViewer.returnURLPathForCardSprites(card2.getCardNameForGui()));
        Image card3Image = new Image(ImageViewer.returnURLPathForCardSprites(card3.getCardNameForGui()));

        communityCards[0].setImage(card1Image);
        communityCards[1].setImage(card2Image);
        communityCards[2].setImage(card3Image);
    }

    public void displayTurn(Card turn) {
        Image turnImage = new Image(ImageViewer.returnURLPathForCardSprites(turn.getCardNameForGui()));
        communityCards[3].setImage(turnImage);
    }

    public void displayRiver(Card river) {
        Image riverImage = new Image(ImageViewer.returnURLPathForCardSprites(river.getCardNameForGui()));
        communityCards[4].setImage(riverImage);
    }

    public void setActionsVisible(boolean visible) {
        betButton.setVisible(visible);
        checkButton.setVisible(visible);
        doubleButton.setVisible(visible);
        foldButton.setVisible(visible);
        maxButton.setVisible(visible);
        potButton.setVisible(visible);

        amountTextfield.setVisible(visible);
    }

    public void playerMadeDecision(int ID, Decision decision) {
        Runnable task;
        if (ID == this.playerID) {
            task = () -> playerLastMoveLabel.setText(decision.toString());
        } else {
            task = () -> opponentLastMoveLabel.setText(decision.toString());
        }
        Platform.runLater(task);
    }

}
