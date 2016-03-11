package main.java.gamelogic;

import main.java.gamelogic.ai.SimpleAI;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by morten on 08.03.16.
 */
public class AITest {

    static final int N = 100;

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

            simpleAi.setHoleCards(Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());
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

            simpleAi.setHoleCards(Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());
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

            simpleAi.setHoleCards(Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());

            HashMap<Integer, Integer> positions = new HashMap<>();
            positions.put(0, 0);
            positions.put(1, 1);
            positions.put(2, 2);
            simpleAi.setPositions(positions);

            HashMap<Integer, Long> stackSizes = new HashMap<>();
            stackSizes.put(0, 1000L);
            stackSizes.put(1, 1000L);
            stackSizes.put(2, 1000L);
            simpleAi.setStackSizes(stackSizes);

            assertNotEquals(new Decision(Decision.Move.FOLD), simpleAi.getDecision());
        }
    }
}