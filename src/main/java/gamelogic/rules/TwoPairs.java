package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by pokki on 08/03/16.
 * <p/>
 * Checks if there are two pairs in the hand. Returns the hand
 */
public class TwoPairs implements IRule {

    private boolean onePair = false;
    private List<Card> returnHand = new ArrayList<Card>();

    @Override
    public boolean match(Hand hand) {
        List<Card> cards = hand.getAllCards();
        cards.sort(Card::compareTo);

        for (int i = cards.size() - 1; i > 0; i--) {

            if (cards.get(i).rank == cards.get(i - 1).rank) {
                if (!onePair) {
                    onePair = true;
                    returnHand.add(cards.get(i));
                    returnHand.add(cards.get(i - 1));
                    cards.remove(i);
                    cards.remove(i - 1);
                    i--;
                } else {
                    returnHand.add(cards.get(i));
                    returnHand.add(cards.get(i - 1));
                    cards.remove(i);
                    cards.remove(i - 1);

                    returnHand.add(cards.get(cards.size() - 1));
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
}

