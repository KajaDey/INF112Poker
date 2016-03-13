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
     * Sent once on game start
     */
    void setPlayerNames(Map<Integer, String> names);

    /**
     * Sends the player's hole cards
     * Sent once at the beginning of each hand
     */
    void setHandForClient(int userID, Card card1, Card card2);

    /**
     * Sends the stack sizes of all players, as a map indexed by the players' IDs
     * Sent at the start of every betting round
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
     * Sent at game start, and every time big blind is changed
     */
    void setBigBlind(int bigBlind);

    /**
     * Sends the value of small blind
     * Sent at game start, and every time small blind is changed
     */
    void setSmallBlind(int smallBlind);

    /**
     * Sends every player's position, as a map indexed by the players' IDs.
     * A value of 0 corresponds to the dealer, 1 is big blind, etc
     * Sent at the start of each hand
     */
    void setPositions(Map<Integer, Integer> setPositions);

    /**
     * Sends amount of players in game
     * Sent once when the game starts
     */
    void setAmountOfPlayers(int amountOfPlayers);

    /**
     * Sends level duration (How long each blind round lasts)
     * Sent at the start of each game
     */
    void setLevelDuration(int levelDuration);


    void setFlop(Card card1, Card card2, Card card3);
    void setTurn(Card turn);
    void setRiver(Card river);

    /**
     * Sent at the start of each hand, before hole cards etc are sent.
     */
    void startNewHand();
}
