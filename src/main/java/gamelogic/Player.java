package gamelogic;


import gui.GameSettings;

import java.util.List;

/**
 * Created by kristianrosland on 07.03.2016.
 *
 * Class to represent a player.
 */
public class Player extends User {
    private int ID;
    private long stackSize;
    private final GameSettings gameSettings;
    private long putOnTableThisRound = 0;
    private Card[] holeCards;
    private boolean allIn = false, hasActed = false;

    //Statistics
    private int handsWon = 0, handsPlayed = 0, foldsPreFlop = 0;
    private int numberOfAggressiveMoves = 0, numberOfPassiveMoves = 0;
    private Hand bestHand;

    public Player(String name, GameSettings gameSettings, int ID) {
        super(name);
        this.gameSettings = gameSettings;
        this.stackSize = gameSettings.getStartStack();
        this.ID = ID;
    }

    /**
     * @return Player ID
     */
    public int getID() {
        return ID;
    }

    /**
     * Execute a given decision
     *
     * @param decision Move to execute
     * @param highestAmountPutOnTable
     */
    public void act(Decision decision, long highestAmountPutOnTable, Pot pot, boolean preFlop) {

        long amountToCall = (highestAmountPutOnTable - putOnTableThisRound);
        switch (decision.move) {
            case SMALL_BLIND: case BIG_BLIND:
                long size = decision.move == Decision.Move.SMALL_BLIND ? gameSettings.getSmallBlind() : gameSettings.getBigBlind();
                putOnTableThisRound = size;
                stackSize -= size;
                pot.addToPot(ID, size);
                break;

            case FOLD:
                numberOfPassiveMoves++;
                if (preFlop) foldsPreFlop++;
                break;

            case CHECK:
                numberOfPassiveMoves++;
                break;

            case CALL:
                numberOfPassiveMoves++;
                amountToCall = Math.min(amountToCall, stackSize);
                stackSize -= amountToCall;
                putOnTableThisRound += amountToCall;
                pot.addToPot(ID, amountToCall);
                break;

            case BET:
                assert putOnTableThisRound == 0;
                numberOfAggressiveMoves++;
                this.putOnTableThisRound = decision.getSize();
                stackSize -= decision.getSize();
                pot.addToPot(ID, decision.getSize());
                break;

            case RAISE:
                numberOfAggressiveMoves++;
                stackSize -= (amountToCall + decision.getSize());
                putOnTableThisRound = highestAmountPutOnTable + decision.getSize();
                pot.addToPot(ID, amountToCall + decision.getSize());
                break;

            case ALL_IN:
                numberOfAggressiveMoves++;
                pot.addToPot(ID, stackSize);
                putOnTableThisRound += stackSize;
                stackSize = 0;
                allIn = true;
                break;
        }
        assert stackSize >= 0 : this.getName() + " received stackSize " + stackSize + " after " + decision + ", amountToCall=" + amountToCall + ", putOnTableThisRound=" + putOnTableThisRound;

        //If a player has posted blind he should not be marked as if he as acted this betting round
        if (decision.move != Decision.Move.SMALL_BLIND && decision.move != Decision.Move.BIG_BLIND)
            hasActed = true;
    }

    /**
     * Gets the total amount the player has put on table this round
     *
     * @return Amount put on table
     */
    public long putOnTable() {
        return putOnTableThisRound;
    }

    /**
     * Gets the player's stack size
     *
     * @return Stack size
     */
    public long getStackSize() {
        return stackSize;
    }

    /**
     * Sets the player's hole cards
     *
     * @param card1 Card one in the hand
     * @param card2 Card two in the hand
     */
    public void setHoleCards(Card card1, Card card2) {
        handsPlayed++;
        this.holeCards = new Card[2];
        holeCards[0] = card1;
        holeCards[1] = card2;
        this.putOnTableThisRound = 0;
        this.allIn = false;
        this.hasActed = false;
    }

    /**
     *  Returns the players hole cards
     * @return Hole cards represented as a Card-array of length 2
     */
    public Card[] getHoleCards() {
        assert holeCards != null : "Hole cards were null when Game asked for them";
        return holeCards;
    }

    /**
     * Increments the player's stack with given amount
     *
     * @param size Amount added to stack
     */
    public void incrementStack(long size) {
        stackSize += size;
    }

    /**
     * Called when a new betting round starts, to reset the betting limitations for this player
     */
    public void newBettingRound() {
        this.putOnTableThisRound = 0;
        hasActed = false;
    }

    /**
     * @return true if the player is currently all in
     */
    public boolean isAllIn() {
        return allIn;
    }

    /**
     *
     * @param communityCards
     * @return
     */
    public Hand getHand(List<Card> communityCards) {
        return new Hand(holeCards[0], holeCards[1], communityCards);
    }

    /**
     * @return True if the player has acted this round (or is all in)
     */
    public boolean hasActed() {
        return allIn || hasActed;
    }

    /**
     *  Notify the player when he wins a hand (or a side pot)
     * @param hand The hand the player won with
     * @param pot  The size of the (side)pot he won
     */
    public void handWon(Hand hand, long pot) {
        handsWon++;

        //Update the best hand
        if (bestHand == null)
            bestHand = hand;
        else if (hand.compareTo(bestHand) > 0)
            bestHand = hand;

        incrementStack(pot);
    }

    /** Statistics-getters  **/
    public int handsWon() { return handsWon; }
    public int handsPlayed() { return handsPlayed; }
    public int preFlopFolds () { return foldsPreFlop; }
    public int aggressiveMoves() { return numberOfAggressiveMoves; }
    public int passiveMoves() { return numberOfPassiveMoves; }
    public String getBestHand() { return bestHand == null ? "<no hands won>" : new HandCalculator(bestHand).getBestHandString(); }

}

