package gamelogic.ai;

import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.Deck;

import java.lang.reflect.Array;
import java.security.cert.PKIXRevocationChecker;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by morten on 09.03.16.
 */
public class GameState {
    private final int amountOfPlayers;
    private ArrayList<main.java.gamelogic.ai.Player> players;

    private main.java.gamelogic.ai.Player currentPlayer;
    private Deck deck;

    private int bigBlindAmount;
    private int smallBlindAmount;
    private int minimumRaise;

    public GameState(int amountOfPlayers, ArrayList<Integer> positions, ArrayList<Long> stackSizes,
                     int smallBlindAmount, int bigBlindAmount) {

        this.amountOfPlayers = amountOfPlayers;
        this.smallBlindAmount = bigBlindAmount;
        this.bigBlindAmount = bigBlindAmount;

        players = new ArrayList<>(amountOfPlayers);
        for (int i = 0; i < amountOfPlayers; i++) {
            players.add(new main.java.gamelogic.ai.Player(i, positions.get(i), stackSizes.get(i)));
        }
        currentPlayer = players.get(0);
        deck = new Deck();
    }

    /**
     *
     * @return
     */
    private ArrayList<Decision> allDecisions() {
        ArrayList<Decision> decisions = new ArrayList<>();
        decisions.add(new Decision(Decision.Move.FOLD));

        return decisions;
    }

    private void giveRandomHoleCard() {
        Deck shuffledDeck = new Deck(deck);
        shuffledDeck.shuffle();
        Card card = shuffledDeck.draw().get();

        assert !currentPlayer.holeCards.get(1).isPresent();
        if (currentPlayer.holeCards.get(0).isPresent()) {
            currentPlayer.holeCards.set(1, Optional.of(card));
        }
        else {
            currentPlayer.holeCards.set(0, Optional.of(card));
        }
    }

    private void ungiveHoleCard() {
        assert currentPlayer.holeCards.get(0).isPresent();
        if (currentPlayer.holeCards.get(1).isPresent()) {
            currentPlayer.holeCards.set(1, Optional.empty());
        }
        else {
            currentPlayer.holeCards.set(0, Optional.empty());
        }

    }

    static private class GameStateChange {
        public final Type type;
        public final Optional<Decision> decision;
        public final Optional<Card> card;

        public GameStateChange(Type type, Decision decision) {
            this.type = type;
            this.decision = Optional.of(decision);
            this.card = Optional.empty();
        }

        public GameStateChange(Type type, Card card) {
            this.type = type;
            this.decision = Optional.empty();
            this.card = Optional.of(card);
        }

        static private enum Type {
            CARD_DEALT_TO_PLAYER, CARD_DEALT_TO_BOARD, PLAYER_DECISION,
        }
    }

}
