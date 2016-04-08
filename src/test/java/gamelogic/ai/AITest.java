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

    public void checksWithShittyHandAsBigBlindProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {
            ai.setBigBlind(50);
            ai.setSmallBlind(25);

            int amountOfPlayers = 3;
            ai.setAmountOfPlayers(amountOfPlayers);

            positions = new HashMap<>();
            stackSizes = new HashMap<>();
            names = new HashMap<>();

            for (int j = 0; j < amountOfPlayers; j++) {
                positions.put(j, (j + 1) % amountOfPlayers);
                stackSizes.put(j, 5000L);
                names.put(j, "AI" + j);
            }

            ai.setPositions(positions);
            ai.setStackSizes(stackSizes);
            ai.setPlayerNames(names);
            ai.setHandForClient(0, Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());

            ai.playerMadeDecision(2, new Decision(Decision.Move.SMALL_BLIND, 25));
            ai.playerMadeDecision(0, new Decision(Decision.Move.BIG_BLIND, 50));
            ai.playerMadeDecision(1, new Decision(Decision.Move.CALL));
            ai.playerMadeDecision(2, new Decision(Decision.Move.CALL));

            Assert.assertEquals(new Decision(Decision.Move.CHECK), ai.getDecision());
        }
    }

    public void foldsWithShittyHandIfNotBlindProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {
            ai.setBigBlind(50);
            ai.setSmallBlind(25);
            ai.setAmountOfPlayers(3);

            positions = new HashMap<>();
            stackSizes = new HashMap<>();
            names = new HashMap<>();

            for (int j = 0; j < 3; j++) {
                positions.put(j, (j + 2) % 3);
                stackSizes.put(j, 5000L);
                names.put(j, "AI" + j);
            }
            ai.setPositions(positions);
            ai.setStackSizes(stackSizes);
            ai.setPlayerNames(names);
            ai.setHandForClient(0, Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());

            ai.playerMadeDecision(1, new Decision(Decision.Move.SMALL_BLIND, 25));
            ai.playerMadeDecision(2, new Decision(Decision.Move.BIG_BLIND, 50));

            assertEquals(new Decision(Decision.Move.FOLD), ai.getDecision());
        }
    }

    public void doesNotFoldWithGreatHandProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {
            ai.setBigBlind(50);
            ai.setSmallBlind(25);
            ai.setAmountOfPlayers(3);

            positions = new HashMap<>();
            stackSizes = new HashMap<>();
            names = new HashMap<>();

            for (int j = 0; j < 3; j++) {
                positions.put(j, (j + 2) % 3);
                stackSizes.put(j, 5000L);
                names.put(j, "AI" + j);
            }
            ai.setPositions(positions);
            ai.setStackSizes(stackSizes);
            ai.setPlayerNames(names);
            ai.setHandForClient(0, Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());

            ai.playerMadeDecision(1, new Decision(Decision.Move.SMALL_BLIND, 25));
            ai.playerMadeDecision(2, new Decision(Decision.Move.BIG_BLIND, 50));

            assertNotEquals(new Decision(Decision.Move.FOLD), ai.getDecision());
        }
    }

    public void doesNotFoldWithGreatHandHeadsUpProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {
            ai.setSmallBlind(25);
            ai.setBigBlind(50);
            int amountOfPlayers = 2;
            ai.setAmountOfPlayers(amountOfPlayers);

            positions = new HashMap<>();
            stackSizes = new HashMap<>();
            names = new HashMap<>();

            for (int j = 0; j < amountOfPlayers; j++) {
                positions.put(j, j);
                stackSizes.put(j, 5000L);
                names.put(j, "AI" + j);
            }
            ai.setPositions(positions);
            ai.setStackSizes(stackSizes);
            ai.setPlayerNames(names);
            ai.setHandForClient(0, Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());

            ai.playerMadeDecision(0, new Decision(Decision.Move.SMALL_BLIND, 25));
            ai.playerMadeDecision(1, new Decision(Decision.Move.BIG_BLIND, 50));

            assertNotEquals(new Decision(Decision.Move.FOLD), ai.getDecision());
        }
    }
}