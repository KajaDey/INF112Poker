package tests.java.hands;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;
import main.java.gamelogic.Rules.xOfaKind;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Vegar on 09/03/16.
 */
public class xOfaKindTest {
    Hand hand = null;
    Hand hand2 = null;
    Card card = null;

    xOfaKind xOf = new xOfaKind();
    //get inn 7 cards, choose best 5 with either 4,3,2 of the same rank)


    @Before
    public void setUp() throws Exception {

        ArrayList<Card> communityCards = new ArrayList<>();

        communityCards.add(card.of(4, Card.Suit.SPADES).get());
        communityCards.add(card.of(4, Card.Suit.HEARTS).get());
        communityCards.add(card.of(5, Card.Suit.SPADES).get());
        communityCards.add(card.of(6, Card.Suit.CLUBS).get());
    }

    @Test
    public void testFourLikeCards() throws Exception {
        Card aceOfSpades = card.of(14, Card.Suit.SPADES).get();

    }
}