package main.java.gamelogic.Rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;
import main.java.gamelogic.rules.IRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by kaja on 08.03.2016.
 *
 */
public class xOfaKind implements IRule {
    private boolean fourOfaKind;
    private boolean threeOfaKind;
    private boolean twoOfaKind;
    private int nrOfEquals=0;
    private List<Card> allCards = new ArrayList<Card>(7);
    private List<Card> returnHand = new ArrayList<>(5);
    private List<Card> tempHand = new ArrayList<>(4);
    private List<Card> markedCards = new ArrayList<>(5);



    @Override
    public boolean match(Hand hand) {
        allCards = hand.getAllCards();
        allCards.sort(Card::compareTo);
        for (int i = allCards.size()-1; i >-1; i--) { //desc
            int mainRank = allCards.get(i).rank;
            tempHand.add(allCards.get(i));
            markedCards.add(allCards.get(i));

            for(int j=allCards.size()-2; j>-1; j--) {

                if ((mainRank == allCards.get(j).rank)&& !markedCards.contains(allCards.get(j))) {
                    tempHand.add(allCards.get(j));
                    markedCards.add(allCards.get(j));
                }
            }
            if(tempHand.size()==4){
                returnHand.addAll(tempHand);
                int nrOfCardsToAdd=1;
                addHighCards(nrOfCardsToAdd,i);
                fourOfaKind =true;
                break;
            }
            else if(tempHand.size()==3){
                returnHand.addAll(tempHand);
                int nrOfCardsToAdd=2;
                addHighCards(nrOfCardsToAdd,i);
                threeOfaKind=true;
                break;
            }
            else if(tempHand.size()==2){
                returnHand.addAll(tempHand);
                int nrOfCardsToAdd=3;
                addHighCards(nrOfCardsToAdd,i);
                twoOfaKind=true;
                break;
            }
            tempHand.clear();
            markedCards.clear();
        }
        return (fourOfaKind || threeOfaKind ||  twoOfaKind);
    }


    public int howManyOfaKind(Hand hand){
        match(hand);
        if(fourOfaKind)
            nrOfEquals=4;
        else if(threeOfaKind)
            nrOfEquals=3;
        else if(twoOfaKind)
            nrOfEquals=2;
        return nrOfEquals;
    }


    @Override
    public Optional<List<Card>> getHand() {
        if (returnHand.size() > 0) {
            return Optional.of(returnHand);
        }
        return Optional.empty();
    }

    private void addHighCards(int nrOfCardsToAdd, int cardNr){
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




