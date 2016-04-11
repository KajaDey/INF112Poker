package gamelogic.ai;

import gamelogic.Card;
import gamelogic.Decision;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the state of a single hand
 */
public class GameState {
    private final ArrayList<Card> deck;

    public final int amountOfPlayers;
    public final List<Player> players;
    public final List<Card> communityCards;

    public Player currentPlayer;

    private final Player dealer;
    private final Player bigBlind;
    private final Player smallBlind;

    public final long allChipsOnTable;

    private int playersGivenHolecards = 0;
    private int playersLeftInHand; // Players who have not folded or gone all in (players still making decisions)
    private int playersAllIn = 0;

    public int getPlayersToMakeDecision() {
        return playersToMakeDecision;
    }

    private int playersToMakeDecision; // Players left to make decision in this betting round


    public GameState(int amountOfPlayers, Map<Integer, Integer> positions, Map<Integer, Long> stackSizes,
                     Map<Integer, String> names) {
        assert amountOfPlayers == positions.size();

        deck = new ArrayList<>(Arrays.asList(Card.getAllCards()));

        this.amountOfPlayers = amountOfPlayers;
        communityCards = new ArrayList<>();
        players = new ArrayList<>(amountOfPlayers);

        allChipsOnTable = stackSizes.keySet().stream().map(stackSizes::get).reduce(0L, Long::sum);
        
        positions.forEach((key, value) -> {
            players.add(new Player(key, value, stackSizes.get(key), names.get(key)));
        });
        players.sort((p1, p2) -> Integer.compare(p1.position, p2.position));

        currentPlayer = players.get(0);

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
        playersLeftInHand = amountOfPlayers;
        playersToMakeDecision = amountOfPlayers;
    }

    /**
     * Copy constructor for doing a deep clone of the old GameState
     */
    public GameState(GameState oldState) {
        this.deck = new ArrayList<>(oldState.deck);
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
            deck.remove(cardDeal.card);
            if (players.get(cardDeal.playerPosition).holeCards.size() == 2) {
                playersGivenHolecards++;
            }
            assert players.get(cardDeal.playerPosition).holeCards.size() <= 2
                    : "Player " + cardDeal.playerPosition + " has " + players.get(cardDeal.playerPosition).holeCards.size() + " hole cards";
            // System.out.println("Giving " + cardDeal.card + " to " + cardDeal.playerPosition);
        }
        else if (move instanceof CardDealtToTable) {
            communityCards.add(((CardDealtToTable)(move)).card);
            deck.remove(((CardDealtToTable)(move)).card);
            assert communityCards.size() <= 5;
            if (communityCards.size() == 3 || communityCards.size() == 4 || communityCards.size() == 5) {
                for (Player player : players) {
                    player.currentBet = 0;
                }
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
                    currentPlayer.putInPot(Math.min(currentPlayer.stackSize, currentPlayer.currentBet));
                    break;
                case BET: case RAISE:
                    assert decision.size >= currentPlayer.minimumRaise : currentPlayer + " made " + decision.size + ", but minimum raise was " + currentPlayer.minimumRaise;
                    assert decision.size > 0 : currentPlayer + " tried to bet/raise " + decision.size;
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
                    playersLeftInHand--;
                    if (currentPlayer.currentBet > currentPlayer.stackSize) {
                        playersToMakeDecision--;
                    }
                    else {
                        playersToMakeDecision = playersLeftInHand;
                    }
                    playersAllIn++;

                    currentPlayer.putInPot(currentPlayer.stackSize);
                    currentPlayer.isAllIn = true;

                    break;
                case BIG_BLIND: case SMALL_BLIND:
                    System.out.println("Received " + decision);
                    players.stream().filter(p -> !p.equals(currentPlayer)).forEach(player -> {
                        player.currentBet += decision.size;
                        player.minimumRaise = decision.size;
                    });
                    currentPlayer.currentBet = 0;
                    currentPlayer.minimumRaise = decision.size;
                    currentPlayer.putInPot(Math.min(decision.size, currentPlayer.stackSize));
                    if (currentPlayer.stackSize == 0) {
                        playersAllIn++;
                        playersToMakeDecision--;
                        playersLeftInHand--;
                    }
                    break;

            }

            // Small blind moves first post-flop
            if (playersToMakeDecision == 0) {
                //System.out.println("Everyone has made decisions");
                currentPlayer = players.get(players.size() - 1);
                for (Player player : players) {
                    player.currentBet = 0;
                }
            }

            for (int i = currentPlayer.position + 1; i < players.size(); i++) {
                if (players.get(i).isInHand && !players.get(i).isAllIn) {
                    currentPlayer = players.get(i);
                    //System.out.println("Gamestate: currentplayer now has position " + i);
                    return;
                }
            }
            for (int i = 0; i < currentPlayer.position; i++) {
                if (players.get(i).isInHand && !players.get(i).isAllIn) {
                    currentPlayer = players.get(i);
                    //System.out.println("Gamestate: currentplayer now has position " + i);
                    return;
                }
            }
        }
    }

    /**
     * Returns the sum of all the chips in play
     * Mostly for debugging purposes
     */
    public static long sumOfChipsInPlay(List<Player> players) {
        return players.stream().reduce(0L, (acc, player) -> acc + player.stackSize + player.contributedToPot, Long::sum);
    }

    /**
     * Gets a list of all possible decisions in the current GameState, without mutating the gamestate.
     * Returns Empty if the node is a terminal node
     */
    public Optional<ArrayList<GameStateChange>> allDecisions() {
        ArrayList<GameStateChange> decisions = new ArrayList<>();

        assert deck.size() + players.stream().map(p -> p.holeCards.size()).reduce(0, Integer::sum) + communityCards.size() == 52 : "Deck has " + deck.size() + " cards, players have " + players.stream().map(p -> p.holeCards.size()).reduce(0, Integer::sum) + " holecards [" + players.stream().map(p -> p.name + ": " + p.holeCards).reduce("", String::concat) + "] and table has " + communityCards.size() + " community cards";
        assert sumOfChipsInPlay(players) == allChipsOnTable :
        "Sum of player chips is " + sumOfChipsInPlay(players) + ", but started with " + allChipsOnTable + " on table.";

        assert players.stream().map(player -> player.stackSize).min(Long::compare).get() >= 0L : "A player has negative stack size";

        if (playersGivenHolecards < amountOfPlayers) {

            for (int i = 0; i < amountOfPlayers; i++) {
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
            assert currentPlayer.isInHand && !currentPlayer.isAllIn : "Asked for decisions for " + currentPlayer + ", but player was not in hand. isAllIn=" + currentPlayer.isAllIn + ", isInHand=" + currentPlayer.isInHand;
            assert currentPlayer.stackSize > 0 : currentPlayer + " has a stacksize of " + currentPlayer.stackSize;
            assert currentPlayer.minimumRaise > 0;

            if (currentPlayer.currentBet > getCurrentPot() || currentPlayer.currentBet > currentPlayer.stackSize / 2) {
                // Only allow the AI to go all in when it needs to
                decisions.add(new PlayerDecision(new Decision(Decision.Move.ALL_IN)));
            }
            if (currentPlayer.currentBet == 0) {
                decisions.add(new PlayerDecision(new Decision(Decision.Move.CHECK)));
            }
            else {
                decisions.add(new PlayerDecision(new Decision(Decision.Move.FOLD)));
            }
            if (currentPlayer.currentBet > 0 && currentPlayer.stackSize > currentPlayer.currentBet) {
                decisions.add(new PlayerDecision(new Decision(Decision.Move.CALL)));
            }
            Decision.Move moneyMove = communityCards.size() > 0 && currentPlayer.currentBet == 0 ? Decision.Move.BET : Decision.Move.RAISE;
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

    /**
     * Gives two random hole cards to the given playerId
     * @param playerId
     */
    public void giveHoleCards(int playerId) {
        giveHoleCards(playerId, Arrays.asList(deck.remove((int)(Math.random() * deck.size())), deck.remove((int)(Math.random() * deck.size()))));
    }

    /*
     * Gives the holecards to the player
     */
    public void giveHoleCards(int playerId, List<Card> holeCards) {
        Player player = players.stream().filter(p -> p.id == playerId).findFirst().get();
        assert player.holeCards.size() == 0;
        player.holeCards.addAll(holeCards);
        deck.removeAll(holeCards);
        playersGivenHolecards++;
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

        public CardDealtToTable(Card card) {
            this.card = card;
        }
        public String toString() {
            return card + " to table.";
        }
    }

    public static class PlayerDecision extends GameStateChange {
        public final Decision decision;

        public PlayerDecision(Decision decision) {
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
        result = 31 * result + playersLeftInHand;
        result = 31 * result + playersAllIn;
        result = 31 * result + playersToMakeDecision;
        return result;
    }
}
