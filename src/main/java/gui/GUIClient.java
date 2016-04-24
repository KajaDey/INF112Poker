package gui;

import gamelogic.*;
import javafx.application.Platform;
import java.util.Map;

/**
 * Created by ady on 08/03/16.
 */


public class GUIClient implements GameClient {

    private GameScreen gameScreen;

    //Storage variables
    private long minimumRaise = 0, highestAmountPutOnTable = 0;
    private Decision decision;
    private Map<Integer, Long> stackSizes;
    private long smallBlind, bigBlind;
    private int numberOfPlayers;

    private int id;

    public GUIClient(int id, GameScreen gameScreen, int maxNumberOfPlayers) {
        this.id = id;
        this.gameScreen = gameScreen;
        this.numberOfPlayers = maxNumberOfPlayers;
        Platform.runLater(() -> gameScreen.setNumberOfPlayers(numberOfPlayers));
    }


    @Override
    public synchronized Decision getDecision(long timeToThink){
        //Make buttons visible
        Decision.Move moveIfTimeRunOut = highestAmountPutOnTable == 0 ? Decision.Move.CHECK : Decision.Move.FOLD;
        Platform.runLater(() -> {
            gameScreen.setActionsVisible(true);
            gameScreen.startTimer(timeToThink, moveIfTimeRunOut);
        });


        try {
            wait();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Make buttons invisible
        Platform.runLater(() -> gameScreen.setActionsVisible(false));

        //Return decision
        return decision;
    }

    /**
     * Called from ButtonListeners-class to notify the client that a decision has been made
     *
     * @param move
     * @param moveSize
     */
    public synchronized void setDecision(Decision.Move move, long moveSize) {
        if (!validMove(move, moveSize))
            return;

        switch (move) {
            case BET:
                if (moveSize == stackSizes.get(this.id))
                    this.decision = new Decision(Decision.Move.ALL_IN);
                else
                    this.decision = new Decision(move, moveSize);
                break;
            case RAISE:
                if (moveSize == stackSizes.get(this.id))
                    this.decision = new Decision(Decision.Move.ALL_IN);
                else
                    this.decision = new Decision(move, moveSize - highestAmountPutOnTable);
                break;
            case CALL:case CHECK:case FOLD: this.decision = new Decision(move);
        }

        Platform.runLater(() -> gameScreen.setErrorStateOfAmountTextField(false));

        notifyAll();
    }

    /**
     *  Check if a decision is valid (according to current stack size etc)
     * @param move
     * @param moveSize
     * @return
     */
    private boolean validMove(Decision.Move move, long moveSize) {
        if ((move == Decision.Move.BET || move == Decision.Move.RAISE) && moveSize > stackSizes.get(id) ) {
            GUIMain.debugPrintln("You don't have this much in your stack. Stack size=" + stackSizes.get(id) + ", moveSize=" + moveSize);
            Platform.runLater(() -> gameScreen.setErrorStateOfAmountTextField(true));
            return false;
        }
        else if (move == Decision.Move.RAISE && moveSize- highestAmountPutOnTable < Math.max(bigBlind, minimumRaise) &&
                (moveSize != stackSizes.get(id))) {
            GUIMain.debugPrint("Raise is too small");
            Platform.runLater(() -> gameScreen.setErrorStateOfAmountTextField(true));
            return false;
        }
        else if (move == Decision.Move.BET && moveSize < bigBlind) {
            GUIMain.debugPrint("Bet is too small, must be a minimum of " + bigBlind);
            Platform.runLater(() -> gameScreen.setErrorStateOfAmountTextField(true));
            return false;
        }

        return true;
    }

    public void setDecision(Decision.Move move) { setDecision(move, 0); }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        Platform.runLater(() ->gameScreen.setNames(names));
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        Platform.runLater(() -> gameScreen.setHandForUser(userID, card1, card2));
    }

    @Override
    public void setFlop(Card card1, Card card2, Card card3) {
        Platform.runLater(() -> gameScreen.displayFlop(card1, card2, card3));
        newBettingRound();
    }

    @Override
    public void setTurn(Card turn) {
        Platform.runLater(() -> gameScreen.displayTurn(turn));
        newBettingRound();
    }

    @Override
    public void setRiver(Card river) {
        Platform.runLater(() -> gameScreen.displayRiver(river));
        newBettingRound();
    }

    @Override
    public void startNewHand() {
        Platform.runLater(() -> gameScreen.startNewHand());
        newBettingRound();
    }

    @Override
    public void playerBust(int playerID, int rank) {
        Platform.runLater(() -> gameScreen.bustPlayer(playerID, rank));
    }

    @Override
    public void gameOver(Statistics stats) {
        Platform.runLater(() -> gameScreen.gameOver(stats));
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        this.stackSizes = stackSizes;
        Platform.runLater(() -> {
            gameScreen.updateStackSizes(stackSizes);

            //Updates the values of the slider
            gameScreen.updateSliderValues();
        });
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        switch (decision.move) {
            case SMALL_BLIND: case BIG_BLIND:
                highestAmountPutOnTable = decision.move == Decision.Move.BIG_BLIND ? bigBlind : smallBlind;
                break;
            case BET:
                highestAmountPutOnTable = decision.getSize();
                break;
            case RAISE:
                minimumRaise = decision.getSize();
                highestAmountPutOnTable += decision.getSize();
                break;
            case ALL_IN:
                break;
        }
        Platform.runLater(() -> gameScreen.playerMadeDecision(playerId, decision));
    }

    @Override
    public void showdown(ShowdownStats showdownStats) {
        Platform.runLater(() -> gameScreen.showdown(showdownStats));
    }

    @Override
    public void setBigBlind(long bigBlind) {
        this.bigBlind = bigBlind;
        Platform.runLater(() -> gameScreen.setBigBlind(bigBlind));
    }

    @Override
    public void setSmallBlind(long smallBlind) {
        this.smallBlind = smallBlind;
        Platform.runLater(() -> gameScreen.setSmallBlind(smallBlind));
    }


    /**
     * Sends every player's position, as a map indexed by the players' IDs.
     * A value of 0 corresponds to the small blind, 1 is big blind..
     * Sent at the start of each hand
     */
    @Override
    public void setPositions(Map<Integer, Integer> positions) {
        Platform.runLater(() -> gameScreen.setPositions(positions));
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        Platform.runLater(() -> gameScreen.setNumberOfPlayers(amountOfPlayers));
    }

    @Override
    public void setLevelDuration(int levelDuration) {
    }

    public void newBettingRound() {
        minimumRaise = 0;
        highestAmountPutOnTable = 0;
        Platform.runLater(() -> gameScreen.newBettingRound());
    }


    public int getID() {
        return id;
    }

    /**
     * Prints the log message to the log field
     * @param message The message to be printed
     */
    public void printToLogField(String message) {

        Platform.runLater(() -> gameScreen.printToLogField(message));
    }

    public void preShowdownWinner(int winnerID) {
        Platform.runLater(() -> gameScreen.preShowdownWinner(winnerID));
    }

    public void showHoleCards(Map<Integer, Card[]> holeCards) {
        Platform.runLater(() -> gameScreen.showHoleCards(holeCards));
    }

    public void highlightPlayerTurn(int id) {
        gameScreen.highlightPlayerTurn(id);
    }
}
