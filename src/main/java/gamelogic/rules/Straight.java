package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by pokki on 08/03/16.
 */
public class Straight implements IRule {
    private int drawCount = 1;
    private int lastCardIndex;
    private List<Card> returnHand = new ArrayList<>();
    private List<Card> cards;

    @Override
    public boolean match(Hand hand) {
        cards = hand.getAllCards();
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
        if (returnHand.size() > 0) {
            return Optional.of(returnHand);
        }
        return Optional.empty();
    }

    private void fillReturnHand(int indexToAdd, boolean addAceLow) {
        int numberOfCardsAdded = 0;
        int maxCardsToAdd = 5;

        if (addAceLow) {
            returnHand.add(cards.get(lastCardIndex));
            maxCardsToAdd = 4;
        }

        while (numberOfCardsAdded < maxCardsToAdd) {
            if (indexToAdd == cards.size() -1) {
                returnHand.add(cards.get(indexToAdd));
                return;
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
}