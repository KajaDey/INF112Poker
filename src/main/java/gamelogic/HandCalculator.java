package main.java.gamelogic;

import main.java.gamelogic.rules.*;

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
        Optional<List<Card>> bestHand = Optional.empty();
        Flush flush = null;
        Straight straight = null;
        TwoPairs twoPairs = null;
        xOfaKind xOfaKind = null;
        HighCard highCard = null;

        //TODO check if hand is straight flush
        if(straight.match(hand)){
          //  if(flush.match(straight.getHand()));

        }
        //TODO check if hand is 4OfaKind
        else if(xOfaKind.howManyOfaKind(hand)==4){
            return xOfaKind.getHand();
        }
        //TODO check if hand is house
       // else if(house.match(hand))
       // {

       // }
        //TODO check if hand is flush
        else if(flush.match(hand)) {
            return flush.getHand();
        }
        //TODO check if hand is straight
        else if(straight.match(hand)){
            return straight.getHand();
        }
        //TODO check if hand is tress
        else if(xOfaKind.howManyOfaKind(hand)==3){
            return xOfaKind.getHand();

        }
        //TODO check if hand is two pairs
        else if(twoPairs.match(hand)){
            return twoPairs.getHand();
        }
        //TODO check if hand is pair
        else if(xOfaKind.howManyOfaKind(hand)==2){
            return xOfaKind.getHand();

        }
        //TODO find high card
        else{highCard.match(hand);
            return highCard.getHand();
        }

        //TODO


        //xOfaKind returns an int saying how many equal cards were found (-1 if no equal cards are found)

        return bestHand;
    }
}


