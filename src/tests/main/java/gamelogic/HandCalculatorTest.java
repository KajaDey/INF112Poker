package main.java.gamelogic;

import main.java.gamelogic.rules.Flush;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Vegar on 10/03/16.
 */
public class HandCalculatorTest {
    HandCalculator handCalculator;
    Hand straightFlush, quad, house, flush, straight, tress, twoPairs, pairs, highCard;
    List<Card> communityCards = new ArrayList<>();
    Card card, aceOfSpades, kingOfSpades,kingOfHearts, jackOfSpades, queenOfSpades;
    Flush flushtest;

    @Before
    public void setUp() throws Exception {


        communityCards.add(card.of(10, Card.Suit.SPADES).get());
        communityCards.add(card.of(8, Card.Suit.SPADES).get());
        communityCards.add(card.of(5, Card.Suit.SPADES).get());
        communityCards.add(card.of(10, Card.Suit.HEARTS).get());
        communityCards.add(card.of(10, Card.Suit.CLUBS).get());

         aceOfSpades = card.of(14, Card.Suit.SPADES).get();
         kingOfSpades = card.of(13, Card.Suit.SPADES).get();
         kingOfHearts = card.of(14, Card.Suit.HEARTS).get();
         jackOfSpades = card.of(11, Card.Suit.SPADES).get();
         queenOfSpades = card.of(12, Card.Suit.SPADES).get();
    }

    @Test
    public void flushReturn() throws Exception{

    }
}