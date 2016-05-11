package gamelogic;

import gamelogic.rules.*;


import java.util.*;
import java.util.function.Consumer;

/**
 * Created by kaja on 08.03.2016.
 *
 * Class to check for the best possible hand:
 * straight, flush, house, xofakind (four/three/two cards of the same rank), 2par, highcard
 */
public class HandCalculator {

    public enum HandType {
        STRAIGHT_FLUSH, QUAD, HOUSE, FLUSH, STRAIGHT, TRIPS, TWO_PAIRS, PAIR, HIGH_CARD
    }

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

    /**
     * Gets the chances each payer has of winning the pot, for use during showdown
     * @param holeCards The holecards for each player
     * @param communityCards The community cards, which may be empty
     * @return A map from player ids to their probability of winning
     */
    public static Map<Integer, Double> getNewWinningPercentages(Map<Integer, Card[]> holeCards, List<Card> communityCards) {
        return getNewWinningPercentages(holeCards, communityCards, (s) -> {});
    }

    /**
     * Gets the chances each payer has of winning the pot, for use during showdown
     * @param holeCards The holecards for each player
     * @param communityCards The community cards, which may be empty
     * @param progressCallback If you do not want to wait for the percentages to be calulated precisely, this function will be called with preliminary results
     * @return A map from player ids to their probability of winning
     */
    public static Map<Integer, Double> getNewWinningPercentages(Map<Integer, Card[]> holeCards, List<Card> communityCards,
                                                                Consumer<Map<Integer, Double>> progressCallback) {
        holeCards = new HashMap<>(holeCards);
        communityCards = communityCards == null ? new ArrayList<>() : new ArrayList<>(communityCards);
        Map<Integer, Integer> scenariosWon = new HashMap<>();
        ArrayList<Card> unusedCards = new ArrayList<>(Arrays.asList(Card.getAllCards()));

        holeCards.forEach((id, cards) -> {
            scenariosWon.put(id, 0);
            unusedCards.removeAll(Arrays.asList(cards));
        });
        unusedCards.removeAll(communityCards);
        Collections.shuffle(unusedCards);

        addWinningPercentages(holeCards, scenariosWon, communityCards, unusedCards, 0, progressCallback);

        return percentagesFromScenariosWon(scenariosWon);
    }

    private static void addWinningPercentages(Map<Integer, Card[]> holeCards, Map<Integer, Integer> scenariosWon,
                                              List<Card> communityCards, ArrayList<Card> unusedCards, int startIndex,
                                              Consumer<Map<Integer, Double>> progressCallback) {
        if (startIndex == unusedCards.size()) {
            return;
        }
        int totalScenariosPlayed = scenariosWon.keySet().stream()
                .map(scenariosWon::get)
                .reduce(0, Integer::sum);
        if (totalScenariosPlayed % 1000 == 0 && totalScenariosPlayed > 0) {
            if (Thread.currentThread().isInterrupted()) {
                //System.out.println(System.currentTimeMillis() % 10000 + "Thread has been interrupted, returning");
                return;
            }
            progressCallback.accept(percentagesFromScenariosWon(scenariosWon));
        }
        if (communityCards.size() == 5) {
            Comparator<Card[]> comp = (hc1, hc2) -> new Hand(hc1[0], hc1[1], communityCards).compareTo(new Hand(hc2[0], hc2[1], communityCards));
            Card[] max = Collections.max(holeCards.values(), comp);
            holeCards.forEach((id, hc) -> {
                if (Arrays.equals(hc, max))
                    scenariosWon.put(id, scenariosWon.get(id) + 1);
            });
            return;
        }
        List<Card> newCommunityCards = new ArrayList<>(communityCards);

        for (int i = startIndex; i < unusedCards.size(); i++) {
            if (totalScenariosPlayed % 1000 == 0) {
                if (Thread.currentThread().isInterrupted()) {
                    //System.out.println(System.currentTimeMillis() % 10000 + "Thread has been interrupted, returning");
                    return;
                }
            }
            newCommunityCards.add(unusedCards.get(i));
            addWinningPercentages(holeCards, scenariosWon, newCommunityCards, unusedCards, i + 1, progressCallback);
            newCommunityCards.remove(newCommunityCards.size() - 1);
        }
    }

    private static Map<Integer, Double> percentagesFromScenariosWon(Map<Integer, Integer> scenariosWon) {
        Map<Integer, Double> percentages = new HashMap<>();
        int totalScenarios = scenariosWon.keySet().stream()
                .map(scenariosWon::get)
                .reduce(0, Integer::sum);
        scenariosWon.forEach((id, numWon) -> percentages.put(id, (double)numWon / totalScenarios));
        return percentages;
    }
}