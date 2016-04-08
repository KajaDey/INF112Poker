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
public class HandTest {
    Hand straightFlushHand, betterStraightFlushHand,
            quadsHand, betterQuadsHand,
            houseHand, betterHouseHand,
            flushHand, betterFlushHand,
            straightHand, betterStraightHand,
            tripsHand, betterTripsHand,
            twoPairsHand, betterTwoPairsHand,
            pairHand, betterPairHand,
            highCardHand, betterHighCardHand;

    Card card1, card2, card3, card4, card5,
            card6, card7, card8, card9, card10,
            card11, card12, card13, card14, card15,
            card16, card17, card18;

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
        card9 = Card.of(9, Card.Suit.DIAMONDS).get();
        card10 = Card.of(2, Card.Suit.CLUBS).get();
        card11 = Card.of(3, Card.Suit.HEARTS).get();
        card12 = Card.of(13, Card.Suit.HEARTS).get();
        card13 = Card.of(14, Card.Suit.HEARTS).get();
        card14 = Card.of(14, Card.Suit.DIAMONDS).get();
        card15 = Card.of(10, Card.Suit.CLUBS).get();
        card16 = Card.of(9, Card.Suit.SPADES).get();
        card17 = Card.of(9, Card.Suit.HEARTS).get();
        card18 = Card.of(10, Card.Suit.DIAMONDS).get();

        straightFlushHand = new Hand(card1, card2, Arrays.asList(card4, card5, card6, card7, card8));
        quadsHand = new Hand(card1, card2, Arrays.asList(card3, card4, card5, card6, card7));
        houseHand = new Hand(card1, card2, Arrays.asList(card3, card6, card7, card8, card9));
        flushHand = new Hand(card4, card5, Arrays.asList(card6, card7, card10, card1, card2));
        straightHand = new Hand(card1, card2, Arrays.asList(card5, card6, card7, card8, card9));
        tripsHand = new Hand(card1, card2, Arrays.asList(card3, card5, card6, card8, card11));
        twoPairsHand = new Hand(card1, card2, Arrays.asList(card7, card8, card9, card10, card11));
        pairHand = new Hand(card1, card2, Arrays.asList(card5, card6, card8, card10, card11));
        highCardHand = new Hand(card1, card5, Arrays.asList(card7, card8, card10, card11, card12));

        betterStraightFlushHand = new Hand(card1, card2, Arrays.asList(card8, card5, card6, card7, card15));
        betterQuadsHand = new Hand(card1, card2, Arrays.asList(card3, card8, card9, card16, card17));
        betterHouseHand = new Hand(card1, card2, Arrays.asList(card3, card6, card7, card13, card14));
        betterFlushHand = new Hand(card4, card5, Arrays.asList(card6, card7, card15, card1, card2));
        betterStraightHand = new Hand(card18, card2, Arrays.asList(card5, card6, card7, card8, card9));
        betterTripsHand = new Hand(card1, card2, Arrays.asList(card3, card5, card6, card8, card12));
        betterTwoPairsHand = new Hand(card1, card2, Arrays.asList(card7, card8, card9, card10, card12));
        betterPairHand = new Hand(card1, card9, Arrays.asList(card5, card6, card8, card10, card11));
        betterHighCardHand = new Hand(card1, card5, Arrays.asList(card7, card8, card10, card11, card13));
    }

    @Test
    public void testGetAllCards() throws Exception {
        List<Card> allCards = straightFlushHand.getAllCards();

        assertTrue(allCards.contains(card1));
        assertTrue(allCards.contains(card2));
        assertTrue(allCards.contains(card4));
        assertTrue(allCards.contains(card5));
        assertTrue(allCards.contains(card6));
        assertTrue(allCards.contains(card7));
        assertTrue(allCards.contains(card8));
    }

    @Test
    public void testAlCardsEqualsHoleCardsPlusCommunityCards() {
        List<Card> allCards = straightFlushHand.getAllCards();
        List<Card> holeCards = straightFlushHand.getHoleCards();
        List<Card> communityCards = straightFlushHand.getCommunityCards().get();
        assertTrue(allCards.containsAll(holeCards));
        assertTrue(allCards.containsAll(communityCards));

        assertTrue(allCards.size() == holeCards.size() + communityCards.size());
    }

    @Test
    public void testStraightFlushVSQuads() {
        assertTrue(straightFlushHand.compareTo(quadsHand) > 0);
    }

    @Test
    public void testQuadsVSHouse() {
        assertTrue(quadsHand.compareTo(houseHand) > 0);
    }

    @Test
    public void testHouseVSFlush() {
        assertTrue(houseHand.compareTo(flushHand) > 0);
    }

    @Test
    public void testFLushVSStraight() {
        assertTrue(flushHand.compareTo(straightHand) > 0);
    }

    @Test
    public void testStraightVSTrips() {
        assertTrue(straightHand.compareTo(tripsHand) > 0);
    }

    @Test
    public void testTripsVSTwoPairs() {
        assertTrue(tripsHand.compareTo(twoPairsHand) > 0);
    }

    @Test
    public void testTwoPairsVSPair() {
        assertTrue(twoPairsHand.compareTo(pairHand) > 0);
    }

    @Test
    public void testPairVSHighCard() {
        assertTrue(twoPairsHand.compareTo(highCardHand) > 0);
    }


    // Test hands with equal rules

    @Test
    public void testStraightFlushVSBetterStraightFlush() {
        assertTrue(straightFlushHand.compareTo(betterStraightFlushHand) < 0);
    }

    @Test
    public void testQuadsVSBetterQuads() {
        assertTrue(quadsHand.compareTo(betterQuadsHand) < 0);
    }

    @Test
    public void testHouseVSBetterHouse() {
        assertTrue(houseHand.compareTo(betterHouseHand) < 0);
    }

    @Test
    public void testFlushVSBetterFlush() {
        assertTrue(flushHand.compareTo(betterFlushHand) < 0);
    }

    @Test
    public void testStraightVSBetterStraight() {
        assertTrue(straightHand.compareTo(betterStraightHand) < 0);
    }

    @Test
    public void testTripsVSBetterTrips() {
        assertTrue(tripsHand.compareTo(betterTripsHand) < 0);
    }

    @Test
    public void testTwoPairsVSBetterTwoPairs() {
        assertTrue(twoPairsHand.compareTo(betterTwoPairsHand) < 0);
    }

    @Test
    public void testPairVSBetterPair() {
        assertTrue(pairHand.compareTo(betterPairHand) < 0);
    }

    @Test
    public void testHighCardVSBetterHighCard() {
        assertTrue(highCardHand.compareTo(betterHighCardHand) < 0);
    }
}