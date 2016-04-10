package gamelogic;

import gamelogic.rules.*;



import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by kaja on 08.03.2016.
 *
 * Class to check for the best possible hand:
 * straight, flush, house, xofakind (four/three/two cards of the same rank), 2par, highcard
 */
public class HandCalculator {

    public enum HandType {
        STRAIGHT_FLUSH, QUAD, HOUSE, FLUSH, STRAIGHT, TRIPS, TWO_PAIRS, PAIR, HIGH_CARD;
    }

    private IRule straightFlush;
    private IRule quad, trips, pair;
    private IRule house;
    private IRule flush;
    private IRule straight;
    private IRule twoPairs;
    private IRule highCard;

    private Optional<List<Card>> bestHand;
    private List<IRule> rules;
    private IRule rule;
    private HandType handType;

    public HandCalculator(Hand hand) {

        bestHand = Optional.empty();
        straightFlush = new StraightFlush();
        quad = new xOfaKind(4);
        house = new House();
        flush = new Flush();
        straight = new Straight();
        trips = new xOfaKind(3);
        twoPairs = new TwoPairs();
        pair = new xOfaKind(2);
        highCard = new HighCard();

        rules = new ArrayList<>();
        rules.add(straightFlush);
        rules.add(quad);
        rules.add(house);
        rules.add(flush);
        rules.add(straight);
        rules.add(trips);
        rules.add(twoPairs);
        rules.add(pair);
        rules.add(highCard);

        for (IRule r : rules) {
            if (r.match(hand)) {
                bestHand = r.getHand();
                handType = r.getType();
                rule = r;
                break;
            }
        }
    }

    /**
     * Gets the list of rules (straight, flush etc)
     * @return
     */
    public List<IRule> getRules() {
        return rules;
    }

    /**
     * Gets the winning hand
     * @return list of cards in best hand
     */
    public Optional<List<Card>> getWinningHand() {
        return bestHand;
    }

    /**
     * Gets the hand type of the winning hand
     * @return Enum of winning hand type
     */
    public HandType getWinningHandType() {
        return handType;
    }

    /**
     * Gets the rule of the winning hand
     * @return found rule
     */
    public IRule getFoundRule() {
        return rule;
    }

    public String getBestHandString() {
        return rule.toString();
    }

}