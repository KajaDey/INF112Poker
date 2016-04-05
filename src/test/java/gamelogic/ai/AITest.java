package gamelogic.ai;

import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.GameClient;
import gamelogic.ai.SimpleAI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by morten on 08.03.16.
 */
public class AITest {

    static final int N = 100;
    static HashMap<Integer, Long> startStack;

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

    public void checksWithShittyHandAsBigBlindProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {
            ai.setBigBlind(20);
            ai.setSmallBlind(10);
            ai.setAmountOfPlayers(3);

            HashMap<Integer, Integer> positions = new HashMap<>();
            positions.put(0, 2);
            positions.put(1, 0);
            positions.put(2, 1);
            ai.setPositions(positions);

            startStack = new HashMap<>();
            startStack.put(0, 1000L);
            startStack.put(1, 1000L);
            startStack.put(2, 1000L);
            ai.setStackSizes(startStack);

            ai.setHandForClient(0, Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());
            HashMap<Integer, Long> stackSizes = new HashMap<>();
            stackSizes.put(0, 1000L);
            stackSizes.put(1, 1000L);
            stackSizes.put(2, 1000L);
            ai.setStackSizes(stackSizes);

            Assert.assertEquals(new Decision(Decision.Move.CHECK), ai.getDecision());
        }
    }

    public void foldsWithShittyHandIfNotBlindProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {
            ai.setBigBlind(20);
            ai.setSmallBlind(10);
            ai.setAmountOfPlayers(3);

            HashMap<Integer, Integer> positions = new HashMap<>();
            positions.put(0, 0);
            positions.put(1, 1);
            positions.put(2, 2);
            ai.setPositions(positions);

            startStack = new HashMap<>();
            startStack.put(0, 1000L);
            startStack.put(1, 1000L);
            startStack.put(2, 1000L);
            ai.setStackSizes(startStack);

            ai.playerMadeDecision(1, new Decision(Decision.Move.BET, 25));
            ai.playerMadeDecision(2, new Decision(Decision.Move.RAISE, 25));

            ai.setHandForClient(0, Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());
            HashMap<Integer, Long> stackSizes = new HashMap<>();
            stackSizes.put(0, 1000L);
            stackSizes.put(1, 1000L);
            stackSizes.put(2, 1000L);
            ai.setStackSizes(stackSizes);

            assertEquals(new Decision(Decision.Move.FOLD), ai.getDecision());
        }
    }

    public void doesNotFoldWithGreatHandProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {
            ai.setBigBlind(20);
            ai.setSmallBlind(10);
            ai.setAmountOfPlayers(3);

            ai.setHandForClient(0, Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());

            HashMap<Integer, Integer> positions = new HashMap<>();
            positions.put(0, 0);
            positions.put(1, 1);
            positions.put(2, 2);
            ai.setPositions(positions);

            startStack = new HashMap<>();
            startStack.put(0, 1000L);
            startStack.put(1, 1000L);
            startStack.put(2, 1000L);
            ai.setStackSizes(startStack);

            HashMap<Integer, Long> stackSizes = new HashMap<>();
            stackSizes.put(0, 1000L);
            stackSizes.put(1, 1000L);
            stackSizes.put(2, 1000L);
            ai.setStackSizes(stackSizes);

            assertNotEquals(new Decision(Decision.Move.FOLD), ai.getDecision());
        }
    }
}