package tests.gamelogic;

import main.gamelogic.Card;
import main.gamelogic.Deck;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeckTest {

    @Test
    public void drawAllCards() {
        Deck deck = new Deck();
        for (int i = 0; i < 52; i++) {
            assertTrue(deck.draw().isPresent());
        }
        assertTrue(!deck.draw().isPresent());
    }

    @Test
    // Gives a weak indication that the deck is properly shuffled
    public void deckStartsAndEndsWithADifferentCardEachTime() {
        Card[] firstAndLastCards = firstAndLastCards(new Deck());

        for (int i = 0; i < 10; i++) {
            Card[] newFirstAndLastCards = firstAndLastCards(new Deck());

            if (firstAndLastCards[0] != newFirstAndLastCards[0]
                    && firstAndLastCards[1] != newFirstAndLastCards[1]) {
                return;
            }
        }
        fail();
    }

    // Returns a two-element array containing the first and last card from the deck
    public Card[] firstAndLastCards(Deck deck) {
        Card firstCard = deck.draw().get();
        for (int i = 0; i < 50; i++) {
            deck.draw();
        }
        Card lastCard = deck.draw().get();
        return new Card[]{firstCard, lastCard};
    }
}