package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Vegar on 09/03/16.
 */
public class StraightTest {

    Hand hand;
    Card card1, card2, card3, card4, card5, card6, card7, card8;
    Straight straight;

    @Before
    public void setUp() throws Exception {
        card1 = Card.of(2, Card.Suit.CLUBS).get();
        card2 = Card.of(3, Card.Suit.CLUBS).get();
        card3 = Card.of(4, Card.Suit.CLUBS).get();
        card4 = Card.of(5, Card.Suit.CLUBS).get();
        card5 = Card.of(6, Card.Suit.CLUBS).get();
        card6 = Card.of(7, Card.Suit.CLUBS).get();
        card7 = Card.of(8, Card.Suit.CLUBS).get();
        card8 = Card.of(14, Card.Suit.CLUBS).get();
    }

    @Test
    public void testMatchWithUniqueRanks() throws Exception {
        hand = new Hand(card1, card2, Arrays.asList(card3, card4, card5, card7, card6));
        straight = new Straight();

        assertTrue(straight.match(hand));
    }

    @Test
    public void testToStringReturnsCorrectHighCard() {
        hand = new Hand(card1, card2, Arrays.asList(card3, card4, card5, card7, card6));
        straight = new Straight();
        straight.match(hand);

        int highCardRank = card7.rank;

        assertEquals(highCardRank + " high straight", straight.toString());
    }

    @Test
    public void testMatchWithEqualRanks() throws Exception {
        hand = new Hand(card1, card2, Arrays.asList(card3, card4, card5, card1, card3));
        straight = new Straight();

        assertTrue(straight.match(hand));
    }

    @Test
    public void testAddedLastCardWhenEqualRanks() throws Exception {
        hand = new Hand(card1, card2, Arrays.asList(card2, card2, card5, card3, card4));
        straight = new Straight();
        straight.match(hand);

        assertTrue(straight.getHand().get().contains(card5));
    }

    @Test
    public void testNoMatchWhenNoStraight() throws Exception {
        hand = new Hand(card1, card2, Arrays.asList(card3, card4, card7, card7, card7));
        straight = new Straight();

        assertFalse(straight.match(hand));
    }

    @Test
    public void testStraightAceToFive() {
        hand = new Hand(card1, card2, Arrays.asList(card3, card4, card8, card7, card7));
        straight = new Straight();

        assertTrue(straight.match(hand));
    }

    @Test
    public void testGetsBestHandWhenLongStraight() {
        hand = new Hand(card1, card2, Arrays.asList(card3, card4, card8, card7, card5));
        straight = new Straight();
        straight.match(hand);

        assertTrue(straight.getHand().get().contains(card5)); // highest in straight
        assertFalse(straight.getHand().get().contains(card8)); // no ace
    }

    @Test
    public void testRightCompareValues() {
        hand = new Hand(card1, card2, Arrays.asList(card3, card4, card7, card7, card8));
        straight = new Straight();

        straight.match(hand);
        List<Integer> compareValues = straight.getCompareValues();

        assertTrue(compareValues.get(0) == 5);
        assertTrue(compareValues.size() == 1);

    }
}