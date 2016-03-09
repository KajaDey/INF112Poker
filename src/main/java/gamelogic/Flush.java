package main.java.gamelogic;

import java.util.List;

/**
 * Created by kaja on 09.03.2016.
 */
public class Flush implements IRule {

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
        return (nrOfClubs == 5 || nrOfSpades == 5 || nrOfHearts == 5 || nrOfDiamonds == 5);
    }
}
