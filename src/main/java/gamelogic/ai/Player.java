package gamelogic.ai;

import gamelogic.Card;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by morten on 09.03.16.
 */
public class Player {
    public final int id;
    public final ArrayList<Card> holeCards;
    public int position;
    public long stackSize;
    public long minimumRaise; // If they want to raise, the minimum they need to raise by
    public long currentBet; // The amount they need to put on the table to remain in the hand

    public boolean isInHand = true;
    public boolean isAllIn = false;

    public Player(Player oldPlayer) {
        this.id = oldPlayer.id;
        this.holeCards = new ArrayList<>(oldPlayer.holeCards);
        this.position = oldPlayer.position;
        this.stackSize = oldPlayer.stackSize;
        this.minimumRaise = oldPlayer.minimumRaise;
        this.currentBet = oldPlayer.currentBet;
        this.isInHand = oldPlayer.isInHand;
        this.isAllIn = oldPlayer.isAllIn;
    }

    public Player(int id, int initialPosition, long initialStackSize) {
        holeCards = new ArrayList<>(2);

        this.id = id;

        position = initialPosition;
        stackSize = initialStackSize;
    }
}
