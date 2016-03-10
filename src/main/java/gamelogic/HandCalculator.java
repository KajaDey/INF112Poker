package main.java.gamelogic;

import main.java.gamelogic.rules.*;
import main.java.gamelogic.rules.Straight;
import main.java.gamelogic.rules.TwoPairs;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by kaja on 08.03.2016.
 *
 *
 * check hand for highcard, xofakind (par, tress, firs), 2par, straight, flush, house
 *
 */
public class HandCalculator {

    public Optional<List<Card>> getUsersBestHand(Hand hand){

        //TODO: Not finished, logic in classes, and missing hands in list need to be filled out

        Optional<List<Card>> bestHand = Optional.empty();
        Flush flush = null;
        Straight straight = null;
        TwoPairs twoPairs = null;
        xOfaKind xOfaKind = null;
        HighCard highCard = null;

        List<IRule> hands = new ArrayList<>();
        hands.add(flush);
        hands.add(straight);
        hands.add(twoPairs);
        hands.add(xOfaKind);
        hands.add(highCard);

        for(IRule ir : hands){
            if(ir.match(hand))
                return ir.getHand();
        }

        return bestHand;
    }
}


