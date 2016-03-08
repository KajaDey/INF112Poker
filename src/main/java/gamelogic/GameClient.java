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

    /**
     * Sends the names of all players, as a map indexed by the players' IDs
     */
    void setPlayerNames(Map<Integer, String> names);

    /**
     * Sends the player's hole cards
     */
    void setHoleCards(Card card1, Card card2);

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
     * Sends every players positions
     */

    void setPositions(Map<Integer, String> setPositions);

    /**
     * Sends amount of start-chips
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

    void setLastMove(String lastMove);
}
