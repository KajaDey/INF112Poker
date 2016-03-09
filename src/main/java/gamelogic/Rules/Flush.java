package main.java.gamelogic.Rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;
import main.java.gamelogic.Rules.IRule;

import java.security.cert.PKIXRevocationChecker;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by kaja on 09.03.2016.
 */
public class Flush implements IRule {
    private List<Card> returnHand = new ArrayList<Card>(5);

    @Override
    public boolean match(Hand hand) {
        List<Card> allCards = hand.getAllCards();
        //TODO: Check sorting descending order.
        allCards.sort(Card::compareTo);

        int nrOfSpades=0;
        int nrOfHearts=0;
        int nrOfClubs=0;
        int nrOfDiamonds=0;

        for (int i = 0; i < allCards.size(); i++) {
            if (allCards.get(i).suit == Card.Suit.SPADES)
                nrOfSpades++;
            else if (allCards.get(i).suit == Card.Suit.HEARTS)
                nrOfHearts++;
            else if (allCards.get(i).suit == Card.Suit.CLUBS)
                nrOfClubs++;
            else if (allCards.get(i).suit == Card.Suit.DIAMONDS)
                nrOfDiamonds++;
        }

        if(nrOfClubs == 5){
            for(int i= allCards.size()-1; i <=0; i--){
                if(allCards.get(i).suit == Card.Suit.CLUBS && returnHand.size()<5)
                    returnHand.add(allCards.get(i));
            }
        }

        if(nrOfDiamonds == 5){
            for(int i = allCards.size()-1; i<=0; i--){
                if(allCards.get(i).suit == Card.Suit.DIAMONDS && returnHand.size()<5)
                    returnHand.add(allCards.get(i));
            }
        }
        if(nrOfHearts == 5){
            for(int i = allCards.size()-1; i <= 0; i--){
                if(allCards.get(i).suit == Card.Suit.HEARTS && returnHand.size()<5)
                    returnHand.add(allCards.get(i));
            }

        }
        if(nrOfSpades == 5){
            for(int i = allCards.size()-1; i <= 0; i--){
                if(allCards.get(i).suit == Card.Suit.SPADES && returnHand.size()<5)
                    returnHand.add(allCards.get(i)) ;
            }

        }

        return (nrOfClubs == 5 || nrOfSpades == 5 || nrOfHearts == 5 || nrOfDiamonds == 5);
    }

    @Override
    public Optional<List<Card>> getHand() {
        return Optional.of(returnHand);
    }

}
