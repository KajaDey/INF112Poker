package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by pokki on 10/03/16.
 */
public class StraightFlushTest {

    Hand hand;
    Card card1, card2, card3, card4, card5, card6, card7, card8;
    StraightFlush straightFlush;

    @Before
    public void setUp() throws Exception {
        card1 = Card.of(2, Card.Suit.CLUBS).get();
        card2 = Card.of(3, Card.Suit.CLUBS).get();
        card3 = Card.of(4, Card.Suit.CLUBS).get();
        card4 = Card.of(5, Card.Suit.CLUBS).get();
        card5 = Card.of(6, Card.Suit.CLUBS).get();
        card6 = Card.of(14, Card.Suit.CLUBS).get();
        card7 = Card.of(3, Card.Suit.HEARTS).get();
    }

    @Test
    public void testMatchWhenStraightFlush() {
        hand = new Hand(card1, card2, Arrays.asList(card3, card4, card6, card5, card6));
        straightFlush = new StraightFlush();

        assertTrue(straightFlush.match(hand));
       assertEquals("6 high straight flush",straightFlush.toString() );
    }

    @Test
    public void testGetHandWhenMatch() {
        hand = new Hand(card1, card2, Arrays.asList(card3, card4, card6, card5, card6));
        straightFlush = new StraightFlush();
        straightFlush.match(hand);

        assertTrue(straightFlush.getHand().isPresent());
        assertTrue(straightFlush.getHand().get().size() == 5);

    }

    @Test
    public void testNoMatchWhenOnlyStraight() {
        hand = new Hand(card1, card7, Arrays.asList(card3, card4, card6, card5, card6));
        straightFlush = new StraightFlush();

        assertFalse(straightFlush.match(hand));
    }

    @Test
    public void testNoMatchWhenOnlyFlush() {
        hand = new Hand(card1, card2, Arrays.asList(card1, card4, card5, card6, card7));
        straightFlush = new StraightFlush();

        assertFalse(straightFlush.match(hand));
    }

    @Test
    public void testNoMatchWhenIndividualStraightAndFlush() {
        card1 = Card.of(2, Card.Suit.HEARTS).get();
        card2 = Card.of(3, Card.Suit.HEARTS).get();
        card3 = Card.of(4, Card.Suit.CLUBS).get();
        card4 = Card.of(5, Card.Suit.CLUBS).get();
        card5 = Card.of(6, Card.Suit.HEARTS).get();
        card6 = Card.of(14, Card.Suit.HEARTS).get();
        card7 = Card.of(10, Card.Suit.HEARTS).get();

        hand = new Hand(card1, card2, Arrays.asList(card3, card4, card5, card6, card7));
        straightFlush = new StraightFlush();

        assertFalse(straightFlush.match(hand));
    }

    @Test
    public void testRightCompareValues() {
        hand = new Hand(card1, card2, Arrays.asList(card3, card4, card5, card6, card6));
        straightFlush = new StraightFlush();

        straightFlush.match(hand);
        List<Integer> compareValues = straightFlush.getCompareValues();

        assertTrue(compareValues.get(0) == 6);
        assertTrue(compareValues.size() == 1);
    }

}