package main.java.gui;
import main.java.gamelogic.Card;
import main.java.gamelogic.Decision;
import main.java.gamelogic.GameClient;

import java.util.List;
import java.util.Map;

/**
 * Created by ady on 08/03/16.
 */


public class GUIClient implements GameClient {

    //Needed variables
    private GameScreen gameScreen;
    private long currentBet = 0;
    private Decision decision;
    private Map<Integer, Long> stackSizes;

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
        if (moveSize > stackSizes.get(id)) {
            //Display error: "You don't have this much in your stack" and return without notifying
            System.out.println("You dont have this much in your stack");
            return;
        }

        switch (move) {
            case BET:
                this.decision = new Decision(move, moveSize);
                break;
            case RAISE:
                this.decision = new Decision(move, moveSize - currentBet);
                break;
            case CALL:case CHECK:case FOLD: this.decision = new Decision(move);
        }

        notifyAll();
    }

    public void setDecision(Decision.Move move) { setDecision(move, 0); }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        for (Integer id : names.keySet()) {
            gameScreen.setName(id, names.get(id));
        }
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
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        this.stackSizes = stackSizes;
        gameScreen.updateStackSizes(stackSizes);
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        switch (decision.move) {
            case BET: currentBet = decision.size; break;
            case RAISE: currentBet += decision.size; break;
        }
        gameScreen.playerMadeDecision(playerId, decision);
    }

    @Override
    public void showdown(Map<Integer, List<Card>> holeCards) {

    }

    @Override
    public void setBigBlind(int bigBlind) {
        //TODO: Update label in GUI
    }

    @Override
    public void setSmallBlind(int smallBlind) {
        //TODO: Update label in GUI
    }

    @Override
    public void setPositions(Map<Integer, Integer> positions) {
        //TODO: Update label in GUI
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
        currentBet = 0;
    }


    public int getID() {
        return id;
    }

}
