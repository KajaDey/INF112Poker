package main.java.gui;

import main.java.gamelogic.Card;
import main.java.gamelogic.Decision;
import main.java.gamelogic.GameClient;

import java.util.List;
import java.util.Map;

/**
 * Created by kristianrosland on 10.03.2016.
 */
public class ClientGUI implements GameClient {


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
    public void setLastMove(Map<Integer, Decision> lastMove) {

    }
}
