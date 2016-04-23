package gamelogic.ai;

import gamelogic.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a player. Used by the AI to store data about itself and its opponents
 */
public class Player {
    public final int id;
    public final List<Card> holeCards;
    public int position;
    public long stackSize;
    public long minimumRaise; // If they want to raise, the minimum they need to raise by
    public long currentBet; // The amount they need to put on the table to remain in the hand
    public long contributedToPot = 0; // Amount contributed to the pot throughout this entire hand

    public boolean isInHand = true;
    public boolean isAllIn = false;
    public final String name;

    public Player(int id, int initialPosition, long initialStackSize, String name) {
        holeCards = new ArrayList<>(2);

        this.name = name;
        this.id = id;

        position = initialPosition;
        stackSize = initialStackSize;
    }

    public Player(Player oldPlayer) {
        this.id = oldPlayer.id;
        this.name = oldPlayer.name;
        this.holeCards = new ArrayList<>(oldPlayer.holeCards);
        this.position = oldPlayer.position;
        this.stackSize = oldPlayer.stackSize;
        this.minimumRaise = oldPlayer.minimumRaise;
        this.currentBet = oldPlayer.currentBet;
        this.contributedToPot = oldPlayer.contributedToPot;

        this.isInHand = oldPlayer.isInHand;
        this.isAllIn = oldPlayer.isAllIn;
    }

    public void putInPot(long amount) {
        stackSize -= amount;
        contributedToPot += amount;
        assert stackSize >= 0 : "Player " + name + " with id " + id + " at position " + position + " has a stacksize of " + stackSize;
    }

    /**
     * A measure of the amount of risk the player is taking
     * The AI may use this to guess whether the opponent has good cards or not
     * @param totalChipsOnTable Total amount of chips in play, including the pot and everyone's stacks.
     * @return A number between 0 and 1,
     */
    public double riskTaken(long totalChipsOnTable) {
        return Math.pow( 0.666 * (double)contributedToPot / (stackSize + contributedToPot) + 0.333 * (double)contributedToPot / totalChipsOnTable, 0.333);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (id != player.id) return false;
        if (position != player.position) return false;
        if (stackSize != player.stackSize) return false;
        if (minimumRaise != player.minimumRaise) return false;
        if (currentBet != player.currentBet) return false;
        if (isInHand != player.isInHand) return false;
        if (isAllIn != player.isAllIn) return false;
        return holeCards.equals(player.holeCards);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + holeCards.hashCode();
        result = 31 * result + position;
        result = 31 * result + (int) (stackSize ^ (stackSize >>> 32));
        result = 31 * result + (int) (minimumRaise ^ (minimumRaise >>> 32));
        result = 31 * result + (int) (currentBet ^ (currentBet >>> 32));
        result = 31 * result + (isInHand ? 1 : 0);
        result = 31 * result + (isAllIn ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Player " + name + "(id=" + id + ", position=" + position + ",  currentBet=" + currentBet + ", stackSize=" + stackSize + ", " + (holeCards.size() == 2 ? holeCards : "") + (isAllIn ? " A" : "") + (!isInHand ? " X" : "") + ")";
    }
}
