package gamelogic;

import gamelogic.rules.*;


import java.util.*;

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

        rules = new ArrayList<>(9);
        rules.add(new StraightFlush());
        rules.add(new xOfaKind(4));
        rules.add(new House());
        rules.add(new Flush());
        rules.add(new Straight());
        rules.add(new xOfaKind(3));
        rules.add(new TwoPairs());
        rules.add(new xOfaKind(2));
        rules.add(new HighCard());

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
     * Converts the hand into a simple integer score, for quickly comparing and sorting hands
     */
    public int getHandScore() {
        List<Integer> compareValues = getFoundRule().getCompareValues();
        assert compareValues.size() <= 5 : "CompareValues had length " + compareValues.size();

        int handScore = (HandType.values().length - handType.ordinal()) << 6 * 4;
        for (int i = 0; i < compareValues.size(); i++) {
            assert compareValues.get(i) < 16;
            handScore |= compareValues.get(i) << (4 * 4 - (i * 4));
        }
        return handScore;
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

    public static Map<Integer, Double> getWinningPercentages(Map<Integer, Card[]> holeCards, List<Card> communityCards) {
        assert communityCards.size() >= 3 : "Computing percentages before the flop is displayed takes too much time";

        Map<Integer, Double> percentages = new HashMap<>();
        Map<Integer, Integer> scenariosWon = new HashMap<>();
        holeCards.forEach((id, hc) -> scenariosWon.put(id, 0));

        ArrayList<Card> usedCards = new ArrayList<>(communityCards);
        holeCards.forEach((id, hc) -> usedCards.addAll(Arrays.asList(hc)));

        addMoreCards(holeCards, new ArrayList<>(communityCards), scenariosWon, new ArrayList<>(usedCards));

        double totalScenarios = 0;
        for (Integer i : scenariosWon.keySet())
            totalScenarios += scenariosWon.get(i);

        final double tot = totalScenarios;
        holeCards.forEach((id, hc) -> percentages.put(id, (double)scenariosWon.get(id) / tot));

        return percentages;
    }

    private static void addMoreCards(Map<Integer, Card[]> holeCards, ArrayList<Card> communityCards, Map<Integer, Integer> scenariosWon, ArrayList<Card> usedCards) {
        if (communityCards.size() == 5) {
            Comparator<Card[]> comp = (hc1, hc2) -> new Hand(hc1[0], hc1[1], communityCards).compareTo(new Hand(hc2[0], hc2[1], communityCards));
            Card[] max = Collections.max(holeCards.values(), comp);
            holeCards.forEach((id, hc) -> {
                if (hc.equals(max))
                    scenariosWon.put(id, scenariosWon.get(id)+1);
            });
            return;
        }

        Deck deck = new Deck();

        Optional<Card> c;
        while((c = deck.draw()).isPresent()) {
            Card card = c.get();
            if (usedCards.contains(card)) continue;

            communityCards.add(card);
            usedCards.add(card);
            addMoreCards(holeCards, communityCards, scenariosWon, usedCards);
            communityCards.remove(card);
            usedCards.remove(card);
        }

    }

}