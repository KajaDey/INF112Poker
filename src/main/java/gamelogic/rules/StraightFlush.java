package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by pokki on 10/03/16.
 */
public class StraightFlush implements IRule {

    private List<Card> returnHand = new ArrayList<>();
    private List<Card> mainSuitCards = new ArrayList<>();
    private List<Card> cards;

    @Override
    public boolean match(Hand hand) {
        cards = hand.getAllCards();

        Card.Suit mainSuit = findMainSuit();
        Card lastCardAdded = null;

        for (Card c : cards) {
            if (c.suit == mainSuit) {
                mainSuitCards.add(c);
                lastCardAdded = c;
            }
        }

        if (mainSuitCards.size() < 5) {
            return false;
        }

        while (mainSuitCards.size() < 7) {
            mainSuitCards.add(lastCardAdded);
        }

        // handWithMinSuit inneholder alle i mainSuit
//         hand = new Hand(card1, card2, Arrays.asList(card3, card4, card5, card7, card6));

        // Send returnHand inn i straight
        Straight straight = new Straight();
//        straight.match(returnHand);

        // sjekk om true




        return false;
    }


    @Override
    public Optional<List<Card>> getHand() {
        if (returnHand.size() > 0) {
            return Optional.of(returnHand);
        }
        return Optional.empty();
    }

    private Card.Suit findMainSuit() {
        // TODO
        return Card.Suit.CLUBS;
    }
}
