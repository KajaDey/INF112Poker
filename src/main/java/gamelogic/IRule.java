package main.java.gamelogic;

import main.java.gamelogic.Hand;

/**
 * Created by Vegar on 08/03/16.
 */
public interface IRule {
    public boolean match(Hand hand);
}
