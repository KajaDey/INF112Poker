package GameLogic;

import org.junit.Test;

import static org.junit.Assert.*;

public class CardTest {

    Card twoOfHearts = Card.of(2, Card.Suit.HEARTS).get();
    Card aceOfClubs = Card.of(14, Card.Suit.CLUBS).get();

    @Test
    public void testThatCorrectCardIsReturned() throws Exception {
        assertEquals(twoOfHearts.rank, 2);
        assertEquals(twoOfHearts.suit, Card.Suit.HEARTS);

        assertEquals(aceOfClubs.rank, 14);
        assertEquals(aceOfClubs.suit, Card.Suit.CLUBS);
    }

    @Test
    public void testThatTwoCardsAreTheSameObject() {
        assertTrue(twoOfHearts == Card.of(2, Card.Suit.HEARTS).get());
    }

    @Test
    public void testCompareTo() throws Exception {
        assertTrue(twoOfHearts.compareTo(aceOfClubs) < 0);
        assertTrue(aceOfClubs.compareTo(twoOfHearts) > 0);
        assertTrue(twoOfHearts.compareTo(twoOfHearts) == 0);
    }
}