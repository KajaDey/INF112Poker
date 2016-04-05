package gamelogic;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by kristianrosland on 05.04.2016.
 */
public class PlayerTest {

    @Test
    public void testToStringFor2CardHand() {
        Card c1 = Card.of(10, Card.Suit.SPADES).get();
        Card c2 = Card.of(10, Card.Suit.CLUBS).get();

        Player p = new Player("Kristian", 5000, 0);

        ArrayList<Card> communityCards = new ArrayList<>();

        p.setHoleCards(c1,c2);
        p.handWon(new Hand(c1,c2, communityCards));

        //Waiting for HandCalculator
        System.out.println();
    }
}