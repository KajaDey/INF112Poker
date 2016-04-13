package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import gamelogic.HandCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by kaja on 09.03.2016.
 *
 * Checks if the hand contains a flush (5 cards of the same suit).
 *
 */
public class Flush implements IRule {
    private List<Card> returnHand = new ArrayList<Card>(5);

    @Override
    public boolean match(Hand hand) {
        List<Card> allCards = hand.getAllCards();

        if (allCards.size() < 5)
            return false;

        allCards.sort(Card::compareTo);

        int nrOfSpades=0;
        int nrOfHearts=0;
        int nrOfClubs=0;
        int nrOfDiamonds=0;

        for (int i = 0; i < allCards.size(); i++) {
            if (allCards.get(i).suit == Card.Suit.SPADES)
                nrOfSpades++;
            else if (allCards.get(i).suit == Card.Suit.HEARTS)
                nrOfHearts++;
            else if (allCards.get(i).suit == Card.Suit.CLUBS)
                nrOfClubs++;
            else if (allCards.get(i).suit == Card.Suit.DIAMONDS)
                nrOfDiamonds++;
        }

        if(nrOfClubs > 4){
            for(int i= allCards.size()-1; i >-1; i--){
                if(allCards.get(i).suit == Card.Suit.CLUBS && returnHand.size()<5)
                    returnHand.add(allCards.get(i));
            }
        }

        if(nrOfDiamonds > 4){
            for(int i = allCards.size()-1; i>-1; i--){
                if(allCards.get(i).suit == Card.Suit.DIAMONDS && returnHand.size()<5)
                    returnHand.add(allCards.get(i));
            }
        }
        if(nrOfHearts > 4){
            for(int i = allCards.size()-1; i >-1; i--){
                if(allCards.get(i).suit == Card.Suit.HEARTS && returnHand.size()<5)
                    returnHand.add(allCards.get(i));
            }

        }
        if(nrOfSpades > 4){
            for(int i = allCards.size()-1; i >-1; i--){
                if(allCards.get(i).suit == Card.Suit.SPADES && returnHand.size()<5) {
                    returnHand.add(allCards.get(i));
                }
            }

        }

        return (nrOfClubs > 4 || nrOfSpades > 4 || nrOfHearts > 4 || nrOfDiamonds > 4);
    }

    @Override
    public Optional<List<Card>> getHand() {
        if (returnHand.size() > 0) {
            return Optional.of(returnHand);
        }
        return Optional.empty();
    }

    @Override
    public HandCalculator.HandType getType() {
        return HandCalculator.HandType.FLUSH;
    }

    @Override
    public List<Integer> getCompareValues() {
        returnHand.sort(Card::compareTo);
        List<Integer> compareValues = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            compareValues.add(returnHand.get(4 - i).rank);
        }
        return compareValues;
    }

    @Override
    public String toString(){
        if(returnHand.isEmpty())
            return "No match";
        returnHand.sort(Card::compareTo);
        return returnHand.get(returnHand.size()-1).getRankString()+ " high flush";
    }
}
