package gui;

import gamelogic.*;
import gui.layouts.BoardLayout;
import gui.layouts.IPlayerLayout;
import gui.layouts.OpponentLayout;
import gui.layouts.PlayerLayout;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Bloom;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Consumer;

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

    //GUI-elements
    private Scene scene;
    private Pane pane = new Pane();
    private PlayerLayout playerLayout;
    private BoardLayout boardLayout;
    private Map<Integer, IPlayerLayout> allPlayerLayouts;
    private TextFlow logField = new TextFlow();
    private ScrollPane scrollPane = new ScrollPane();
    private TextField chatField = new TextField();
    private SoundPlayer soundPlayer = new SoundPlayer();
    private Optional<Consumer<String>> chatListener = Optional.empty();

    private final Logger logger;

    public GameScreen(int ID, Logger logger) {
        this.playerID = ID;
        this.logger = logger;

        //Set onKeyRelease and onMouseClick events for pane
        pane.setOnKeyReleased(ke -> ButtonListeners.keyReleased(ke, playerLayout, boardLayout, chatField));
        pane.setOnMouseClicked((event) -> playerLayout.setFocus());

        //Create the scene
        scene = new Scene(ImageViewer.setBackground("table&background", pane, 1920, 1080), 1280, 720);

        this.allPlayerLayouts = new HashMap<>();

        insertLogField();
        insertChatField();
        addMenuBarToGameScreen();
        pane.requestFocus();
    }

    /**
     * Creates the game screen
     *
     * @return a scene containing the game screen
     */
    public Scene createSceneForGameScreen() {
        boardLayout = new BoardLayout(0, 0);
        boardLayout.setLayoutX(scene.getWidth() / 4 - 30);
        boardLayout.setLayoutY(scene.getHeight() / 3 + 30);
        pane.getChildren().add(boardLayout);
        return scene;
    }

    /**
     * Insert players to the screen
     *
     * @param userID Users ID
     * @param name Users name
     * @return True if the numberOfPlayer was seated
     */
    public boolean insertPlayer(int userID, String name) {
        this.names.put(userID, name);
        this.stackSizes.put(userID, 0L);

        if (userID == playerID) {
            PlayerLayout pLayout = new PlayerLayout(name);
            playerLayout = pLayout;
            pLayout.setLayoutX(scene.getWidth()/4);
            pLayout.setLayoutY(scene.getHeight()-190);
            pane.getChildren().addAll(pLayout);
            allPlayerLayouts.put(userID, pLayout);
        } else {
            int oppPosition = positions[opponentsAdded];
            OpponentLayout oppLayout = new OpponentLayout(name, oppPosition);

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
                logger.println("Error: " + numberOfPlayers + " players in game, GUI cannot set positions", Logger.MessageType.WARNINGS);
                return null;
        }
    }

    /**
     * Inserts a text field for the game log.
     * It is put in the lower, left corner.
     */
    public void insertLogField(){
        scrollPane.setMaxWidth(300);
        scrollPane.setMinWidth(300);
        scrollPane.setMaxHeight(100);
        scrollPane.setMinHeight(100);
        scrollPane.setLayoutX(5);
        scrollPane.setLayoutY(scene.getHeight() - 140);
        scrollPane.setContent(logField);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        pane.getChildren().add(scrollPane);
        scrollPane.setOpacity(0.9);
        logField.setPrefWidth(280);
        logField.setPadding(new Insets(5,5,5,5));

        logField.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue((Double) newValue);
        });

        logField.setFocusTraversable(false);

    }


    /**
     * Inserts an input field and a button for chatting with the other players
     */
    public void insertChatField(){
        chatField = ObjectStandards.makeTextFieldForGameScreen("");
        chatField.setMinWidth(225);
        chatField.setMaxWidth(300);
        chatField.setLayoutX(5);
        chatField.setLayoutY(scene.getHeight() - 40);
        chatField.setOpacity(0.9);

        Button sendTextButton = ObjectStandards.makeStandardButton("Send");
        sendTextButton.setLayoutX(230);
        sendTextButton.setLayoutY(scene.getHeight() - 40);
        sendTextButton.setFocusTraversable(false);

        pane.getChildren().addAll(chatField, sendTextButton);

        //Listener for chat field
        Runnable chatTask = (() -> {
            if (chatListener.isPresent())
                chatListener.get().accept(chatField.getText());
            chatField.setText("");
            pane.requestFocus();
        });

        //Set listeners for chat field
        chatField.setOnAction(event -> chatTask.run());
        sendTextButton.setOnAction(event -> chatTask.run());
    }

    /**
     * Adds text to the previously made log field.
     * @param printInfo The text to add to the field.
     */
    public void printToLogField(String printInfo){
        Text text = new Text();
        text.setText("\n" + printInfo);
        logField.getChildren().addAll(text);
    }

    /**
     * Adds text to the previously made log field.
     * @param printInfo The text to add to the field.
     */
    public void printChatToLogField(String printInfo){
        Text text = new Text();
        text.setText("\n" + printInfo);
        text.setStyle("-fx-font-weight: bold");
        logField.getChildren().addAll(text);
    }

    public void addMenuBarToGameScreen(){
        MenuBar menuBar = ObjectStandards.createMenuBar();
        menuBar.setLayoutX(0);
        menuBar.setLayoutY(0);
        pane.getChildren().add(menuBar);
    }

    /**
     * Displays the card pictures to the screen
     */
    public void setHandForUser(int userID, Card leftCard, Card rightCard) {
        // If you are sent hole cards for another numberOfPlayer, assume all hole cards will be sent soon
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
     * Shows the cards of the players around the table and display the winner(s)
     * @param holeCardsToShowdown cards of players to showdown
     * @param winnerString String containing who won the pot(s). '... won the main pot of ... ' etc.
     */
    public void showdown(Map<Integer, Card[]> holeCardsToShowdown, String winnerString) {
        //List<Player> playersToShowdown = showdownStats.getAllPlayers();
        printToLogField(holeCardsToShowdown.size() + " players to showdown");

        holeCardsToShowdown.forEach((id, cards) -> {
            //Print the players hand
            printToLogField(names.get(id) + ": " + cards[0] + " " + cards[1]);
        });

        boardLayout.setWinnerLabel(winnerString);

        //Print all community cards to in-game log
        printCommunityCardsToLogField();
        printToLogField(winnerString);
    }

    /**
     * Displays the first three cards (the flop) on the screen
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
        //showPercentages();

        new SoundPlayer().playDealCardSound();
        printToLogField("Flop " + card1 + " " + card2 + " " + card3);
    }

    /**
     * Displays the fourth card on the board
     */
    public void displayTurn(Card turnCard) {
        Image turnImage = new Image(ImageViewer.returnURLPathForCardSprites(turnCard.getCardNameForGui()));
        boardLayout.showTurn(turnImage);
        communityCards.add(turnCard);
        updateYourHandLabel();
        //showPercentages();

        printToLogField("Turn " + turnCard);
        soundPlayer.playDealCardSound();
    }

    /**
     * Displays the fifth card on the board
     */
    public void displayRiver(Card riverCard) {
        Image riverImage = new Image(ImageViewer.returnURLPathForCardSprites(riverCard.getCardNameForGui()));
        boardLayout.showRiver(riverImage);
        communityCards.add(riverCard);
        updateYourHandLabel();
        soundPlayer.playDealCardSound();
        //showPercentages();

        printToLogField("River " + riverCard);
    }

    /**
     * Show the players possible actions (buttons)
     * @param visible True if buttons should be visible
     */
    public void setActionsVisible(boolean visible) {
        playerLayout.setActionsVisible(visible);
    }

    /**
     * Update buttons and show any players last move
     *
     * @param ID Id of the numberOfPlayer that made the move
     * @param decision The decision that was made
     */
    public synchronized void playerMadeDecision(int ID, Decision decision) {
        //Remove numberOfPlayer highlighting
        allPlayerLayouts.get(ID).highlightTurn(false);

        //Update all values in the GUI and return a string of the decision that can be displayed
        final String finalDecision = evaluateDecision(ID, decision);

        //Set button texts depending on the action
        updateButtonTexts(ID, decision.move);

        //Play the appropriate sound for this decision
        soundPlayer.getSoundForDecision(decision.move);

        allPlayerLayouts.get(ID).setLastMove(finalDecision, getChipImage(ID));
        allPlayerLayouts.get(ID).setStackLabel("" + stackSizes.get(ID));
    }

    /**
     *  Update the buttons in the GUI depending on the last move made
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
     *  Update GUI and all numberOfPlayer values depending on the decision
     * @param ID  ID of the numberOfPlayer that made the decision
     * @param decision  The decision the numberOfPlayer made
     * @return  A String of the decision, example "Call 200" or "Fold"
     */
    private String evaluateDecision(int ID, Decision decision) {
        long newStackSize = stackSizes.get(ID);
        String decisionText = decision.move.toString() + " ";

        //Init putOnTable-map
            putOnTable.putIfAbsent(ID, 0L);

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

        //Update stack size of the numberOfPlayer that acted
        stackSizes.put(ID, newStackSize);
        return decisionText;
    }

    /**
     * Updates the stack size for all the players
     *
     * @param stackSizes map of stack sizes (id, size)
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
     * @param pot size of the pot
     */
    public void setPot(long pot) {
        String potString = Long.toString(pot);
        boardLayout.setPotLabel("Pot " + potString);
    }

    /**
     * Set name to all the players
     *
     * @param names Map containing all numberOfPlayer names (id, name)
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
        if (communityCards == null) {
            communityCards = new ArrayList<>();
        }
        else {
            communityCards.clear();
        }

        //Set opponent hands
        Image backImage = ImageViewer.getImage(ImageViewer.Image_type.CARD_BACK);
        allPlayerLayouts.forEach((id, layout) -> {
            if (!layout.isBust())
                layout.setCardImage(backImage, backImage);
            layout.setPercentLabel("");
        });

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

        Label endGameScreen = ObjectStandards.makeStandardLabelBlack(names.get(winnerID) + " has won the game!", "");
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
     * @param message The text to set
     */
    public void setAmountTextfield(String message) {
        playerLayout.setAmountTextField(message);
    }

    /**
     *  Set the border around the amount textfield to red, indicating an error
     * @param error True if textfield should be set to error-state
     */
    public void setErrorStateOfAmountTextField(boolean error) {
        if (error)
            playerLayout.setTextfieldStyle("-fx-border-color: rgba(255, 0, 0, 0.49) ; -fx-border-width: 3px ;");
        else
            playerLayout.setTextfieldStyle("-fx-border-color: rgb(255, 255, 255) ; -fx-border-width: 3px ;");
    }

    /**
     * Set the positions of the players. 0 = sb, 1 = bb, ...
     * @param positions Map of numberOfPlayer positions (id, pos)
     */
    public void setPositions(Map<Integer, Integer> positions) {
        positions.forEach((id, pos) -> {
            String posName = getPositionName(pos, numberOfPlayers);
            if (posName.equalsIgnoreCase("dealer"))
                allPlayerLayouts.get(id).setPositionLabel(posName, ImageViewer.getImage(ImageViewer.Image_type.DEALER_BUTTON));
            else
                allPlayerLayouts.get(id).setPositionLabel(posName, null);
        });
    }

    /**
     *  Turn an integer position into a string
     * @return Text version of the players position ("Dealer", "Small blind", ...)
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
     * @param playerID   ID of the numberOfPlayer that busted
     * @param rank       The place the numberOfPlayer came in
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
     * @param winnerID  The numberOfPlayer that was left in the hand
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

    /**
     *  Highlight the players turn (set glow effect on numberOfPlayer cards)
     */
    public void highlightPlayerTurn(int id) {
        if (allPlayerLayouts.get(id) != null)
            allPlayerLayouts.get(id).highlightTurn(true);
    }

    /**
     *  Start the remaining time progress bar
     * @param timeToThink Time it takes for the bar to time out
     */
    public void startTimer(long timeToThink, Decision.Move moveToExecute) {
        playerLayout.startTimer(this, timeToThink, moveToExecute);
    }
    public void stopTimer() {
        playerLayout.stopTimer();
    }

    // The thread that is currently calculating winning percentages. Only one thread should be modifying the GUI at the time
    private volatile Optional<Thread> winningPercentageComputer = Optional.empty();
    /**
     * If hole cards are shown, calculate percentages for all players
     */
    public void showPercentages(Map<Integer, Card[]> holeCardsStillInHand, List<Card> communityCards) {
        assert holeCardsStillInHand != null;

        Consumer<Map<Integer, Double>> callBack = (percentages) -> {
            // Make sure another, older thread cannot simultaneously modify the GUI
            if (winningPercentageComputer.isPresent() && winningPercentageComputer.get().getId() == Thread.currentThread().getId()) {
                Platform.runLater(() -> {
                    allPlayerLayouts.forEach((id, layout) -> layout.setPercentLabel(""));
                    percentages.forEach((id, pcnt) -> allPlayerLayouts.get(id).setPercentLabel((int) (pcnt * 100) + "%"));
                });
            }
        };

        winningPercentageComputer.ifPresent(Thread::interrupt);
        winningPercentageComputer = Optional.of(new Thread(() ->  {
            Map<Integer, Double> percentages = HandCalculator.getNewWinningPercentages(holeCardsStillInHand, communityCards, callBack);
            if (!Thread.currentThread().isInterrupted()) {
                callBack.accept(percentages);
                logger.println("Computed winning percentages for " + communityCards.size() + " community cards: "
                        + percentages.keySet().stream().map(id -> this.names.get(id) + ": " + percentages.get(id) + ", ").reduce("", String::concat), Logger.MessageType.DEBUG);
            }
        }));
        if (winningPercentageComputer.isPresent())
            winningPercentageComputer.get().start();
    }

    public void setChatListener(Consumer<String> chatListener) {
        this.chatListener = Optional.of(chatListener);
    }
}
