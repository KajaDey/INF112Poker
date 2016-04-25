package gamelogic;

import java.util.Map;

/**
An interface which represents any poker game client,
 which receives information throughout the game, and returns its decisions
 */
public interface GameClient {

    /**
     * Tells the client to make a decision, and blocks until the decision is made and the client returns
     * @param timeToThink Maximum amount of time the client has to think, in milliseconds.
     */
    Decision getDecision(long timeToThink);

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
     * Sends a decision made by any player. Is sent to all players, including the player that made the decision
     */
    void playerMadeDecision(Integer playerId, Decision decision);

    /**
     * After a showdown, the client receives the hole cards of all the players still in the hand,
     * as a map indexed by the players' IDs
     */
    void showdown(ShowdownStats showdownStats);

    /**
     * Sends the value of big blind
     * Sent at game start, and every time big blind is changed
     */
    void setBigBlind(long bigBlind);

    /**
     * Sends the value of small blind
     * Sent at game start, and every time small blind is changed
     */
    void setSmallBlind(long smallBlind);

    /**
     *  Sends every player's position, as a map indexed by the players' IDs.
     *  A value of 0 corresponds to the dealer, 1 is small blind, 2 is big blind etc
     *  Sent at the start of each hand
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

    /**
     *   Called every time a new flop is displayed
     * @param card1, card2, card3
     */
    void setFlop(Card card1, Card card2, Card card3);

    /**
     *   Called every time a the turn is displayed
     * @param turn
     */
    void setTurn(Card turn);

    /**
     *   Called every time a the river is displayed
     * @param river
     */
    void setRiver(Card river);

    /**
     * Sent at the start of each hand, before hole cards etc are sent.
     */
    void startNewHand();

    /**
     *  Sent every time a player busts
     * @param playerID
     * @param rank      The place the busted player finished in
     */
    void playerBust(int playerID, int rank);

    /**
     *  Called when the game ends (only 1 player has chips left)
     * @param winnerID ID of winner
     */
    void gameOver(Statistics winnerID);
}
