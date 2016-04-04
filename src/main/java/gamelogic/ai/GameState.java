package gamelogic.ai;

import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.Deck;
import gamelogic.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents the state of a single hand
 */
public class GameState {
    private final ArrayList<Card> deck = new ArrayList<>(Arrays.asList(Card.getAllCards()));

    private final int amountOfPlayers;
    private final ArrayList<Player> players;
    private final ArrayList<Card> communityCards;

    private Player currentPlayer;

    private final Player dealer;
    private final Player bigBlind;
    private final Player smallBlind;

    private final int bigBlindAmount;
    private final int smallBlindAmount;

    private long pot;
    private int playersLeftInHand; // Players who have not folded or gone all in (players still making decisions)
    private int playersAllIn = 0;
    private int playersToMakeDecision; // Players left to make decision in this betting round

    public GameState(int amountOfPlayers, ArrayList<Integer> positions, ArrayList<Long> stackSizes,
                     int smallBlindAmount, int bigBlindAmount) {

        this.amountOfPlayers = amountOfPlayers;
        this.smallBlindAmount = bigBlindAmount;
        this.bigBlindAmount = bigBlindAmount;
        communityCards = new ArrayList<>();

        players = new ArrayList<>(amountOfPlayers);
        for (int i = 0; i < amountOfPlayers; i++) {
            players.add(new Player(i, positions.get(i), stackSizes.get(i)));
            players.get(i).minimumRaise = bigBlindAmount;
            players.get(i).currentBet = bigBlindAmount;
        }
        players.sort((p1, p2) -> Integer.compare(p1.position, p2.position));

        currentPlayer = players.get(1);

        if (amountOfPlayers == 2) {
            dealer = players.get(0);
            smallBlind = players.get(0);
            bigBlind = players.get(1);
        }
        else {
            dealer = players.get(0);
            smallBlind = players.get(1);
            bigBlind = players.get(2);
        }
        smallBlind.currentBet = bigBlindAmount - smallBlindAmount;
        bigBlind.currentBet = 0;

        //Post blinds
        pot = smallBlindAmount + bigBlindAmount;
        if (smallBlind.stackSize > smallBlindAmount) {
            smallBlind.stackSize -= smallBlindAmount;
            pot += smallBlindAmount;
        }
        else {
            smallBlind.stackSize = 0;
            pot += smallBlind.stackSize;
            playersLeftInHand -= 1;
            playersAllIn++;
            playersToMakeDecision -= 1;
        }

        if (bigBlind.stackSize > bigBlindAmount) {
            bigBlind.stackSize -= bigBlindAmount;
            pot += bigBlindAmount;
        }
        else {
            bigBlind.stackSize = 0;
            pot += bigBlind.stackSize;
            playersLeftInHand -= 1;
            playersAllIn++;
            playersToMakeDecision -= 1;
        }

        playersLeftInHand = amountOfPlayers;
        playersToMakeDecision = amountOfPlayers;
    }

    /**
     * Copy constructor for doing a deep clone of the old GameState
     * @param oldState
     */
    public GameState(GameState oldState) {
        this.amountOfPlayers = oldState.amountOfPlayers;
        this.players = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            this.players.add(new Player(oldState.players.get(i)));
        }
        this.communityCards = new ArrayList<>(oldState.communityCards);

        currentPlayer = this.players.get(oldState.currentPlayer.id);

        if (amountOfPlayers == 2) {
            dealer = players.get(0);
            smallBlind = players.get(0);
            bigBlind = players.get(1);
        }
        else {
            dealer = players.get(0);
            smallBlind = players.get(1);
            bigBlind = players.get(2);
        }
        this.bigBlindAmount = oldState.bigBlindAmount;
        this.smallBlindAmount = oldState.smallBlindAmount;
        this.pot = oldState.pot;
        this.playersLeftInHand = oldState.playersLeftInHand;
        this.playersAllIn = oldState.playersAllIn;
        this.playersToMakeDecision = oldState.playersToMakeDecision;
    }

    public void makeGameStateChange(GameStateChange move) {
        if (move instanceof CardDealtToPlayer) {
            CardDealtToPlayer cardDeal = (CardDealtToPlayer)move;
            players.get(cardDeal.playerId).holeCards.add(cardDeal.card);
        }
        else if (move instanceof CardDealtToTable) {
            communityCards.add(((CardDealtToTable)(move)).card);
        }
        else if (move instanceof PlayerDecision) {
            Decision decision = ((PlayerDecision)move).decision;
            switch (decision.move) {
                case FOLD:
                    playersLeftInHand--;
                    playersToMakeDecision--;
                    currentPlayer.isInHand = false;
                    break;
                case CHECK:
                    playersToMakeDecision--;
                    break;
                case CALL:
                    playersToMakeDecision--;
                    currentPlayer.stackSize -= currentPlayer.currentBet;
                    pot += currentPlayer.currentBet;
                    break;
                case BET: case RAISE:
                    playersToMakeDecision = playersLeftInHand - 1;
                    currentPlayer.stackSize -= currentPlayer.currentBet + decision.size;
                    pot += currentPlayer.currentBet + decision.size;
                    break;
                case ALL_IN:
                    pot += currentPlayer.stackSize;
                    currentPlayer.stackSize = 0;
                    currentPlayer.isAllIn = true;
                    playersToMakeDecision = playersLeftInHand; // TODO: Everyone must make a new decision, even if it was just a call. This may not be correct behaviour
                    playersLeftInHand--;
                    playersAllIn++;
                    break;

            }
            for (int i = currentPlayer.position; i < players.size(); i++) {
                if (players.get(i).isInHand && !players.get(i).isAllIn) {
                    currentPlayer = players.get(i);
                    return;
                }
            }
            for (int i = 0; i < currentPlayer.position; i++) {
                if (players.get(i).isInHand && !players.get(i).isAllIn) {
                    currentPlayer = players.get(i);
                    return;
                }
            }
            throw new IllegalStateException("MonteCarloAI: Player made a decision when it was the only player playing");
        }
    }


    /**
     *
     * @return
     */
    private Optional<ArrayList<GameStateChange>> allDecisions() {
        ArrayList<GameStateChange> decisions = new ArrayList<>();

        if (playersToMakeDecision == 0) {
            if (communityCards.size() == 5) {
                return Optional.empty();
            }
            else {
                decisions.addAll(deck.stream().map(CardDealtToTable::new).collect(Collectors.toList()));
                return Optional.of(decisions);
            }
        }
        else if (playersLeftInHand + playersAllIn == 1) {
            return Optional.empty();
        }
        else {
            decisions.add(new PlayerDecision(new Decision(Decision.Move.FOLD)));
            decisions.add(new PlayerDecision(new Decision(Decision.Move.ALL_IN)));
            if (currentPlayer.currentBet == 0) {
                decisions.add(new PlayerDecision(new Decision(Decision.Move.CHECK)));
            }
            if (currentPlayer.stackSize > currentPlayer.currentBet) {
                decisions.add(new PlayerDecision(new Decision(Decision.Move.CALL)));
            }
            Decision.Move moneyMove = currentPlayer.currentBet == 0 ? Decision.Move.BET : Decision.Move.RAISE;
            if (currentPlayer.stackSize > currentPlayer.minimumRaise) {
                decisions.add(new PlayerDecision(new Decision(moneyMove, currentPlayer.minimumRaise)));
            }
            if (currentPlayer.stackSize > currentPlayer.currentBet + pot / 2 &&  pot / 2 > currentPlayer.minimumRaise) {
                decisions.add(new PlayerDecision(new Decision(moneyMove, pot / 2)));
            }
            if (currentPlayer.stackSize > currentPlayer.currentBet + pot && pot > currentPlayer.minimumRaise) {
                decisions.add(new PlayerDecision(new Decision(moneyMove.RAISE, pot)));
            }
            return Optional.of(decisions);
        }

    }
/*
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
*/

    private static abstract class GameStateChange {
    }

    private static class CardDealtToPlayer extends GameStateChange {
        public final Card card;
        public final int playerId;

        private CardDealtToPlayer(Card card, int playerId) {
            this.card = card;
            this.playerId = playerId;
        }
    }

    private static class CardDealtToTable extends GameStateChange {
        public final Card card;

        private CardDealtToTable(Card card) {
            this.card = card;
        }
    }

    private static class PlayerDecision extends GameStateChange {
        public final Decision decision;

        private PlayerDecision(Decision decision) {
            this.decision = decision;
        }
    }

}
