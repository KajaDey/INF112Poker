package gamelogic;

import gamelogic.rules.*;



import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by kaja on 08.03.2016.
 * <p/>
 * check for the best possible hand:
 * straight, flush, house, xofakind (four/three/two cards of the same rank), 2par, highcard
 */
public class HandCalculator {

    public enum HandType {
        STRAIGHT_FLUSH, QUAD, HOUSE, FLUSH, STRAIGHT, TRIPS, TWO_PAIRS, PAIR, HIGH_CARD;
    }

    private Optional<List<Card>> bestHand;
    private List<IRule> rules;
    private HandType handType;
    private StraightFlush straightFlush;
    private xOfaKind quad, trips, pair;
    private House house;
    private Flush flush;
    private Straight straight;
    private TwoPairs twoPairs;
    private HighCard highCard;
    private IRule rule;

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

    public List<IRule> getRules() {
        return rules;
    }

    public Optional<List<Card>> getBestHand() {
        return bestHand;
    }

    public HandType getHandType() {
        return handType;
    }

    public IRule getFoundRule() {
        return rule;
    }
}


