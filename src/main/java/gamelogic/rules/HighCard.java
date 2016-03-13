package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;
import main.java.gamelogic.HandCalculator;

import java.util.*;

/**
 * Created by Vegar on 09/03/16.
 */
public class HighCard implements IRule {
    private List<Card> returnHand = new ArrayList<Card>();

    @Override
    public boolean match(Hand hand) {
        List<Card> allCards = hand.getAllCards();
        Collections.sort(allCards);
        for(int i= allCards.size()-1; i >1; i--) {
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
}
