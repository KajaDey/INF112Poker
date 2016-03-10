package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;

import java.util.*;

/**
 * Created by henrik on 09.03.16.
 */
public class House implements IRule {
    private List<Card> allCards = new ArrayList<Card>();
    private boolean isFullHouse = false;


    @Override
    public boolean match(Hand hand) {

        List<Card> cards = hand.getAllCards();
        cards.sort(Card::compareTo);
        allCards = cards;

        Map<Integer,Integer> cardCount = new HashMap<Integer,Integer>();


    //count how many of each card we gt
    for(Card card:cards)
        if(!cardCount.containsKey(card.rank))
            cardCount.put(card.rank,0);
        else
            cardCount.put(card.rank,cardCount.get(card)+1);


        for(Card card:cards) {
            if (cardCount.get(card.rank) >= 2) {
                for (Card otherCard : cards) {
                    if (otherCard.rank != card.rank && cardCount.get(otherCard.rank) > 2) {
                        isFullHouse = true;
                    }
                }
            }
        }

    return isFullHouse;

    }

    @Override
    public Optional<List<Card>> getHand() {

        List<Card> bestCards = new ArrayList<Card>();

        //search cards for a triplet
        for(int i = allCards.size();i>1;i--)
        {
            if(allCards.get(i).rank == allCards.get(i-1).rank && allCards.get(i-1).rank == allCards.get(i-2).rank)
            {
                //add the best triplet
                bestCards.add(allCards.get(i));
                bestCards.add(allCards.get(i-1));
                bestCards.add(allCards.get(i-2));

                //remove it for further use of best cards, remove top three
                allCards.remove(allCards.get(i));
                allCards.remove(allCards.get(i-1));
                allCards.remove(allCards.get(i-2));
                break;

            }
        }

        //search cards for the remaining double(may be a remaining triple but we only choose the two first)
        //
        for(int i = allCards.size();i>0;i++)
        {
            if(allCards.get(i).rank == allCards.get(i-1).rank) {
                bestCards.add(allCards.get(i));
                bestCards.add(allCards.get(i - 1));
                break;
            }
        }


        return Optional.of(bestCards);



    }
}
