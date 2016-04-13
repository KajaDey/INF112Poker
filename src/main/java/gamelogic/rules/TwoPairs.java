package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import gamelogic.HandCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by pokki on 08/03/16.
 * <p/>
 * Checks if a hand contains two pairs.
 */
public class TwoPairs implements IRule {

    private List<Card> returnHand = new ArrayList<Card>();
    private List<Integer> compareValues = new ArrayList<>();
    private boolean onePair = false;

    @Override
    public boolean match(Hand hand) {
        List<Card> cards = hand.getAllCards();

        if (cards.size() < 5)
            return false;

        cards.sort(Card::compareTo);

        for (int i = cards.size() - 1; i > 0; i--) {

            if (cards.get(i).rank == cards.get(i - 1).rank) {
                if (!onePair) {
                    onePair = true;
                    returnHand.add(cards.get(i));
                    returnHand.add(cards.get(i - 1));
                    compareValues.add(cards.get(i).rank);
                    cards.remove(i);
                    cards.remove(i - 1);
                    i--;

                } else {
                    returnHand.add(cards.get(i));
                    returnHand.add(cards.get(i - 1));
                    compareValues.add(cards.get(i).rank);
                    cards.remove(i);
                    cards.remove(i - 1);
                    returnHand.add(cards.get(cards.size() - 1)); // high card
                    compareValues.add(cards.get(cards.size() - 1).rank); // high card

                    return true;
                }
            }
        }
        return false;
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
        return HandCalculator.HandType.TWO_PAIRS;
    }

    @Override
    public List<Integer> getCompareValues() {
        return compareValues;
    }

    @Override
    public String toString(){
        if (returnHand.isEmpty())
            return "No match";
        return "Two pairs, " +returnHand.get(0).getRankString() +"s and " +returnHand.get(2).getRankString() +"s";
    }
}

