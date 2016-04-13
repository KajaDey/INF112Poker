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
    long timeToThink = 1000L;

    @Test
    public void testAllInAsCall() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0);
        testAllInAsCallProperty(simpleAi);
        testAllInAsCallProperty(mctsAi);
    }

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

            Assert.assertEquals(new Decision(Decision.Move.CHECK), ai.getDecision(timeToThink));
        }
    }

    public void checksWithShittyHandAsBigBlindHeadsUpProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {

            ai.setHandForClient(0, Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());
            setupAi(ai, 2, 1);
            ai.playerMadeDecision(1, new Decision(Decision.Move.CALL));

            Assert.assertEquals(new Decision(Decision.Move.CHECK), ai.getDecision(timeToThink));
        }
    }

    public void foldsWithShittyHandIfNotBlindProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {
            ai.setHandForClient(0, Card.of(2, Card.Suit.HEARTS).get(), Card.of(7, Card.Suit.SPADES).get());
            setupAi(ai, 3, 2);

            assertEquals(new Decision(Decision.Move.FOLD), ai.getDecision(timeToThink));
        }
    }

    public void doesNotFoldWithGreatHandProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {

            ai.setHandForClient(0, Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());
            setupAi(ai, 3, 2);

            assertNotEquals(new Decision(Decision.Move.FOLD), ai.getDecision(timeToThink));
        }
    }

    public void doesNotFoldWithGreatHandHeadsUpProperty(GameClient ai) {
        for (int i = 0; i < N; i++) {

            ai.setHandForClient(0, Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());
            setupAi(ai, 2, 0);

            assertNotEquals(new Decision(Decision.Move.FOLD), ai.getDecision(timeToThink));
        }
    }

    public void testAllInAsCallProperty(GameClient ai) {
        ai.setHandForClient(0, Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());

        HashMap<Integer, Integer> positions = new HashMap<>();
        HashMap<Integer, String> names = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            positions.put(i, i);
            names.put(i, "AI-" + i);
        }

        HashMap<Integer, Long> stackSizes = new HashMap<>();
        stackSizes.put(0, 3000L);
        stackSizes.put(1, 1000L);
        stackSizes.put(2, 3000L);
        stackSizes.put(3, 4000L);

        ai.setAmountOfPlayers(4);
        ai.setStackSizes(stackSizes);
        ai.setPlayerNames(names);
        ai.setPositions(positions);
        ai.setSmallBlind(smallBlind);
        ai.setBigBlind(bigBlind);

        ai.playerMadeDecision(0, new Decision(Decision.Move.SMALL_BLIND, smallBlind));
        ai.playerMadeDecision(1, new Decision(Decision.Move.BIG_BLIND, bigBlind));
        ai.playerMadeDecision(2, new Decision(Decision.Move.CALL));
        ai.playerMadeDecision(3, new Decision(Decision.Move.CALL));
        ai.playerMadeDecision(0, new Decision(Decision.Move.CALL));
        ai.playerMadeDecision(1, new Decision(Decision.Move.CHECK));

        ai.setFlop(Card.of(14, Card.Suit.DIAMONDS).get(), Card.of(13, Card.Suit.HEARTS).get(), Card.of(13, Card.Suit.SPADES).get(), 75L);

        ai.playerMadeDecision(0, new Decision(Decision.Move.RAISE, 1000));
        ai.playerMadeDecision(1, new Decision(Decision.Move.ALL_IN));
        ai.playerMadeDecision(2, new Decision(Decision.Move.FOLD));
        ai.playerMadeDecision(3, new Decision(Decision.Move.CALL));

        ai.setTurn(Card.of(14, Card.Suit.CLUBS).get(), 75L);

        ai.getDecision(timeToThink);
        ai.playerMadeDecision(0, new Decision(Decision.Move.ALL_IN));
        ai.playerMadeDecision(3, new Decision(Decision.Move.CALL));

        ai.setRiver(Card.of(2, Card.Suit.CLUBS).get(), 75L);
    }
    @Test
    public void testPreflopShowdown() {
        MCTSAI ai = new MCTSAI(3);
        ai.setHandForClient(3, Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());

        HashMap<Integer, Integer> positions = new HashMap<>();
        HashMap<Integer, String> names = new HashMap<>();
        HashMap<Integer, Long> stackSizes = new HashMap<>();
        positions.put(0, 1);
        stackSizes.put(0, 10000L);
        names.put(0, "Morten");
        positions.put(3, 2);
        stackSizes.put(3, 15000L);
        names.put(3, "Hermoine");
        positions.put(4, 0);
        stackSizes.put(4, 5000L);
        names.put(4, "Ron");

        ai.setAmountOfPlayers(3);
        ai.setStackSizes(stackSizes);
        ai.setPlayerNames(names);
        ai.setPositions(positions);

        ai.playerMadeDecision(4, new Decision(Decision.Move.SMALL_BLIND, smallBlind));
        ai.playerMadeDecision(0, new Decision(Decision.Move.BIG_BLIND, bigBlind));
        ai.playerMadeDecision(3, new Decision(Decision.Move.RAISE, 75));
        ai.playerMadeDecision(4, new Decision(Decision.Move.FOLD));
        ai.playerMadeDecision(0, new Decision(Decision.Move.ALL_IN));
        ai.playerMadeDecision(3, new Decision(Decision.Move.CALL));

        ai.setFlop(Card.of(14, Card.Suit.DIAMONDS).get(), Card.of(13, Card.Suit.HEARTS).get(), Card.of(13, Card.Suit.SPADES).get(), 75L);

        ai.setTurn(Card.of(14, Card.Suit.CLUBS).get(), 75L);

        ai.setRiver(Card.of(2, Card.Suit.CLUBS).get(), 75L);
    }

    /**
     * Prepares the AI by setting names, positions and stacksizes of the players to default values
     * Also gives blinds
     * @param positionOffset Determines the positions of the AIs. If 0, id=0 -> position=0. If 2, id=0 -> position=2.
     */
    public void setupAi(GameClient ai, int amountOfPlayers, int positionOffset) {
        positions = new HashMap<>();
        stackSizes = new HashMap<>();
        names = new HashMap<>();
        ai.setSmallBlind(smallBlind);
        ai.setBigBlind(bigBlind);

        for (int j = 0; j < amountOfPlayers; j++) {
            positions.put(j, (j + positionOffset) % amountOfPlayers);
            stackSizes.put(j, startStack);
            names.put(j, "AI-" + j);
        }
        ai.setAmountOfPlayers(amountOfPlayers);
        ai.setPositions(positions);
        ai.setStackSizes(stackSizes);
        ai.setPlayerNames(names);

        ai.playerMadeDecision((0 + amountOfPlayers - positionOffset) % amountOfPlayers, new Decision(Decision.Move.SMALL_BLIND, smallBlind));
        ai.playerMadeDecision((1 + amountOfPlayers - positionOffset) % amountOfPlayers, new Decision(Decision.Move.BIG_BLIND, bigBlind));
    }
}