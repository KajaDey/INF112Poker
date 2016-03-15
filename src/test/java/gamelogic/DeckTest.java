package gamelogic;

import gamelogic.Card;
import gamelogic.Deck;
import org.junit.Test;

import java.util.ArrayList;

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
    public void allCardsAreUnique() {
        Deck deck = new Deck();
        ArrayList<Card> allCards = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            allCards.add(deck.draw().get());
        }
        for (int i = 0; i < 52; i++) {
            for (int j = i + 1; j < 52; j++) {
                assertNotEquals(allCards.get(i), allCards.get(j));
            }
        }
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