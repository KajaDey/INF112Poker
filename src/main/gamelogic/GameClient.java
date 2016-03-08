package main.gamelogic;

import java.util.List;
import java.util.Map;

/**

 */
public interface GameClient {

    public Decision getDecision();

    public void setPlayerNames(Map<Integer, String> names);

    public void setHoleCards(Card card1, Card card2);

    public void setStackSizes(Map<Integer, Long> stackSizes);

    public void playerMadeDecision(Map<Integer, Decision> decisions);

    public void showdown(Map<Integer, List<Card>> holeCards);
}
