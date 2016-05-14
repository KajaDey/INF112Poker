package gamelogic.ai;

import gamelogic.*;

import java.util.*;
import java.util.function.Consumer;

/**
 * An AI using monte carlo tree search to choose decisions
 */
public class MCTSAI implements GameClient {

    public final double contemptFactor;
    private final int playerID;
    private int amountOfPlayers;
    private long smallBlindAmount;
    private long bigBlindAmount;
    private List<Card> holeCards = new ArrayList<>();

    private Optional<GameState> gameState;
    private Optional<Map<Integer, Long>> stackSizes;
    private Optional<Map<Integer, Integer>> positions;
    private Optional<Map<Integer, String>> names;
    private final Logger logger;

    public MCTSAI(int playerID, double contemptFactor, Logger logger) {
        this.logger = logger;
        gameState = Optional.empty();
        stackSizes = Optional.empty();
        positions = Optional.empty();
        names = Optional.empty();
        this.playerID = playerID;
        this.contemptFactor = contemptFactor;
    }

    public MCTSAI(int playerID, Logger logger) {
        this(playerID, 1.0, logger);
    }

    @Override
    public Decision getDecision(long timeToThink) {
        assert stackSizes.isPresent();
        assert holeCards.size() == 2 : "AI was asked to make a decision after receiving " + holeCards.size() + " hole cards.";
        assert stackSizes.get().get(playerID) > 0 : "AI was asked to make a decicion after going all in (stacksize=" + stackSizes.get().get(playerID) + ")";
        assert positions.isPresent() : "AI was asked to make a decision without receiving positions";
        assert names.isPresent() : "AI was asked to make a decision without receiving names";
        assert smallBlindAmount > 0 && bigBlindAmount > 0 : "AI was sent decision with receiving blinds";
        if (!gameState.isPresent()) {
            gameState = Optional.of(new GameState(amountOfPlayers, positions.get(), stackSizes.get(), names.get(), smallBlindAmount, bigBlindAmount, logger));
            gameState.get().giveHoleCards(this.playerID, holeCards);
        }

        PokerMCTS mcts = new PokerMCTS(gameState.get(), amountOfPlayers, playerID, Math.sqrt(contemptFactor), logger);
        return mcts.calculateFor(timeToThink);
    }

    @Override
    public String getName() {
        return "";
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
    public void gameOver(Statistics statistics) {
    }

    @Override
    public void printToLogField(String output) {

    }

    @Override
    public void preShowdownWinner(int winnerID) {

    }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        assert names.size() == amountOfPlayers : "MCTSAI received names for " + names.size() + " players, but there are " + amountOfPlayers + " players.";
        this.names = Optional.of(names);
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        holeCards = new ArrayList<>(2);
        assert holeCards.size() == 0;
        holeCards.add(card1);
        holeCards.add(card2);
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        assert stackSizes.size() == amountOfPlayers : "Received stacksizes for " + stackSizes.size() + " players, but there are " + amountOfPlayers + " playing.";
        this.stackSizes = Optional.of(stackSizes);
    }

    @Override
    public void playerMadeDecision(Integer playerID, Decision decision) {
        assert stackSizes.isPresent() : " MCTSAI was sent a decicion without first receving stacksizes";
        assert stackSizes.get().size() == amountOfPlayers : "MCTSAI received stackSizes for " + positions.get().size() + " players, but there are " + amountOfPlayers + " in the game";
        assert positions.isPresent() : "MCTSAI was sent a decision without receiving positions";
        assert positions.get().size() == amountOfPlayers : "MCTSAI received positions for " + positions.get().size() + " players, but there are " + amountOfPlayers + " in the game";
        assert names.isPresent() : "MCTSAI was sent a decision without receiving names";
        assert names.get().size() == amountOfPlayers;
        assert holeCards.size() == 2 : "MCTSAi received a decision without being dealt hole cards";
        assert smallBlindAmount > 0 && bigBlindAmount > 0 : "AI was sent decision with receiving blinds";
        if (!gameState.isPresent()) {
            gameState = Optional.of(new GameState(amountOfPlayers, positions.get(), stackSizes.get(), names.get(), smallBlindAmount, bigBlindAmount, logger));
            gameState.get().giveHoleCards(this.playerID, holeCards);
        }
        assert playerID == gameState.get().currentPlayer.id
                : "Received decision " + decision + " for player " + playerID + " at position " + positions.get().get(playerID) + ", but currentPlayer is " + gameState.get().currentPlayer.id + " at position " + gameState.get().currentPlayer.position;
        try {
            gameState.get().makeGameStateChange(new GameState.PlayerDecision(decision));
        } catch (IllegalDecisionException e) {
            e.printStackTrace();
            logger.println("Error: AI received illegal decision.", Logger.MessageType.AI, Logger.MessageType.WARNINGS);
            System.exit(1);
        }
    }

    //@Override
    /*public void showdown(ShowdownStats showdownStats) {

    }*/

    @Override
    public void showdown(String[] winnerStrings) {

    }

    @Override
    public void setBigBlind(long bigBlind) {
        bigBlindAmount = bigBlind;
    }

    @Override
    public void setSmallBlind(long smallBlind) {
        smallBlindAmount = smallBlind;
    }

    @Override
    public void setPositions(Map<Integer, Integer> positions) {
        assert positions.size() == amountOfPlayers :
                "AI received positions " + positions.size() + " for players, but there are " + amountOfPlayers + " playing.";
        assert positions.get(playerID) != null : "AI received positions object which didn't contain its own position";

        this.positions = Optional.of(positions);
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        this.amountOfPlayers = amountOfPlayers;
    }

    @Override
    public void setLevelDuration(int levelDuration) {
    }

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
            logger.println("Error: AI received illegal decision.", Logger.MessageType.AI, Logger.MessageType.WARNINGS);
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
            logger.println("Error: AI received illegal decision.", Logger.MessageType.AI, Logger.MessageType.WARNINGS);
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
            logger.println("Error: AI received illegal decision.", Logger.MessageType.AI, Logger.MessageType.WARNINGS);
            System.exit(1);
        }

    }

    @Override
    public void setChatListener(Consumer<String> chatListener) {
    }

    @Override
    public void setCallback(Consumer<Boolean> callBack) {

    }
}
