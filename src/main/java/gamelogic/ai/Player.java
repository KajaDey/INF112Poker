package main.java.gamelogic.ai;

import main.java.gamelogic.Card;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by morten on 09.03.16.
 */
public class Player {
    public final int id;
    public final ArrayList<Optional<Card>> holeCards;
    public int position;
    public long stackSize;
    public long minimumBetThisBettingRound; // The amount the player needs to put on the table to remain in the hand

    public Player(int id, int initialPosition, long initialStackSize) {
        holeCards = new ArrayList<>();
        holeCards.add(Optional.empty());
        holeCards.add(Optional.empty());

        this.id = id;

        position = initialPosition;
        stackSize = initialStackSize;
    }
}
