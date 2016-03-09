package tests.java.hands;

import com.sun.org.apache.xpath.internal.SourceTree;
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
    Hand handOf4 = null;
    Hand handOf3 = null;
    Hand handOf2 = null;
    Card card = null;

    xOfaKind xOf = new xOfaKind();
    //get inn 7 cards, choose best 5 with either 4,3,2 of the same rank)


    @Before
    public void setUp() throws Exception {

        ArrayList<Card> communityCards = new ArrayList<>();

        communityCards.add(card.of(9, Card.Suit.SPADES).get());
        communityCards.add(card.of(9, Card.Suit.HEARTS).get());
        communityCards.add(card.of(12, Card.Suit.SPADES).get());
        communityCards.add(card.of(11, Card.Suit.CLUBS).get());
        communityCards.add(card.of(7, Card.Suit.DIAMONDS).get());


        Card holeCard1 = card.of(9, Card.Suit.CLUBS).get();
        Card holeCard2 = card.of(9, Card.Suit.DIAMONDS).get();
        Card holeCard3 = card.of(11, Card.Suit.SPADES).get();
        Card holeCard4 = card.of(10, Card.Suit.SPADES).get();



        handOf4 = new Hand(holeCard1,holeCard2,communityCards);
        handOf3 = new Hand(holeCard1,holeCard4,communityCards);
        handOf2 = new Hand(holeCard3,holeCard4,communityCards);




    }

    @Test
    public void testFourLikeCards() throws Exception {
        assertEquals(4,xOf.howManyOfaKind(handOf4));
        System.out.println("4 table: "+handOf4.toString());
        System.out.println("4: "+xOf.getHand());
    }

    @Test
    public void testThreeLikeCards() throws Exception {

        assertEquals(3,xOf.howManyOfaKind(handOf3));
        System.out.println("3 talbe: "+handOf3.toString());
        System.out.println("3: "+xOf.getHand());
    }

    @Test
    public void testTwoLikeCards() throws Exception {
        assertEquals(2,xOf.howManyOfaKind(handOf2));
        System.out.println("2 table: "+handOf2.toString());
        System.out.println("2 "+xOf.getHand());

    }



}