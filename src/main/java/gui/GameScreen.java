package gui;

import gui.layouts.BoardLayout;
import gui.layouts.OpponentLayout;
import gui.layouts.PlayerLayout;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
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

   // BorderPane borderPane;
    Scene scene;
    private int playerID;
    private int [] positions;
    private int numberOfOpponentsAddedToTheGame = 0;
    private int numberOfPlayers = 2;

    private Label endGameScreen;

    //Storagevariables
    private long highestAmountPutOnTable = 0, pot = 0;
    private long currentSmallBlind, currentBigBlind;
    private Map<Integer, String> names = new HashMap<>();
    private Map<Integer, Long> stackSizes = new HashMap<>();
    private Map<Integer, Long> putOnTable = new HashMap<>();
    private Map<Integer, OpponentLayout> opponents;

    private Pane pane = new Pane();

    PlayerLayout playerLayout = new PlayerLayout();
    BoardLayout boardLayout = new BoardLayout();

    private TextArea textArea = new TextArea();
    private String logText = "";

    private Button exitButton;


    public GameScreen(int ID, int maxNumberOfPlayers) {
        this.playerID = ID;
        scene = new Scene(ImageViewer.setBackground("PokerTable", pane, 1920, 1080), 1280, 720);
        this.opponents = new HashMap<>();

        numberOfPlayers = maxNumberOfPlayers;
        initializePlayerLayouts(maxNumberOfPlayers);
        insertLogField();
        addExitButton();
    }

    private void initializePlayerLayouts(int N) {
        playerLayout = new PlayerLayout();
        for (int i = 1; i < N; i++) {
            opponents.put(i, new OpponentLayout());
        }
    }

    /**
     * Creates the game screen
     *
     * @param settings
     * @return a scene containing a gamescreen
     */

    public Scene createSceneForGameScreen(GameSettings settings) {
        VBox box = boardLayout.updateLayout(settings.getSmallBlind(), settings.getBigBlind());
        box.setLayoutX(scene.getWidth() / 4 - 30);
        box.setLayoutY(scene.getHeight() / 3 + 30);
        pane.getChildren().addAll((box));
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
        playerLayout.setStackSize(stackSize);

        if (userID == playerID) {
            VBox vbox = playerLayout.updateLayout(userID,name,stackSize);
            vbox.setLayoutX(scene.getWidth()/4);
            vbox.setLayoutY(scene.getHeight()-160);
            pane.getChildren().addAll(vbox);
        } else {

            OpponentLayout oppLayout = opponents.get(userID);
            oppLayout.setPosition(positions[numberOfOpponentsAddedToTheGame]);
            oppLayout.updateLayout(name, stackSize);

            switch (oppLayout.getPosition()){
                case 1:
                    oppLayout.setLayoutX(20);
                    oppLayout.setLayoutY(scene.getHeight() / 2);
                    break;
                case 2:
                    oppLayout.setLayoutX(20);
                    oppLayout.setLayoutY(scene.getHeight() / 6);
                    break;
                case 3:
                    oppLayout.setLayoutX(scene.getWidth() / 3);
                    oppLayout.setLayoutY(20);
                    break;
                case 4:
                    oppLayout.setLayoutX(scene.getWidth() - 280);
                    oppLayout.setLayoutY(scene.getHeight() / 6);
                    break;
                case 5:
                    oppLayout.setLayoutX(scene.getWidth() - 280);
                    oppLayout.setLayoutY(scene.getHeight() / 2);
                    break;
                default:
                    GUIMain.debugPrintln("Cannot place opponent");
            }

            numberOfOpponentsAddedToTheGame++;
            pane.getChildren().add(oppLayout);
            opponents.put(userID, oppLayout);
        }

        return true;
    }

    /**
     *
     * Generates an array of all the opponents positions.
     * Different amount of players give different positions.
     *
     * @return An array of all the positions.
     */
    private int[] giveOpponentPosition() {

        int [] positions = new int[numberOfPlayers-1];

        switch (numberOfPlayers){
            case 2:
                positions[0] = 3;
                break;
            case 3:
                positions[0] = 2;
                positions[1] = 4;
                break;
            case 4:
                positions[0] =2;
                positions[1] =3;
                positions[2] =4;
                break;
            case 5:
                positions[0] =1;
                positions[1] =2;
                positions[2] =4;
                positions[3] =5;
                break;
            case 6:
                positions[0] =1;
                positions[1] =2;
                positions[2] =3;
                positions[3] =4;
                positions[4] =5;
                break;
            default:
                GUIMain.debugPrint("Too many players");
                break;
        }

        return positions;

    }

    /**
     * Inserts a text field for the game log.
     * It is put in the lower, left corner.
     */
    public void insertLogField(){
        textArea.setMaxWidth(300);
        textArea.setMaxHeight(100);
        textArea.setEditable(false);
        textArea.setLayoutX(5);
        textArea.setLayoutY(scene.getHeight()-105);
        textArea.setWrapText(true);
        pane.getChildren().add(textArea);
        textArea.setOpacity(0.9);
    }

    /**
     * Adds text to the previously made log field.
     * @param printInfo The text to add to the field.
     */
    public void printToLogField(String printInfo){
        Runnable task = () -> {
            logText = printInfo + "\n" + logText;
            textArea.setText(logText);
        };
        Platform.runLater(task);

    }

    /**
     * Adds a working exit button to the game screen. The button takes you back to the lobby screen
     */
    public void addExitButton(){
        exitButton = ObjectStandards.makeStandardButton("Exit");

        exitButton.setLayoutX(scene.getWidth()- 80);
        exitButton.setLayoutY(3);

        pane.getChildren().add(exitButton);

        exitButton.setOnAction(event -> ButtonListeners.exitButtonListener());
    }

    /**
     * Displays the card pictures to the screen
     *
     * @param userID
     * @param leftCard
     * @param rightCard
     */

    public void setHandForUser(int userID, Card leftCard, Card rightCard) {
        //Images
        Image leftImage = new Image(ImageViewer.returnURLPathForCardSprites(leftCard.getCardNameForGui()));
        Image rightImage = new Image(ImageViewer.returnURLPathForCardSprites(rightCard.getCardNameForGui()));
        Image backImage = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));

        Runnable task = () -> {
            playerLayout.setCardImage(leftImage,rightImage);

            //Set opponent hand
            for (int i=1;i<numberOfPlayers;i++) {
                opponents.get(i).setCardImage(backImage, backImage);
            }
        };
        Platform.runLater(task);
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
                    opponents.get(i).setCardImage(leftImage,rightImage);
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

            boardLayout.setFlop(card1Image,card2Image,card3Image);

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
        Runnable task = () -> boardLayout.setTurn(turnImage);
        Platform.runLater(task);
    }

    /**
     * Displays the fifth card on the board
     *
     * @param river
     */

    public void displayRiver(Card river) {
        Image riverImage = new Image(ImageViewer.returnURLPathForCardSprites(river.getCardNameForGui()));
        Runnable task = () -> boardLayout.setRiver(riverImage);
        Platform.runLater(task);
    }

    /**
     * Show the buttons on the board
     *
     * @param visible
     */

    public void setActionsVisible(boolean visible) {
        Runnable task = () -> {
            playerLayout.setVisible(visible);
            playerLayout.setSliderVisibility();
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

        //Init putOnTable-map
        if (putOnTable.get(ID) == null)
            putOnTable.put(ID, 0L);

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
            case FOLD:
                Runnable task;
                if (ID == playerID)
                    task = () -> playerLayout.removeHolecards();
                else
                    task = () -> opponents.get(ID).removeHolecards();

                Platform.runLater(task);
                break;
        }

        if (ID == playerID) {
            playerLayout.setStackSize(newStackSize);
        }

        stackSizes.put(ID, newStackSize);

        //Set button texts depending on the action
        String finalText = checkCallButtonText;
        switch (decision.move) {
            case BET:case RAISE:case BIG_BLIND:case SMALL_BLIND:
                Runnable task = () -> {
                    playerLayout.setCheckCallButton(finalText);
                    playerLayout.setBetRaiseButton("Raise to");
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
                playerLayout.setLastMoveLabel(finalDecision);
                playerLayout.setStackLabel(stackSizeText);
            };
        } else {
            task = () -> {
                opponents.get(ID).setLastMoveLabel(finalDecision);
                opponents.get(ID).setStackSizeLabel(stackSizeText);
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
                task = () -> playerLayout.setStackLabel("Amount of chips: " + stackSizeText);
            } else {
                task = () -> {
                    for (int i=1;i<numberOfPlayers;i++) {
                        opponents.get(i).setStackSizeLabel("Amount of chips: " + stackSizeText);
                    }
                };
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
            playerLayout.setLastMoveLabel("");
            playerLayout.setCheckCallButton("Check");
            playerLayout.setBetRaiseButton("Bet");
            this.setAmountTextfield(currentBigBlind + "");
            this.setErrorStateOfAmountTextfield(false);

            for (int i = 1; i < numberOfPlayers; i++ ){
                opponents.get(i).setLastMoveLabel("");
            }
        };
        Platform.runLater(task);
        playerLayout.updateSliderValues();
    }

    /**
     * Set the pot label
     *
     * @param pot
     */
    public void setPot(long pot) {
        this.pot = pot;
        String potString = Long.toString(pot);
        Runnable task = () -> boardLayout.setPotLabel("Pot: " + potString);
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
            playerLayout.setNameLabel("Name: " + names.get(playerID));
            //TODO: Fix hardcoding

            for (Integer i : opponents.keySet()) {
                opponents.get(i).setNameLabel("Name: " + names.get(i));
            }
        };
        Platform.runLater(task);
    }

    /**
     * Start a new hard
     */

    public void startNewHand() {
        Image image = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));
        Runnable task = () -> {
            for (ImageView imageview : boardLayout.getCommunityCards()) {
                imageview.setImage(image);
                imageview.setVisible(false);
            }
            boardLayout.setWinnerLabel("");
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

        Runnable task = () -> boardLayout.setWinnerLabel(winnerName + " won the pot of: " + potString);
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
            endGame.setAlwaysOnTop(true);
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
        Runnable task = () -> playerLayout.setAmountTextfield(message);
        Platform.runLater(task);
    }

    /**
     *  Set the border around the amount textfield to red, indicating an error
     * @param error
     */

    public void setErrorStateOfAmountTextfield(boolean error) {
        Runnable task;
        if (error) {
            task = () -> playerLayout.setTextfieldStyle("-fx-border-color: rgba(255, 0, 0, 0.49) ; -fx-border-width: 3px ;");
        }
        else {
            task = () -> playerLayout.setTextfieldStyle("-fx-border-color: rgb(255, 255, 255) ; -fx-border-width: 3px ;");
        }

        Platform.runLater(task);
    }

    /**
     *
     * Set the positions of the players
     *
     * @param positions
     */

    public void setPositions(Map<Integer, Integer> positions) {
        Runnable task;
        for (Integer id : positions.keySet()) {
            String pos = "Position: " + getPositionName(positions.get(id));
            if (id == playerID) {
                task = () -> playerLayout.setPositionLabel(pos);
            } else {
                task = () -> {
                    opponents.get(id).setPositionLabel(pos);
                };
            }
            Platform.runLater(task);
        }
    }


    /**
     * Get the position of each player
     *
     * @param pos
     * @return position
     */
    private String getPositionName(int pos) {
        return (pos == 0 ? "Dealer" : pos == 1 ? "Small blind" : pos == 2 ? "Big blind" : pos == 3 ? "UTG" : "UTG+" + (pos-3));
    }

    /**
     * Updates slider values
     */
   public void updateSliderValues(){
       playerLayout.updateSliderValues();
   }

    /**
     * Setter for number of players.
     *
     * @param numberOfPlayers
     */
    public void setNumberOfPlayers(int numberOfPlayers){
        this.numberOfPlayers = numberOfPlayers;
        positions = giveOpponentPosition();

    }
}
