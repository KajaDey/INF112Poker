package main.java.gamelogic;

import java.util.List;
import java.util.Map;

/**
An interface which represents any poker game client,
 which receives information throughout the game, and returns its decisions
 */
public interface GameClient {

    /**
     * Tells the client to make a decision, and blocks until the decision is made and the client returns
     */
    Decision getDecision();

    int getID();

    /**
     * Sends the names of all players, as a map indexed by the players' IDs
     */
    void setPlayerNames(Map<Integer, String> names);

    /**
     * Sends the player's hole cards
     */
    void setHandForClient(int userID, Card card1, Card card2);

    /**
     * Sends the stack sizes of all players, as a map indexed by the players' IDs
     */
    void setStackSizes(Map<Integer, Long> stackSizes);

    /**
     * Sends the decision of another player
     */
    void playerMadeDecision(Integer playerId, Decision decision);

    /**
     * After a showdown, the client receives the hole cards of all the players still in the hand,
     * as a map indexed by the players' IDs
     */
    void showdown(Map<Integer, List<Card>> holeCards);

    /**
     * Sends the value of big blind
     */

    void setBigBlind(int bigBlind);

    /**
     * Sends the value of small blind
     */

    void setSmallBlind(int smallBlind);

    /**
     * Sends every player's position, as a map indexed by the players' IDs.
     * A value of 0 corresponds to the dealer, 1 is big blind, etc
     */

    void setPositions(Map<Integer, Integer> setPositions);

    /**
     * Sends amount of playGame-chips
     */

    void setStartChips(long startChips);

    /**
     * Sends amount of players in game
     */

    void setAmountOfPlayers(int amountOfPlayers);

    /**
     * Sends level duration
     */

    void setLevelDuration(int levelDuration);

    /**
     * Sends last move
     */

    void setLastMove(Map<Integer,Decision> lastMove);

    void setFlop(Card card1, Card card2, Card card3);
    void setTurn(Card turn);
    void setRiver(Card river);
}
