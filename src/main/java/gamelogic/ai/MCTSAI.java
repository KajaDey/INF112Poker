package gamelogic.ai;

import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.GameClient;
import gamelogic.ai.SimpleAI;
import sun.java2d.pipe.SpanShapeRenderer;

import java.util.*;

/**
 * Created by morten on 11.03.16.
 */
public class MCTSAI implements GameClient {

    private final int playerId;
    private int amountOfPlayers;
    private List<Card> holeCards = new ArrayList<>();

    private Optional<GameState> gameState;
    private Optional<Map<Integer, Long>> stackSizes;
    private Optional<Map<Integer, Integer>> positions;
    private Optional<Map<Integer, String>> names;

    public MCTSAI(int playerId) {
        gameState = Optional.empty();
        stackSizes = Optional.empty();
        positions = Optional.empty();
        names = Optional.empty();
        this.playerId = playerId;
    }

    @Override
    public Decision getDecision() {
        assert stackSizes.isPresent();
        assert holeCards.size() == 2: "SimpleAI was asked to make a decision after receiving " + holeCards.size() + " hole cards.";
        assert stackSizes.get().get(playerId) > 0: "SimpleAI was asked to make a decicion after going all in (stacksize=" + stackSizes.get().get(playerId) + ")";
        assert positions.isPresent() : "AI was asked to make a decision without receiving positions";
        assert names.isPresent() : "AI was asked to make a decision without receiving names";

        GameState gameState = new GameState(amountOfPlayers, positions.get(), stackSizes.get(), names.get());

        PokerMCTS mcts = new PokerMCTS(gameState, amountOfPlayers, playerId, holeCards);
        //assert gameState.players.get(position).holeCards.size() == 2 : "Player has " + gameState.players.get(position).holeCards.size() + " holecards";
        return mcts.calculateFor(1000);
    }

    @Override
    public int getID() {
        return playerId;
    }

    @Override
    public void startNewHand() {
        System.out.println("MCTSAI: Starting new hand");
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
        holeCards.clear();
    }

    @Override
    public void gameOver(int winnerID) { }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        this.names = Optional.of(names);
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        assert this.getID() == userID;

        holeCards = new ArrayList<Card>(2);
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
        assert positions.isPresent() : "MCTSAI was sent a decision without receiving positions";
        assert names.isPresent() : "MCTSAI was sent a decision without receiving names";
        if (!gameState.isPresent()) {
            gameState = Optional.of(new GameState(amountOfPlayers, positions.get(), stackSizes.get(), names.get()));
        }
        assert playerId == gameState.get().currentPlayer.id
                : "Received decision " + decision + " for player " + playerId + " at position " + positions.get().get(playerId) + ", but currentPlayer is " + gameState.get().currentPlayer.id + " at position " + gameState.get().currentPlayer.position;
        System.out.println("Received decision " + decision + " for player " + playerId);
        gameState.get().makeGameStateChange(new GameState.PlayerDecision(decision));
    }

    @Override
    public void showdown(List<Integer> playersStillPlaying, int winnerID, Map<Integer, Card[]> holeCards, long pot) { }

    @Override
    public void setBigBlind(long bigBlind) {
    }

    @Override
    public void setSmallBlind(long smallBlind) {
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
    public void setFlop(Card card1, Card card2, Card card3, long currentPotSize) {
        assert gameState.isPresent();
        assert gameState.get().communityCards.isEmpty() && gameState.get().playersToMakeDecision == 0;
        gameState.get().makeGameStateChange(new GameState.CardDealtToTable(card1));
        assert gameState.get().playersToMakeDecision == 0;
        gameState.get().makeGameStateChange(new GameState.CardDealtToTable(card2));
        assert gameState.get().playersToMakeDecision == 0;
        gameState.get().makeGameStateChange(new GameState.CardDealtToTable(card3));
    }

    @Override
    public void setTurn(Card turn, long currentPotSize) {
        assert gameState.isPresent();
        assert gameState.get().communityCards.size() == 3 && gameState.get().playersToMakeDecision == 0;
        gameState.get().makeGameStateChange(new GameState.CardDealtToTable(turn));
    }

    @Override
    public void setRiver(Card river, long currentPotSize) {
        assert gameState.isPresent();
        assert gameState.get().communityCards.size() == 4 && gameState.get().playersToMakeDecision == 0;
        gameState.get().makeGameStateChange(new GameState.CardDealtToTable(river));

    }
}
