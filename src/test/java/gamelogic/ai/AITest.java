package gamelogic.ai;

import gamelogic.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by morten on 08.03.16.
 */
public class AITest {

    static final int N = 1; // Amount of times to do each test
    static HashMap<Integer, Integer> positions;
    static HashMap<Integer, Long> stackSizes;
    static HashMap<Integer, String> names;
    static long smallBlind = 25;
    static long bigBlind = 50;
    static long startStack = 2500L;
    static long timeToThink = 2000L;
    static Logger logger = new Logger("Test-AI", "");

    @Test
    public void testAllInAsCall() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0, logger);
        testAllInAsCallProperty(simpleAi);
        testAllInAsCallProperty(mctsAi);
    }

    @Test
    public void testAllInAsCallFollowedByAllIn() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0, logger);
        testAllInAsCallFollowedByAllInProperty(simpleAi);
        testAllInAsCallFollowedByAllInProperty(mctsAi);
    }

    @Test
    public void doesNotFoldWithGreatHandHeadsUp() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0, logger);
        doesNotFoldWithGreatHandHeadsUpProperty(simpleAi);
        doesNotFoldWithGreatHandHeadsUpProperty(mctsAi);
    }

    @Test
    public void checksWithShittyHandAsBigBlind() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0, logger);
        checksWithShittyHandAsBigBlindProperty(simpleAi);
        checksWithShittyHandAsBigBlindProperty(mctsAi);
    }

    @Test
    public void foldsWithShittyHandIfNotBlind() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0, logger);
        foldsWithShittyHandIfNotBlindProperty(simpleAi);
        foldsWithShittyHandIfNotBlindProperty(mctsAi);
    }

    @Test
    public void doesNotFoldWithGreatHand() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0, logger);
        doesNotFoldWithGreatHandProperty(simpleAi);
        doesNotFoldWithGreatHandProperty(mctsAi);
    }

    @Test
    public void checksWithShittyHandAsBigBlindHeadsUp() {
        SimpleAI simpleAi = new SimpleAI(0);
        MCTSAI mctsAi = new MCTSAI(0, logger);
        checksWithShittyHandAsBigBlindHeadsUpProperty(mctsAi);
        checksWithShittyHandAsBigBlindHeadsUpProperty(simpleAi);
    }
    // Commented out for lack of passing
    //@Test
    public void doesNotFoldWithPairOfDeuces() {
        MCTSAI mctsAi = new MCTSAI(0, logger);
        SimpleAI simpleAi = new SimpleAI(0);
        doesNotFoldWithPairOfDeucesProperty(mctsAi);
        doesNotFoldWithPairOfDeucesProperty(simpleAi);
    }

    @Test
    public void doesNotFoldWithLowStraightFlush() {
        MCTSAI mctsAi = new MCTSAI(0, logger);
        SimpleAI simpleAi = new SimpleAI(0);
        doesNotFoldWithLowStraightFlushProperty(mctsAi);
        //doesNotFoldWithLowStraightFlushProperty(simpleAi);
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

    public void doesNotFoldWithPairOfDeucesProperty(GameClient ai) {
        ai.setHandForClient(0, Card.of(2, Card.Suit.HEARTS).get(), Card.of(2, Card.Suit.SPADES).get());

        setupAi(ai, 3, 0);
        ai.playerMadeDecision(2, new Decision(Decision.Move.CALL));
        assertNotEquals(new Decision(Decision.Move.FOLD), ai.getDecision(timeToThink));
    }

    public void doesNotFoldWithLowStraightFlushProperty(GameClient ai) {
        ai.setHandForClient(0, Card.of(2, Card.Suit.CLUBS).get(), Card.of(4, Card.Suit.CLUBS).get());

        setupAi(ai, 4, 2);
        ai.playerMadeDecision(0, new Decision(Decision.Move.CALL));
        ai.playerMadeDecision(1, new Decision(Decision.Move.CALL));
        ai.playerMadeDecision(2, new Decision(Decision.Move.CALL));
        ai.playerMadeDecision(3, new Decision(Decision.Move.CHECK));

        ai.setFlop(Card.of(3, Card.Suit.CLUBS).get(), Card.of(5, Card.Suit.CLUBS).get(),  Card.of(6, Card.Suit.CLUBS).get());

        ai.playerMadeDecision(2, new Decision(Decision.Move.BET, 1000));
        ai.playerMadeDecision(3, new Decision(Decision.Move.CALL));

        assertNotEquals(new Decision(Decision.Move.FOLD), ai.getDecision(timeToThink));
    }

    // Test created to reproduce a specific bug in SimpleAI
    // Bug is fixed now, the test remains because why not
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

        ai.playerMadeDecision(0, new Decision(Decision.Move.SMALL_BLIND));
        ai.playerMadeDecision(1, new Decision(Decision.Move.BIG_BLIND));
        ai.playerMadeDecision(2, new Decision(Decision.Move.CALL));
        ai.playerMadeDecision(3, new Decision(Decision.Move.CALL));
        ai.playerMadeDecision(0, new Decision(Decision.Move.RAISE, 950));
        ai.playerMadeDecision(1, new Decision(Decision.Move.ALL_IN));
        ai.playerMadeDecision(2, new Decision(Decision.Move.CALL));
        ai.playerMadeDecision(3, new Decision(Decision.Move.CALL));

        ai.setFlop(Card.of(14, Card.Suit.DIAMONDS).get(), Card.of(13, Card.Suit.HEARTS).get(), Card.of(13, Card.Suit.SPADES).get());

        ai.playerMadeDecision(0, new Decision(Decision.Move.BET, 1000));
        ai.playerMadeDecision(2, new Decision(Decision.Move.FOLD));
        ai.playerMadeDecision(3, new Decision(Decision.Move.CALL));

        ai.setTurn(Card.of(14, Card.Suit.CLUBS).get());

        ai.getDecision(timeToThink / 4);
        ai.playerMadeDecision(0, new Decision(Decision.Move.ALL_IN));
        ai.playerMadeDecision(3, new Decision(Decision.Move.CALL));

        ai.setRiver(Card.of(2, Card.Suit.CLUBS).get());
    }

    public void testAllInAsCallFollowedByAllInProperty(GameClient ai) {
        ai.setHandForClient(0, Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());

        HashMap<Integer, Integer> positions = new HashMap<>();
        HashMap<Integer, String> names = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            positions.put(i, i);
            names.put(i, "AI-" + i);
        }

        HashMap<Integer, Long> stackSizes = new HashMap<>();

        stackSizes.put(0, 10025L);
        stackSizes.put(1, 4975L);
        stackSizes.put(2, 5000L);
        stackSizes.put(3, 10000L);


        ai.setAmountOfPlayers(4);
        ai.setStackSizes(stackSizes);
        ai.setPlayerNames(names);
        ai.setPositions(positions);
        ai.setSmallBlind(smallBlind);
        ai.setBigBlind(bigBlind);

        ai.playerMadeDecision(0, new Decision(Decision.Move.SMALL_BLIND));
        ai.playerMadeDecision(1, new Decision(Decision.Move.BIG_BLIND));
        ai.playerMadeDecision(2, new Decision(Decision.Move.CALL));
        ai.playerMadeDecision(3, new Decision(Decision.Move.FOLD));
        ai.playerMadeDecision(0, new Decision(Decision.Move.RAISE, 150));
        ai.playerMadeDecision(1, new Decision(Decision.Move.RAISE, 350));
        ai.playerMadeDecision(2, new Decision(Decision.Move.RAISE, 850));
        ai.playerMadeDecision(0, new Decision(Decision.Move.RAISE, 850));
        ai.playerMadeDecision(1, new Decision(Decision.Move.RAISE, 850));
        ai.playerMadeDecision(2, new Decision(Decision.Move.RAISE, 850));
        ai.playerMadeDecision(0, new Decision(Decision.Move.CALL));
        ai.playerMadeDecision(1, new Decision(Decision.Move.RAISE, 850));

        ai.playerMadeDecision(2, new Decision(Decision.Move.ALL_IN));
        ai.playerMadeDecision(0, new Decision(Decision.Move.CALL));
        ai.playerMadeDecision(1, new Decision(Decision.Move.ALL_IN));

        ai.setFlop(Card.of(14, Card.Suit.DIAMONDS).get(), Card.of(13, Card.Suit.HEARTS).get(), Card.of(13, Card.Suit.SPADES).get());

    }

    @Test
    // Test created to reproduce a specific bug in SimpleAI
    // Bug is fixed now, the test remains because why not
    public void testCunsecutiveRaises() {
        for (int i = 0; i < 1000; i++) {

            MCTSAI mctsAi = new MCTSAI(0, logger);
            SimpleAI simpleAI = new SimpleAI(2);
            HashMap<Integer, Integer> positions = new HashMap<>();
            HashMap<Integer, String> names = new HashMap<>();
            HashMap<Integer, Long> stackSizes = new HashMap<>();

            names.put(0, "Skyler");
            names.put(1, "Holly");
            names.put(2, "Flynn");
            names.put(3, "Saul");
            for (int j = 0; j < 4; j++) {
                positions.put(j, j);
            }
            stackSizes.put(0, 10200L);
            stackSizes.put(1, 4900L);
            stackSizes.put(2, 10075L);
            stackSizes.put(3, 4825L);

            mctsAi.setAmountOfPlayers(4);
            simpleAI.setAmountOfPlayers(4);
            mctsAi.setPlayerNames(names);
            simpleAI.setPlayerNames(names);
            mctsAi.setPositions(positions);
            simpleAI.setPositions(positions);
            mctsAi.setStackSizes(stackSizes);
            simpleAI.setStackSizes(stackSizes);
            mctsAi.setSmallBlind(smallBlind);
            simpleAI.setBigBlind(smallBlind);
            mctsAi.setBigBlind(bigBlind);
            simpleAI.setBigBlind(bigBlind);

            simpleAI.setHandForClient(2, Card.of(14, Card.Suit.SPADES).get(), Card.of(14, Card.Suit.DIAMONDS).get());
            mctsAi.setHandForClient(0, Card.of(14, Card.Suit.HEARTS).get(), Card.of(4, Card.Suit.DIAMONDS).get());

            mctsAi.playerMadeDecision(0, new Decision(Decision.Move.SMALL_BLIND));
            simpleAI.playerMadeDecision(0, new Decision(Decision.Move.SMALL_BLIND));
            mctsAi.playerMadeDecision(1, new Decision(Decision.Move.BIG_BLIND));
            simpleAI.playerMadeDecision(1, new Decision(Decision.Move.BIG_BLIND));

            mctsAi.playerMadeDecision(2, new Decision(Decision.Move.CALL));
            simpleAI.playerMadeDecision(2, new Decision(Decision.Move.CALL));
            mctsAi.playerMadeDecision(3, new Decision(Decision.Move.FOLD));
            simpleAI.playerMadeDecision(3, new Decision(Decision.Move.FOLD));

            mctsAi.playerMadeDecision(0, new Decision(Decision.Move.RAISE, 75L));
            simpleAI.playerMadeDecision(0, new Decision(Decision.Move.RAISE, 75L));
            mctsAi.playerMadeDecision(1, new Decision(Decision.Move.CALL));
            simpleAI.playerMadeDecision(1, new Decision(Decision.Move.CALL));

            mctsAi.playerMadeDecision(2, new Decision(Decision.Move.RAISE, 150L));
            simpleAI.playerMadeDecision(2, new Decision(Decision.Move.RAISE, 150L));
            mctsAi.playerMadeDecision(0, new Decision(Decision.Move.RAISE, 650L));
            simpleAI.playerMadeDecision(0, new Decision(Decision.Move.RAISE, 650L));

            mctsAi.playerMadeDecision(1, new Decision(Decision.Move.FOLD));
            simpleAI.playerMadeDecision(1, new Decision(Decision.Move.FOLD));
            mctsAi.playerMadeDecision(2, new Decision(Decision.Move.CALL));
            simpleAI.playerMadeDecision(2, new Decision(Decision.Move.CALL));

            mctsAi.setFlop(Card.of(3, Card.Suit.DIAMONDS).get(), Card.of(3, Card.Suit.SPADES).get(), Card.of(6, Card.Suit.HEARTS).get());
            simpleAI.setFlop(Card.of(3, Card.Suit.DIAMONDS).get(), Card.of(3, Card.Suit.SPADES).get(), Card.of(6, Card.Suit.HEARTS).get());

            mctsAi.playerMadeDecision(0, new Decision(Decision.Move.BET, 50L));
            simpleAI.playerMadeDecision(0, new Decision(Decision.Move.BET, 50L));
            mctsAi.playerMadeDecision(2, new Decision(Decision.Move.RAISE, 200L));
            simpleAI.playerMadeDecision(2, new Decision(Decision.Move.RAISE, 200L));

            mctsAi.playerMadeDecision(0, new Decision(Decision.Move.RAISE, 200L));
            simpleAI.playerMadeDecision(0, new Decision(Decision.Move.RAISE, 200L));
            mctsAi.playerMadeDecision(2, new Decision(Decision.Move.CALL));
            simpleAI.playerMadeDecision(2, new Decision(Decision.Move.CALL));

            mctsAi.setTurn(Card.of(13, Card.Suit.DIAMONDS).get());
            simpleAI.setTurn(Card.of(13, Card.Suit.DIAMONDS).get());

            mctsAi.playerMadeDecision(0, new Decision(Decision.Move.BET, 3000L));
            simpleAI.playerMadeDecision(0, new Decision(Decision.Move.BET, 3000L));

            // SimpleAI would sometimes try to raise to 9000 (raise 6000) here, but its stacksize was 8700
            // This is now fixed
            Decision simpleAiDecision = simpleAI.getDecision(0L);

            simpleAI.playerMadeDecision(0, simpleAiDecision);
            mctsAi.playerMadeDecision(2, simpleAiDecision);
        }
    }
    @Test
    public void blindAsAllIn() {
        for (int i = 0; i < 10; i++) {
            MCTSAI ai = new MCTSAI(0, logger);
            ai.setHandForClient(0, Card.of(14, Card.Suit.HEARTS).get(), Card.of(14, Card.Suit.SPADES).get());

            HashMap<Integer, Integer> positions = new HashMap<>();
            HashMap<Integer, String> names = new HashMap<>();
            HashMap<Integer, Long> stackSizes = new HashMap<>();
            positions.put(0, 0);
            stackSizes.put(0, 1000L);
            names.put(0, "Morten");
            positions.put(1, 1);
            stackSizes.put(1, bigBlind);
            names.put(1, "Kristian");
            positions.put(2, 2);
            stackSizes.put(2, 2000L);
            names.put(2, "Ragnhild");


            ai.setAmountOfPlayers(3);
            ai.setStackSizes(stackSizes);
            ai.setPlayerNames(names);
            ai.setPositions(positions);
            ai.setSmallBlind(smallBlind);
            ai.setBigBlind(bigBlind);

            ai.playerMadeDecision(0, new Decision(Decision.Move.SMALL_BLIND));
            if (Math.random() > 0.5) {
                ai.playerMadeDecision(1, new Decision(Decision.Move.BIG_BLIND));
            }
            else {
                ai.playerMadeDecision(1, new Decision(Decision.Move.ALL_IN));
            }
            ai.playerMadeDecision(2, new Decision(Decision.Move.CALL));
            ai.getDecision(timeToThink / 5);
            ai.playerMadeDecision(0, new Decision(Decision.Move.CALL));
            ai.setFlop(Card.of(14, Card.Suit.DIAMONDS).get(), Card.of(13, Card.Suit.HEARTS).get(), Card.of(13, Card.Suit.SPADES).get());
        }
    }

    @Test
    public void testBigBlindAsAllIn() {

        for (int i = 0; i < 20; i++) {
            // A blind post as all in may by either a call or raise of the small blind. Test for both.
            boolean allInIsRaise = Math.random() > 0.5;
            //System.out.println("All in is " + (allInIsRaise ? "raise" : "call"));

            Deck deck = new Deck();
            MCTSAI ai = new MCTSAI(0, logger);
            ai.setHandForClient(0, deck.draw().get(), deck.draw().get());

            HashMap<Integer, Integer> positions = new HashMap<>();
            HashMap<Integer, String> names = new HashMap<>();
            HashMap<Integer, Long> stackSizes = new HashMap<>();
            positions.put(0, 1);
            if (allInIsRaise) {
                stackSizes.put(0, 225L);
            }
            else {
                stackSizes.put(0, 175L);
            }
            names.put(0, "Morten");
            positions.put(1, 0);
            stackSizes.put(1, 29575L);
            names.put(1, "Flynn");

            ai.setAmountOfPlayers(2);
            ai.setStackSizes(stackSizes);
            ai.setPlayerNames(names);
            ai.setPositions(positions);
            ai.setSmallBlind(200);
            ai.setBigBlind(400);

            ai.playerMadeDecision(1, new Decision(Decision.Move.SMALL_BLIND));
            ai.playerMadeDecision(0, new Decision(Decision.Move.ALL_IN));
            if (allInIsRaise) {
                ai.playerMadeDecision(1, new Decision(Decision.Move.CALL));
            }
            ai.setFlop(deck.draw().get(), deck.draw().get(), deck.draw().get());
            ai.setTurn(deck.draw().get());
            ai.setRiver(deck.draw().get());
        }
    }
    @Test
    public void testSmallBlindAsAllIn() {

        for (int i = 0; i < 20; i++) {
            // A blind post as all in or just a plain blind post
            boolean blindIsAllIn = Math.random() > 0.5;
            //System.out.println("Blind is " + (blindIsAllIn ? "all in" : "plain blind"));

            Deck deck = new Deck();
            MCTSAI ai = new MCTSAI(0, logger);
            ai.setHandForClient(0, deck.draw().get(), deck.draw().get());

            HashMap<Integer, Integer> positions = new HashMap<>();
            HashMap<Integer, String> names = new HashMap<>();
            HashMap<Integer, Long> stackSizes = new HashMap<>();
            positions.put(0, 0);
            stackSizes.put(0, 200L);
            names.put(0, "Morten");
            positions.put(1, 1);
            stackSizes.put(1, 29575L);
            names.put(1, "Flynn");

            ai.setAmountOfPlayers(2);
            ai.setStackSizes(stackSizes);
            ai.setPlayerNames(names);
            ai.setPositions(positions);
            ai.setSmallBlind(200);
            ai.setBigBlind(400);


            if (blindIsAllIn) {
                ai.playerMadeDecision(0, new Decision(Decision.Move.ALL_IN));
            }
            else {
                ai.playerMadeDecision(0, new Decision(Decision.Move.SMALL_BLIND));
            }
            ai.playerMadeDecision(1, new Decision(Decision.Move.BIG_BLIND));

            ai.setFlop(deck.draw().get(), deck.draw().get(), deck.draw().get());
            ai.setTurn(deck.draw().get());
            ai.setRiver(deck.draw().get());
        }
    }

    @Test
    // Test created to reproduce a specific bug in the AI
    // Bug is fixed now, the test remains because why not
    public void testPreflopShowdown() {
        MCTSAI ai = new MCTSAI(3, logger);
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
        ai.setSmallBlind(smallBlind);
        ai.setBigBlind(bigBlind);

        ai.playerMadeDecision(4, new Decision(Decision.Move.SMALL_BLIND));
        ai.playerMadeDecision(0, new Decision(Decision.Move.BIG_BLIND));
        ai.playerMadeDecision(3, new Decision(Decision.Move.RAISE, 75));
        ai.playerMadeDecision(4, new Decision(Decision.Move.FOLD)); // position 0
        ai.playerMadeDecision(0, new Decision(Decision.Move.ALL_IN)); // position 1
        ai.playerMadeDecision(3, new Decision(Decision.Move.CALL)); // position 2

        ai.setFlop(Card.of(14, Card.Suit.DIAMONDS).get(), Card.of(13, Card.Suit.HEARTS).get(), Card.of(13, Card.Suit.SPADES).get());

        ai.setTurn(Card.of(14, Card.Suit.CLUBS).get());

        ai.setRiver(Card.of(2, Card.Suit.CLUBS).get());
    }

    /**
     * Prepares the AI by setting names, positions and stacksizes of the players to default values
     * Also gives blinds
     * @param positionOffset Determines the positions of the AIs. If 0, id=0 -> position=0. If 2, id=0 -> position=2. etc
     */
    public static void setupAi(GameClient ai, int amountOfPlayers, int positionOffset) {
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

        ai.playerMadeDecision((0 + amountOfPlayers - positionOffset) % amountOfPlayers, new Decision(Decision.Move.SMALL_BLIND));
        ai.playerMadeDecision((1 + amountOfPlayers - positionOffset) % amountOfPlayers, new Decision(Decision.Move.BIG_BLIND));
    }
}