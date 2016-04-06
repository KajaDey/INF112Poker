package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import gamelogic.HandCalculator;

import java.util.*;

/**
 * Created by henrik on 09.03.16.
 *
 * Checks if a given hand is a full house
 */
public class House implements IRule {
    private boolean isFullHouse = false;
    private List<Card> bestCards, allCards, cards;
    private List<Integer> compareValues = new ArrayList<>();

    public Map<Integer, Integer> cardCount = new HashMap<Integer, Integer>();
    public Card tripsCard, pairCard;

    @Override
    public boolean match(Hand hand) {
        cards = hand.getAllCards();

        if (cards.size() < 5)
            return false;

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
                        tripsCard=otherCard;
                        pairCard=card;
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

        return tripsCard.getRankString()+"'s full of "+pairCard.getRankString()+"'s";
    }

    /**
     * Fills the hand with the pair and trip included in the full house.
     */
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
}
