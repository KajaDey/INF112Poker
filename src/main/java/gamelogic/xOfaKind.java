package main.java.gamelogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kaja on 08.03.2016.
 *
 *
 * NOT FINISHED!
 */
public class xOfaKind implements IRule {
    private boolean FourOfaKind;
    private boolean ThreeOfaKind;
    private boolean TwoOfaKind;
    private List<Card> hand = new ArrayList<Card>();

    //check 4,3,2

    @Override
    public boolean match(Hand hand) {
        List<Card> allCards = hand.getAllCards();
        //TODO: Sorting should place cards in descending order.
        allCards.sort(Card::compareTo);

        int nrOfEqualCards =0;
        for (int i = 0; i < allCards.size() - 1; i++) {
            if(allCards.get(i).rank == allCards.get(i+1).rank){
                nrOfEqualCards++;
            }
        }
        if(nrOfEqualCards==4){
            FourOfaKind=true;
        }
        else if(nrOfEqualCards==3){
            ThreeOfaKind=true;
        }

        // TODO: putt hånda inn i hand
        return false;
    }

    @Override
    public List<Card> getHand() {
        return hand;
    }
}


