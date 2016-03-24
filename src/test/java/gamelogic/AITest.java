package gamelogic;

import gamelogic.ai.SimpleAI;
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
        for (int i = 0; i < N; i++) {
            SimpleAI simpleAi = new SimpleAI(0);
            simpleAi.setBigBlind(20);
            simpleAi.setSmallBlind(10);
            simpleAi.setAmountOfPlayers(3);

            HashMap<Integer, Integer> positions = new HashMap<>();
            positions.put(0, 2);
            positions.put(1, 0);
            positions.put(2, 1);
            simpleAi.setPositions(positions);

            startStack = new HashMap<>();
            startStack.put(0, 1000L);
            startStack.put(1, 1000L);
            startStack.put(2, 1000L);
            simpleAi.setStackSizes(startStack);

            simpleAi.setHandForClient(0, Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());
            HashMap<Integer, Long> stackSizes = new HashMap<>();
            stackSizes.put(0, 1000L);
            stackSizes.put(1, 1000L);
            stackSizes.put(2, 1000L);
            simpleAi.setStackSizes(stackSizes);

            assertEquals(new Decision(Decision.Move.CHECK), simpleAi.getDecision());
        }
    }

    @Test
    public void foldsWithShittyHandIfNotBlind() {
        for (int i = 0; i < N; i++) {
            SimpleAI simpleAi = new SimpleAI(0);
            simpleAi.setBigBlind(20);
            simpleAi.setSmallBlind(10);
            simpleAi.setAmountOfPlayers(3);

            HashMap<Integer, Integer> positions = new HashMap<>();
            positions.put(0, 0);
            positions.put(1, 1);
            positions.put(2, 2);
            simpleAi.setPositions(positions);

            startStack = new HashMap<>();
            startStack.put(0, 1000L);
            startStack.put(1, 1000L);
            startStack.put(2, 1000L);
            simpleAi.setStackSizes(startStack);

            simpleAi.playerMadeDecision(1, new Decision(Decision.Move.BET, 25));
            simpleAi.playerMadeDecision(2, new Decision(Decision.Move.RAISE, 25));

            simpleAi.setHandForClient(0, Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());
            HashMap<Integer, Long> stackSizes = new HashMap<>();
            stackSizes.put(0, 1000L);
            stackSizes.put(1, 1000L);
            stackSizes.put(2, 1000L);
            simpleAi.setStackSizes(stackSizes);

            assertEquals(new Decision(Decision.Move.FOLD), simpleAi.getDecision());
        }
    }

    @Test
    public void doesNotFoldWithGreatHand() {
        for (int i = 0; i < N; i++) {
            SimpleAI simpleAi = new SimpleAI(0);
            simpleAi.setBigBlind(20);
            simpleAi.setSmallBlind(10);
            simpleAi.setAmountOfPlayers(3);

            simpleAi.setHandForClient(0, Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());

            HashMap<Integer, Integer> positions = new HashMap<>();
            positions.put(0, 0);
            positions.put(1, 1);
            positions.put(2, 2);
            simpleAi.setPositions(positions);

            startStack = new HashMap<>();
            startStack.put(0, 1000L);
            startStack.put(1, 1000L);
            startStack.put(2, 1000L);
            simpleAi.setStackSizes(startStack);

            HashMap<Integer, Long> stackSizes = new HashMap<>();
            stackSizes.put(0, 1000L);
            stackSizes.put(1, 1000L);
            stackSizes.put(2, 1000L);
            simpleAi.setStackSizes(stackSizes);

            assertNotEquals(new Decision(Decision.Move.FOLD), simpleAi.getDecision());
        }
    }
}