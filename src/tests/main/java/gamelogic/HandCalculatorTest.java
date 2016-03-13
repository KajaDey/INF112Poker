package main.java.gamelogic;

import main.java.gamelogic.rules.Flush;
import main.java.gamelogic.rules.StraightFlush;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Vegar on 10/03/16.
 */
public class HandCalculatorTest {
    Hand hand;
    List<Card> communityCards = new ArrayList<>();
    Card card, holeCard1, holeCard2;
    Deck deck = new Deck();
    HandCalculator hc; //= new HandCalculator();
    @Before
    public void setUp() throws Exception {
        communityCards.add(card.of(7, Card.Suit.SPADES).get());
        communityCards.add(card.of(7, Card.Suit.HEARTS).get());
        communityCards.add(card.of(9, Card.Suit.SPADES).get());
        communityCards.add(card.of(10, Card.Suit.SPADES).get());
        communityCards.add(card.of(13, Card.Suit.HEARTS).get());
    }

    @Test
    public void testStraightFlushReturn() throws Exception{
        holeCard1=card.of(8, Card.Suit.SPADES).get();
        holeCard2=card.of(6, Card.Suit.SPADES).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
       // System.out.println("SF: "+hc.getBestHand(hand));
    }
    @Test
    public void testQuadReturn() throws Exception{
        holeCard1=card.of(7, Card.Suit.CLUBS).get();
        holeCard2=card.of(7, Card.Suit.DIAMONDS).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
       // System.out.println("Quad: "+hc.getBestHand(hand));
    }

    @Test
    public void testHouseReturn() throws Exception{
        holeCard1=card.of(7, Card.Suit.CLUBS).get();
        holeCard2=card.of(10, Card.Suit.DIAMONDS).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
       // System.out.println("House: "+hc.getBestHand(hand));
    }
    @Test
    public void testFlushReturn() throws Exception{
        holeCard1=card.of(4, Card.Suit.SPADES).get();
        holeCard2=card.of(2, Card.Suit.SPADES).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
        //System.out.println("Flush: "+hc.getBestHand(hand));
    }
    @Test
    public void testStraightReturn() throws Exception{
        holeCard1=card.of(8, Card.Suit.HEARTS).get();
        holeCard2=card.of(6, Card.Suit.CLUBS).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
       // System.out.println("Straight: "+hc.getBestHand(hand));
    }
    @Test
    public void testTressReturn() throws Exception{
        holeCard1=card.of(7, Card.Suit.DIAMONDS).get();
        holeCard2=card.of(12, Card.Suit.CLUBS).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
        //System.out.println("Tress: "+hc.getBestHand(hand));
    }
    @Test
    public void testTwoPairsReturn() throws Exception{
        holeCard1=card.of(10, Card.Suit.HEARTS).get();
        holeCard2=card.of(9, Card.Suit.CLUBS).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
       // System.out.println("Two Pairs: "+hc.getBestHand(hand));
    }
    @Test
    public void testPairReturn() throws Exception{
        holeCard1=card.of(12, Card.Suit.HEARTS).get();
        holeCard2=card.of(2, Card.Suit.CLUBS).get();
        hand = new Hand(holeCard1, holeCard2,communityCards);
        //System.out.println("Pair: "+hc.getBestHand(hand));
    }
}