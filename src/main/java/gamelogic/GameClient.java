package main.java.gamelogic;

import java.util.List;
import java.util.Map;

/**
A game client 
 */
public interface GameClient {

    /*
    Tells the client to make a decision, and blocks until the decision is made and the client returns
     */
    public Decision getDecision();

    /*
    Sends the names of all players, as a map indexed by the players' IDs
     */
    public void setPlayerNames(Map<Integer, String> names);

    /*
    Sends the player's hole cards
     */
    public void setHoleCards(Card card1, Card card2);

    /*
    Sends the stack sizes of all players, as a map indexed by the players' IDs
     */
    public void setStackSizes(Map<Integer, Long> stackSizes);

    /*
    Sends the decision of another player
     */
    public void playerMadeDecision(Integer playerId, Decision decision);

    /*
    After a showdown, the client receives the hole cards of all the players still in the hand,
    as a map index by the players' IDs
     */
    public void showdown(Map<Integer, List<Card>> holeCards);
}
