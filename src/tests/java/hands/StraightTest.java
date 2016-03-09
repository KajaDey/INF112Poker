package tests.java.hands;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;
import main.java.gamelogic.rules.Straight;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by Vegar on 09/03/16.
 */
public class StraightTest {

    Hand hand;
    Card card1, card2, card3, card4, card5, card6, card7;
    Straight straigt;

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
        straigt = new Straight(hand);
    }

//    @Test
//    public void testMatch() throws Exception {
//        System.out.println(straigt.cards);
//        assertTrue(straigt.match(hand));
//    }

//    @Test
//    public void testGetHand() throws Exception {
//        straigt.match(hand);
//
//        assertTrue(straigt.getHand().get().contains(card3));
//        assertTrue(straigt.getHand().get().contains(card7));
//    }

    @Test
    public void testSortingCards() {
        straigt.match(hand);
        assertTrue(straigt.cards.get(0).equals(card3));
        assertTrue(straigt.cards.get(4).equals(card7));
    }

//    @Test
//    public void testReturnHand() {
//        straigt.match(hand);
//
//        assertTrue(straigt.returnHand.contains(card3));
//    }
}