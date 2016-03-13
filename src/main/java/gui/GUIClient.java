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
    private Decision decision;

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

    public synchronized void decisionMade() {
        notifyAll();
    }

    public void setDecision(Decision decision) { this.decision = decision; }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        for (Integer id : names.keySet()) {
            gameScreen.setName(id, names.get(id));
        }
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        gameScreen.setHandForUser(userID, card1, card2);
        gameScreen.newBettingRound(0);
    }

    @Override
    public void setFlop(Card card1, Card card2, Card card3, long currentPotSize) {
        gameScreen.displayFlop(card1, card2, card3);
        gameScreen.newBettingRound(currentPotSize);
    }

    @Override
    public void setTurn(Card turn, long currentPotSize) {
        gameScreen.displayTurn(turn);
        gameScreen.newBettingRound(currentPotSize);
    }

    @Override
    public void setRiver(Card river, long currentPotSize) {
        gameScreen.displayRiver(river);
        gameScreen.newBettingRound(currentPotSize);
    }

    @Override
    public void startNewHand() {
        gameScreen.startNewHand();
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        gameScreen.updateStackSizes(stackSizes);
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        gameScreen.playerMadeDecision(playerId, decision);
    }

    @Override
    public void showdown(Map<Integer, List<Card>> holeCards) {
        //TODO: Update label in GUI
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

    public int getID() {
        return id;
    }

}
