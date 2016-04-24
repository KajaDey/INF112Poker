package gamelogic.ai;

import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.IllegalDecisionException;
import gamelogic.ai.SimpleAI.AIDecision;

import java.util.*;

/**
 * Represents the state of a single hand, for use by the MCTSAI
 */
public class GameState {
    private final List<Card> deck;

    public final int amountOfPlayers;
    public final List<Player> players;
    public final List<Card> communityCards;

    public Player currentPlayer;

    public final long smallBlindAmount;
    public final long bigBlindAmount;
    public final long allChipsOnTable;
    private int playersGivenHoleCards = 0;
    private int playersLeftInHand; // Players who have not folded or gone all in (players still making decisions)

    public int getPlayersAllIn() {
        return playersAllIn;
    }

    private int playersAllIn = 0;

    private int playersToMakeDecision; // Players left to make decision in this betting round

    public int getPlayersToMakeDecision() {
        return playersToMakeDecision;
    }
    public int getPlayersLeftInHand() {
        return playersLeftInHand;
    }

    public GameState(int amountOfPlayers, Map<Integer, Integer> positions, Map<Integer, Long> stackSizes,
                     Map<Integer, String> names, long smallBlindAmount, long bigBlindAmount) {
        this.smallBlindAmount = smallBlindAmount;
        this.bigBlindAmount = bigBlindAmount;
        assert amountOfPlayers == positions.size();

        deck = new ArrayList<>(Arrays.asList(Card.getAllCards()));

        this.amountOfPlayers = amountOfPlayers;
        communityCards = new ArrayList<>();
        players = new ArrayList<>(amountOfPlayers);

        allChipsOnTable = stackSizes.keySet().stream().map(stackSizes::get).reduce(0L, Long::sum);
        
        positions.forEach((key, value) -> players.add(new Player(key, value, stackSizes.get(key), names.get(key))));
        players.sort((p1, p2) -> Integer.compare(p1.position, p2.position));

        currentPlayer = players.get(0);

        playersLeftInHand = amountOfPlayers;
        playersToMakeDecision = amountOfPlayers;
    }

    /**
     * Copy constructor for doing a deep clone of the old GameState
     */
    public GameState(GameState oldState) {
        this.smallBlindAmount = oldState.smallBlindAmount;
        this.bigBlindAmount = oldState.bigBlindAmount;
        this.deck = new ArrayList<>(oldState.deck);
        this.amountOfPlayers = oldState.amountOfPlayers;
        this.players = new ArrayList<>();
        for (int i = 0; i < oldState.players.size(); i++) {
            this.players.add(new Player(oldState.players.get(i)));
        }
        this.communityCards = new ArrayList<>(oldState.communityCards);

        currentPlayer = this.players.get(oldState.currentPlayer.position);

        this.allChipsOnTable = oldState.allChipsOnTable;
        this.playersLeftInHand = oldState.playersLeftInHand;
        this.playersAllIn = oldState.playersAllIn;
        this.playersToMakeDecision = oldState.playersToMakeDecision;
        this.playersGivenHoleCards = oldState.playersGivenHoleCards;
    }

    public long getCurrentPot() {
        long sum = 0;
        for (Player player : players) {
            sum += player.contributedToPot;
        }
        return sum;
    }

    /**
     * Applies a gameStateChange to the gameState, modifying the object and its players
     * The gameState should only change through this method, except for giving holecards to the AI player
     */
    public void makeGameStateChange(GameStateChange move) throws IllegalDecisionException {
        if (move instanceof CardDealtToPlayer) {
            CardDealtToPlayer cardDeal = (CardDealtToPlayer)move;
            players.get(cardDeal.playerPosition).holeCards.add(cardDeal.card);
            deck.remove(cardDeal.card);
            if (players.get(cardDeal.playerPosition).holeCards.size() == 2) {
                playersGivenHoleCards++;
            }
        }
        else if (move instanceof CardDealtToTable) {
            if (communityCards.size() >= 5) {
                throw new IllegalDecisionException();
            }
            communityCards.add(((CardDealtToTable)(move)).card);
            deck.remove(((CardDealtToTable)(move)).card);
            if (communityCards.size() == 3 || communityCards.size() == 4 || communityCards.size() == 5) {
                for (Player player : players) {
                    player.currentBet = 0;
                    player.minimumRaise = bigBlindAmount;
                }
                if (playersLeftInHand > 1) {
                    playersToMakeDecision = playersLeftInHand;
                }
            }
        }
        else if (move instanceof PlayerDecision || move instanceof AIMove) {
            Decision decision;
            if (move instanceof AIMove) {
                decision = ((AIMove)move).decision.toRealDecision(currentPlayer.currentBet, currentPlayer.minimumRaise, currentPlayer.stackSize, getCurrentPot(), playersLeftInHand == 1, currentPlayer.currentBet > 0 || communityCards.size() == 0);
            }
            else {
                decision = ((PlayerDecision)move).decision;
            }
            switch (decision.move) {
                case FOLD:
                    playersLeftInHand--;
                    playersToMakeDecision--;
                    currentPlayer.isInHand = false;
                    // If the player never received hole cards, count as if they did
                    if (currentPlayer.holeCards.size() < 2) {
                        playersGivenHoleCards++;
                    }
                    break;
                case CHECK:
                    playersToMakeDecision--;
                    break;
                case CALL:
                    playersToMakeDecision--;
                    currentPlayer.putInPot(Math.min(currentPlayer.stackSize, currentPlayer.currentBet));
                    currentPlayer.currentBet = 0;
                    break;
                case BET:
                case RAISE:
                    if (!(decision.move == Decision.Move.RAISE || communityCards.size() > 0)) {
                        throw new IllegalDecisionException("Received " + decision + " with " + communityCards.size() + " community cards on table");
                    }
                    if (!(decision.move == Decision.Move.BET || communityCards.size() == 0 || currentPlayer.currentBet > 0)) {
                        throw new IllegalDecisionException("Received bet decision with " + communityCards.size() + " community cards and currentBet " + currentPlayer.currentBet);
                    }
                    if (decision.getSize() < currentPlayer.minimumRaise) {
                        throw new IllegalDecisionException(currentPlayer + " made " + decision + " from " + move + ", but minimum raise was " + currentPlayer.minimumRaise);
                    }
                    if (decision.getSize() + currentPlayer.currentBet > currentPlayer.stackSize) {
                        throw new IllegalDecisionException(currentPlayer + " tried " + decision + " on currentBet " + currentPlayer.currentBet + ", but had stackSize " + currentPlayer.stackSize);
                    }
                    if (decision.getSize() <= 0) {
                        throw new IllegalDecisionException(currentPlayer + " tried to bet/raise " + decision.getSize());
                    }
                    playersToMakeDecision = playersLeftInHand - 1;

                    currentPlayer.putInPot(currentPlayer.currentBet + decision.getSize());

                    for (Player player : players) {
                        if (player.id != currentPlayer.id) {
                            player.currentBet += decision.getSize();
                            player.minimumRaise = decision.getSize();
                        }
                    }
                    currentPlayer.minimumRaise = decision.getSize();
                    currentPlayer.currentBet = 0;
                    break;
                case ALL_IN:
                    for (Player player : players) {
                        if (player.id != currentPlayer.id) {
                            player.currentBet += Math.max(0, currentPlayer.stackSize - currentPlayer.currentBet);
                            player.minimumRaise = Math.max(currentPlayer.stackSize - currentPlayer.minimumRaise, player.minimumRaise);
                        }
                    }
                    playersLeftInHand--;
                    if (currentPlayer.currentBet >= currentPlayer.stackSize) {
                        playersToMakeDecision--;
                    }
                    else {
                        playersToMakeDecision = playersLeftInHand;
                    }
                    playersAllIn++;

                    currentPlayer.putInPot(currentPlayer.stackSize);
                    currentPlayer.isAllIn = true;
                    break;
                case SMALL_BLIND:
                    for (Player player : players) {
                        player.currentBet = smallBlindAmount;
                        player.minimumRaise = smallBlindAmount;
                    }

                    currentPlayer.currentBet = 0;
                    currentPlayer.putInPot(Math.min(smallBlindAmount, currentPlayer.stackSize));
                    if (currentPlayer.stackSize == 0) {
                        playersAllIn++;
                        playersToMakeDecision--;
                        playersLeftInHand--;
                    }
                    assert currentPlayer.stackSize >= 0;
                    break;
                case BIG_BLIND:
                    for (Player player : players) {

                        // If is small blind
                        if (player.currentBet == 0) {
                            player.currentBet = bigBlindAmount - currentPlayer.currentBet;
                        }
                        else {
                            player.currentBet = bigBlindAmount;
                        }
                        player.minimumRaise = bigBlindAmount;
                    }

                    currentPlayer.currentBet = 0;
                    currentPlayer.putInPot(Math.min(bigBlindAmount, currentPlayer.stackSize));
                    if (currentPlayer.stackSize == 0) {
                        playersAllIn++;
                        playersToMakeDecision--;
                        playersLeftInHand--;
                    }
                    assert currentPlayer.stackSize >= 0;
                    break;

            }

            // Small blind moves first post-flop
            if (playersToMakeDecision == 0) {
                //System.out.println("Everyone has made decisions");
                if (amountOfPlayers == 2) {
                    currentPlayer = players.get(0);
                }
                else {
                    currentPlayer = players.get(players.size() - 1);
                }
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

    public Optional<GameStateChange> getRandomDecision(Random random) {
        switch (getNextNodeType()) {
            case DEAL_COMMUNITY_CARD:
                return Optional.of(new CardDealtToTable(deck.get(random.nextInt(deck.size()))));
            case DEAL_HAND_CARD:
                // Loop trying to give the opponents a good hand if they have bet a lot of chips
                assert currentPlayer.holeCards.size() < 2;

                for (int j = 0; j < 100; j++) {
                    int randomCardIndex = random.nextInt(deck.size());
                    if (currentPlayer.holeCards.size() == 0) {
                        double handQuality = deck.get(randomCardIndex).rank;
                        if (handQuality / (14 - j / 2) > currentPlayer.riskTaken(allChipsOnTable)) {
                            //System.out.println("Rejected " + j + " while giving first hole card");
                            return Optional.of(new CardDealtToPlayer(deck.get(randomCardIndex), currentPlayer.position));
                        }
                    }
                    else {
                        double handQuality = HandEstimator.handQuality(currentPlayer.holeCards.get(0), deck.get(randomCardIndex), communityCards);
                        if (handQuality / (50 - j) > currentPlayer.riskTaken(allChipsOnTable)) {
                            //System.out.println("Rejected " + j + " while giving second hole card");
                            return Optional.of(new CardDealtToPlayer(deck.get(randomCardIndex), currentPlayer.position));
                        }
                    }
                }

            case PLAYER_DECISION:
                double handQuality = HandEstimator.handQuality(currentPlayer.holeCards.get(0), currentPlayer.holeCards.get(1), communityCards) * Math.pow(0.95, playersLeftInHand);

                // Random modifier between 0.5 and 1.5
                double randomModifier = (Math.random() + Math.random()) / 2 + 0.5;
                SimpleAI.AIDecision aiDecision;

                if (randomModifier * (handQuality / 18.0) > 1) {
                    // If the hand is considered "good", raise or bet if no one else has done it
                    if (currentPlayer.currentBet == 0) {
                        aiDecision = HandEstimator.getRaiseAmount(randomModifier, handQuality, 1.0);
                        if (aiDecision == AIDecision.RAISE_HALF_POT && getCurrentPot() / 2 < currentPlayer.minimumRaise) {
                            aiDecision = AIDecision.RAISE_MINIMUM;
                        }
                    }
                    // If someone has already raised, raise anyway if the hand is really good
                    else if (randomModifier * (handQuality / 22.0) > 1) {
                        aiDecision = HandEstimator.getRaiseAmount(randomModifier, handQuality, 1.0);
                        if (aiDecision == AIDecision.RAISE_HALF_POT && getCurrentPot() / 2 < currentPlayer.minimumRaise) {
                            aiDecision = AIDecision.RAISE_POT;
                        }
                    }
                    else {
                        aiDecision = AIDecision.CALL;
                    }
                }
                else if (randomModifier * (handQuality / 14.0) > 1) { // If the hand is decent
                    if (currentPlayer.currentBet == 0) {
                        aiDecision = AIDecision.CHECK;
                    }
                    else if (currentPlayer.currentBet < currentPlayer.stackSize  / 20 * randomModifier) { // If it's a small call
                        aiDecision = AIDecision.CALL;
                    }
                    else {
                        aiDecision = AIDecision.FOLD;
                    }
                }
                else {
                    if (currentPlayer.currentBet == 0) {
                        aiDecision = AIDecision.CHECK;
                    }
                    else {
                        aiDecision = AIDecision.FOLD;
                    }
                }
                return Optional.of(new AIMove(aiDecision));
            case TERMINAL:
                return Optional.empty();
            default: throw new IllegalStateException();
        }
    }

    /**
     * Gets a list of all possible decisions in the current GameState, without mutating the gamestate.
     * Returns Empty if the node is a terminal node
     */
    public Optional<List<GameStateChange>> allDecisions() {
        List<GameStateChange> decisions = new ArrayList<>(20);

        assert deck.size() + players.stream().map(p -> p.holeCards.size()).reduce(0, Integer::sum) + communityCards.size() == 52 : "Deck has " + deck.size() + " cards, players have " + players.stream().map(p -> p.holeCards.size()).reduce(0, Integer::sum) + " holecards [" + players.stream().map(p -> p.name + ": " + p.holeCards).reduce("", String::concat) + "] and table has " + communityCards.size() + " community cards";
        assert sumOfChipsInPlay(players) == allChipsOnTable :
        "Sum of player chips is " + sumOfChipsInPlay(players) + ", but started with " + allChipsOnTable + " on table.";

        assert players.stream().map(player -> player.stackSize).min(Long::compare).get() >= 0L : "A player has negative stack size";

        switch (getNextNodeType()) {
            case DEAL_HAND_CARD:
                assert currentPlayer.holeCards.size() < 2 : "Tried to deal hole card to " + currentPlayer + ", but they had " + currentPlayer.holeCards.size();
                assert currentPlayer.isInHand || currentPlayer.isAllIn : currentPlayer + " is neither in hand nor all in, players in hand=" + playersLeftInHand + ", players given hole cards=" + playersGivenHoleCards;
                assert currentPlayer.holeCards.size() < 2;
                for (Card card : deck) {
                    decisions.add(new CardDealtToPlayer(card, currentPlayer.position));
                }

                assert decisions.size() > 0 : "Was not able to deal any hand card to " + currentPlayer + ", deck has " + deck.size() + " cards.";
                return Optional.of(decisions);

            case DEAL_COMMUNITY_CARD:
                for (Card card : deck) {
                    decisions.add(new CardDealtToTable(card));
                }
                assert decisions.size() > 0;
                return Optional.of(decisions);
            case PLAYER_DECISION:
                assert playersLeftInHand > 0: "Trying to generate possible decisions for player, when there are no players left (" +
                        playersAllIn + " players all in, " + playersToMakeDecision + " players to make decisions)";
                assert currentPlayer.isInHand && !currentPlayer.isAllIn : "Asked for decisions for " + currentPlayer + ", but player was not in hand. isAllIn=" + currentPlayer.isAllIn + ", isInHand=" + currentPlayer.isInHand;
                assert currentPlayer.stackSize > 0 : currentPlayer + " has a stacksize of " + currentPlayer.stackSize;
                assert currentPlayer.minimumRaise > 0;

                if (currentPlayer.currentBet == 0) {
                    decisions.add(new AIMove(AIDecision.CHECK));
                }
                else {
                    decisions.add(new AIMove(AIDecision.FOLD));
                }
                if (currentPlayer.currentBet > 0 && currentPlayer.stackSize >= currentPlayer.currentBet) {
                    decisions.add(new AIMove(AIDecision.CALL));
                }
                decisions.add(new AIMove(AIDecision.RAISE_MINIMUM));
                if (getCurrentPot() / 2 > currentPlayer.minimumRaise) {
                    decisions.add(new AIMove(AIDecision.RAISE_HALF_POT));
                }
                decisions.add(new AIMove(AIDecision.RAISE_POT));

                return Optional.of(decisions);
            case TERMINAL:
                return Optional.empty();
            default: throw new IllegalStateException();
        }
    }

    /**
     * Gives two random hole cards to the given playerId
     */
    public void giveHoleCards(int playerId) {
        List<Card> newHoleCards = new ArrayList<>();
        while (players.stream().filter(p -> p.id == playerId).findFirst().get().holeCards.size() + newHoleCards.size() < 2) {
            newHoleCards.add(deck.get((int)Math.random()));
        }
        giveHoleCards(playerId, newHoleCards);
    }

    public int getPlayersGivenHoleCards() {
        return playersGivenHoleCards;
    }

    /*
         * Gives the holecards to the player
         */
    public void giveHoleCards(int playerId, List<Card> holeCards) {
        Player player = players.stream().filter(p -> p.id == playerId).findFirst().get();
        player.holeCards.addAll(holeCards);
        deck.removeAll(holeCards);
        playersGivenHoleCards++;
        assert player.holeCards.size() == 2;
    }

    // Returns the kind of decision that needs to be done in this gamestate
    public NodeType getNextNodeType() {
        NodeType nodeType;
        if (playersLeftInHand + playersAllIn == 1) {
            // If everyone except one player has folded
            nodeType = NodeType.TERMINAL;
        }
        else if (playersToMakeDecision == 0 || (playersLeftInHand == 0 && playersAllIn > 2)) {
            if (communityCards.size() == 5) {
                nodeType = NodeType.TERMINAL;
            }
            else {
                nodeType = NodeType.DEAL_COMMUNITY_CARD;
            }
        }
        else if (currentPlayer.holeCards.size() < 2) {
            nodeType = NodeType.DEAL_HAND_CARD;
        }
        else {
            nodeType = NodeType.PLAYER_DECISION;
        }
        return nodeType;
    }

    public enum NodeType {DEAL_COMMUNITY_CARD, DEAL_HAND_CARD, PLAYER_DECISION, TERMINAL}

    /**
     * Abstract class that represents any change to the gameState, including player decisions
     * or community cards being played
     */
    public static abstract class GameStateChange {

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CardDealtToPlayer that = (CardDealtToPlayer) o;

            if (playerPosition != that.playerPosition) return false;
            return card.equals(that.card);

        }
        @Override
        public int hashCode() {
            int result = card.hashCode();
            result = 31 * result + playerPosition;
            return result;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CardDealtToTable that = (CardDealtToTable) o;

            return card.equals(that.card);

        }

        @Override
        public int hashCode() {
            return card.hashCode();
        }
    }

    public static class PlayerDecision extends GameStateChange {
        public final Decision decision;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PlayerDecision that = (PlayerDecision) o;

            return decision.equals(that.decision);

        }

        @Override
        public int hashCode() {
            return decision.hashCode();
        }

        public PlayerDecision(Decision decision) {
            this.decision = decision;
        }

        public String toString() {
            return decision.toString();
        }
    }

    public static class AIMove extends GameStateChange {
        public final AIDecision decision;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AIMove that = (AIMove) o;

            return decision.equals(that.decision);

        }

        @Override
        public int hashCode() {
            return decision.hashCode();
        }

        public AIMove(AIDecision decision) {
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
        return true;

    }

    @Override
    public int hashCode() {
        int result = deck.hashCode();
        result = 31 * result + amountOfPlayers;
        result = 31 * result + players.hashCode();
        result = 31 * result + communityCards.hashCode();
        result = 31 * result + currentPlayer.hashCode();

        result = 31 * result + playersLeftInHand;
        result = 31 * result + playersAllIn;
        result = 31 * result + playersToMakeDecision;
        return result;
    }
}
