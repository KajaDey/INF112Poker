package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import gamelogic.HandCalculator;

import java.util.List;
import java.util.Optional;

/**
 * Created by Vegar on 08/03/16.
 *
 * Interface about the different rules in poker
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

    /**
     * Tells which kind of hand you have, represented by an enum
     * @return the type of rule
     */
    public HandCalculator.HandType getType();


    /**
     * Returns a sorted list of values in a hand to easily compare hands of the same rule.
     * E.g House will return {quad value, pair value}.
     *
     * @return list of values to compare
     */
    public List<Integer> getCompareValues();

    /**
     * Returns the correct string with information about the hand (e.g.: "8 high straight")
     *
     * @return hand information String
     */
    public String toString();

}
