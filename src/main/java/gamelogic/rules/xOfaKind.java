package gamelogic.rules;

import gamelogic.Card;
import gamelogic.Hand;
import gamelogic.HandCalculator;

import java.util.*;

/**
 * Created by kaja on 08.03.2016.
 * <p/>
 * Checks if a given hand has either 4/3/2 cards of the same rank.
 */
public class xOfaKind implements IRule {
    private boolean fourOfaKind;
    private boolean threeOfaKind;
    private boolean twoOfaKind;
    private List<Card> allCards = new ArrayList<>(7);
    private List<Card> returnHand = new ArrayList<>(5);
    private List<Card> tempHand = new ArrayList<>(5);
    private List<Card> markedCards = new ArrayList<>(5);

    //private Card quadCard, tressCArd, pairCard;

    private int nrToCheck;
    private List<Integer> compareValues = new ArrayList<>();

    public xOfaKind(int nrToCheck) {
        this.nrToCheck = nrToCheck;
    }

    /**
     * Goes through an arrayList of up to seven cards in descending order. For every card of the original hand, we check if there are other equal car
     *
     * @param hand
     * @return true/false if there is a match with either 4/3/2 cards of the same rank.
     */
    @Override
    public boolean match(Hand hand) {
        allCards = hand.getAllCards();

        allCards.sort(Card::compareTo);

        for (int i = allCards.size() - 1; i > -1; i--) { //desc

            int rankToCheck = allCards.get(i).rank;
            tempHand.add(allCards.get(i));
            markedCards.add(allCards.get(i));

            for (int j = allCards.size() - 2; j > -1; j--) {

                if ((rankToCheck == allCards.get(j).rank) && !markedCards.contains(allCards.get(j))) {
                    tempHand.add(allCards.get(j));
                    markedCards.add(allCards.get(j));
                }
            }

            switch (nrToCheck) {
                case 4:
                    if (tempHand.size() == 4) {
                        returnHand.addAll(tempHand);
                        int nrOfCardsToAdd = 1;
                        compareValues.add(rankToCheck);
                        addHighCards(nrOfCardsToAdd);
                        fourOfaKind = true;
                        return true;
                    }
                    break;
                case 3:
                    if (tempHand.size() == 3) {
                        returnHand.addAll(tempHand);
                        int nrOfCardsToAdd = 2;
                        compareValues.add(rankToCheck);
                        addHighCards(nrOfCardsToAdd);
                        threeOfaKind = true;
                        return true;
                    }
                    break;
                case 2:
                    if (tempHand.size() == 2) {
                        returnHand.addAll(tempHand);
                        int nrOfCardsToAdd = 3;
                        compareValues.add(rankToCheck);
                        addHighCards(nrOfCardsToAdd);
                        twoOfaKind = true;
                        return true;
                    }
                    break;
            }
            tempHand.clear();
            markedCards.clear();
            if (fourOfaKind || threeOfaKind || twoOfaKind)
                break;
        }
        return (false);
    }


    @Override
    public Optional<List<Card>> getHand() {
        if (returnHand.size() > 0) {
            return Optional.of(returnHand);
        }
        return Optional.empty();
    }


    @Override
    public HandCalculator.HandType getType() {
        return nrToCheck == 4 ? HandCalculator.HandType.QUAD
                : nrToCheck == 3 ? HandCalculator.HandType.TRIPS
                : HandCalculator.HandType.PAIR;
    }

    @Override
    public List<Integer> getCompareValues() {
        return compareValues;
    }

    @Override
    public String toString(){
        if(fourOfaKind)
            return "Quad "+returnHand.get(0).getRankString()+"'s";
        else if(threeOfaKind)
            return "Trip "+returnHand.get(0).getRankString()+"'s";
        else if (twoOfaKind)
            return "Pair of "+returnHand.get(0).getRankString()+"'s";
        return "";
    }

    /**
     * Fills return hand with high cards after the pair/trip/quad was added.
     * @param nrOfCardsToAdd Number of high cards to add
     */
    private void addHighCards(int nrOfCardsToAdd) {
        int counter = 0;
        for (int i = allCards.size() - 1; i > -1; i--) { //desc
            if (!markedCards.contains(allCards.get(i))) {
                returnHand.add(allCards.get(i));
                compareValues.add(allCards.get(i).rank);
                counter++;
                if (counter == nrOfCardsToAdd)
                    break;
            }
        }
    }
}
