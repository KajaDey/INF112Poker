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
    Hand hand;
    Card card1, card2, card3, card4, card5, card6, card7;

    @Before
    public void setUp() throws Exception {
        // lage Hand-objekt
        card1 = Card.of(5, Card.Suit.CLUBS).get();
        card2 = Card.of(3, Card.Suit.CLUBS).get();
        card3 = Card.of(2, Card.Suit.CLUBS).get();
        card4 = Card.of(4, Card.Suit.CLUBS).get();
        card5 = Card.of(9, Card.Suit.CLUBS).get();
        card6 = Card.of(13, Card.Suit.CLUBS).get();
        card7 = Card.of(6, Card.Suit.CLUBS).get();

        hand = new Hand(card1, card2, Arrays.asList(card3, card4, card5, card6, card7));
    }

    @Test
    public void testGetAllCards() throws Exception {
        assertTrue(hand.getAllCards().contains(card1));
        assertTrue(hand.getAllCards().contains(card2));
        assertTrue(hand.getAllCards().contains(card3));
        assertTrue(hand.getAllCards().contains(card4));
        assertTrue(hand.getAllCards().contains(card5));
        assertTrue(hand.getAllCards().contains(card6));
        assertTrue(hand.getAllCards().contains(card7));
    }
}