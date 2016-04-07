package gamelogic;

import java.util.*;

/**
 * A deck that starts shuffled, with all 52 cards
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
     * Copy constructor. Creates an exact copy of the deck, with the cards in the same order
     */
    public Deck(Deck deck) {
        cards = (ArrayList<Card>)deck.cards.clone();
    }

    /**
     * Draws the top card from the deck and returns it
     * Returns Empty if the deck is empty
     */
    public Optional<Card> draw() {
        if (cards.size() == 0) return Optional.empty();

        return Optional.of(cards.remove(cards.size() - 1));
    }

    /**
     * Shuffles the remaining cards
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    @Override
    public String toString() {
        ArrayList<Card> sortedCards = (ArrayList<Card>)cards.clone();
        sortedCards.sort(Card::compareTo);

        StringJoiner joiner = new StringJoiner("[", ", ", " ]");
        for (Card card : sortedCards) {
            joiner.add(card.toString());
        }
        return joiner.toString();
    }
}
