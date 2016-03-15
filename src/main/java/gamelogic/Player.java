package gamelogic;

import java.util.Optional;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Player extends User {
    private int ID;
    private long stackSize;
    private long putOnTableThisRound = 0;
    private Card[] holeCards;
    private boolean folded = false;

    public Player(String name, long stackSize, int ID) {
        super(name);
        this.stackSize = stackSize;
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
    public void act(Decision decision, long highestAmountPutOnTable) {
        switch (decision.move) {
            case SMALL_BLIND: case BIG_BLIND:
                putOnTableThisRound += Math.min(stackSize, decision.size);
                stackSize -= Math.min(stackSize, decision.size);
                System.out.println("Player " + this.getName() + " posted his " + decision.move.toString().toLowerCase());
                break;

            case FOLD:
                this.folded = true;
                break;

            case CHECK:
                break;

            case BET:
                assert putOnTableThisRound == 0;
                break;

            case RAISE:
                break;

            case ALL_IN:
                break;
        }
    }

    /**
     * Gets the total amount the player has put on table this round
     *
     * @return Amount put on table
     */
    public long getAmountPutOnTableThisBettingRound() {
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
        this.holeCards = new Card[2];
        holeCards[0] = card1;
        holeCards[1] = card2;
    }

    /**
     *  Returns the players hole cards
     * @return Hole cards represented as a Card-array of length 2
     */
    public Card[] getHoleCards() {
        assert holeCards != null : "Hole cards where null when Game asked for them";
        return holeCards;
    }

    /**
     * @return true if the player is still playing, else false
     */
    public boolean stillPlaying() {
        return !folded;
    }


    /**
     * Increments the player's stack with given amount
     *
     * @param size Amount added to stack
     */
    public void incrementStack(long size) {
        stackSize += size;
    }

}

