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
 * check for the best possible hand:
 * straight, flush, house, xofakind (four/three/two cards of the same rank), 2par, highcard
 *
 */
public class HandCalculator {

    public Optional<List<Card>> getUsersBestHand(Hand hand){

        //TODO: Not finished, logic in classes, and missing hands in list need to be filled out

        Optional<List<Card>> bestHand = Optional.empty();
        StraightFlush straightFlush = new StraightFlush();
        xOfaKind quad = new xOfaKind(4);
        House house  = new House();
        Flush flush = new Flush();
        Straight straight = new Straight();
        xOfaKind tress = new xOfaKind(3);
        TwoPairs twoPairs = new TwoPairs();
        xOfaKind pair = new xOfaKind(2);
        HighCard highCard = new HighCard();

        List<IRule> hands = new ArrayList<>();
        hands.add(straightFlush);
        hands.add(quad);
        hands.add(house);
        hands.add(flush);
        hands.add(straight);
        hands.add(tress);
        hands.add(twoPairs);
        hands.add(pair);
        hands.add(highCard);

        for(IRule ir : hands){
            if(ir.match(hand))
                return ir.getHand();
        }

        return bestHand;
    }
}


