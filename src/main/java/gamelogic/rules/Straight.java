package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;
import main.java.gamelogic.HandCalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by pokki on 08/03/16.
 */
public class Straight implements IRule {
    private int drawCount = 1;
    private int lastCardIndex;
    private List<Card> cards, returnCards;
    private boolean lookingForStraightFlush = false;
    private HandCalculator.HandType type = HandCalculator.HandType.STRAIGHT;
    private int highCard = 0;

    /**
     * Regular constructor, used in most cases
     */
    public Straight() {
        cards = new ArrayList<>();
        returnCards = new ArrayList<>();
    }

    /**
     * Alternate constructor, used when looking for straight flush
     * @param flushCards list of cards to check for straight
     */
    public Straight(List<Card> flushCards){
        cards = flushCards;
        lookingForStraightFlush = true;
        returnCards = new ArrayList<>();
    }

    @Override
    public boolean match(Hand hand) {

        if (!lookingForStraightFlush) {
            cards = hand.getAllCards();
        }

        lastCardIndex = cards.size() - 1;

        cards.sort(Card::compareTo);

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
        if (returnCards.size() > 0) {
            return Optional.of(returnCards);
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
        int maxCardsToAdd = 5;

        if (addAceLow) {
            returnCards.add(cards.get(lastCardIndex));
            maxCardsToAdd = 4;
        }

        while (numberOfCardsAdded < maxCardsToAdd) {
            if (indexToAdd == cards.size() -1) {
                returnCards.add(cards.get(indexToAdd));
                return;
            }
            int thisRank = cards.get(indexToAdd).rank;
            int nextRank = cards.get(indexToAdd + 1).rank;

            if (thisRank == nextRank) {
                indexToAdd++;
                continue;
            }
            returnCards.add(cards.get(indexToAdd));
            numberOfCardsAdded++;
            indexToAdd++;
        }

        returnCards.sort(Card::compareTo);

        if (addAceLow) {
            for (int i = 0; i < 5; i++) {
                if (returnCards.get(4 - i).rank != 14) {
                    highCard = returnCards.get(4-i).rank;
                    break;
                }
            }
        } else {
            highCard = returnCards.get(4).rank;
        }
    }

    @Override
    public HandCalculator.HandType getType() {
        return HandCalculator.HandType.STRAIGHT;
    }

    @Override
    public List<Integer> getCompareValues() {
        return Arrays.asList(highCard);
    }
}