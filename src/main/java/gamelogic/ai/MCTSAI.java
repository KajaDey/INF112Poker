package gamelogic.ai;

import gamelogic.*;

import java.util.*;

/**
 * An AI using monte carlo tree search to choose decisions
 */
public class MCTSAI implements GameClient {

    public final double contemptFactor;
    private final int playerId;
    private int amountOfPlayers;
    private long smallBlindAmount;
    private long bigBlindAmount;
    private List<Card> holeCards = new ArrayList<>();

    private Optional<GameState> gameState;
    private Optional<Map<Integer, Long>> stackSizes;
    private Optional<Map<Integer, Integer>> positions;
    private Optional<Map<Integer, String>> names;

    public MCTSAI(int playerId, double contemptFactor) {
        gameState = Optional.empty();
        stackSizes = Optional.empty();
        positions = Optional.empty();
        names = Optional.empty();
        this.playerId = playerId;
        this.contemptFactor = contemptFactor;
    }

    public MCTSAI(int playerId) {
        this(playerId, 1.0);
    }

    @Override
    public Decision getDecision(long timeToThink) {
        assert stackSizes.isPresent();
        assert holeCards.size() == 2: "AI was asked to make a decision after receiving " + holeCards.size() + " hole cards.";
        assert stackSizes.get().get(playerId) > 0: "AI was asked to make a decicion after going all in (stacksize=" + stackSizes.get().get(playerId) + ")";
        assert positions.isPresent() : "AI was asked to make a decision without receiving positions";
        assert names.isPresent() : "AI was asked to make a decision without receiving names";
        if (!gameState.isPresent()) {
            gameState = Optional.of(new GameState(amountOfPlayers, positions.get(), stackSizes.get(), names.get(), smallBlindAmount, bigBlindAmount));
            gameState.get().giveHoleCards(this.playerId, holeCards);
        }

        PokerMCTS mcts = new PokerMCTS(gameState.get(), amountOfPlayers, playerId, Math.sqrt(contemptFactor));
        return mcts.calculateFor(timeToThink);
    }

    @Override
    public int getID() {
        return playerId;
    }

    @Override
    public void startNewHand() {
        gameState = Optional.empty();
        stackSizes = Optional.empty();
        positions = Optional.empty();
        holeCards.clear();
    }

    @Override
    public void playerBust(int playerID, int rank) {
        amountOfPlayers--;
        gameState = Optional.empty();
        stackSizes = Optional.empty();
        positions = Optional.empty();
        names.get().remove(playerID);
        holeCards.clear();
    }

    @Override
    public void gameOver(Statistics stats) { }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        assert names.size() == amountOfPlayers : "MCTSAI received names for " + names.size() + " players, but there are " + amountOfPlayers + " players.";
        this.names = Optional.of(names);
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        assert this.getID() == userID;

        holeCards = new ArrayList<>(2);
        assert holeCards.size() == 0;
        holeCards.add(card1);
        holeCards.add(card2);
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        assert stackSizes.size() == amountOfPlayers;
        this.stackSizes = Optional.of(stackSizes);
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        assert stackSizes.isPresent() : " MCTSAI was sent a decicion without first receving stacksizes";
        assert stackSizes.get().size() == amountOfPlayers;
        assert positions.isPresent() : "MCTSAI was sent a decision without receiving positions";
        assert positions.get().size() == amountOfPlayers;
        assert names.isPresent() : "MCTSAI was sent a decision without receiving names";
        assert names.get().size() == amountOfPlayers;
        assert holeCards.size() == 2 : "MCTSAi received a decision without being dealt hole cards";
        if (!gameState.isPresent()) {
            gameState = Optional.of(new GameState(amountOfPlayers, positions.get(), stackSizes.get(), names.get(), smallBlindAmount, bigBlindAmount));
            gameState.get().giveHoleCards(this.playerId, holeCards);
        }
        assert playerId == gameState.get().currentPlayer.id
                : "Received decision " + decision + " for player " + playerId + " at position " + positions.get().get(playerId) + ", but currentPlayer is " + gameState.get().currentPlayer.id + " at position " + gameState.get().currentPlayer.position;
        try {
            gameState.get().makeGameStateChange(new GameState.PlayerDecision(decision));
        } catch (IllegalDecisionException e) {
            e.printStackTrace();
            System.out.println("Error: AI received illegal decision.");
            System.exit(1);
        }
    }

    @Override
    public void showdown(ShowdownStats showdownStats) {

    }

    @Override
    public void setBigBlind(long bigBlind) {
        bigBlindAmount = bigBlind;
    }

    @Override
    public void setSmallBlind(long smallBlind) { smallBlindAmount = smallBlind;
    }

    @Override
    public void setPositions(Map<Integer, Integer> positions) {
        assert positions.size() == amountOfPlayers :
                "AI received positions " + positions.size() + " for players, but there are " + amountOfPlayers + " playing.";
        assert positions.get(playerId) != null : "AI received positions object which didn't contain its own position";

        this.positions = Optional.of(positions);
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        this.amountOfPlayers = amountOfPlayers;
    }

    @Override
    public void setLevelDuration(int levelDuration) { }

    @Override
    public void setFlop(Card card1, Card card2, Card card3) {
        assert gameState.isPresent();
        assert gameState.get().communityCards.isEmpty() : "MCTS received flop card when it had " + gameState.get().communityCards.size();
        assert gameState.get().getPlayersToMakeDecision() == 0 : "MCTS received flop cards when " + gameState.get().getPlayersToMakeDecision() + " players still need to make decisions (currentPlayer=" + gameState.get().currentPlayer + ")";
        try {
            gameState.get().makeGameStateChange(new GameState.CardDealtToTable(card1));
            assert gameState.get().getPlayersToMakeDecision() == 0;
            gameState.get().makeGameStateChange(new GameState.CardDealtToTable(card2));
            assert gameState.get().getPlayersToMakeDecision() == 0;
            gameState.get().makeGameStateChange(new GameState.CardDealtToTable(card3));
        } catch (IllegalDecisionException e) {
            e.printStackTrace();
            System.out.println("Error: AI received illegal decision.");
            System.exit(1);
        }

    }

    @Override
    public void setTurn(Card turn) {
        assert gameState.isPresent();
        assert gameState.get().communityCards.size() == 3 : "MCTS received turn card when it had " + gameState.get().communityCards.size();
        assert gameState.get().getPlayersToMakeDecision() == 0 : "MCTS received turn card when " + gameState.get().getPlayersToMakeDecision() + " players still need to make decisions (currentPlayer=" + gameState.get().currentPlayer + ")";
        try {
            gameState.get().makeGameStateChange(new GameState.CardDealtToTable(turn));
        } catch (IllegalDecisionException e) {
            e.printStackTrace();
            System.out.println("Error: AI received illegal decision.");
            System.exit(1);
        }
    }

    @Override
    public void setRiver(Card river) {
        assert gameState.isPresent();
        assert gameState.get().communityCards.size() == 4 : "MCTS received river card when it had " + gameState.get().communityCards.size();
        assert gameState.get().getPlayersToMakeDecision() == 0 : "MCTS received river card when " + gameState.get().getPlayersToMakeDecision() + " players still need to make decisions (currentPlayer=" + gameState.get().currentPlayer + ")";
        try {
            gameState.get().makeGameStateChange(new GameState.CardDealtToTable(river));
        } catch (IllegalDecisionException e) {
            e.printStackTrace();
            System.out.println("Error: AI received illegal decision.");
            System.exit(1);
        }

    }
}
