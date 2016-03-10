package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;

import java.util.List;
import java.util.Optional;

/**
 * Created by Vegar on 08/03/16.
 */
public interface IRule {
    public boolean match(Hand hand);

    public Optional<List<Card>> getHand();
}
