package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by kristianrosland on 09.03.2016.
 */
public class HighCardTest {
    Hand hand;
    HighCard hc = new HighCard();
    Card card = null;
    ArrayList<Card> communityCards = new ArrayList<>();
    Card card1, card2, card3 , card4, card5;

    @Before
    public void setUp() throws Exception {

        communityCards.add(card.of(2, Card.Suit.SPADES).get());
        communityCards.add(card.of(4, Card.Suit.SPADES).get());
        communityCards.add(card.of(10, Card.Suit.SPADES).get());
        communityCards.add(card.of(11, Card.Suit.HEARTS).get());
        communityCards.add(card.of(8, Card.Suit.CLUBS).get());

        card3 = communityCards.get(3);
        card4 = communityCards.get(2);
        card5 = communityCards.get(4);

        card1 = card.of(13, Card.Suit.SPADES).get();
        card2 = card.of(14, Card.Suit.CLUBS).get();

        hand = new Hand(card1,card2,communityCards);
        hc.match(hand);

    }

    @Test
    public void testHighCard() throws Exception {
        assertEquals(card2, hc.getHand().get().get(0));
        assertNotEquals(card1, hc.getHand().get().get(0));
        assertEquals("High card Ace",hc.toString());
    }

    @Test
    public void testEntireHand() throws Exception {
        assertEquals(card2, hc.getHand().get().get(0));
        assertEquals(card1, hc.getHand().get().get(1));
        assertEquals(card3, hc.getHand().get().get(2));
        assertEquals(card4, hc.getHand().get().get(3));
        assertEquals(card5, hc.getHand().get().get(4));
    }

    @Test
    public void testRightCompareValues() throws Exception {
        List<Integer> compareValues = hc.getCompareValues();
        // 14, 13, 11, 10, 8
        assertTrue(compareValues.get(0) == 14);
        assertTrue(compareValues.get(1) == 13);
        assertTrue(compareValues.get(2) == 11);
        assertTrue(compareValues.get(3) == 10);
        assertTrue(compareValues.get(4) == 8);

        assertTrue(compareValues.size() == 5);

    }
}