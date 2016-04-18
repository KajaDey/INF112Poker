package gamelogic.ai;

import gamelogic.Card;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by morten on 18.04.16.
 */
public class HandQualityTest {

    @Test
    public void lowStraightHasHighQuality() {
        List<Card> communityCards = Arrays.asList(Card.of(3, Card.Suit.CLUBS).get(), Card.of(5, Card.Suit.HEARTS).get(), Card.of(6, Card.Suit.DIAMONDS).get());
        double handQuality = SimpleAI.handQuality(Card.of(2, Card.Suit.CLUBS).get(), Card.of(4, Card.Suit.SPADES).get(), communityCards);
        assertTrue("handQuality is " + handQuality, handQuality > 50 && handQuality < 70);
    }

    @Test
    public void goodChanceForLowFourStraightHasDecentQuality() {
        List<Card> communityCards = Arrays.asList(Card.of(4, Card.Suit.CLUBS).get(), Card.of(6, Card.Suit.HEARTS).get(), Card.of(11, Card.Suit.DIAMONDS).get());
        double handQuality = SimpleAI.handQuality(Card.of(3, Card.Suit.CLUBS).get(), Card.of(5, Card.Suit.SPADES).get(), communityCards);
        assertTrue("handQuality is " + handQuality, handQuality > 25 && handQuality < 40);
    }
}
