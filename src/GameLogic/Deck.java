package GameLogic;

import java.util.*;

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
     * Returns Empty if the deck is empty
     */
    public Optional<Card> draw() {
        if (cards.size() == 0) return Optional.empty();

        return Optional.of(cards.remove(cards.size() - 1));
    }
}
