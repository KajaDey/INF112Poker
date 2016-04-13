package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import gamelogic.HandCalculator;

import java.util.*;

/**
 * Created by henrik on 09.03.16.
 *
 * Checks if a hand contains a house. (3 + 2 cards of the same rank).
 */
public class House implements IRule {
    private List<Card> returnHand;
    private List<Integer> compareValues = new ArrayList<>();

    @Override
    public boolean match(Hand hand) {
        List<Card> cards = hand.getAllCards();

        if (cards.size() < 5)
            return false;

        cards.sort(Card::compareTo);
        List<Card> allCards = cards;

        Map<Integer, Integer> cardCount = new HashMap<Integer, Integer>();

        //count how many of each card we gt
        for (Card card : cards)
            if (!cardCount.containsKey(card.rank)) {
                cardCount.put(card.rank, 1);
            } else {
                cardCount.put(card.rank, cardCount.get(card.rank) + 1);
            }


        for (Card card : cards) {
            if (cardCount.get(card.rank) > 1) {
                for (Card otherCard : cards) {
                    if (!(otherCard.rank == card.rank) && cardCount.get(otherCard.rank) > 2) {
                        setHand(allCards);
                        return true;
                    }
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
        return HandCalculator.HandType.HOUSE;
    }

    @Override
    public List<Integer> getCompareValues() {
        return compareValues;
    }

    @Override
    public String toString(){
        if (compareValues.isEmpty())
            return "No match";

        return returnHand.get(0).getRankString()+"s full of "+returnHand.get(3).getRankString()+"s";
    }

    private void setHand(List<Card> allCards) {

        returnHand = new ArrayList<>();

        //search cards for a triplet
        for (int i = allCards.size() - 1; i > 1; i--) {
            if (allCards.get(i).rank == allCards.get(i - 1).rank && allCards.get(i).rank == allCards.get(i - 2).rank) {
                //add the best triplet
                returnHand.add(allCards.get(i));
                returnHand.add(allCards.get(i - 1));
                returnHand.add(allCards.get(i - 2));

                compareValues.add(allCards.get(i).rank);

                //remove it for further use of best cards, remove top three
                allCards.remove(i);
                allCards.remove(i - 1);
                allCards.remove(i - 2);
                break;

            }
        }

        //search cards for the remaining double(may be a remaining triple but we only choose the two first)
        //
        for (int i = allCards.size() - 1; i > 0; i--) {
            if (allCards.get(i).rank == allCards.get(i - 1).rank) {
                returnHand.add(allCards.get(i));
                returnHand.add(allCards.get(i - 1));
                compareValues.add(allCards.get(i).rank);

                break;
            }
        }
    }
}
