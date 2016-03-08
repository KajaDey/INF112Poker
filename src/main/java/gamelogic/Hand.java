package main.java.gamelogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Hand {
    private List<Card> holeCards = new ArrayList<Card>(2);
    private List<Card> communityCards;

    public Hand(Card card1, Card card2, List<Card> communityCards) {
        holeCards.add(card1);
        holeCards.add(card2);
        this.communityCards = communityCards;
    }

    public List<Card> getHoleCards() {
        return holeCards;
    }

    public List<Card> getCommunityCards() {
        return communityCards;
    }

    public List<List<Card>> getHand() {
        List<List<Card>> hand = new ArrayList<List<Card>>(7);
        hand.add(holeCards);
        hand.add((communityCards));
        return hand;
    }
}
