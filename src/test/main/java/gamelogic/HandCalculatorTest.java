package main.java.gamelogic;

import main.java.gamelogic.rules.*;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Vegar on 10/03/16.
 */
public class HandCalculatorTest {
    Hand hand, wantedHand;
    List<Card> communityCards = new ArrayList<>();
    List<Card> highCards = new ArrayList<>();
    Card card, holeCard1, holeCard2;
    HandCalculator hc;
    Card c1,c2,c3,c4,c5,c6;
    @Before
    public void setUp() throws Exception {

        c1 = Card.of(7,Card.Suit.SPADES).get();
        c2 = card.of(7, Card.Suit.HEARTS).get();
        c3 = card.of(9, Card.Suit.SPADES).get();
        c4 = card.of(10, Card.Suit.SPADES).get();
        c5 = card.of(13, Card.Suit.HEARTS).get();

        communityCards.add(c1);
        communityCards.add(c2);
        communityCards.add(c3);
        communityCards.add(c4);
        communityCards.add(c5);
    }

    @Test
    public void testStraightFlushReturn() throws Exception{
        holeCard1=card.of(8, Card.Suit.SPADES).get();
        holeCard2=card.of(6, Card.Suit.SPADES).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
        hc = new HandCalculator(hand);
        wantedHand = new Hand(holeCard1,holeCard2, Arrays.asList(c1,c3,c4));
        StraightFlush sf = new StraightFlush();
        sf.match(wantedHand);
        assertEquals(sf.getHand(),hc.getBestHand());
    }

    @Test
    public void testQuadReturn() throws Exception{
        holeCard1=card.of(7, Card.Suit.CLUBS).get();
        holeCard2=card.of(7, Card.Suit.DIAMONDS).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
        hc = new HandCalculator(hand);

        wantedHand = new Hand(holeCard1,holeCard2,communityCards);
        xOfaKind quad = new xOfaKind(4);
        quad.match(wantedHand);
        assertEquals(quad.getHand(),hc.getBestHand());
    }

    @Test
    public void testHouseReturn() throws Exception{
        holeCard1=card.of(7, Card.Suit.CLUBS).get();
        holeCard2=card.of(10, Card.Suit.DIAMONDS).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
        hc = new HandCalculator(hand);

        wantedHand = new Hand(holeCard1,holeCard2,communityCards);
        House house = new House();
        house.match(wantedHand);
        assertEquals(house.getHand(),hc.getBestHand());
    }
    @Test
    public void testFlushReturn() throws Exception{
        holeCard1=card.of(4, Card.Suit.SPADES).get();
        holeCard2=card.of(2, Card.Suit.SPADES).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
        hc = new HandCalculator(hand);

        wantedHand = new Hand(holeCard1,holeCard2,communityCards);
        Flush flush = new Flush();
        flush.match(wantedHand);
        assertEquals(flush.getHand(),hc.getBestHand());
    }
    @Test
    public void testStraightReturn() throws Exception{
        holeCard1=card.of(8, Card.Suit.HEARTS).get();
        holeCard2=card.of(6, Card.Suit.CLUBS).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
        hc = new HandCalculator(hand);
        wantedHand = new Hand(holeCard1,holeCard2,communityCards);

        Straight straight = new Straight();
        straight.match(wantedHand);
        assertEquals(straight.getHand(),hc.getBestHand());
    }
    @Test
    public void testTripsReturn() throws Exception{
        holeCard1=card.of(7, Card.Suit.DIAMONDS).get();
        holeCard2=card.of(12, Card.Suit.CLUBS).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
        hc = new HandCalculator(hand);
        wantedHand = new Hand(holeCard1,holeCard2,communityCards);

        xOfaKind trips = new xOfaKind(3);
        trips.match(wantedHand);
        assertEquals(trips.getHand(),hc.getBestHand());

    }
    @Test
    public void testTwoPairsReturn() throws Exception{
        holeCard1=card.of(10, Card.Suit.HEARTS).get();
        holeCard2=card.of(9, Card.Suit.CLUBS).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
        hc = new HandCalculator(hand);
        wantedHand = new Hand(holeCard1,holeCard2,communityCards);

        TwoPairs twoPair = new TwoPairs();
        twoPair.match(wantedHand);
        assertEquals(twoPair.getHand(),hc.getBestHand());
    }
    @Test
    public void testPairReturn() throws Exception{
        holeCard1=card.of(12, Card.Suit.HEARTS).get();
        holeCard2=card.of(2, Card.Suit.CLUBS).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
        hc = new HandCalculator(hand);
        wantedHand= new Hand(holeCard1,holeCard2,communityCards);

        xOfaKind pair = new xOfaKind(2);
        pair.match(wantedHand);
        assertEquals(pair.getHand(),hc.getBestHand());
    }

    @Test
    public void testNoMatches() throws Exception{
        c6 =  card.of(5, Card.Suit.DIAMONDS).get();
        holeCard1=card.of(12, Card.Suit.HEARTS).get();
        holeCard2=card.of(2, Card.Suit.CLUBS).get();
        highCards.add(c1);
        highCards.add(c3);
        highCards.add(c4);
        highCards.add(c5);
        highCards.add(c6);

        hand = new Hand(holeCard1, holeCard2,highCards);
        hc = new HandCalculator(hand);
        wantedHand= new Hand(holeCard1,holeCard2,highCards);

        HighCard high = new HighCard();
        high.match(wantedHand);
        assertEquals(high.getHand(),hc.getBestHand());

    }
}