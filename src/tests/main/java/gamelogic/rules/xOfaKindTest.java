package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Vegar on 09/03/16.
*/
public class xOfaKindTest {
    Hand handOf4, handOf3, handOf2 = null;
    Card card, holeCard1, holeCard2, holeCard3,holeCard4 = null;
    ArrayList<Card> communityCards= new ArrayList<>();

    xOfaKind xOf4 = new xOfaKind(4);
    xOfaKind xOf3 = new xOfaKind(3);
    xOfaKind xOf2 = new xOfaKind(2);


    //get inn 7 cards, choose best 5 with either 4,3,2 of the same rank


    @Before
    public void setUp() throws Exception {


        communityCards.add(card.of(9, Card.Suit.SPADES).get());
        communityCards.add(card.of(9, Card.Suit.HEARTS).get());
        communityCards.add(card.of(12, Card.Suit.SPADES).get());
        communityCards.add(card.of(11, Card.Suit.CLUBS).get());
        communityCards.add(card.of(7, Card.Suit.DIAMONDS).get());


        holeCard1 = card.of(9, Card.Suit.CLUBS).get();
        holeCard2 = card.of(9, Card.Suit.DIAMONDS).get();
        holeCard3 = card.of(11, Card.Suit.SPADES).get();
        holeCard4 = card.of(10, Card.Suit.SPADES).get();


    }

    @Test
    public void testFourLikeCards() throws Exception {
        handOf4 = new Hand(holeCard1,holeCard2,communityCards);
        assertEquals(true,xOf4.match(handOf4));
        assertEquals(false,xOf3.match(handOf4));
        assertEquals(false,xOf2.match(handOf4));
    }

    @Test
    public void testThreeLikeCards() throws Exception {
        handOf3 = new Hand(holeCard1,holeCard4,communityCards);
        assertEquals(true,xOf3.match(handOf3));
        assertEquals(false,xOf2.match(handOf3));
    }

    @Test
    public void testTwoLikeCards() throws Exception {
        handOf2 = new Hand(holeCard3,holeCard4,communityCards);
        assertEquals(true,xOf2.match(handOf2));
    }
}