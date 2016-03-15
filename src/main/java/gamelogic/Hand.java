package gamelogic;

import gamelogic.rules.IRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Hand {
    private List<Card> holeCards = new ArrayList<Card>(2);
    private List<Card> communityCards;
    private List<Card> allCards = new ArrayList<>();

    public Hand(Card card1, Card card2, List<Card> communityCards) {
        holeCards.add(card1);
        holeCards.add(card2);
        this.communityCards = communityCards;

        allCards.add(card1);
        allCards.add(card2);
        allCards.addAll(communityCards);
    }

    /**
     * Gets the two hole cards in the hand
     * @return List of hole cards
     */
    public List<Card> getHoleCards() {
        List<Card> holeC = new ArrayList<Card>();
        holeC.addAll(holeCards);

        return holeC;
    }

    /**
     * Gets the 3-5 community cards in the hand
     * @return List of community cards
     */
    public List<Card> getCommunityCards() {
        List<Card> communityC = new ArrayList<Card>();
        communityC.addAll(communityCards);

        return communityC;
    }

    /**
     * Gets all 5-7 cards in the hand, both hole cards and community cards.
     * @return List of all cards
     */
    public List<Card> getAllCards() {
        List<Card> allC = new ArrayList<Card>();
        allC.addAll(allCards);

        return allC;
    }

    @Override
    public String toString() {
        return "Hand{" +
                "allCards=" + allCards +
                '}';
    }

    /**
     * Compares two hands using the rules of poker hands.
     * @param other Hand to compare to this
     * @return 1 if this hand is better, -1 if other hand is better, 0 if equal
     */
    public int compareTo(Hand other) {
        HandCalculator myCalculator = new HandCalculator(this);
        HandCalculator otherCalculator = new HandCalculator(other);

        HandCalculator.HandType myHandType = myCalculator.getHandType();
        HandCalculator.HandType otherHandType = otherCalculator.getHandType();

        if (myHandType == otherHandType) {
            List<Integer> myCompareValues = myCalculator.getFoundRule().getCompareValues();
            List<Integer> otherCompareValues = otherCalculator.getFoundRule().getCompareValues();

            for (int i = 0; i < myCompareValues.size(); i++) {
                int comp = myCompareValues.get(i).compareTo(otherCompareValues.get(i));
                if (comp != 0) return comp;
            }
            return 0;
        }

        for (IRule rule : myCalculator.getRules()) {
            if (rule.getType() == myHandType) return 1;
            else if (rule.getType() == otherHandType) return -1;
        }
        return 0;
    }
}