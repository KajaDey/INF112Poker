package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.rules.Flush;
import main.java.gamelogic.Hand;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Vegar on 09/03/16.
 */
public class FlushTest {
    Hand hand = null;
    Hand hand2 = null;
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

        hand = new Hand(aceOfSpades,kingOfSpades,communityCards);
        hand2 = new Hand(aceOfSpades,kingOfHearts,communityCards);
    }

    @Test
    public void testFlushTrue() throws Exception {
        assertTrue(flush.match(hand));
    }

    @Test
    public void testFlushFalse() throws Exception {
        assertFalse(flush.match(hand2));
    }
}