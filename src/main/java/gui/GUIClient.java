package gui;
import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.GameClient;

import java.util.List;
import java.util.Map;

/**
 * Created by ady on 08/03/16.
 */


public class GUIClient implements GameClient {

    private GameScreen gameScreen;

    //Storage variables
    private long minimumRaise = 0, highestAmountPutOnTableThisBettingRound = 0;
    private Decision decision;
    private Map<Integer, Long> stackSizes;
    private long smallBlind, bigBlind;
    private int numberOfPlayers;

    private int id;

    public GUIClient(int id, GameScreen gameScreen, int maxNumberOfPlayers) {
        this.id = id;
        this.gameScreen = gameScreen;
        this.numberOfPlayers = maxNumberOfPlayers;
        gameScreen.setNumberOfPlayers(numberOfPlayers);
    }


    @Override
    public synchronized Decision getDecision(){
        //Make buttons visible
        gameScreen.setActionsVisible(true);

        try {
            wait();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Make buttons invisible
        gameScreen.setActionsVisible(false);

        //Return decision
        return decision;
    }

    /**
     * Called from ButtonListeners-class to nofify the client that a decision has been made
     *
     * @param move
     * @param moveSize
     */
    public synchronized void setDecision(Decision.Move move, long moveSize) {
        if ((move == Decision.Move.BET || move == Decision.Move.RAISE) && moveSize > stackSizes.get(id) ) {
            GUIMain.debugPrint("You don't have this much in your stack");
            gameScreen.setErrorStateOfAmountTextfield(true);
            return;
        }

        if (move == Decision.Move.RAISE && moveSize-highestAmountPutOnTableThisBettingRound < Math.max(bigBlind, minimumRaise) &&
                (moveSize != stackSizes.get(id))) {
            GUIMain.debugPrint("Raise is too small");
            gameScreen.setErrorStateOfAmountTextfield(true);
            return;
        }

        if (move == Decision.Move.BET && moveSize < bigBlind) {
            GUIMain.debugPrint("Bet is too small, must be a minimum of " + bigBlind);
            gameScreen.setErrorStateOfAmountTextfield(true);
            return;
        }

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
                    this.decision = new Decision(move, moveSize - highestAmountPutOnTableThisBettingRound);
                break;
            case CALL:case CHECK:case FOLD: this.decision = new Decision(move);
        }

        gameScreen.setErrorStateOfAmountTextfield(false);

        notifyAll();
    }

    public void setDecision(Decision.Move move) { setDecision(move, 0); }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        gameScreen.setNames(names);
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        gameScreen.setHandForUser(userID, card1, card2);
    }

    @Override
    public void setFlop(Card card1, Card card2, Card card3, long currentPotSize) {
        gameScreen.displayFlop(card1, card2, card3);
        newBettingRound(currentPotSize);
    }

    @Override
    public void setTurn(Card turn, long currentPotSize) {
        gameScreen.displayTurn(turn);
        newBettingRound(currentPotSize);
    }

    @Override
    public void setRiver(Card river, long currentPotSize) {
        gameScreen.displayRiver(river);
        newBettingRound(currentPotSize);
    }

    @Override
    public void startNewHand() {
        gameScreen.startNewHand();
        newBettingRound(0);
    }

    @Override
    public void playerBust(int playerID, int rank) {
        gameScreen.bustPlayer(playerID, rank);
    }

    @Override
    public void gameOver(int winnerID) {
        gameScreen.gameOver(winnerID);
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        this.stackSizes = stackSizes;
        gameScreen.updateStackSizes(stackSizes);

        //Updates the values of the slider
        gameScreen.updateSliderValues();
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        switch (decision.move) {
            case BET:case SMALL_BLIND: case BIG_BLIND:
                highestAmountPutOnTableThisBettingRound = decision.size;
                break;
            case RAISE:
                minimumRaise = decision.size;
                highestAmountPutOnTableThisBettingRound += decision.size;
                break;
            case ALL_IN:
                break;
        }
        gameScreen.playerMadeDecision(playerId, decision);
    }

    @Override
    public void showdown(List<Integer> playersStillPlaying, int winnerID, Map<Integer, Card[]> holeCards, long pot) {
        gameScreen.setPot(pot);
        gameScreen.showDown(playersStillPlaying, winnerID, holeCards);
    }

    @Override
    public void setBigBlind(long bigBlind) {
        this.bigBlind = bigBlind;
        gameScreen.setBigBlind(bigBlind);
    }

    @Override
    public void setSmallBlind(long smallBlind) {
        this.smallBlind = smallBlind;
        //TODO: Update label in GUI
    }

    @Override
    /**
     * Sends every player's position, as a map indexed by the players' IDs.
     * A value of 0 corresponds to the dealer, 1 is small blind, 2 is big blind etc
     * Sent at the start of each hand
     */
    public void setPositions(Map<Integer, Integer> positions) {
        gameScreen.setPositions(positions);
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        gameScreen.setNumberOfPlayers(amountOfPlayers);
    }

    @Override
    public void setLevelDuration(int levelDuration) {
        //TODO: Update label in GUI
    }

    public void newBettingRound(long potSize) {
        gameScreen.newBettingRound(potSize);
        minimumRaise = 0;
        highestAmountPutOnTableThisBettingRound = 0;
    }


    public int getID() {
        return id;
    }

    /**
     * Prints the log message to the log field
     * @param message The message to be printed
     */
    public void printToLogfield(String message) {
        gameScreen.printToLogField(message);
    }
}
