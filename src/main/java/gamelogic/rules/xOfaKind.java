package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by kaja on 08.03.2016.
 *
 * Checks if we have a hand with either 4/3/2 cards of the same rank.
 *
 *
 */
public class xOfaKind implements IRule {
    private boolean fourOfaKind;
    private boolean threeOfaKind;
    private boolean twoOfaKind;
    private List<Card> allCards = new ArrayList<>(7);
    private List<Card> returnHand = new ArrayList<>(5);
    private List<Card> tempHand = new ArrayList<>(5);
    private List<Card> markedCards = new ArrayList<>(5);

    private int nrToCheck=0;

    public xOfaKind(int nrToCheck){
        this.nrToCheck=nrToCheck;
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

        for (int i = allCards.size()-1; i >-1; i--) { //desc

            int rankToCheck = allCards.get(i).rank;
            tempHand.add(allCards.get(i));
            markedCards.add(allCards.get(i));

            for(int j=allCards.size()-2; j>-1; j--) {

                if ((rankToCheck == allCards.get(j).rank)&& !markedCards.contains(allCards.get(j))) {
                    tempHand.add(allCards.get(j));
                    markedCards.add(allCards.get(j));
                }
            }

            switch(nrToCheck) {
                case 4:
                    if(tempHand.size() == 4) {
                        returnHand.addAll(tempHand);
                        int nrOfCardsToAdd = 1;
                        addHighCards(nrOfCardsToAdd);
                        fourOfaKind = true;
                        return true;
                    }
                    break;
                case 3:
                    if (tempHand.size() == 3) {
                        returnHand.addAll(tempHand);
                        int nrOfCardsToAdd = 2;
                        addHighCards(nrOfCardsToAdd);
                        threeOfaKind = true;
                        return true;
                    }
                    break;
                case 2:
                     if (tempHand.size() == 2) {
                        returnHand.addAll(tempHand);
                        int nrOfCardsToAdd = 3;
                        addHighCards(nrOfCardsToAdd);
                        twoOfaKind = true;
                         return true;
                     }
                     break;
            }
            tempHand.clear();
            markedCards.clear();
            if(fourOfaKind || threeOfaKind ||  twoOfaKind)
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

    private void addHighCards(int nrOfCardsToAdd){
        int counter=0;
        for (int i = allCards.size()-1; i >-1; i--) { //desc
            if (!markedCards.contains(allCards.get(i))) {
                returnHand.add(allCards.get(i));
                counter++;
                if (counter == nrOfCardsToAdd )
                    break;
            }
        }
    }
}