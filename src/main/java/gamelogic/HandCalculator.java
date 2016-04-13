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
        straightFlush = new StraightFlush();
        quad = new xOfaKind(4);
        house = new House();
        flush = new Flush();
        straight = new Straight();
        trips = new xOfaKind(3);
        twoPairs = new TwoPairs();
        pair = new xOfaKind(2);
        highCard = new HighCard();

        rules = new ArrayList<>(9);
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

    public static Map<Integer, Double> getWinningPercentages(List<Player> playersInHand, List<Card> communityCards) {
        assert communityCards.size() >= 3 : "Computing percentages before the flop is displayed takes too much time";

        Map<Integer, Double> percentages = new HashMap<>();
        Map<Integer, Integer> scenariosWon = new HashMap<>();
        playersInHand.stream().forEach(p -> scenariosWon.put(p.getID(), 0));

        ArrayList<Card> usedCards = new ArrayList<>(communityCards);
        playersInHand.stream().forEach(p -> usedCards.addAll(Arrays.asList(p.getHoleCards())));

        addMoreCards(playersInHand, new ArrayList<>(communityCards), scenariosWon, new ArrayList<>(usedCards));

        double totalScenarios = 0;
        for (Integer i : scenariosWon.keySet())
            totalScenarios += scenariosWon.get(i);

        for (Player p : playersInHand)
            percentages.put(p.getID(), (double)scenariosWon.get(p.getID()) / totalScenarios);

        return percentages;
    }

    private static void addMoreCards(List<Player> playersInHand, ArrayList<Card> communityCards, Map<Integer, Integer> scenariosWon, ArrayList<Card> usedCards) {
        if (communityCards.size() == 5) {
            Comparator<Player> comp = (p1, p2) -> p1.getHand(communityCards).compareTo(p2.getHand(communityCards));
            Player winner = playersInHand.stream().max(comp).get();
            scenariosWon.put(winner.getID(), scenariosWon.get(winner.getID())+1);
            return;
        }

        Deck deck = new Deck();

        Optional<Card> c;
        while((c = deck.draw()).isPresent()) {
            Card card = c.get();
            if (usedCards.contains(card)) continue;

            communityCards.add(card);
            usedCards.add(card);
            addMoreCards(playersInHand, communityCards, scenariosWon, usedCards);
            communityCards.remove(card);
            usedCards.remove(card);
        }

    }

}