package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;
import main.java.gamelogic.rules.Straight;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by Vegar on 09/03/16.
 */
public class HandTest {
    Hand hand, straightFlushHand, quadsHand;
    Card card1, card2, card3, card4, card5, card6, card7, card8;

    @Before
    public void setUp() throws Exception {
        // make Hand-object
        card1 = Card.of(5, Card.Suit.SPADES).get();
        card2 = Card.of(5, Card.Suit.HEARTS).get();
        card3 = Card.of(5, Card.Suit.DIAMONDS).get();
        card4 = Card.of(5, Card.Suit.CLUBS).get();
        card5 = Card.of(6, Card.Suit.CLUBS).get();
        card6 = Card.of(7, Card.Suit.CLUBS).get();
        card7 = Card.of(8, Card.Suit.CLUBS).get();
        card8 = Card.of(9, Card.Suit.CLUBS).get();

        quadsHand = new Hand(card1, card2, Arrays.asList(card3, card4, card5, card6, card7));
        straightFlushHand = new Hand(card1, card2, Arrays.asList(card4, card5, card6, card7, card8));
    }

    @Test
    public void testGetAllCards() throws Exception {
        assertTrue(straightFlushHand.getAllCards().contains(card1));
        assertTrue(straightFlushHand.getAllCards().contains(card2));
        assertTrue(straightFlushHand.getAllCards().contains(card4));
        assertTrue(straightFlushHand.getAllCards().contains(card5));
        assertTrue(straightFlushHand.getAllCards().contains(card6));
        assertTrue(straightFlushHand.getAllCards().contains(card7));
        assertTrue(straightFlushHand.getAllCards().contains(card8));
    }

    @Test
    public void testStraightFlushVSQuads() {
        assertTrue(straightFlushHand.compareTo(quadsHand) > 0);
    }

    // TODO: more tests with different hands
}




















