package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import gamelogic.HandCalculator;

import java.util.*;

/**
 * Created by Vegar on 09/03/16.
 *
 * Finds the highest ranking cards in the hand.
 *
 */
public class HighCard implements IRule {
    private List<Card> returnHand = new ArrayList<Card>();

    @Override
    public boolean match(Hand hand) {
        List<Card> allCards = hand.getAllCards();
        Collections.sort(allCards);

        for(int i= allCards.size()-1; i >=0; i--) {
            if (returnHand.size() == 5)
                return true;
            returnHand.add(allCards.get(i));
        }

        return true;
    }

    @Override
    public Optional<List<Card>> getHand() {
        return Optional.of(returnHand);
    }

    @Override
    public HandCalculator.HandType getType() {
        return HandCalculator.HandType.HIGH_CARD;
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
        return "High card "+returnHand.get(0).getRankString();
    }
}
