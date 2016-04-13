package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import gamelogic.HandCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by pokki on 10/03/16.
 *
 * Checks if a hand contains a straight flush. (5 succeeding cards in the same suit)
 *
 */
public class StraightFlush implements IRule {

    private List<Card> returnHand = new ArrayList<>();
    private List<Integer> compareValues;

    @Override
    public boolean match(Hand hand) {
        List<Card> cards = hand.getAllCards();

        if (cards.size() < 5)
            return false;

        Card.Suit mainSuit = findMainSuit(cards);
        List<Card> mainSuitCards = getMainSuitCards(mainSuit, cards);

        if (mainSuitCards.size() < 5) {
            return false;
        }

        Straight straight = new Straight(mainSuitCards);
        boolean straightFlushMatch = straight.match(hand);

        if (straightFlushMatch) {
            returnHand.addAll(straight.getHand().get());
            returnHand.sort(Card::compareTo);
            compareValues = straight.getCompareValues();
        }

        return straightFlushMatch;
    }

    @Override
    public Optional<List<Card>> getHand() {
        if (!returnHand.isEmpty()) {
            return Optional.of(returnHand);
        }
        return Optional.empty();
    }

    @Override
    public HandCalculator.HandType getType() {
        return HandCalculator.HandType.STRAIGHT_FLUSH;
    }

    @Override
    public List<Integer> getCompareValues() {
        return compareValues;
    }

    @Override
    public String toString(){
        if (returnHand.isEmpty())
            return "No match";
        Card highCard = returnHand.get(returnHand.size()-1);
        if(highCard.getRankString()=="Ace")
            return "Royal flush";
        return highCard.getRankString()+" high straight flush";
    }

    /**
     * Finds the most common suit (main suit) in the array of cards.
     *
     * @param cards hole cards and community cards
     * @return main suit
     */
    private Card.Suit findMainSuit(List<Card> cards) {
        int numClubs = 0;
        int numSpades = 0;
        int numDiamonds = 0;
        int numHearts = 0;

        for (Card c : cards) {
            Card.Suit suit = c.suit;
            switch (suit) {
                case CLUBS: numClubs++; break;
                case SPADES: numSpades++; break;
                case DIAMONDS: numDiamonds++; break;
                case HEARTS: numHearts++; break;
            }
        }

        int mainSuitCount = numClubs;
        Card.Suit mainSuit = Card.Suit.CLUBS;

        if (Math.max(numSpades, mainSuitCount) == numSpades) {
            mainSuitCount = numSpades;
            mainSuit = Card.Suit.SPADES;
        }
        if (Math.max(numDiamonds, mainSuitCount) == numDiamonds) {
            mainSuitCount = numDiamonds;
            mainSuit = Card.Suit.DIAMONDS;
        }
        if (Math.max(numHearts, mainSuitCount) == numHearts) {
            mainSuit = Card.Suit.HEARTS;
        }
        return mainSuit;
    }

    /**
     *  Fills and returns an array of all cards of the most common suit.
     * @param mainSuit the most common Suit
     * @return cards of the main suit
     */
    private List<Card> getMainSuitCards(Card.Suit mainSuit, List<Card> cards) {
        List<Card> mainSuitCards = new ArrayList<>();
        for (Card c : cards) {
            if (c.suit == mainSuit) {
                mainSuitCards.add(c);
            }
        }
        return mainSuitCards;
    }
}
