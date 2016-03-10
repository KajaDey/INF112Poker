package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;

import java.util.List;
import java.util.Optional;

/**
 * Created by Vegar on 08/03/16.
 */
public interface IRule {

    /**
     * Checks if the hand (hole cards and community cards) matches the specific rule.
     * @param hand Hand of 5-7 cards
     * @return true if the hand matches the rule
     */
    public boolean match(Hand hand);

    /**
     * Gets the five-card hand found in the match-method, if there is a match.
     * Returns empty if there is no match with the rule.
     *
     * @return the five-card hand, or empty
     */
    public Optional<List<Card>> getHand();
}
