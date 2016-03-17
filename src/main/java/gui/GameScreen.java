package gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import gamelogic.Card;
import gamelogic.Decision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ady on 07/03/16.
 */
public class GameScreen {

    BorderPane borderPane;
    Scene scene;
    private int playerID;
    private int numberOfPlayers = 0;

    //Labels
    private Label playerStackLabel, playerPositionLabel, playerLastMoveLabel, playerNameLabel;
    private Label opponentNameLabel, opponentStackSizeLabel, opponentPositionLabel, opponentLastMoveLabel;
    private Label currentBBLabel, currentSBLabel, nextBBLabel, nextSBLabel, potLabel, winnerLabel;
    private Label endGameScreen;

    //ImageViews
    private ImageView playerLeftCardImage, playerRightCardImage;
    private ImageView opponentLeftCardImage, opponentRightCardImage;
    private ImageView[] communityCards = new ImageView[5];

    //Buttons
    private Button betRaiseButton, checkCallButton, foldButton;

    //Textfields
    private TextField amountTextfield;

    //Storagevariables
    private long highestAmountPutOnTable = 0, pot = 0;
    private long currentSmallBlind, currentBigBlind;
    private Map<Integer, String> names = new HashMap<>();
    private Map<Integer, Long> stackSizes = new HashMap<>();
    private Map<Integer, Long> putOnTable = new HashMap<>();

    public GameScreen(int ID) {
        this.playerID = ID;
        borderPane = new BorderPane();
        scene = new Scene(ImageViewer.setBackground("PokerTable", borderPane, 1920, 1080), 1280, 720);
    }

    /**
     * Creates the game screen
     *
     * @param settings
     * @return a scene containing a gamscreen
     */

    public Scene createSceneForGameScreen(GameSettings settings) {
        borderPane.setCenter(makeBoardLayout(settings.getSmallBlind(), settings.getBigBlind()));
        return scene;
    }

    /**
     * Insert players to the screen
     *
     * @param userID
     * @param name
     * @param stackSize
     * @return player objects
     */

    public boolean insertPlayer(int userID, String name, long stackSize) {
        this.names.put(userID, name);
        this.stackSizes.put(userID, stackSize);
        if (userID == playerID) {
            //Insert player
            borderPane.setBottom(makePlayerLayout(userID, name, stackSize));
        } else {
            //insert opponent
            borderPane.setTop(makeOpponentLayout(userID, name, stackSize));
        }
        this.numberOfPlayers++;
        return true;
    }

    /**
     * Displays the card pictures to the screen
     *
     * @param userID
     * @param leftCard
     * @param rightCard
     */

    public void setHandForUser(int userID, Card leftCard, Card rightCard) {
        DropShadow dropShadow = new DropShadow();
        //Images
        Image leftImage = new Image(ImageViewer.returnURLPathForCardSprites(leftCard.getCardNameForGui()));
        Image rightImage = new Image(ImageViewer.returnURLPathForCardSprites(rightCard.getCardNameForGui()));
        Image backImage = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));

        Runnable task = () -> {
            playerLeftCardImage.setImage(leftImage);
            playerRightCardImage.setImage(rightImage);

            playerLeftCardImage.setEffect(dropShadow);
            playerRightCardImage.setEffect(dropShadow);

            playerLeftCardImage.setVisible(true);
            playerRightCardImage.setVisible(true);
            //Set opponent hand
            opponentLeftCardImage.setImage(backImage);
            opponentRightCardImage.setImage(backImage);

            opponentLeftCardImage.setEffect(dropShadow);
            opponentRightCardImage.setEffect(dropShadow);

            opponentLeftCardImage.setVisible(true);
            opponentRightCardImage.setVisible(true);

        };
        Platform.runLater(task);
    }

    /**
     * A method for making a playerLayout
     *
     * @return A VBox with the player layout
     */
    public VBox makePlayerLayout(int userID, String name, long stackSize) {
        //Make ALL the boxes
        HBox fullBox = new HBox();
        VBox fullBoxWithLastMove = new VBox();
        VBox stats = new VBox();
        VBox inputAndButtons = new VBox();
        HBox twoButtonsUnderInput = new HBox();
        twoButtonsUnderInput.setMaxWidth(50);
        VBox twoButtonsLeft = new VBox();

        //////Make all the elements i want to add to the playerLayout//////////
        playerStackLabel = ObjectStandards.makeStandardLabelWhite("Stack size:", stackSize + "");
        playerPositionLabel = ObjectStandards.makeStandardLabelWhite("Position: ", "");
        playerLastMoveLabel = ObjectStandards.makeStandardLabelWhite("", "");
        playerNameLabel = ObjectStandards.makeStandardLabelWhite("Name: ", name);


        Image backOfCards = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));

        playerLeftCardImage = ImageViewer.getEmptyImageView("player");
        playerRightCardImage = ImageViewer.getEmptyImageView("player");
        playerLeftCardImage.setImage(backOfCards);
        playerRightCardImage.setImage(backOfCards);
        playerLeftCardImage.setVisible(false);
        playerRightCardImage.setVisible(false);

        amountTextfield = ObjectStandards.makeTextFieldForGameScreen("Amount");

        //Buttons in the VBox
        checkCallButton = ObjectStandards.makeStandardButton("Check");
        foldButton = ObjectStandards.makeStandardButton("Fold");
        betRaiseButton = ObjectStandards.makeStandardButton("Bet");
        betRaiseButton.setMinHeight(66);

        //Actions
        betRaiseButton.setOnAction(e -> {
            if(!amountTextfield.getText().equals(""))
            ButtonListeners.betButtonListener(amountTextfield.getText(), betRaiseButton.getText());
        });

        amountTextfield.setOnAction(e -> {
            if (!amountTextfield.getText().equals(""))
                ButtonListeners.betButtonListener(amountTextfield.getText(), betRaiseButton.getText());
        });
        checkCallButton.setOnAction(e -> ButtonListeners.checkButtonListener(checkCallButton.getText()));
        foldButton.setOnAction(e -> ButtonListeners.foldButtonListener());

        //Add objects to the boxes
        stats.getChildren().addAll(playerNameLabel, playerStackLabel, playerPositionLabel);
        stats.setAlignment(Pos.CENTER);

        twoButtonsUnderInput.getChildren().addAll(checkCallButton, foldButton);

        inputAndButtons.getChildren().addAll(amountTextfield, twoButtonsUnderInput);
        inputAndButtons.setAlignment(Pos.CENTER);

        twoButtonsLeft.getChildren().addAll(betRaiseButton);
        twoButtonsLeft.setAlignment(Pos.CENTER);

        fullBox.getChildren().addAll(stats, playerLeftCardImage, playerRightCardImage, inputAndButtons, twoButtonsLeft);
        fullBox.setAlignment(Pos.CENTER);

        fullBoxWithLastMove.getChildren().addAll(playerLastMoveLabel, fullBox);
        fullBoxWithLastMove.setAlignment(Pos.CENTER);

        this.setActionsVisible(false);
        return fullBoxWithLastMove;
    }

    /**
     * Generates a boardLayout
     *
     * @return a boardLayout
     */
    public VBox makeBoardLayout(long smallBlind, long bigBlind) {
        DropShadow dropShadow = new DropShadow();
        this.currentSmallBlind = smallBlind;
        this.currentBigBlind = bigBlind;

        for (int i = 0; i < communityCards.length; i++) {
            communityCards[i] = ImageViewer.getEmptyImageView("player");
            communityCards[i].setEffect(dropShadow);
        }

        HBox cardLayout = new HBox();
        VBox statsLayout = new VBox();
        VBox fullLayout = new VBox();

        currentBBLabel = ObjectStandards.makeStandardLabelWhite("Current BB:", bigBlind + "$");
        currentSBLabel = ObjectStandards.makeStandardLabelWhite("Current SM:", smallBlind + "$");
        nextBBLabel = ObjectStandards.makeStandardLabelWhite("Next BB: ", bigBlind * 2 + "$");
        nextSBLabel = ObjectStandards.makeStandardLabelWhite("Next SB: ", smallBlind * 2 + "$");
        potLabel = ObjectStandards.makeStandardLabelWhite("", "");
        winnerLabel = ObjectStandards.makeStandardLabelWhite("", "");

        statsLayout.getChildren().addAll(currentBBLabel, currentSBLabel, nextBBLabel, nextSBLabel, potLabel);
        statsLayout.setSpacing(10);
        statsLayout.setAlignment(Pos.CENTER);

        cardLayout.getChildren().add(statsLayout);
        cardLayout.getChildren().addAll(communityCards);

        cardLayout.setSpacing(10);
        cardLayout.setAlignment(Pos.CENTER);

        fullLayout.getChildren().setAll(cardLayout, winnerLabel);
        fullLayout.setAlignment(Pos.CENTER);


        return fullLayout;
    }

    /**
     * Makes the layout for the opponentScreen
     *
     * @param userID
     * @param name
     * @param stackSize
     * @return a layout
     */
    public VBox makeOpponentLayout(int userID, String name, long stackSize) {
        opponentLeftCardImage = ImageViewer.getEmptyImageView("opponent");
        opponentRightCardImage = ImageViewer.getEmptyImageView("opponent");

        Image backOfCards = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));

        opponentLeftCardImage.setImage(backOfCards);
        opponentRightCardImage.setImage(backOfCards);
        opponentLeftCardImage.setVisible(false);
        opponentRightCardImage.setVisible(false);

        opponentNameLabel = ObjectStandards.makeStandardLabelWhite("Name:", name);
        opponentStackSizeLabel = ObjectStandards.makeStandardLabelWhite("Stack size:", stackSize + "");
        opponentPositionLabel = ObjectStandards.makeStandardLabelWhite("Position: ","");
        opponentLastMoveLabel = ObjectStandards.makeStandardLabelWhite("", "");

        HBox cardsAndStats = new HBox();
        VBox opponentStats = new VBox();
        VBox fullBox = new VBox();

        opponentStats.getChildren().addAll(opponentNameLabel, opponentStackSizeLabel, opponentPositionLabel);
        opponentStats.setSpacing(5);
        opponentStats.setAlignment(Pos.CENTER);
        cardsAndStats.getChildren().addAll(opponentLeftCardImage, opponentRightCardImage, opponentStats);
        cardsAndStats.setSpacing(10);
        cardsAndStats.setAlignment(Pos.CENTER);
        fullBox.getChildren().addAll(cardsAndStats, opponentLastMoveLabel);
        fullBox.setAlignment(Pos.CENTER);

        return fullBox;
    }

    /**
     * Shows the cards of the players around the table
     *
     * @param stillPlaying The players who are still in the game
     * @param winnerID     The winner of the game
     */
    public void showDown(List<Integer> stillPlaying, int winnerID, Map<Integer, Card[]> holeCards) {
        Card[] cards;

        for (Integer i : stillPlaying) {
            cards = holeCards.get(i);
            Image leftImage = new Image(ImageViewer.returnURLPathForCardSprites(cards[0].getCardNameForGui()));
            Image rightImage = new Image(ImageViewer.returnURLPathForCardSprites(cards[1].getCardNameForGui()));
            Runnable task = () -> {
                if (i != playerID) {
                    opponentLeftCardImage.setImage(leftImage);
                    opponentRightCardImage.setImage(rightImage);
                }
            };
            Platform.runLater(task);

            showWinner(names.get(winnerID), pot);
        }
    }

    /**
     * Displays the flop on the screen
     *
     * @param card1
     * @param card2
     * @param card3
     */

    public void displayFlop(Card card1, Card card2, Card card3) {
        Image card1Image = new Image(ImageViewer.returnURLPathForCardSprites(card1.getCardNameForGui()));
        Image card2Image = new Image(ImageViewer.returnURLPathForCardSprites(card2.getCardNameForGui()));
        Image card3Image = new Image(ImageViewer.returnURLPathForCardSprites(card3.getCardNameForGui()));

        Runnable task = () -> {
            communityCards[0].setImage(card1Image);
            communityCards[0].setVisible(true);
            communityCards[1].setImage(card2Image);
            communityCards[1].setVisible(true);
            communityCards[2].setImage(card3Image);
            communityCards[2].setVisible(true);
        };
        Platform.runLater(task);
    }

    /**
     * Displays the fourth card on the board
     *
     * @param turn
     */

    public void displayTurn(Card turn) {
        Image turnImage = new Image(ImageViewer.returnURLPathForCardSprites(turn.getCardNameForGui()));
        Runnable task = () -> {
            communityCards[3].setImage(turnImage);
            communityCards[3].setVisible(true);
        };
        Platform.runLater(task);
    }

    /**
     * Displays the fifth card on the board
     *
     * @param river
     */

    public void displayRiver(Card river) {
        Image riverImage = new Image(ImageViewer.returnURLPathForCardSprites(river.getCardNameForGui()));
        Runnable task = () -> {
            communityCards[4].setImage(riverImage);
            communityCards[4].setVisible(true);
        };
        Platform.runLater(task);
    }

    /**
     * Show the buttons on the board
     *
     * @param visible
     */

    public void setActionsVisible(boolean visible) {
        Runnable task = () -> {
            betRaiseButton.setVisible(visible);
            checkCallButton.setVisible(visible);
            foldButton.setVisible(visible);
            amountTextfield.setVisible(visible);
        };
        Platform.runLater(task);
    }

    /**
     * Update buttons and show any players last move
     *
     * @param ID
     * @param decision
     */
    public void playerMadeDecision(int ID, Decision decision) {
        long newStackSize = stackSizes.get(ID);
        String decisionText = decision.move.toString() + " ";
        String checkCallButtonText = "Call";

        switch (decision.move) {
            case CALL:
                newStackSize -= Math.min(stackSizes.get(ID), (highestAmountPutOnTable - putOnTable.get(ID)));
                putOnTable.put(ID,Math.min(stackSizes.get(ID),highestAmountPutOnTable));
                break;

            case BET:
                putOnTable.put(ID, decision.size);
                newStackSize -= decision.size;
                decisionText += (highestAmountPutOnTable = decision.size);
                setAmountTextfield(highestAmountPutOnTable *2 + "");
                break;
            case RAISE:
                long theCall = highestAmountPutOnTable - putOnTable.get(ID);
                newStackSize -= (theCall + decision.size);
                decisionText += (highestAmountPutOnTable += decision.size);
                setAmountTextfield((highestAmountPutOnTable + decision.size) + "");
                putOnTable.put(ID, highestAmountPutOnTable);
                break;
            case BIG_BLIND:
                newStackSize -= decision.size;
                decisionText += (highestAmountPutOnTable = decision.size);
                setAmountTextfield("" + currentBigBlind * 2);
                if (ID == playerID) { checkCallButtonText = "Check"; }
                putOnTable.put(ID, highestAmountPutOnTable);
                break;
            case SMALL_BLIND:
                newStackSize -= decision.size;
                decisionText += (decision.size);
                setAmountTextfield("" + currentBigBlind * 2);
                putOnTable.put(ID, decision.size);
                break;
            case ALL_IN:
                if (putOnTable.get(ID) + stackSizes.get(ID) >= highestAmountPutOnTable) { //If raise is valid
                    highestAmountPutOnTable = putOnTable.get(ID) + stackSizes.get(ID);
                }
                putOnTable.put(ID, putOnTable.get(ID) + stackSizes.get(ID));
                newStackSize = 0;
                break;
        }

        stackSizes.put(ID, newStackSize);

        //Set button texts depending on the action
        String finalText = checkCallButtonText;
        switch (decision.move) {
            case BET:case RAISE:case BIG_BLIND:case SMALL_BLIND:
                Runnable task = () -> {
                    checkCallButton.setText(finalText);
                    betRaiseButton.setText("Raise to");
                };
                Platform.runLater(task);
                break;
        }

        setErrorStateOfAmountTextfield(false);
        final String finalDecision = decisionText;
        final String stackSizeText = "Stack size: " + newStackSize;

        Runnable task;
        if (ID == this.playerID) {
            task = () -> {
                playerLastMoveLabel.setText(finalDecision);
                playerStackLabel.setText(stackSizeText);
            };
        } else {
            task = () -> {
                opponentLastMoveLabel.setText(finalDecision);
                opponentStackSizeLabel.setText(stackSizeText);
            };
        }
        Platform.runLater(task);
    }

    /**
     * Updates the stack size for all the players
     *
     * @param stackSizes
     */

    public void updateStackSizes(Map<Integer, Long> stackSizes) {
        for (Integer clientID : stackSizes.keySet()) {
            this.stackSizes = stackSizes;
            String stackSizeText = "" + stackSizes.get(clientID);

            Runnable task;
            if (clientID == playerID) {
                task = () -> this.playerStackLabel.setText("Amount of chips: " + stackSizeText);
            } else {
                task = () -> this.opponentStackSizeLabel.setText("Amount of chips: " + stackSizeText);
            }
            Platform.runLater(task);
        }
    }

    /**
     * Starting a new betting round an resets buttons
     *
     * @param potSize
     */

    public void newBettingRound(long potSize) {
        setPot(potSize);

        //Reset everything people have put on the table
        for (Integer i : putOnTable.keySet())
            putOnTable.put(i, 0L);
        highestAmountPutOnTable = 0;

        try {
            Thread.sleep(1500L);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Runnable task = () -> {
            this.highestAmountPutOnTable = 0;
            this.playerLastMoveLabel.setText("");
            this.opponentLastMoveLabel.setText("");
            checkCallButton.setText("Check");
            betRaiseButton.setText("Bet");
            this.setAmountTextfield(currentBigBlind+"");
            this.setErrorStateOfAmountTextfield(false);
        };
        Platform.runLater(task);
    }

    /**
     * Set the pot label
     *
     * @param pot
     */
    public void setPot(long pot) {
        this.pot = pot;
        String potString = Long.toString(pot);

        Runnable task = () -> potLabel.setText("Pot: " + potString);
        Platform.runLater(task);
    }

    /**
     * Set name to all the players
     *
     * @param names
     */
    public void setNames(Map<Integer, String> names) {
        this.names = names;
        Runnable task = () -> {
            playerNameLabel.setText("Name: " + names.get(playerID));
            opponentNameLabel.setText("Name: " + names.get(1));
        };
        Platform.runLater(task);
    }

    /**
     * Start a new hard
     */

    public void startNewHand() {
        Image image = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));
        Runnable task = () -> {
            for (ImageView imageview : communityCards) {
                imageview.setImage(image);
                imageview.setVisible(false);
            }
            winnerLabel.setText("");
        };
        Platform.runLater(task);
        setPot(0);
    }

    /**
     * Displays the winner of the round
     *
     * @param winnerName The name of the winner
     * @param pot        The pot that the winner won
     */
    public void showWinner(String winnerName, long pot) {
        String potString = String.valueOf(pot);

        Runnable task = () -> winnerLabel.setText(winnerName + " won the pot of: " + potString);
        Platform.runLater(task);

    }

    /**
     * Called when the game is over. Display a message with who the winner is
     *
     * @param userId
     */
    public void gameOver(int userId){

        Runnable task = () -> {

            VBox vBox = new VBox();
            Button backToMainScreen = ObjectStandards.makeButtonForLobbyScreen("Back to main menu");
            backToMainScreen.setMinWidth(200);

            endGameScreen = ObjectStandards.makeStandardLabelBlack(names.get(userId) + " is the winner!","");
            endGameScreen.setFont(new Font("Areal", 30));

            Stage endGame = new Stage();
            endGame.initModality(Modality.APPLICATION_MODAL);
            endGame.setTitle("Congratulation!");

            vBox.setAlignment(Pos.CENTER);


            vBox.setStyle("-fx-background-color:#42b43d, " +
                    "linear-gradient(#309e2a 0%, #2bbd24 20%, #42b43d 100%), " +
                    "linear-gradient(#218a0f, #42b43d), " +
                    "radial-gradient(center 50% 0%, radius 100%, rgba(63,191,63,0.9), rgba(51,151,51,1)); " +
                    "-fx-text-fill: linear-gradient(white, #d0d0d0) ; ");

            backToMainScreen.setOnAction(e -> {
                endGame.close();
                ButtonListeners.returnToMainMenuButtonListener();
            });

            vBox.getChildren().addAll(endGameScreen,backToMainScreen);
            Scene scene = new Scene(vBox,600,100);
            endGame.setScene(scene);
            endGame.show();
        };
        Platform.runLater(task);
    }

    /**
     * Set the text in the amount text field
     * @param message
     */
    public void setAmountTextfield(String message) {
        Runnable task = () -> amountTextfield.setText(message);
        Platform.runLater(task);
    }

    /**
     *  Set the border around the amount textfield to red, indicating an error
     * @param error
     */

    public void setErrorStateOfAmountTextfield(boolean error) {
        Runnable task;
        if (error) {
            task = () -> amountTextfield.setStyle("-fx-border-color: rgba(255, 0, 0, 0.49) ; -fx-border-width: 3px ;");
        }
        else {
            task = () -> amountTextfield.setStyle("-fx-border-color: rgb(255, 255, 255) ; -fx-border-width: 3px ;");
        }

        Platform.runLater(task);
    }

    public void setPositions(Map<Integer, Integer> positions) {
        Runnable task;
        for (Integer id : positions.keySet()) {
            String pos = "Position: " + getPositionName(positions.get(id));
            if (id == playerID) {
                task = () -> playerPositionLabel.setText(pos);
            } else {
                task = () -> opponentPositionLabel.setText(pos);
            }
            Platform.runLater(task);
        }
    }

    private String getPositionName(int pos) {
        return (pos == 0 ? "Dealer" : pos == 1 ? "Small blind" : pos == 2 ? "Big blind" : pos == 3 ? "UTG" : "UTG+" + (pos-3));
    }
}
