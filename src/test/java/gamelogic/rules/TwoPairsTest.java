package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by pokki on 09/03/16.
 */
public class TwoPairsTest {
    Card card, holeCard, holeCard1, holeCard2, holeCard3;
    Hand hand;
    ArrayList communityCards = new ArrayList<>();
    TwoPairs twoPairs;

    @Before
    public void setUp() throws Exception {

        communityCards.add(card.of(5, Card.Suit.CLUBS).get());
        communityCards.add(card.of(8, Card.Suit.HEARTS).get());
        communityCards.add(card.of(7, Card.Suit.DIAMONDS).get());
        communityCards.add(card.of(2, Card.Suit.SPADES).get());
        communityCards.add(card.of(4, Card.Suit.SPADES).get());

         holeCard = card.of(5, Card.Suit.HEARTS).get();
         holeCard1 = card.of(8, Card.Suit.CLUBS).get();
         holeCard2 = card.of(10, Card.Suit.CLUBS).get();

    }

    @Test
    public void testMatch() throws Exception {
        hand = new Hand(holeCard, holeCard1,communityCards);
        twoPairs = new TwoPairs();

        assertTrue(twoPairs.match(hand));
        assertEquals("Two pairs, 8s and 5s",twoPairs.toString());
    }

    @Test
    public void testNoMatch() throws Exception{
        hand = new Hand(holeCard, holeCard2,communityCards);
        twoPairs = new TwoPairs();

        assertFalse(twoPairs.match(hand));
    }

    @Test
    public void testGetHand() throws Exception {
        hand = new Hand(holeCard, holeCard1, communityCards);
        twoPairs = new TwoPairs();

        twoPairs.match(hand);

        assertTrue(hand.getAllCards().contains(holeCard));
        assertTrue(hand.getAllCards().contains(holeCard1));
        assertTrue(hand.getAllCards().contains(communityCards.get(0)));
        assertTrue(hand.getAllCards().contains(communityCards.get(1)));
        assertTrue(hand.getAllCards().contains(communityCards.get(2)));
    }

    @Test
    public void testRightCompareValues() {
        hand = new Hand(holeCard, holeCard1,communityCards);
        twoPairs = new TwoPairs();
        twoPairs.match(hand);

        List<Integer> compareValues = twoPairs.getCompareValues();

        assertTrue(compareValues.get(0) == 8);
        assertTrue(compareValues.get(1) == 5);
        assertTrue(compareValues.get(2) == 7);

        assertTrue(compareValues.size() == 3);

    }
}
