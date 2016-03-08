package main.java.gui;

import main.java.gamelogic.Card;
import main.java.gamelogic.Decision;
import main.java.gamelogic.GameClient;

import java.util.List;
import java.util.Map;

/**
 * Created by ady on 08/03/16.
 */
public class GameControllerComunication implements GameClient {
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
    public void playerMadeDecision(Map<Integer, Decision> decisions) {

    }

    @Override
    public void showdown(Map<Integer, List<Card>> holeCards) {

    }
}
