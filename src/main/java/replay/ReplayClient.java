package replay;

import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.Statistics;
import gui.GUIClient;
import gui.GameScreen;

import java.util.*;

/**
 *  Client used for replaying games from log files.
 *  Can be used both with and without game screen (gui).
 *  Returns
 *
 * @author Kristian Rosland
 */
public class ReplayClient extends GUIClient {

    private boolean gui;
    private ReplayReader replayReader;

    /**
     *  This constructor is used if the replay client should have an attached GUI.
     * @param id Client ID
     * @param gameScreen Game screen
     */
    public ReplayClient(int id, GameScreen gameScreen, ReplayReader replayReader) {
        super(id, gameScreen);
        gui = true;

        this.replayReader = replayReader;
    }

    /**
     *  This constructor is used if the replay client should not have an attached GUI.
     *  This object will not use any of the information passed to it.
     * @param id ID of this client
     */
    public ReplayClient(int id, ReplayReader replayReader) {
        super(id, null);
        gui = false;

        this.replayReader = replayReader;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("GUI-clients don't have their own names");
    }

    @Override
    /**
     * Get decision from client (read from replayReader)
     * @return The next decision in the replayed game, null if game ended
     */
    public synchronized Decision getDecision(long timeToThink){
        delay(1500);
        Optional<Decision> decision = replayReader.getNextDecision();

        if (!decision.isPresent()) {
            delay(5000);
            System.exit(0);
        }

        return decision.get();
    }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        if (gui)
            super.setPlayerNames(names);
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        if (gui)
            super.setHandForClient(userID, card1, card2);
    }

    @Override
    public void setFlop(Card card1, Card card2, Card card3) {
        if (gui)
            super.setFlop(card1, card2, card3);
    }

    @Override
    public void setTurn(Card turn) {
        if (gui)
            super.setTurn(turn);
    }

    @Override
    public void setRiver(Card river) {
        if (gui)
            super.setRiver(river);
    }

    @Override
    public void startNewHand() {
        if (gui)
            super.startNewHand();
    }

    @Override
    public void playerBust(int playerID, int rank) {
        if (gui)
            super.playerBust(playerID, rank);
    }

    @Override
    public void gameOver(Statistics stats) {
        if (gui)
            super.gameOver(stats);
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        if (gui)
            super.setStackSizes(stackSizes);
    }

    public void initGameState() {
        if (gui)
            super.initGameState();
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        if (gui)
            super.playerMadeDecision(playerId, decision);
    }

    @Override
    public void showdown(String[] winnerStrings) {
        if (gui)
            super.showdown(winnerStrings);
    }

    @Override
    public void setBigBlind(long bigBlind) {
        if (gui)
            super.setBigBlind(bigBlind);
    }

    @Override
    public void setSmallBlind(long smallBlind) {
        if (gui)
            super.setSmallBlind(smallBlind);
    }

    /**
     * Sends every player's position, as a map indexed by the players' IDs.
     * A value of 0 corresponds to the small blind, 1 is big blind..
     * Sent at the start of each hand
     */
    @Override
    public void setPositions(Map<Integer, Integer> positions) {
        if (gui)
            super.setPositions(positions);
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        if (gui)
            super.setAmountOfPlayers(amountOfPlayers);
    }

    @Override
    public void setLevelDuration(int levelDuration) {
        if (gui)
           super.setLevelDuration(levelDuration);
    }

    public void newBettingRound() {
        if (gui)
            super.newBettingRound();
    }

    /**
     * Prints the log message to the log field
     * @param message The message to be printed
     */
    public void printToLogField(String message) {
        if (gui)
            super.printToLogField(message);
    }

    public void preShowdownWinner(int winnerID) {
        if (gui)
            super.preShowdownWinner(winnerID);
    }

    private void delay(long delayTimeMillis) {
        try {
            Thread.sleep(delayTimeMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
