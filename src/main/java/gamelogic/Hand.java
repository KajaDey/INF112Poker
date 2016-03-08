package main.java.gamelogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Hand {
    private List<Card> holeCards = new ArrayList<Card>(2);
    private List<Card> communityCards;
    private List<Card> allCards;

    public Hand(Card card1, Card card2, List<Card> communityCards) {
        holeCards.add(card1);
        holeCards.add(card2);
        this.communityCards = communityCards;

        allCards.add(card1);
        allCards.add(card2);
        allCards.addAll(communityCards);
    }

    public List<Card> getHoleCards() {
        return holeCards;
    }

    public List<Card> getCommunityCards() {
        return communityCards;
    }

    public List<Card> getAllCards() { return allCards; }
}
