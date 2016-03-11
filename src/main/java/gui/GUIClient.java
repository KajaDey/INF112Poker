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

    public Thread t;

    //Needed variables
    private GameScreen gameScreen;
    private Decision decision;

    private Map<Integer,String> name;

    private Card card1;
    private Card card2;
    private Map<Integer,Long> stackSizes;

    private int id;
    private int bigBlind;
    private int smallBlind;
    private Map<Integer,Integer> position;
    private long startChips;
    private int amountOfPlayers;
    private int levelDuration;
    private Map<Integer, Decision> lastMove;
    private int pot;

    public GUIClient(int id, GameScreen gameScreen) {
        this.id = id;
        this.gameScreen = gameScreen;
    }

    public void setName(Map<Integer, String> name) {
        this.name = name;
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

    public void setDecision(Decision decision){
        this.decision = decision;
    }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {

    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        gameScreen.setHandForUser(userID, card1, card2);
    }

    @Override
    public void setFlop(Card card1, Card card2, Card card3) {
        gameScreen.displayFlop(card1, card2, card3);
    }

    @Override
    public void setTurn(Card turn) {
        gameScreen.displayTurn(turn);
    }

    @Override
    public void setRiver(Card river) {
        gameScreen.displayRiver(river);
    }


    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        this.stackSizes = stackSizes;
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        gameScreen.playerMadeDecision(playerId, decision);
    }

    @Override
    public void showdown(Map<Integer, List<Card>> holeCards) {

    }

    @Override
    public void setBigBlind(int bigBlind) {
        this.bigBlind = bigBlind;
    }

    @Override
    public void setSmallBlind(int smallBlind) {
        this.smallBlind = smallBlind;
    }

    @Override
    public void setPositions(Map<Integer, Integer> positions) {
        this.position = positions;
    }

    @Override
    public void setStartChips(long startChips) {
        this.startChips = startChips;
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        this.amountOfPlayers = amountOfPlayers;
    }

    @Override
    public void setLevelDuration(int levelDuration) {
        this.levelDuration = levelDuration;
    }

    @Override
    public void setLastMove(Map<Integer,Decision> lastMove) {
        this.lastMove = lastMove;
    }

    public void setPot(int pot){
        this.pot = pot;
    }

    public Map<Integer, String> getName() {
        return name;
    }

    public Card getCard1() {
        return card1;
    }

    public Card getCard2() {
        return card2;
    }

    public Map<Integer,Decision> getLastMove() {
        return lastMove;
    }

    public int getLevelDuration() {
        return levelDuration;
    }

    public long getStartChips() {
        return startChips;
    }

    public int getAmountOfPlayers() {
        return amountOfPlayers;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public Map<Integer,Integer> getPosition() {
        return position;
    }

    public int getPot() {
        return pot;
    }

    public int getID() {
        return id;
    }

    public Map<Integer, Long> getStackSizes() {
        return stackSizes;
    }
}
