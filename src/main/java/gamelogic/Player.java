package main.java.gamelogic;

import java.util.Optional;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Player extends User {

    private Hand hand;
    private Table table;

    private int ID;
    private long stackSize;
    private long putOnTableThisRound = 0;

    public Player(String name, long stackSize, Table table, int ID) {
        super(name);
        this.stackSize = stackSize;
        this.table = table;
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
     * @param currentBet Amount TODO explain?
     */
    public void act(Decision decision, long currentBet) {
        switch (decision.move) {
            case BET:
                assert putOnTableThisRound == 0 : "Player " + ID + " bet while putOnTableThisRound was != 0";
                this.putOnTableThisRound = decision.size;
                break;
            case RAISE:
                long totalPutOnTable = ((currentBet-putOnTableThisRound) + decision.size);
                this.putOnTableThisRound += totalPutOnTable;
                break;
            case CALL:
                this.putOnTableThisRound = Math.min(currentBet, this.stackSize);
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
    public void setHand(Card card1, Card card2) {
        this.hand = new Hand(card1, card2, table.getCommunityCards());
        // TODO change
    }

    /**
     * Gets the player's hole cards as a string
     *
     * @return Player's hole cards
     */
    public String cardsOnHand() {
        return hand.getHoleCards().get(0) + " " + hand.getHoleCards().get(1);
    }

    /**
     * @return true if the player is still playing, else false
     */
    public boolean stillPlaying() {
        return stackSize > 0;
    }

    /**
     * Sets the total amount the player has put on table this betting round
     * @param amount Total amount put on table
     */
    public void setAmountPutOnTableThisBettingRound(long amount) {
        this.putOnTableThisRound = amount;
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
     * Removes amount player put on table this round from stack
     */
    public void updateStackSize() {
        stackSize -= putOnTableThisRound;
    }
}

