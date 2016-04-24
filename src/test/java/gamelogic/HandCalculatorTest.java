package gamelogic;

import gamelogic.rules.*;
import gui.GameSettings;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    GameSettings settings = new GameSettings(5000,25,50, 5, 0, AIType.MCTS_AI);

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
        hand = new Hand(holeCard1, holeCard2, communityCards);
        hc = new HandCalculator(hand);
        wantedHand = new Hand(holeCard1,holeCard2, communityCards);
        StraightFlush sf = new StraightFlush();
        sf.match(wantedHand);
        assertEquals(sf.getHand(),hc.getWinningHand());
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
        assertEquals(quad.getHand(),hc.getWinningHand());
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
        assertEquals(house.getHand(),hc.getWinningHand());
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
        assertEquals(flush.getHand(),hc.getWinningHand());
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
        assertEquals(straight.getHand(),hc.getWinningHand());
    }
    @Test
    public void testTripsReturn() throws Exception{
        holeCard1=card.of(7, Card.Suit.DIAMONDS).get();
        holeCard2=card.of(12, Card.Suit.CLUBS).get();
        hand = new Hand(holeCard1, holeCard2, communityCards);
        hc = new HandCalculator(hand);
        wantedHand = new Hand(holeCard1,holeCard2, communityCards);

        xOfaKind trips = new xOfaKind(3);
        trips.match(wantedHand);
        assertEquals(trips.getHand(),hc.getWinningHand());

    }
    @Test
    public void testTwoPairsReturn() throws Exception{
        holeCard1=card.of(10, Card.Suit.HEARTS).get();
        holeCard2=card.of(9, Card.Suit.CLUBS).get();
        hand = new Hand(holeCard1, holeCard2, communityCards);
        hc = new HandCalculator(hand);
        wantedHand = new Hand(holeCard1,holeCard2, communityCards);

        TwoPairs twoPair = new TwoPairs();
        twoPair.match(wantedHand);
        assertEquals(twoPair.getHand(),hc.getWinningHand());
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
        assertEquals(pair.getHand(),hc.getWinningHand());
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
        assertEquals(high.getHand(),hc.getWinningHand());
    }

    @Test
    public void testThatPercentagesCalculationsWorkCorrectly() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bob", settings, 0));
        players.add(new Player("Alice", settings, 1));
        players.add(new Player("Kate", settings, 2));

        ArrayList<Card> communityCards = new ArrayList<>();
        communityCards.add(Card.of(10, Card.Suit.HEARTS).get());
        communityCards.add(Card.of(11, Card.Suit.SPADES).get());
        communityCards.add(Card.of(12, Card.Suit.HEARTS).get());

        players.get(0).setHoleCards(Card.of(2, Card.Suit.HEARTS).get(), Card.of(3, Card.Suit.HEARTS).get()); // ~38%
        players.get(1).setHoleCards(Card.of(13, Card.Suit.SPADES).get(), Card.of(14, Card.Suit.CLUBS).get()); // ~62%
        players.get(2).setHoleCards(Card.of(4, Card.Suit.CLUBS).get(), Card.of(5, Card.Suit.DIAMONDS).get()); // 0%

        Map<Integer, Double> percentages = HandCalculator.getWinningPercentages(players, communityCards);

        assertEquals(0.38, percentages.get(0), 0.03);
        assertEquals(0.62, percentages.get(1), 0.03);
        assertEquals(0.0, percentages.get(2), 0.0);
    }
}