package main.java.gamelogic;

import java.util.List;

/**
 * Created by pokki on 08/03/16.
 */
public class Straight implements IRule {

    private int drawCount = 0;

    @Override
    public boolean match(Hand hand) {

        // TODO: finn BESTE straight
        // TODO: tenk på ess, rank 14

        List<Card> cards = hand.getAllCards();
        cards.sort(Card::compareTo);

        for (int i = 0; i < cards.size() - 1; i++) {
            if (cards.get(i).rank == cards.get(i+1).rank - 1) {
                drawCount++;
            }
            else {
                drawCount = 0;
            }
        }
        return drawCount >= 5;
    }
}
