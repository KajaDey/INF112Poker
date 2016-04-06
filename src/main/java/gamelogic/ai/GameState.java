package gamelogic.ai;

import gamelogic.Card;
import gamelogic.Decision;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the state of a single hand
 */
public class GameState {
    private final ArrayList<Card> deck = new ArrayList<>(Arrays.asList(Card.getAllCards())); // TODO: players can get the same card multiple times

    public final int amountOfPlayers;
    public final List<Player> players;
    public final List<Card> communityCards;

    public Player currentPlayer;

    private final Player dealer;
    private final Player bigBlind;
    private final Player smallBlind;

    private final long bigBlindAmount;
    private final long smallBlindAmount;

    public final long allChipsOnTable;

    private int playersGivenHolecards = 1;
    private int playersLeftInHand; // Players who have not folded or gone all in (players still making decisions)
    private int playersAllIn = 0;
    private int playersToMakeDecision; // Players left to make decision in this betting round


    public GameState(int amountOfPlayers, Map<Integer, Integer> positions, Map<Integer, Long> stackSizes,
                     long smallBlindAmount, long bigBlindAmount) {

        assert amountOfPlayers == positions.size();

        this.amountOfPlayers = amountOfPlayers;
        this.smallBlindAmount = bigBlindAmount;
        this.bigBlindAmount = bigBlindAmount;
        communityCards = new ArrayList<>();

        players = new ArrayList<>(amountOfPlayers);

        List<Long> stackSizesArray = new ArrayList<>();
        stackSizes.forEach((playerId, stackSize) -> {
            assert playerId == stackSizesArray.size();
            stackSizesArray.add(stackSize); }
        );
        allChipsOnTable = stackSizesArray.stream().reduce(Long::sum).get();

        for (int i = 0; i < amountOfPlayers; i++) {
            assert positions.containsKey(i) : "AI didn't get position for playerPosition " + i;
            players.add(new Player(positions.get(i), positions.get(i), stackSizes.get(i)));
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
        if (smallBlind.stackSize > smallBlindAmount) {
            smallBlind.putInPot(smallBlindAmount);
        }
        else {
            smallBlind.putInPot(smallBlind.stackSize);
            smallBlind.isAllIn = true;
            playersLeftInHand -= 1;
            playersAllIn++;
        }

        if (bigBlind.stackSize > bigBlindAmount) {
            bigBlind.putInPot(bigBlindAmount);
        }
        else {
            bigBlind.putInPot(bigBlind.stackSize);
            bigBlind.isAllIn = true;
            playersLeftInHand -= 1;
            playersAllIn++;
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
        for (int i = 0; i < oldState.players.size(); i++) {
            this.players.add(new Player(oldState.players.get(i)));
        }
        this.communityCards = new ArrayList<>(oldState.communityCards);

        currentPlayer = this.players.get(oldState.currentPlayer.position);

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
        this.allChipsOnTable = oldState.allChipsOnTable;
        this.playersLeftInHand = oldState.playersLeftInHand;
        this.playersAllIn = oldState.playersAllIn;
        this.playersToMakeDecision = oldState.playersToMakeDecision;
        this.playersGivenHolecards = oldState.playersGivenHolecards;
    }

    public long getCurrentPot() {
        return players.stream().reduce(0L, (acc, p) -> acc + p.contributedToPot, Long::sum);
    }

    public void makeGameStateChange(GameStateChange move) {
        if (move instanceof CardDealtToPlayer) {
            CardDealtToPlayer cardDeal = (CardDealtToPlayer)move;
            players.get(cardDeal.playerPosition).holeCards.add(cardDeal.card);
            if (players.get(cardDeal.playerPosition).holeCards.size() == 2) {
                playersGivenHolecards++;
            }
            assert players.get(cardDeal.playerPosition).holeCards.size() <= 2
                    : "Player " + cardDeal.playerPosition + " has " + players.get(cardDeal.playerPosition).holeCards.size() + " hole cards";
            System.out.println("Giving " + cardDeal.card + " to " + cardDeal.playerPosition);
        }
        else if (move instanceof CardDealtToTable) {
            communityCards.add(((CardDealtToTable)(move)).card);
            assert communityCards.size() <= 5;
            if (communityCards.size() == 3 || communityCards.size() == 4 || communityCards.size() == 5) {
                playersToMakeDecision = playersLeftInHand;
            }
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
                    currentPlayer.putInPot(currentPlayer.currentBet);
                    break;
                case BET: case RAISE:
                    assert decision.size >= currentPlayer.minimumRaise;
                    playersToMakeDecision = playersLeftInHand - 1;

                    currentPlayer.putInPot(currentPlayer.currentBet + decision.size);

                    players.stream().filter(p -> !p.equals(currentPlayer)).forEach(player -> {
                        player.currentBet += decision.size;
                        player.minimumRaise = decision.size;
                    });
                    currentPlayer.minimumRaise = decision.size;
                    currentPlayer.currentBet = 0;
                    break;
                case ALL_IN:
                    players.stream().filter(p -> !p.equals(currentPlayer)).forEach(player -> {
                        player.currentBet += currentPlayer.stackSize;
                        player.minimumRaise = Math.max(currentPlayer.stackSize, player.minimumRaise); // TODO: Everyone must match the all-in to raise further. May not be correct behaviour
                    });

                    currentPlayer.putInPot(currentPlayer.stackSize);
                    currentPlayer.isAllIn = true;
                    playersLeftInHand--;
                    playersToMakeDecision = playersLeftInHand; // TODO: Everyone must make a new decision, even if it was just a call. This may not be correct behaviour
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
            assert playersAllIn > 0 : "MonteCarloAI: Player made a decision when it was the only player playing";
        }
    }

    /**
     * Gets a list of all possible decisions in the current GameState, without mutating the gamestate.
     * Returns Empty if the node is a terminal node
     */
    public Optional<ArrayList<GameStateChange>> allDecisions() {
        ArrayList<GameStateChange> decisions = new ArrayList<>();
        //System.out.println(playersLeftInHand + " players left in hand, " + playersAllIn + " all in, " + playersToMakeDecision + " players to make decisions");

        long playerChipsSum = players.stream().reduce(0L, (acc, player) -> acc + player.stackSize + player.contributedToPot, Long::sum);
        assert playerChipsSum == allChipsOnTable :
        "Sum of player chips is " + playerChipsSum + ", but started with " + allChipsOnTable + " on table.";

        assert players.stream().map(player -> player.stackSize).min(Long::compare).get() >= 0L : "A player has negative stack size";

        if (playersGivenHolecards < amountOfPlayers) {

            for (int i = 0; i < amountOfPlayers; i++) {
                System.out.println("Player " + i + " has " + players.get(i).holeCards.size() + " holecards");
                int icopy = i;
                assert players.get(i).position == i;
                if (players.get(i).holeCards.size() < 2) {
                    decisions.addAll(deck.stream()
                            .map(card -> new CardDealtToPlayer(card, icopy))
                            .collect(Collectors.toList())
                    );
                    return Optional.of(decisions);
                }
            }
            throw new IllegalStateException("playersGivenHoleCards=" + playersGivenHolecards + " but all players had the cards");
        }
        else if (playersLeftInHand + playersAllIn == 1) {
            return Optional.empty();
        }
        else if (playersToMakeDecision == 0 || (playersLeftInHand == 0 && playersAllIn > 2)) {
            if (communityCards.size() == 5) {
                return Optional.empty();
            }
            else {
                decisions.addAll(deck.stream().map(CardDealtToTable::new).collect(Collectors.toList()));

                return Optional.of(decisions);
            }
        }
        else {
            assert playersLeftInHand > 0: "Trying to generate possible decisions for player, when there are no players left (" +
                    playersAllIn + " players all in, " + playersToMakeDecision + " players to make decisions)";
            assert currentPlayer.isInHand && !currentPlayer.isAllIn;
            assert currentPlayer.stackSize > 0;

            decisions.add(new PlayerDecision(new Decision(Decision.Move.FOLD)));
            decisions.add(new PlayerDecision(new Decision(Decision.Move.ALL_IN)));
            if (currentPlayer.currentBet == 0) {
                decisions.add(new PlayerDecision(new Decision(Decision.Move.CHECK)));
            }
            if (currentPlayer.currentBet > 0 && currentPlayer.stackSize > currentPlayer.currentBet) {
                decisions.add(new PlayerDecision(new Decision(Decision.Move.CALL)));
            }
            Decision.Move moneyMove = currentPlayer.currentBet == 0 ? Decision.Move.BET : Decision.Move.RAISE;
            if (currentPlayer.stackSize > currentPlayer.currentBet + currentPlayer.minimumRaise) {
                decisions.add(new PlayerDecision(new Decision(moneyMove, currentPlayer.minimumRaise)));
            }
            if (currentPlayer.stackSize > currentPlayer.currentBet + getCurrentPot() / 2 &&  getCurrentPot() / 2 > currentPlayer.minimumRaise) {
                decisions.add(new PlayerDecision(new Decision(moneyMove, getCurrentPot() / 2)));
            }
            if (currentPlayer.stackSize > currentPlayer.currentBet + getCurrentPot() && getCurrentPot() > currentPlayer.minimumRaise) {
                decisions.add(new PlayerDecision(new Decision(moneyMove, getCurrentPot())));
            }
            return Optional.of(decisions);
        }
    }

    // Returns the kind of decision that needs to be done in this gamestate
    public NodeType getNextNodeType() {
        NodeType nodeType;
        if (playersGivenHolecards < amountOfPlayers) {
            nodeType = NodeType.DealCard;
        }
        else if (playersLeftInHand + playersAllIn == 1) {
            nodeType = NodeType.Terminal;
        }
        else if (playersToMakeDecision == 0 || (playersLeftInHand == 0 && playersAllIn > 2)) {
            if (communityCards.size() == 5) {
                nodeType = NodeType.Terminal;
            }
            else {
                nodeType = NodeType.DealCard;
            }
        }
        else {
            nodeType = NodeType.PlayerDecision;
        }
        return nodeType;
    }

    public static enum NodeType {DealCard, PlayerDecision, Terminal }


    public static abstract class GameStateChange {
        public NodeType getStartingNodeType() {
            if (this instanceof PlayerDecision) {
                return NodeType.PlayerDecision;
            }
            else {
                return NodeType.DealCard;
            }
        }
    }


    public static class CardDealtToPlayer extends GameStateChange {
        public final Card card;
        public final int playerPosition;

        private CardDealtToPlayer(Card card, int playerPosition) {
            this.card = card;
            this.playerPosition = playerPosition;
        }
        public String toString() {
            return card + " to player " + playerPosition;
        }
    }

    public static class CardDealtToTable extends GameStateChange {
        public final Card card;

        private CardDealtToTable(Card card) {
            this.card = card;
        }
        public String toString() {
            return card + " to table.";
        }
    }

    public static class PlayerDecision extends GameStateChange {
        public final Decision decision;

        private PlayerDecision(Decision decision) {
            this.decision = decision;
        }

        public String toString() {
            return decision.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameState gameState = (GameState) o;

        if (amountOfPlayers != gameState.amountOfPlayers) return false;
        if (bigBlindAmount != gameState.bigBlindAmount) return false;
        if (smallBlindAmount != gameState.smallBlindAmount) return false;
        if (playersLeftInHand != gameState.playersLeftInHand) return false;
        if (playersAllIn != gameState.playersAllIn) return false;
        if (playersToMakeDecision != gameState.playersToMakeDecision) return false;
        if (!deck.equals(gameState.deck)) return false;
        if (!players.equals(gameState.players)) return false;
        if (!communityCards.equals(gameState.communityCards)) return false;
        if (!currentPlayer.equals(gameState.currentPlayer)) return false;
        if (!dealer.equals(gameState.dealer)) return false;
        if (!bigBlind.equals(gameState.bigBlind)) return false;
        return smallBlind.equals(gameState.smallBlind);

    }

    @Override
    public int hashCode() {
        int result = deck.hashCode();
        result = 31 * result + amountOfPlayers;
        result = 31 * result + players.hashCode();
        result = 31 * result + communityCards.hashCode();
        result = 31 * result + currentPlayer.hashCode();
        result = 31 * result + dealer.hashCode();
        result = 31 * result + bigBlind.hashCode();
        result = 31 * result + smallBlind.hashCode();
        result = 31 * result + (int)bigBlindAmount;
        result = 31 * result + (int)smallBlindAmount;
        result = 31 * result + playersLeftInHand;
        result = 31 * result + playersAllIn;
        result = 31 * result + playersToMakeDecision;
        return result;
    }
}
