package tests.java.gamelogic;

import main.java.gamelogic.AI;
import main.java.gamelogic.Card;
import main.java.gamelogic.Decision;
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
            AI ai = new AI(0);
            ai.setBigBlind(20);
            ai.setSmallBlind(10);
            ai.setAmountOfPlayers(3);

            HashMap<Integer, Integer> positions = new HashMap<>();
            positions.put(0, 1);
            positions.put(1, 2);
            positions.put(2, 0);
            ai.setPositions(positions);

            ai.setHoleCards(Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());
            HashMap<Integer, Long> stackSizes = new HashMap<>();
            stackSizes.put(0, 1000L);
            stackSizes.put(1, 1000L);
            stackSizes.put(2, 1000L);
            ai.setStackSizes(stackSizes);

            assertEquals(new Decision(Decision.Move.CHECK), ai.getDecision());
        }
    }

    @Test
    public void foldsWithShittyHandIfNotBlind() {
        for (int i = 0; i < N; i++) {
            AI ai = new AI(0);
            ai.setBigBlind(20);
            ai.setSmallBlind(10);
            ai.setAmountOfPlayers(3);

            HashMap<Integer, Integer> positions = new HashMap<>();
            positions.put(0, 0);
            positions.put(1, 1);
            positions.put(2, 2);
            ai.setPositions(positions);

            ai.setHoleCards(Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());
            HashMap<Integer, Long> stackSizes = new HashMap<>();
            stackSizes.put(0, 1000L);
            stackSizes.put(1, 1000L);
            stackSizes.put(2, 1000L);
            ai.setStackSizes(stackSizes);

            assertEquals(new Decision(Decision.Move.FOLD), ai.getDecision());
        }
    }

    @Test
    public void doesNotFoldWithGreatHand() {
        for (int i = 0; i < N; i++) {
            AI ai = new AI(0);
            ai.setBigBlind(20);
            ai.setSmallBlind(10);
            ai.setAmountOfPlayers(3);

            ai.setHoleCards(Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());

            HashMap<Integer, Integer> positions = new HashMap<>();
            positions.put(0, 0);
            positions.put(1, 1);
            positions.put(2, 2);
            ai.setPositions(positions);

            HashMap<Integer, Long> stackSizes = new HashMap<>();
            stackSizes.put(0, 1000L);
            stackSizes.put(1, 1000L);
            stackSizes.put(2, 1000L);
            ai.setStackSizes(stackSizes);

            assertNotEquals(new Decision(Decision.Move.FOLD), ai.getDecision());
        }
    }
}