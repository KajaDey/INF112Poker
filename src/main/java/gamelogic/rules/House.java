package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;
import main.java.gamelogic.HandCalculator;

import java.util.*;

/**
 * Created by henrik on 09.03.16.
 */
public class House implements IRule {
    private boolean isFullHouse = false;
    private List<Card> bestCards, allCards, cards;
    private List<Integer> compareValues = new ArrayList<>();

    public Map<Integer, Integer> cardCount = new HashMap<Integer, Integer>();

    @Override
    public boolean match(Hand hand) {

        cards = hand.getAllCards();
        cards.sort(Card::compareTo);
        allCards = cards;

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
                        isFullHouse = true;
                    }
                }
            }
        }
        setHand();
        return isFullHouse;
    }

    @Override
    public Optional<List<Card>> getHand() {
        if (bestCards.size() > 0) {
            return Optional.of(bestCards);
        }
        return Optional.empty();
    }

    public void setHand() {

        bestCards = new ArrayList<Card>();

        //search cards for a triplet
        for (int i = allCards.size() - 1; i > 1; i--) {
            if (allCards.get(i).rank == allCards.get(i - 1).rank && allCards.get(i).rank == allCards.get(i - 2).rank) {
                //add the best triplet
                bestCards.add(allCards.get(i));
                bestCards.add(allCards.get(i - 1));
                bestCards.add(allCards.get(i - 2));

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
                bestCards.add(allCards.get(i));
                bestCards.add(allCards.get(i - 1));
                compareValues.add(allCards.get(i).rank);
                break;
            }
        }
    }

    @Override
    public HandCalculator.HandType getType() {
        return HandCalculator.HandType.HOUSE;
    }

    @Override
    public List<Integer> getCompareValues() {
        return compareValues;
    }
}
