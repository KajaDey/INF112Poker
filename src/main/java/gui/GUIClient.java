package main.java.gui;

import com.apple.laf.ScreenMenuBar;
import javafx.stage.Screen;
import main.java.gamelogic.Card;
import main.java.gamelogic.Decision;
import main.java.gamelogic.GameClient;

import java.util.List;
import java.util.Map;

/**
 * Created by ady on 08/03/16.
 */


public class GUIClient extends ButtonListeners implements GameClient {

    private Map<Integer,String> name;

    private Card card1;
    private Card card2;
    private Map<Integer,Long> stackSizes;

    private int id;
    private int bigBlind;
    private int smallBlind;
    private Map<Integer,Integer> position;
    private int startChips;
    private int amountOfPlayers;
    private int levelDuration;
    private Map<Integer, Decision> lastMove;
    private int pot;


    public GUIClient(int id) {
        this.id = id;
    }

    public void setName(Map<Integer, String> name) {
        this.name = name;
        SceneBuilder.updateGameScreen(this);
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
        this.card1 = card1;
        this.card2 = card2;
        SceneBuilder.updateGameScreen(this);
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        this.stackSizes = stackSizes;
        SceneBuilder.updateGameScreen(this);
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {

    }

    @Override
    public void showdown(Map<Integer, List<Card>> holeCards) {

    }

    @Override
    public void setBigBlind(int bigBlind) {
        this.bigBlind = bigBlind;
        SceneBuilder.updateGameScreen(this);
    }

    @Override
    public void setSmallBlind(int smallBlind) {
        this.smallBlind = smallBlind;
        SceneBuilder.updateGameScreen(this);
    }

    @Override
    public void setPositions(Map<Integer, Integer> positions) {
        this.position = positions;
        SceneBuilder.updateGameScreen(this);
    }

    @Override
    public void setStartChips(long startChips) {
        this.startChips = smallBlind;
        SceneBuilder.updateGameScreen(this);
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        this.amountOfPlayers = amountOfPlayers;
        SceneBuilder.updateGameScreen(this);
    }

    @Override
    public void setLevelDuration(int levelDuration) {
        this.levelDuration = levelDuration;
        SceneBuilder.updateGameScreen(this);
    }

    @Override
    public void setLastMove(Map<Integer,Decision> lastMove) {
        this.lastMove = lastMove;
        SceneBuilder.updateGameScreen(this);
    }

    public void setPot(int pot){
        this.pot = pot;
        SceneBuilder.updateGameScreen(this);
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

    public Map<Integer,Integer> getPosition() {
        return position;
    }

    public int getPot() {
        return pot;
    }

    public int getId() {
        return id;
    }

    public Map<Integer, Long> getStackSizes() {
        return stackSizes;
    }
}