package main.java.gamelogic.Rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;
import main.java.gamelogic.Rules.IRule;
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
    private int nrOfEquals=-1;
    private List<Card> allCards = new ArrayList<Card>(7);
    private List<Card> returnHand = new ArrayList<>(5);
    private List<Card> tempHand = new ArrayList<>(4);



    @Override
    public boolean match(Hand hand) {
        allCards = hand.getAllCards();
        allCards.sort(Card::compareTo);
        for (int i = allCards.size()-1; i >0; i--) { //desc
            int mainRank = allCards.get(i).rank;
            tempHand.add(allCards.get(i));

            for(int j=allCards.size()-1; i>-1; i--) {

                if (mainRank == allCards.get(i - 1).rank) {
                    tempHand.add(allCards.get(j));
                    allCards.remove(j);
                }
            }
            if(tempHand.size()==4){
                fourOfaKind =true;
                returnHand.addAll(tempHand);

            }
            else if(tempHand.size()==3){
                returnHand.addAll(tempHand);
                threeOfaKind=true;
            }
            else if(tempHand.size()==2){
                returnHand.addAll(tempHand);
                twoOfaKind=true;
            }
            tempHand.clear();
        }
        return (fourOfaKind || threeOfaKind ||  twoOfaKind);
    }


    public int howManyOfaKind(){
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

}




