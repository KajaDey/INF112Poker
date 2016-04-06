package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Vegar on 09/03/16.
 */
public class FlushTest {
    Hand hand1,hand2 = null;
    Flush flush = new Flush();

    @Before
    public void setUp() throws Exception {
        Card card = null;
        ArrayList<Card> communityCards = new ArrayList<>();

        communityCards.add(card.of(10, Card.Suit.SPADES).get());
        communityCards.add(card.of(8, Card.Suit.SPADES).get());
        communityCards.add(card.of(5, Card.Suit.SPADES).get());
        communityCards.add(card.of(10, Card.Suit.HEARTS).get());
        communityCards.add(card.of(10, Card.Suit.CLUBS).get());

        Card aceOfSpades = card.of(14, Card.Suit.SPADES).get();
        Card kingOfSpades = card.of(13, Card.Suit.SPADES).get();
        Card kingOfHearts = card.of(14, Card.Suit.HEARTS).get();

        hand1 = new Hand(aceOfSpades,kingOfSpades,communityCards);
        hand2 = new Hand(aceOfSpades,kingOfHearts,communityCards);
    }

    @Test
    public void testFlushTrue() throws Exception {
        assertTrue(flush.match(hand1));
        assertEquals("Ace high flush",flush.toString());
    }

    @Test
    public void testFlushFalse() throws Exception {
        assertFalse(flush.match(hand2));
    }

    @Test
    public void testRightCompareValues() {
        flush.match(hand1);
        List<Integer> compareValues = flush.getCompareValues();

        assertTrue(compareValues.get(0) == 14);
        assertTrue(compareValues.get(1) == 13);
        assertTrue(compareValues.get(2) == 10);
        assertTrue(compareValues.get(3) == 8);
        assertTrue(compareValues.get(4) == 5);

        assertTrue(compareValues.size() == 5);

    }
}