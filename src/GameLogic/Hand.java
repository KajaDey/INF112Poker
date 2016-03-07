package GameLogic;

import java.util.ArrayList;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Hand {
    private ArrayList<Card> holeCards = new ArrayList<Card>(2);
    private ArrayList<Card> communityCards;

    public Hand(Card card1, Card card2, ArrayList<Card> communityCards) {
        holeCards.add(card1);
        holeCards.add(card2);
        this.communityCards = communityCards;
    }

    public ArrayList<Card> getHoleCards() {
        return holeCards;
    }

    public ArrayList<Card> getCommunityCards() {
        return communityCards;
    }
}
