package gui;

import gamelogic.*;
import gui.layouts.BoardLayout;
import gui.layouts.OpponentLayout;
import gui.layouts.PlayerLayout;
import javafx.application.Platform;
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

import java.util.*;

/**
 * TODO: Add class description
 *
 * @author André Dyrstad
 * @author Jostein Kringlen
 * @author Kristian Rosland
 */
public class GameScreen {

   // BorderPane borderPane;
    private Scene scene;
    private int playerID;
    private int [] positions;
    private int numberOfOpponentsAddedToTheGame = 0;
    private int numberOfPlayers = 2;

    private Label endGameScreen;

    //Storagevariables
    private long highestAmountPutOnTable = 0, pot = 0;
    private long currentBigBlind;
    private Map<Integer, String> names = new HashMap<>();
    private Map<Integer, Long> stackSizes = new HashMap<>();
    private Map<Integer, Long> putOnTable = new HashMap<>();
    private Map<Integer, OpponentLayout> opponents;
    private ArrayList<Card> holeCards, communityCards;

    private Pane pane = new Pane();

    private PlayerLayout playerLayout = new PlayerLayout();
    private BoardLayout boardLayout = new BoardLayout();

    private TextArea textArea = new TextArea();
    private String logText = "";

    public GameScreen(int ID, int numberOfPlayers) {
        this.playerID = ID;
        scene = new Scene(ImageViewer.setBackground("PokerTable", pane, 1920, 1080), 1280, 720);
        this.opponents = new HashMap<>();

        initializePlayerLayouts(numberOfPlayers);
        insertLogField();
        addMenuBarToGameScreen();
    }

    /**
     *  Initiate all the layouts in the GUI
     * @param numberOfPlayers
     */
    private void initializePlayerLayouts(int numberOfPlayers) {
        playerLayout = new PlayerLayout();
        for (int i = 1; i < numberOfPlayers; i++) {
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
        final String os = System.getProperty("os.version");


        this.names.put(userID, name);
        this.stackSizes.put(userID, stackSize);
        //playerLayout.setStackSize(stackSize);

        if (userID == playerID) {
            VBox vbox = playerLayout.updateLayout(userID,name,stackSizes.get(0));
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
                    if (!os.isEmpty()) {
                        if (!os.startsWith("Mac"))
                            oppLayout.setLayoutY(30);
                        else
                            oppLayout.setLayoutY(20);
                    }
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

    public void addMenuBarToGameScreen(){
        MenuBar menuBar = ObjectStandards.createMenuBar();
        menuBar.setLayoutX(0);
        menuBar.setLayoutY(0);
        pane.getChildren().add(menuBar);
    }

    /**
     * Displays the card pictures to the screen
     *
     * @param userID
     * @param leftCard
     * @param rightCard
     */
    public void setHandForUser(int userID, Card leftCard, Card rightCard) {
        assert userID == playerID : "Player " + playerID + " was sent someone elses cards";
        //Images
        Image leftImage = new Image(ImageViewer.returnURLPathForCardSprites(leftCard.getCardNameForGui()));
        Image rightImage = new Image(ImageViewer.returnURLPathForCardSprites(rightCard.getCardNameForGui()));

        Runnable task = () -> {
            playerLayout.setCardImage(leftImage,rightImage);
        };
        Platform.runLater(task);

        holeCards = new ArrayList<>();
        holeCards.add(leftCard);
        holeCards.add(rightCard);
        updateYourHandLabel();
    }

    /**
     * Shows the cards of the players around the table
     *
     * @param showdownStats Information about the showdown
     */
    public void showdown(ShowdownStats showdownStats) {
        List<Player> playersToShowdown = showdownStats.getAllPlayers();
        printToLogField(showdownStats.numberOfPlayers() + " players to showdown");

        Map<Integer, Card[]> hCards = showdownStats.getHoleCards();

        for (Player p : playersToShowdown) {
            Card[] cards = hCards.get(p.getID());
            Image leftImage = new Image(ImageViewer.returnURLPathForCardSprites(cards[0].getCardNameForGui()));
            Image rightImage = new Image(ImageViewer.returnURLPathForCardSprites(cards[1].getCardNameForGui()));

            //Print the players hand
            printToLogField(names.get(p.getID()) + ": " + cards[0] + " " + cards[1]);
            if (p.getID() != playerID && opponents.get(p.getID()) != null)
                Platform.runLater(() -> opponents.get(p.getID()).setCardImage(leftImage,rightImage));
        }

        String winnerString = showdownStats.getWinnerText();
        Platform.runLater(() -> boardLayout.setWinnerLabel(winnerString));

        //Print all community cards to in-game log
        printCommunityCardsToLogField();
        printToLogField(winnerString);
    }

    /**
     * Displays the first three cards (the flop) on the screen
     *
     * @param card1
     * @param card2
     * @param card3
     */
    public void displayFlop(Card card1, Card card2, Card card3) {
        Image card1Image = new Image(ImageViewer.returnURLPathForCardSprites(card1.getCardNameForGui()));
        Image card2Image = new Image(ImageViewer.returnURLPathForCardSprites(card2.getCardNameForGui()));
        Image card3Image = new Image(ImageViewer.returnURLPathForCardSprites(card3.getCardNameForGui()));
        Platform.runLater(() -> boardLayout.showFlop(card1Image,card2Image,card3Image));

        communityCards.add(card1);
        communityCards.add(card2);
        communityCards.add(card3);
        updateYourHandLabel();

        printToLogField("Flop " + card1 + " " + card2 + " " + card3);
    }

    /**
     * Displays the fourth card on the board
     * @param turnCard
     */
    public void displayTurn(Card turnCard) {
        Image turnImage = new Image(ImageViewer.returnURLPathForCardSprites(turnCard.getCardNameForGui()));
        Platform.runLater(() -> boardLayout.showTurn(turnImage));
        communityCards.add(turnCard);
        updateYourHandLabel();

        printToLogField("Turn " + turnCard);
    }

    /**
     * Displays the fifth card on the board
     *
     * @param riverCard
     */
    public void displayRiver(Card riverCard) {
        Image riverImage = new Image(ImageViewer.returnURLPathForCardSprites(riverCard.getCardNameForGui()));
        Platform.runLater(() -> boardLayout.showRiver(riverImage));
        communityCards.add(riverCard);
        updateYourHandLabel();

        printToLogField("River " + riverCard);
    }

    /**
     * Show the players possible actions (buttons)
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
        //Update all values in the GUI and return a string of the decision that can be displayed
        final String finalDecision = evaluateDecision(ID, decision);

        //Set button texts depending on the action
        updateButtonTexts(ID, decision.move);

        //Play sound
        new SoundPlayer().playSound(SoundPlayer.Sound.CHIPS_SOUND);

        Runnable task;
        if (ID == this.playerID) {
            task = () -> {
                playerLayout.setLastMove(finalDecision, getChipImage(ID));
                playerLayout.setStackLabel("Stack size: " + stackSizes.get(ID));
            };
        } else {
            task = () -> {
                opponents.get(ID).setLastMove(finalDecision, getChipImage(ID));
                opponents.get(ID).setStackSizeLabel("Stack size: " + stackSizes.get(ID));
            };
        }
        Platform.runLater(task);
    }

    /**
     *   Update the buttons in the GUI depending on the last move made
     * @param id
     * @param move
     */
    private void updateButtonTexts(int id, Decision.Move move) {
        Runnable task = () -> {
            switch (move) {
                case BIG_BLIND:
                    if (id == playerID) {
                        playerLayout.setCheckCallButton("Check");
                        break;
                    }
                case BET:case RAISE:case SMALL_BLIND:
                    playerLayout.setCheckCallButton("Call");
                    playerLayout.setBetRaiseButton("Raise to");
                    break;
            }

            updateSliderValues();
        };
        Platform.runLater(task);
    }

    /**
     *  Update GUI and all player values depending on the decision
     * @param ID  ID of the player that made the decision
     * @param decision  The decision the player made
     * @return  A String of the decision, example "Call 200" or "Fold"
     */
    private String evaluateDecision(int ID, Decision decision) {
        long newStackSize = stackSizes.get(ID);
        String decisionText = decision.move.toString() + " ";

        //Init putOnTable-map
        if (putOnTable.get(ID) == null)
            putOnTable.put(ID, 0L);

        switch (decision.move) {
            case CALL:
                newStackSize -= Math.min(stackSizes.get(ID), (highestAmountPutOnTable - putOnTable.get(ID)));
                putOnTable.put(ID,Math.min(stackSizes.get(ID),highestAmountPutOnTable));
                decisionText += putOnTable.get(ID);
                printToLogField(names.get(ID) + " called " + putOnTable.get(ID));
                break;

            case BET:
                putOnTable.put(ID, decision.size);
                newStackSize -= decision.size;
                decisionText += (highestAmountPutOnTable = decision.size);
                setAmountTextfield(highestAmountPutOnTable *2 + "");
                printToLogField(names.get(ID) + " bet " + decision.size);
                break;
            case RAISE:

                long theCall = highestAmountPutOnTable - putOnTable.get(ID);
                newStackSize -= (theCall + decision.size);
                decisionText += (highestAmountPutOnTable += decision.size);
                setAmountTextfield((highestAmountPutOnTable + decision.size) + "");
                putOnTable.put(ID, highestAmountPutOnTable);
                printToLogField(names.get(ID) + " raised to " + highestAmountPutOnTable);
                break;
            case BIG_BLIND:
                newStackSize -= decision.size;
                decisionText += (highestAmountPutOnTable = decision.size);
                setAmountTextfield("" + currentBigBlind * 2);
                putOnTable.put(ID, highestAmountPutOnTable);
                printToLogField(names.get(ID) + " posted big blind");
                break;
            case SMALL_BLIND:
                newStackSize -= decision.size;
                decisionText += (decision.size);
                setAmountTextfield("" + currentBigBlind * 2);
                putOnTable.put(ID, decision.size);
                printToLogField(names.get(ID) + " posted small blind");
                break;
            case ALL_IN:
                if (putOnTable.get(ID) + stackSizes.get(ID) >= highestAmountPutOnTable) { //If raise is valid
                    highestAmountPutOnTable = putOnTable.get(ID) + stackSizes.get(ID);
                }
                putOnTable.put(ID, putOnTable.get(ID) + stackSizes.get(ID));
                newStackSize = 0;
                decisionText += putOnTable.get(ID);
                printToLogField(names.get(ID) + " went all in with " + putOnTable.get(ID));
                break;
            case FOLD:
                if (ID == playerID)
                    Platform.runLater(() -> playerLayout.removeHolecards());
                else
                    Platform.runLater(() -> opponents.get(ID).removeHolecards());

                printToLogField(names.get(ID) + " folded");
                break;
            case CHECK:
                printToLogField(names.get(ID) + " checked");
        }

        //Reset the error state of the amountTextField (remove potential red frame)
        setErrorStateOfAmountTextField(false);

        //Update stack size of the player that acted
        stackSizes.put(ID, newStackSize);
        return decisionText;
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
                task = () -> opponents.get(clientID).setStackSizeLabel("Amount of chips: " + stackSizeText);
            }
            Platform.runLater(task);
        }
    }

    /**
     * Start a new betting round and reset buttons
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
            playerLayout.setLastMove("", null);
            playerLayout.setCheckCallButton("Check");
            playerLayout.setBetRaiseButton("Bet");
            this.setAmountTextfield(currentBigBlind + "");
            this.setErrorStateOfAmountTextField(false);

            for (Integer id : opponents.keySet()) {
                opponents.get(id).setLastMove("", null);
            }
        };
        Platform.runLater(task);
        updateSliderValues();
    }

    /**
     * Set the pot label
     * @param pot
     */
    public void setPot(long pot) {
        this.pot = pot;
        String potString = Long.toString(pot);
        Platform.runLater(() -> boardLayout.setPotLabel("Pot: " + potString));
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
        printToLogField(" ------ New hand ------");
        communityCards = new ArrayList<>();

        Image backImage = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));
        Runnable task = () -> {
            for (ImageView imageview : boardLayout.getCommunityCards()) {
                imageview.setImage(backImage);
                imageview.setVisible(false);
            }
            boardLayout.setWinnerLabel("");

            //Set opponent hand
            for (Integer id : opponents.keySet()) {
                OpponentLayout opp = opponents.get(id);
                if (!opp.isBust())
                    opp.setCardImage(backImage, backImage);
            }

        };
        Platform.runLater(task);
        setPot(0);
    }

    /**
     * Called when the game is over. Display a message with who the winner is
     *
     * @param stats Statistics of the game just played
     */
    public void gameOver(Statistics stats){
        int winnerID = stats.getWinnerID();

        Runnable task = () -> {
            VBox vBox = new VBox();
            Button backToMainScreenButton = ObjectStandards.makeButtonForLobbyScreen("Back to main menu");
            Button saveStatisticsButton = ObjectStandards.makeButtonForLobbyScreen("Save statistics to file");
            backToMainScreenButton.setMinWidth(200);
            saveStatisticsButton.setMinWidth(200);

            endGameScreen = ObjectStandards.makeStandardLabelBlack(names.get(winnerID) + " has won the game!","");
            endGameScreen.setFont(new Font("Areal", 30));

            Label statsLabel = ObjectStandards.makeStandardLabelWhite(stats.toString(), "");
            statsLabel.setWrapText(true);
            statsLabel.setFont(new Font("Areal", 15));

            Stage endGame = new Stage();
            endGame.setAlwaysOnTop(true);
            endGame.initModality(Modality.APPLICATION_MODAL);
            endGame.setTitle("Game over!");

            vBox.setAlignment(Pos.CENTER);

            vBox.setStyle("-fx-background-color:#42b43d, " +
                    "linear-gradient(#309e2a 0%, #2bbd24 20%, #42b43d 100%), " +
                    "linear-gradient(#218a0f, #42b43d), " +
                    "radial-gradient(center 50% 0%, radius 100%, rgba(63,191,63,0.9), rgba(51,151,51,1)); " +
                    "-fx-text-fill: linear-gradient(white, #d0d0d0) ; ");

            backToMainScreenButton.setOnAction(e -> {
                endGame.close();
                ButtonListeners.returnToMainMenuButtonListener();
            });

            saveStatisticsButton.setOnAction(e -> {
                ButtonListeners.saveToFile(stats);
            });

            vBox.getChildren().addAll(endGameScreen, statsLabel, saveStatisticsButton, backToMainScreenButton);
            Scene scene = new Scene(vBox,600,450);
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
        Runnable task = () -> playerLayout.setAmountTextField(message);
        Platform.runLater(task);
    }

    /**
     *  Set the border around the amount textfield to red, indicating an error
     * @param error
     */
    public void setErrorStateOfAmountTextField(boolean error) {
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
     * Set the positions of the players
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
     *  Turn an integer position into a string ("Dealer", "Small blind", ...)
     * @param pos
     * @return position
     */
    private String getPositionName(int pos) {
        if (numberOfPlayers == 2)
            return pos == 0 ? "Dealer" : "Big Blind";
        return (pos == numberOfPlayers-1 ? "Dealer" : pos == 0 ? "Small blind" : pos == 1 ? "Big blind" : pos == 2 ? "UTG" : "UTG+" + (pos-2  ));
    }

    /**
     * Updates slider values
     */
    public void updateSliderValues(){
        if (stackSizes.get(0) != null)
            playerLayout.updateSliderValues(stackSizes.get(0));
   }

    /** Sent in the start of a game to set the number of players */
    public void setNumberOfPlayers(int numberOfPlayers){
        this.numberOfPlayers = numberOfPlayers;
        positions = giveOpponentPosition();

    }

    /**
     *   Set a playerLayout as bust. Prints to log.
     * @param playerID   ID of the player that busted
     * @param rank       The place the player came in
     */
    public void bustPlayer(int playerID, int rank) {
        numberOfPlayers--;
        String bustedText = "Busted in " + rank + (rank == 1 ? "st" : (rank == 2) ? "nd" : (rank == 3) ? "rd" : "th");
        if (rank == 1) bustedText = "Won";

        if (this.playerID == playerID) {
            playerLayout.bustPlayer(bustedText);
            playerLayout.setBestHand("");
        } else {
            opponents.get(playerID).bustPlayer(bustedText);
            opponents.remove(playerID);
        }

        printToLogField(names.get(playerID) + " " + bustedText.toLowerCase());
    }

    /** Set the big blind */
    public void setBigBlind(long bigBlind) {
        this.currentBigBlind = bigBlind;
    }

    /**
     *  Set the 'Best hand'-label to the players current best hand (e.g.: "Pair of 2's")
     */
    private void updateYourHandLabel() {
        if (holeCards.isEmpty())
            playerLayout.setBestHand("Your hand: ");

        HandCalculator hc = new HandCalculator(new Hand(holeCards.get(0), holeCards.get(1), communityCards));
        playerLayout.setBestHand("Your hand: " + hc.getBestHandString());
    }

    /**
     *   Sent if the hand is over before showdown
     * @param winnerID  The player that was left in the hand
     * @param potsize   The amount the player won
     */
    public void preShowdownWinner(int winnerID, long potsize) {
        Platform.runLater(() ->  {
            boardLayout.setWinnerLabel("Everyone else folded, " + names.get(winnerID) + " won the pot of " + String.valueOf(potsize));
            printToLogField(names.get(winnerID) + " won the pot of " + potsize);
        });
    }

    /**
     * Print all the community cards to the logField
     */
    private void printCommunityCardsToLogField() {
        String communityCardsText = "";
        for (Card c : communityCards)
            communityCardsText += c.toString() + " ";
        printToLogField(communityCardsText);
    }

    /**
     *  Show the hole cards of players remaining in the hand
     * @param playerList Integer list containing the ID of all the players left in the hand
     * @param holeCards  Map from a player's ID to his hole cards
     */
    public void showHoleCards(ArrayList<Integer> playerList, Map<Integer, Card[]> holeCards) {
        for (Integer id : playerList) {
            Image leftCard = new Image(ImageViewer.returnURLPathForCardSprites(holeCards.get(id)[0].getCardNameForGui()));
            Image rightCard = new Image(ImageViewer.returnURLPathForCardSprites(holeCards.get(id)[1].getCardNameForGui()));
            if (id == playerID)
                Platform.runLater(() -> playerLayout.setCardImage(leftCard, rightCard));
            else
                Platform.runLater(() -> opponents.get(id).setCardImage(leftCard, rightCard));
        }
    }

    /**
     *  Get the correct image for this decision (based ont the decision and the amount)
     */
    private Image getChipImage(int id) {
        if (putOnTable.get(id) == 0)
            return null;
        else if (putOnTable.get(id) <= currentBigBlind / 2)
            return ImageViewer.getChipImage("sb_image");
        if (putOnTable.get(id) <= currentBigBlind)
            return ImageViewer.getChipImage("bb_image");
        else if (putOnTable.get(id) <= currentBigBlind * 3)
            return ImageViewer.getChipImage("poker1");
        else if (putOnTable.get(id) <= currentBigBlind * 5)
            return ImageViewer.getChipImage("poker2");
        else if (putOnTable.get(id) <= currentBigBlind * 8)
            return ImageViewer.getChipImage("poker3");
        else if (putOnTable.get(id) <= currentBigBlind * 12)
            return ImageViewer.getChipImage("poker4");
        else if (putOnTable.get(id) <= currentBigBlind * 20)
            return ImageViewer.getChipImage("poker6");
        else if(putOnTable.get(id) <= currentBigBlind * 50)
            return ImageViewer.getChipImage("poker7");
        else
            return ImageViewer.getChipImage("poker8");
    }
}
