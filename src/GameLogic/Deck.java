package GameLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * A shuffled deck that starts with all 52 cards
 */
public class Deck {

    private final ArrayList<Card> cards;

    /**
     * Creates a new, shuffled deck
     */
    public Deck() {
        cards = new ArrayList<>(Arrays.asList(Card.getAllCards()));
        Collections.shuffle(cards);
    }

    /**
     * Draws the top card from the deck and returns it
     * @throws IndexOutOfBoundsException if the deck is empty
     */
    public Card draw() {
        return cards.remove(cards.size() - 1);
    }
}
