package main.java.gamelogic.Rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Vegar on 09/03/16.
 */
public class HighCard implements IRule {
    private List<Card> returnHand = new ArrayList<Card>(5);

    @Override
    public boolean match(Hand hand) {
        List<Card> allCards = hand.getAllCards();
        for(int i= allCards.size()-1; i <=2; i--) {
            returnHand.add(allCards.get(i));
        }
        return true;
    }

    @Override
    public Optional<List<Card>> getHand() {
        return Optional.of(returnHand);
    }
}
