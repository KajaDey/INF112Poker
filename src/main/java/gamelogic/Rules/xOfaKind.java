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
 *
 * NOT FINISHED!
 */
public class xOfaKind implements IRule {
    private boolean fourOfaKind;
    private boolean threeOfaKind;
    private boolean twoOfaKind;
    private List<Card> hand = new ArrayList<Card>();


    @Override
    public boolean match(Hand hand) {
        List<Card> allCards = hand.getAllCards();
        allCards.sort(Card::compareTo);

        int nrOfEqualCards =0;
        for (int i = allCards.size(); i >0; i--) {
            if(allCards.get(i).rank == allCards.get(i+1).rank){
                nrOfEqualCards++;
            }
        }
        if(nrOfEqualCards==4){
            fourOfaKind =true;
        }
        else if(nrOfEqualCards==3){
            threeOfaKind=true;
        }
        else if(nrOfEqualCards==2){
            twoOfaKind=true;
        }
        // TODO: putt hånda inn i hand
        return (fourOfaKind || threeOfaKind ||  twoOfaKind);
    }
    public int howManyOfaKind(){
        if(fourOfaKind)
            return 4;
        else if(threeOfaKind)
            return 3;
        else if(twoOfaKind)
            return 2;
        else
            return -1;
    }

    @Override
    public Optional<List<Card>> getHand() {
        return Optional.of(hand);
    }
}


