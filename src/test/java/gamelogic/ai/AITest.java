package gamelogic.ai;

import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.GameClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by morten on 08.03.16.
 */
public class AITest {

    static final int N = 1;
    HashMap<Integer, Integer> positions;
    HashMap<Integer, Long> stackSizes;
    HashMap<Integer, String> names;
    int smallBlind = 25;
    int bigBlind = 50;
    long startStack = 1000L;

    @Test
    public void doesNotFoldWithGreatHandHeadsUp() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0);
        doesNotFoldWithGreatHandHeadsUpProperty(simpleAi);
        doesNotFoldWithGreatHandHeadsUpProperty(mctsAi);
    }

    @Test
    public void checksWithShittyHandAsBigBlind() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0);
        checksWithShittyHandAsBigBlindProperty(simpleAi);
        checksWithShittyHandAsBigBlindProperty(mctsAi);
    }

    @Test
    public void foldsWithShittyHandIfNotBlind() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0);
        foldsWithShittyHandIfNotBlindProperty(simpleAi);
        foldsWithShittyHandIfNotBlindProperty(mctsAi);
    }

    @Test
    public void doesNotFoldWithGreatHand() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0);
        doesNotFoldWithGreatHandProperty(simpleAi);
        doesNotFoldWithGreatHandProperty(mctsAi);
    }

    @Test
    public void checksWithShittyHandAsBigBlindHeadsUp() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0);
        checksWithShittyHandAsBigBlindHeadsUpProperty(mctsAi);
        checksWithShittyHandAsBigBlindHeadsUpProperty(simpleAi);
    }

    public void checksWithShittyHandAsBigBlindProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {

            ai.setHandForClient(0, Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());
            setupAi(ai, 3, 1);
            ai.playerMadeDecision(1, new Decision(Decision.Move.CALL));
            ai.playerMadeDecision(2, new Decision(Decision.Move.CALL));

            Assert.assertEquals(new Decision(Decision.Move.CHECK), ai.getDecision());
        }
    }

    public void checksWithShittyHandAsBigBlindHeadsUpProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {

            ai.setHandForClient(0, Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());
            setupAi(ai, 2, 1);
            ai.playerMadeDecision(1, new Decision(Decision.Move.CALL));

            Assert.assertEquals(new Decision(Decision.Move.CHECK), ai.getDecision());
        }
    }

    public void foldsWithShittyHandIfNotBlindProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {
            setupAi(ai, 3, 2);
            ai.setHandForClient(0, Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());

            assertEquals(new Decision(Decision.Move.FOLD), ai.getDecision());
        }
    }

    public void doesNotFoldWithGreatHandProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {

            ai.setHandForClient(0, Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());
            setupAi(ai, 3, 2);

            assertNotEquals(new Decision(Decision.Move.FOLD), ai.getDecision());
        }
    }

    public void doesNotFoldWithGreatHandHeadsUpProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {

            ai.setHandForClient(0, Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());
            setupAi(ai, 2, 0);

            assertNotEquals(new Decision(Decision.Move.FOLD), ai.getDecision());
        }
    }

    public void setupAi(GameClient ai, int amountOfPlayers, int positionOffset) {
        positions = new HashMap<>();
        stackSizes = new HashMap<>();
        names = new HashMap<>();
        ai.setSmallBlind(smallBlind);
        ai.setBigBlind(bigBlind);

        for (int j = 0; j < amountOfPlayers; j++) {
            positions.put(j, (j + positionOffset) % amountOfPlayers);
            stackSizes.put(j, startStack);
            names.put(j, "AI" + j);
        }
        ai.setAmountOfPlayers(amountOfPlayers);
        ai.setPositions(positions);
        ai.setStackSizes(stackSizes);
        ai.setPlayerNames(names);

        ai.playerMadeDecision((0 + positionOffset) % amountOfPlayers, new Decision(Decision.Move.SMALL_BLIND, 25));
        ai.playerMadeDecision((1 + positionOffset) % amountOfPlayers, new Decision(Decision.Move.BIG_BLIND, 50));
    }
}