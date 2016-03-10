package main.java.gamelogic.rules;

import main.java.gamelogic.Card;
import main.java.gamelogic.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by pokki on 10/03/16.
 */
public class StraightFlush implements IRule {

    private List<Card> returnHand = new ArrayList<>();
    private List<Card> cards;

    @Override
    public boolean match(Hand hand) {
        cards = hand.getAllCards();



        return false;
    }

    @Override
    public Optional<List<Card>> getHand() {
        if (returnHand.size() > 0) {
            return Optional.of(returnHand);
        }
        return Optional.empty();
    }}
