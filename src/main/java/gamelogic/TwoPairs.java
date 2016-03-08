package main.java.gamelogic;

import java.util.List;

/**
 * Created by pokki on 08/03/16.
 */
public class TwoPairs implements IRule {

    private boolean onePair = false;

    @Override
    public boolean match(Hand hand) {
        List<Card> cards = hand.getAllCards();
        cards.sort(Card::compareTo);

        for (int i = 0; i < cards.size() - 1; i++) {

            if (cards.get(i).rank == cards.get(i + 1).rank) {
                if (!onePair) {
                    onePair = true;
                    i++;
                } else {
                    return true;
                }
            }
        }
        return false;
    }
}

