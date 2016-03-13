package main.java.gamelogic;

import main.java.gamelogic.rules.IRule;

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

    public List<Card> getHoleCards() {
        List<Card> holeC = new ArrayList<Card>();
        holeC.addAll(holeCards);

        return holeC;
    }

    public List<Card> getCommunityCards() {
        List<Card> communityC = new ArrayList<Card>();
        communityC.addAll(communityCards);

        return communityC;
    }

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

    public int compareTo(Hand other) {
        HandCalculator myCalculator = new HandCalculator(this);
        HandCalculator otherCalculator = new HandCalculator(other);

        HandCalculator.HandType myHandType = myCalculator.getHandType();
        HandCalculator.HandType otherHandType = otherCalculator.getHandType();

        List<Card> myHand = myCalculator.getBestHand().get();
        List<Card> otherHand = otherCalculator.getBestHand().get();

        myHand.sort(Card::compareTo);
        otherHand.sort(Card::compareTo);

        int lastIndex = myHand.size() - 1;
        int comp;

        if (myHandType == otherHandType) {
            // TODO: like hender, sjekk kortene
            switch (myHandType) {
                case STRAIGHT_FLUSH: case STRAIGHT:
                    return myHand.get(lastIndex).compareTo(otherHand.get(lastIndex));

                case QUAD:case HOUSE:
                    for (int i = 0; i <= lastIndex; i+=4) {
                        comp = myHand.get(lastIndex - i).compareTo(otherHand.get(lastIndex - i));

                        if (comp != 0) return comp;
                    }
                    return 0;

                case FLUSH:case HIGH_CARD:
                    for (int i = 0; i < myHand.size(); i++) {
                        comp = myHand.get(lastIndex - i).compareTo(otherHand.get(lastIndex - i));

                        if (comp != 0) return comp;
                    }
                    break;

                case TRIPS:
                    break;
                case TWO_PAIRS:
                    break;
                case PAIR:
                    break;
            }


        }

        for (IRule rule : myCalculator.getRules()) {
            if (rule.getType() == myHandType) {
                return 1;
            } else if (rule.getType() == otherHandType) {
                return -1;
            }
        }

        return 0;
    }
}
