package gamelogic;

import gamelogic.Card;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class CardTest {

    Card twoOfHearts = Card.of(2, Card.Suit.HEARTS).get();
    Card aceOfClubs = Card.of(14, Card.Suit.CLUBS).get();
    Card fiveOfClubs = Card.of(5, Card.Suit.CLUBS).get();

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

    @Test
    public void testAscendingSorting() throws Exception {
        ArrayList<Card> arr = new ArrayList<>();
        arr.add(fiveOfClubs);
        arr.add(aceOfClubs);
        arr.add(twoOfHearts);
        arr.sort(Card::compareTo);

        assertTrue(arr.get(0).rank == twoOfHearts.rank);
        assertTrue(arr.get(1).rank == fiveOfClubs.rank);
        assertTrue(arr.get(2).rank == aceOfClubs.rank);
    }
}