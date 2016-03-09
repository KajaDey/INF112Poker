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
    private int drawCount = 0;
    private int lastCardIndex;
    public List<Card> returnHand = new ArrayList<Card>(); // TODO private
    public List<Card> cards; // TODO private

    public Straight(Hand hand) {
        this.cards = hand.getAllCards();
    }

    @Override
    public boolean match(Hand hand) {
        cards = hand.getAllCards();
        lastCardIndex = cards.size() - 1;

        cards.sort(Card::compareTo);

//        for (int i = lastCardIndex; i > 0; i--) {
//            int thisRank = cards.get(i).rank;
//            int nextRank = cards.get(i - 1).rank;
//
//            if (drawCount == 5) {
//                fillReturnHand(i, false);
//                return true;
//            }
//
//            // Found cards 2-5, plus Ace
//            if (drawCount == 4 && thisRank == 2 && cards.get(lastCardIndex).rank == 14) {
//                fillReturnHand(i, true);
//                return true;
//            }
//
//            if (thisRank == nextRank + 1) {
//                drawCount++;
//            } else if (thisRank == nextRank) {
//                continue;
//            } else {
//                drawCount = 0;
//            }
//        }
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
        int numberOfCardsToAdd = 5;

        if (addAceLow) {
            returnHand.add(cards.get(lastCardIndex));
            numberOfCardsToAdd = 4;
        }

        for (int i = 0; i <= numberOfCardsToAdd; i++) {
            int thisRank = cards.get(indexToAdd).rank;
            int nextRank = cards.get(indexToAdd + 1).rank;

            while (thisRank == nextRank) {
                indexToAdd++;
                thisRank = nextRank;
                nextRank = cards.get(indexToAdd + 1).rank;
            }
            returnHand.add(cards.get(indexToAdd));
            indexToAdd++;
        }
    }
}