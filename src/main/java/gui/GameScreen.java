package gui;

import gamelogic.*;
import gui.layouts.BoardLayout;
import gui.layouts.IPlayerLayout;
import gui.layouts.OpponentLayout;
import gui.layouts.PlayerLayout;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Bloom;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

/**
 * This class contains all the information about the game screen.
 * It gets information from gameClient and moves it to the layouts, which displays it on the screen.
 *
 *
 * @author André Dyrstad
 * @author Jostein Kringlen
 * @author Kristian Rosland
 */
public class GameScreen {


    private int playerID;
    private int numberOfPlayers, opponentsAdded = 0;

    //Storage variables
    private long highestAmountPutOnTable = 0, potSize = 0;
    private long  currentBigBlind, currentSmallBlind;
    private int [] positions;
    private Map<Integer, String> names = new HashMap<>();
    private Map<Integer, Long> stackSizes = new HashMap<>();
    private Map<Integer, Long> putOnTable = new HashMap<>();
    private ArrayList<Card> holeCards, communityCards;
    private Map<Integer, Card[]> allHoleCards;
    private boolean holeCardsShown = false;


    //GUI-elements
    private Scene scene;
    private Pane pane = new Pane();
    private PlayerLayout playerLayout;
    private BoardLayout boardLayout;
    private Map<Integer, IPlayerLayout> allPlayerLayouts;
    private Label endGameScreen;
    private static TextArea textArea = new TextArea();
    private SoundPlayer soundPlayer = new SoundPlayer();

    public GameScreen(int ID) {
        this.playerID = ID;

        //Set onKeyRelease and onMouseClick events for pane
        pane.setOnKeyReleased(ke -> ButtonListeners.keyReleased(ke, playerLayout, boardLayout));
        pane.setOnMouseClicked((event) -> playerLayout.setFocus());

        //Create the scene
        scene = new Scene(ImageViewer.setBackground("PokerTable", pane, 1920, 1080), 1280, 720);

        this.allPlayerLayouts = new HashMap<>();

        insertLogField();
        addMenuBarToGameScreen();
    }

    /**
     * Creates the game screen
     *
     * @param settings
     * @return a scene containing a gamescreen
     */
    public Scene createSceneForGameScreen(GameSettings settings) {
        long sb = settings.getSmallBlind(), bb = settings.getBigBlind();
        boardLayout = new BoardLayout(sb, bb);
        boardLayout.setLayoutX(scene.getWidth() / 4 - 30);
        boardLayout.setLayoutY(scene.getHeight() / 3 + 30);
        pane.getChildren().add(boardLayout);
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
            PlayerLayout pLayout = new PlayerLayout(userID,name,stackSizes.get(0));
            playerLayout = pLayout;
            pLayout.setLayoutX(scene.getWidth()/4);
            pLayout.setLayoutY(scene.getHeight()-190);
            pane.getChildren().addAll(pLayout);
            allPlayerLayouts.put(userID, pLayout);
        } else {
            int oppPosition = positions[opponentsAdded];
            OpponentLayout oppLayout = new OpponentLayout(name, stackSize, oppPosition);

            //Set X/Y-layout of this opponent
            double height = scene.getHeight(), width = scene.getWidth();
            oppLayout.setLayoutX(OpponentLayout.getLayoutX(oppPosition, width));
            oppLayout.setLayoutY(OpponentLayout.getLayoutY(oppPosition, height));

            opponentsAdded++;
            pane.getChildren().add(oppLayout);
            allPlayerLayouts.put(userID, oppLayout);
        }

        return true;
    }

    /**
     * Generates an array of all the opponents positions.
     * Different amount of players give different positions.
     *
     * @return An array of all the positions.
     */
    private int[] giveOpponentPosition() {
        switch (numberOfPlayers){
            case 2: return new int[]{3};
            case 3: return new int[]{2,4};
            case 4: return new int[]{2,3,4};
            case 5: return new int[]{1,2,4,5};
            case 6: return new int[]{1,2,3,4,5};
            default:
                GUIMain.debugPrintln("Error: " + numberOfPlayers + " players in game, cannot set positions");
                return null;
        }
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
        textArea.setLayoutY(scene.getHeight() - 105);
        textArea.setWrapText(true);
        pane.getChildren().add(textArea);
        textArea.setOpacity(0.9);

        //Add listener to listen for changes and automatically scroll to the bottom
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            textArea.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
        });

        //Remove highlight of textfield
        textArea.setFocusTraversable(false);
    }

    /**
     * Adds text to the previously made log field.
     * @param printInfo The text to add to the field.
     */
    public static void printToLogField(String printInfo){
        textArea.appendText("\n" + printInfo);
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
        //Images
        Image leftImage = new Image(ImageViewer.returnURLPathForCardSprites(leftCard.getCardNameForGui()));
        Image rightImage = new Image(ImageViewer.returnURLPathForCardSprites(rightCard.getCardNameForGui()));

        allPlayerLayouts.get(userID).setCardImage(leftImage, rightImage);

        if (userID == this.playerID) {
            holeCards = new ArrayList<>();
            holeCards.add(leftCard);
            holeCards.add(rightCard);
            updateYourHandLabel();
        }
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
            if (p.getID() != playerID && allPlayerLayouts.get(p.getID()) != null)
                allPlayerLayouts.get(p.getID()).setCardImage(leftImage,rightImage);
        }

        String winnerString = showdownStats.getWinnerText();
        boardLayout.setWinnerLabel(winnerString);

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
        boardLayout.showFlop(card1Image, card2Image, card3Image);

        communityCards.add(card1);
        communityCards.add(card2);
        communityCards.add(card3);
        updateYourHandLabel();
        showPercentages();

        new SoundPlayer().playDealCardSound();
        printToLogField("Flop " + card1 + " " + card2 + " " + card3);
    }

    /**
     * Displays the fourth card on the board
     * @param turnCard
     */
    public void displayTurn(Card turnCard) {
        Image turnImage = new Image(ImageViewer.returnURLPathForCardSprites(turnCard.getCardNameForGui()));
        boardLayout.showTurn(turnImage);
        communityCards.add(turnCard);
        updateYourHandLabel();
        showPercentages();

        printToLogField("Turn " + turnCard);
        soundPlayer.playDealCardSound();
    }

    /**
     * Displays the fifth card on the board
     *
     * @param riverCard
     */
    public void displayRiver(Card riverCard) {
        Image riverImage = new Image(ImageViewer.returnURLPathForCardSprites(riverCard.getCardNameForGui()));
        boardLayout.showRiver(riverImage);
        communityCards.add(riverCard);
        updateYourHandLabel();
        soundPlayer.playDealCardSound();
        showPercentages();

        printToLogField("River " + riverCard);
    }

    /**
     * Show the players possible actions (buttons)
     * @param visible
     */
    public void setActionsVisible(boolean visible) {
        playerLayout.setActionsVisible(visible);
    }

    /**
     * Update buttons and show any players last move
     *
     * @param ID
     * @param decision
     */
    public synchronized void playerMadeDecision(int ID, Decision decision) {
        //Remove player highlighting
        allPlayerLayouts.get(ID).highlightTurn(false);

        //Update all values in the GUI and return a string of the decision that can be displayed
        final String finalDecision = evaluateDecision(ID, decision);

        //Set button texts depending on the action
        updateButtonTexts(ID, decision.move);

        //Play the appropriate sound for this decision
        soundPlayer.getSoundForDecision(decision.move);

        allPlayerLayouts.get(ID).setLastMove(finalDecision, getChipImage(ID));
        allPlayerLayouts.get(ID).setStackLabel(""+stackSizes.get(ID));
    }

    /**
     *   Update the buttons in the GUI depending on the last move made
     * @param id
     * @param move
     */
    private void updateButtonTexts(int id, Decision.Move move) {
        switch (move) {
            case BIG_BLIND:
                if (id == playerID) {
                    playerLayout.setCheckCallButton("Check");
                    break;
                }
            case BET:
            case RAISE:
            case SMALL_BLIND:
            case ALL_IN:
                playerLayout.setCheckCallButton("Call");
                playerLayout.setBetRaiseButton("Raise to");
                break;
        }

        updateSliderValues();
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
                putOnTable.put(ID, decision.getSize());
                newStackSize -= decision.getSize();
                decisionText += (highestAmountPutOnTable = decision.getSize());
                setAmountTextfield(highestAmountPutOnTable *2 + "");
                printToLogField(names.get(ID) + " bet " + decision.getSize());
                break;
            case RAISE:

                long theCall = highestAmountPutOnTable - putOnTable.get(ID);
                newStackSize -= (theCall + decision.getSize());
                decisionText += (highestAmountPutOnTable += decision.getSize());
                setAmountTextfield((highestAmountPutOnTable + decision.getSize()) + "");
                putOnTable.put(ID, highestAmountPutOnTable);
                printToLogField(names.get(ID) + " raised to " + highestAmountPutOnTable);
                break;
            case BIG_BLIND:
                newStackSize -= currentBigBlind;
                decisionText += (highestAmountPutOnTable = currentBigBlind);
                setAmountTextfield("" + currentBigBlind * 2);
                putOnTable.put(ID, currentBigBlind);
                printToLogField(names.get(ID) + " posted big blind");
                break;
            case SMALL_BLIND:
                newStackSize -= currentSmallBlind;
                decisionText += (currentSmallBlind);
                setAmountTextfield("" + currentBigBlind * 2);
                putOnTable.put(ID, currentSmallBlind);
                printToLogField(names.get(ID) + " posted small blind");
                break;
            case ALL_IN:
                if (putOnTable.get(ID) + stackSizes.get(ID) >= highestAmountPutOnTable) //If raise is valid
                    highestAmountPutOnTable = putOnTable.get(ID) + stackSizes.get(ID);
                putOnTable.put(ID, putOnTable.get(ID) + stackSizes.get(ID));
                newStackSize = 0;
                decisionText += putOnTable.get(ID);
                printToLogField(names.get(ID) + " went all in with " + putOnTable.get(ID));
                break;
            case FOLD:
                allPlayerLayouts.get(ID).foldPlayer();
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
            allPlayerLayouts.get(clientID).setStackLabel("" + stackSizeText);
        }
    }

    /**
     * Start a new betting round and reset buttons
     */
    public void newBettingRound() {
        updatePot();

        //Reset everything people have put on the table
        putOnTable.forEach((id, putIn) -> putOnTable.put(id, 0L));

        this.highestAmountPutOnTable = 0;
        playerLayout.setCheckCallButton("Check");
        playerLayout.setBetRaiseButton("Bet");
        this.setAmountTextfield(currentBigBlind + "");
        this.setErrorStateOfAmountTextField(false);

        allPlayerLayouts.forEach((id, layout) -> layout.setLastMove("", null));
        updateSliderValues();
    }

    /**
     * Set the pot label
     * @param pot
     */
    public void setPot(long pot) {
        String potString = Long.toString(pot);
        boardLayout.setPotLabel("Pot: " + potString);
    }

    /**
     * Set name to all the players
     *
     * @param names
     */
    public void setNames(Map<Integer, String> names) {
        this.names = names;
        allPlayerLayouts.forEach((id, layout) -> layout.setNameLabel(names.get(id)));
    }

    /**
     * Start a new hard
     */
    public void startNewHand() {
        new SoundPlayer().playShuffleSound();
        printToLogField(" ------ New hand ------");
        communityCards = new ArrayList<>();

        //Set opponent hands
        Image backImage = ImageViewer.getImage(ImageViewer.Image_type.CARD_BACK);
        allPlayerLayouts.forEach((id, layout) -> {
            if (!layout.isBust())
                layout.setCardImage(backImage, backImage);
            layout.setPercentLabel("");
        });

        //Reset hole cards
        this.holeCardsShown = false;
        this.allHoleCards = null;

        //Reset board
        boardLayout.newHand();
        putOnTable.forEach((id, putIn) -> putOnTable.put(id, 0L));
        potSize = 0;
        updatePot();
    }

    /**
     * Called when the game is over. Display a message with who the winner is
     *
     * @param stats Statistics of the game just played
     */
    public void gameOver(Statistics stats){
        int winnerID = stats.getWinnerID();

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
            if (saveStatisticsButton.getText().equals("Save statistics to file")) {
                ButtonListeners.saveToFile(stats);
                saveStatisticsButton.setText("Statistics saved!");
                saveStatisticsButton.setEffect(new Bloom(-0.9));
            }
        });

        vBox.getChildren().addAll(endGameScreen, statsLabel, saveStatisticsButton, backToMainScreenButton);
        Scene scene = new Scene(vBox,600,450);
        endGame.setScene(scene);
        endGame.show();
    }

    /**
     * Set the text in the amount text field
     * @param message
     */
    public void setAmountTextfield(String message) {
        playerLayout.setAmountTextField(message);
    }

    /**
     *  Set the border around the amount textfield to red, indicating an error
     * @param error
     */
    public void setErrorStateOfAmountTextField(boolean error) {
        if (error)
            playerLayout.setTextfieldStyle("-fx-border-color: rgba(255, 0, 0, 0.49) ; -fx-border-width: 3px ;");
        else
            playerLayout.setTextfieldStyle("-fx-border-color: rgb(255, 255, 255) ; -fx-border-width: 3px ;");
    }

    /**
     * Set the positions of the players
     * @param positions
     */
    public void setPositions(Map<Integer, Integer> positions) {
        for (Integer id : positions.keySet()) {
            String pos = getPositionName(positions.get(id), numberOfPlayers);
            allPlayerLayouts.get(id).setPositionLabel(pos, getButtonImage(id, positions.get(id)));
        }
    }

    /**
     *  Turn an integer position into a string ("Dealer", "Small blind", ...)
     * @param pos
     * @return position
     */
    public static String getPositionName(int pos, int numberOfPlayers) {
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

        if (this.playerID == playerID)
            playerLayout.setBestHand("");

        allPlayerLayouts.get(playerID).bustPlayer(bustedText);
        allPlayerLayouts.remove(playerID);

        printToLogField(names.get(playerID) + " " + bustedText.toLowerCase());
    }

    /** Set the big blind */
    public void setBigBlind(long bigBlind) {
        boardLayout.setBigBlindLabel(this.currentBigBlind = bigBlind);
    }

    public void setSmallBlind(long smallBlind) { boardLayout.setSmallBlindLabel(this.currentSmallBlind = smallBlind); }

    /**
     *  Set the 'Best hand'-label to the players current best hand (e.g.: "Pair of 2's")
     */
    private void updateYourHandLabel() {
        if (holeCards.isEmpty())
            playerLayout.setBestHand("");

        HandCalculator hc = new HandCalculator(new Hand(holeCards.get(0), holeCards.get(1), communityCards));
        playerLayout.setBestHand(hc.getBestHandString());
    }

    /**
     *   Sent if the hand is over before showdown
     * @param winnerID  The player that was left in the hand
     */
    public void preShowdownWinner(int winnerID) {
        updatePot();
        boardLayout.setWinnerLabel(names.get(winnerID) + " won the pot of " + String.valueOf(potSize));
        printToLogField(names.get(winnerID) + " won the pot of " + potSize);
    }

    /**
     *
     */
    private void updatePot() {
        for (Integer id : putOnTable.keySet())
            potSize += putOnTable.get(id);
        setPot(potSize);
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
     * @param holeCards  Map from a player's ID to his hole cards
     */
    public void showHoleCards(Map<Integer, Card[]> holeCards) {
        holeCards.forEach((id, cards) -> {
            Image leftCard = new Image(ImageViewer.returnURLPathForCardSprites(cards[0].getCardNameForGui()));
            Image rightCard = new Image(ImageViewer.returnURLPathForCardSprites(cards[1].getCardNameForGui()));

            allPlayerLayouts.get(id).setCardImage(leftCard, rightCard);
        });

        this.allHoleCards = holeCards;
        this.holeCardsShown = true;
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

    private Image getButtonImage(int player, int id){
        if (player == 0) {
            if(getPositionName(id, numberOfPlayers).equals("Dealer"))
                return ImageViewer.getImage(ImageViewer.Image_type.DEALER_BUTTON);
            else return null;
        }
        if (player > 0){
            if (getPositionName(id, numberOfPlayers).endsWith("Dealer"))
                return ImageViewer.getImage(ImageViewer.Image_type.DEALER_BUTTON);
            else return null;
        }
        return null;
    }

    public void highlightPlayerTurn(int id) {
        if (allPlayerLayouts.get(id) != null)
            allPlayerLayouts.get(id).highlightTurn(true);
    }

    /**
     *  Start the remaining time progress bar
     * @param timeToThink
     */
    public void startTimer(long timeToThink, Decision.Move moveToExecute) {
        playerLayout.startTimer(timeToThink, moveToExecute);
    }
    public void stopTimer() {
        playerLayout.stopTimer();
    }

    /**
     * If hole cards are shown, calculate percentages for all players
     */
    private void showPercentages() {
        if (!holeCardsShown || allHoleCards == null || communityCards.size() < 3)
            return;

        Map<Integer, Double> percentages = HandCalculator.getWinningPercentages(allHoleCards, communityCards);

        percentages.forEach((id, pcnt) -> allPlayerLayouts.get(id).setPercentLabel((int)(pcnt*100) + "%"));
    }
}
