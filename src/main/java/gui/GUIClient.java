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

    private Map<Integer,String> name;
    private Card card1;
    private Card card2;
    private Map<Integer,Long> stackSizes;

    private int bigBlind;
    private int smallBlind;
    private String position;
    private int startChips;
    private int amountOfPlayers;
    private int levelDuration;
    private String lastMove;

    public GUIClient() {

    }

    @Override
    public Decision getDecision() {
        return null;
    }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {

    }

    @Override
    public void setHoleCards(Card card1, Card card2) {

    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {

    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {

    }

    @Override
    public void showdown(Map<Integer, List<Card>> holeCards) {

    }

    @Override
    public void setBigBlind(int bigBlind) {

    }

    @Override
    public void setSmallBlind(int smallBlind) {

    }

    @Override
    public void setPositions(Map<Integer, Integer> setPositions) {

    }

    @Override
    public void setStartChips(long startChips) {

    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {

    }

    @Override
    public void setLevelDuration(int levelDuration) {

    }

    @Override
    public void setLastMove(String lastMove) {

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

    public String getLastMove() {

        return lastMove;
    }

    public int getLevelDuration() {
        return levelDuration;
    }

    public int getStartChips() {
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

    public String getPosition() {
        return position;
    }

}
