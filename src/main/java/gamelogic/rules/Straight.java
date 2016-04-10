package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import gamelogic.HandCalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by pokki on 08/03/16.
 *
 * Checks if a hand contains a straight (5 succeeding cards. Ace can be both 1 and 14).
 *
 */
public class Straight implements IRule {
    private List<Card> cards, returnHand;
    private boolean lookingForSF = false;

    /**
     * Regular constructor, used in most cases
     */
    public Straight() {
        cards = new ArrayList<>();
        returnHand = new ArrayList<>();
    }

    /**
     * Alternate constructor, used when looking for straight flush
     * @param flushCards list of cards to check for straight
     */
    public Straight(List<Card> flushCards){
        cards = flushCards;
        lookingForSF = true;
        returnHand = new ArrayList<>();
    }

    @Override
    public boolean match(Hand hand) {

        if (!lookingForSF) {
            cards = hand.getAllCards();
        }

        if (cards.size() < 5) {
            return false;
        }

        int lastCardIndex = cards.size() - 1;

        cards.sort(Card::compareTo);
        int drawCount = 1;

        for (int i = lastCardIndex; i > 0; i--) {
            int thisRank = cards.get(i).rank;
            int nextRank = cards.get(i - 1).rank;

            if (thisRank == nextRank + 1) {
                drawCount++;

            } else if (thisRank == nextRank) {
                continue;
            } else {
                drawCount = 1;
            }

            if (drawCount == 5) {
                fillReturnHand(i-1, false);
                return true;
            }
            // Found cards 2-5, plus Ace
            if (drawCount == 4 && nextRank == 2 && cards.get(lastCardIndex).rank == 14) {
                fillReturnHand(i-1, true);
                return true;
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

    /**
     * Puts the cards that make up the straight in a list
     *
     * @param indexToAdd index of first card in the straight
     * @param addAceLow true if the lowest card in the straight is an ace
     */
    private void fillReturnHand(int indexToAdd, boolean addAceLow) {
        int numberOfCardsAdded = 0;
        int cardsToAdd = 5;

        if (addAceLow) {
            returnHand.add(cards.get(cards.size() - 1));
            cardsToAdd = 4;
        }

        while (numberOfCardsAdded < cardsToAdd) {
            if (indexToAdd == cards.size() -1) {
                returnHand.add(cards.get(indexToAdd));
                break;
            }
            int thisRank = cards.get(indexToAdd).rank;
            int nextRank = cards.get(indexToAdd + 1).rank;

            if (thisRank == nextRank) {
                indexToAdd++;
                continue;
            }
            returnHand.add(cards.get(indexToAdd));
            numberOfCardsAdded++;
            indexToAdd++;
        }
    }

    @Override
    public HandCalculator.HandType getType() {
        return HandCalculator.HandType.STRAIGHT;
    }

    @Override
    public List<Integer> getCompareValues() {
        return Arrays.asList(returnHand.get(returnHand.size()-1).rank);
    }

    @Override
    public String toString() {
        if (returnHand.isEmpty())
            return "No match";
        return returnHand.get(returnHand.size()-1).getRankString() +" high straight";
    }
}