package gui;
import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.GameClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ady on 08/03/16.
 */


public class GUIClient implements GameClient {

    private GameScreen gameScreen;

    //Storage variables
    private Map<Integer, Long> amountPutOnTableThisBettingRound;
    private long minimumRaise = 0;
    private Decision decision;
    private Map<Integer, Long> stackSizes;
    private long smallBlind, bigBlind;

    private int id;

    public GUIClient(int id, GameScreen gameScreen) {
        this.id = id;
        this.gameScreen = gameScreen;
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
        System.out.println("Decision " + move + " " + moveSize);

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
    public void gameOver(int winnerID) {
        gameScreen.gameOver(winnerID);
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        this.stackSizes = stackSizes;
        gameScreen.updateStackSizes(stackSizes);
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        switch (decision.move) {
            //case BET: currentBet = decision.size; break;
            //case RAISE: currentRaise = decision.size; currentBet += currentRaise; break;
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
        //TODO: Update label in GUI
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
        //TODO: Update label in GUI
    }

    @Override
    public void setLevelDuration(int levelDuration) {
        //TODO: Update label in GUI
    }

    public void newBettingRound(long potSize) {
        gameScreen.newBettingRound(potSize);
        minimumRaise = 0;
        amountPutOnTableThisBettingRound = new HashMap<>();
    }


    public int getID() {
        return id;
    }

}
