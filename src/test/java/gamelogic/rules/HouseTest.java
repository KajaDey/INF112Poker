package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by henrik on 10.03.16.
 */
public class HouseTest {

    Hand hand;
    Card card1, card2, card3, card4, card5, card6, card7, card8, card9, card10;
    House house = new House();

    @Before
    public void setUp() throws Exception{

        card1 = Card.of(2, Card.Suit.SPADES).get();
        card2 = Card.of(2, Card.Suit.SPADES).get();
        card3 = Card.of(2, Card.Suit.SPADES).get();
        card4 = Card.of(3, Card.Suit.HEARTS).get();
        card5 = Card.of(3, Card.Suit.CLUBS).get();
        card6 = Card.of(3, Card.Suit.SPADES).get();
        card7 = Card.of(2, Card.Suit.HEARTS).get();
        card8 = Card.of(4, Card.Suit.HEARTS).get();
        card9 = Card.of(5, Card.Suit.HEARTS).get();
        card10 = Card.of(6, Card.Suit.HEARTS).get();
    }

    @Test
    public void testMatchWhenFullHouse() throws Exception {
        hand = new Hand(card6, card8, Arrays.asList(card1, card2, card3, card4, card5));
        assertTrue(house.match(hand));
        assertEquals("3s full of 2s", house.toString());
    }

    @Test
    public void testReturnHandSize() throws Exception {
        hand = new Hand(card6, card7, Arrays.asList(card1, card4, card2, card3, card5));
        house.match(hand);
        Optional<List<Card>> bestHand = house.getHand();

        assertTrue(bestHand.isPresent());
        assertTrue(bestHand.get().size() == 5);
    }

    @Test
    public void testBestThreesReturnedWhenMatch() {
        hand = new Hand(card6, card7, Arrays.asList(card1, card4, card2, card3, card5));
        house.match(hand);
        List<Card> bestHand = house.getHand().get();

        assertTrue(bestHand.contains(card4));
        assertTrue(bestHand.contains(card5));
        assertTrue(bestHand.contains(card6));
    }

    @Test
    public void testNoMatchWhenNoFullHouse() {
        hand = new Hand(card1, card2, Arrays.asList(card4, card5, card8, card9, card10));

        assertFalse(house.match(hand));
    }

    @Test
    public void testRightCompareValues() {
        hand = new Hand(card6, card7, Arrays.asList(card1, card2, card3, card4, card5));
        house.match(hand);

        List<Integer> compareValues = house.getCompareValues();

        assertTrue(compareValues.get(0) == 3);
        assertTrue(compareValues.get(1) == 2);
        assertTrue(compareValues.size() == 2);
    }
}