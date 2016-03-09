package main.java.gamelogic;

import main.java.gamelogic.Hand;

import java.util.List;

/**
 * Created by Vegar on 08/03/16.
 */
public interface IRule {
    public boolean match(Hand hand);

    public List<Card> getHand();
}
