package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by henrik on 10.03.16.
 */
public class HouseTest {
    Hand hand = null;

    House house = new House();


    @Before
    public void setUp() throws Exception{

        Card card = null;

        ArrayList<Card> communityCards = new ArrayList<>();

        communityCards.add(card.of(2, Card.Suit.SPADES).get());
        communityCards.add(card.of(2, Card.Suit.SPADES).get());
        communityCards.add(card.of(2, Card.Suit.SPADES).get());
        communityCards.add(card.of(3, Card.Suit.HEARTS).get());
        communityCards.add(card.of(3, Card.Suit.CLUBS).get());

        Card firstPocket = card.of(3, Card.Suit.SPADES).get();
        Card secondPocket = card.of(2, Card.Suit.HEARTS).get();

        //this will hopefully be registered as a full house
        hand = new Hand(firstPocket,secondPocket,communityCards);


    }

    @Test
    public void testIfFullHouseIsRegisteredAsFullHouse(){

        boolean full = house.match(hand);

        System.out.println("count of each card");
        System.out.println(house.cardCount); //prints all cards for reference
        assert(full);
    }

    @Test
    public void testIfBestHandIsChosen(){

        boolean fullHouse = house.match(hand);

        Optional<List<Card>> houseHand = house.getHand();

        System.out.println("best hand possible on table and pocket");
        System.out.println(houseHand); //prints hand for dank visual reference ‎( ͡° ͜ʖ ͡°)

        

    }






}
