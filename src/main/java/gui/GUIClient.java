package gui;

import gamelogic.*;
import gamelogic.ai.GameState;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This client is communicating between back- and frontend
 *
 * @author Kristian Rosland
 * @author André Dyrstad
 */


public class GUIClient implements GameClient {

    private int ID;
    private GameScreen gameScreen;

    private Optional<GameState> gameState = Optional.empty();
    private Optional<Map<Integer, Integer>> positions = Optional.empty();
    private Optional<Map<Integer, String>> names = Optional.empty();
    private Map<Integer, Card[]> holeCards = new HashMap<>();
    private List<Card> communityCards = new ArrayList<>();
    private ArrayBlockingQueue<Decision> decisionBlockingQueue = new ArrayBlockingQueue<>(3);

    private int amountOfPlayers;
    private long minimumRaise = 0, highestAmountPutOnTable = 0;
    private Map<Integer, Long> stackSizes;
    private long smallBlind, bigBlind;
    private boolean playersSeated = false;

    private final Logger logger;

    public GUIClient(int ID, GameScreen gameScreen, Logger logger) {
        this.ID = ID;
        this.gameScreen = gameScreen;
        this.logger = logger;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("GUI-clients don't have their own names");
    }

    @Override
    public Decision getDecision(long timeToThink){
        if (!gameState.isPresent()) {
            initGameState();
        }
        //Make buttons visible
        Decision.Move moveIfTimeRunOut = highestAmountPutOnTable == 0 ? Decision.Move.CHECK : Decision.Move.FOLD;
        Platform.runLater(() -> {
            gameScreen.setActionsVisible(true);
            gameScreen.startTimer(timeToThink, moveIfTimeRunOut);
        });

        Decision decision;
        try {
            decision = decisionBlockingQueue.take();
        } catch (InterruptedException ie) {
            decision = Decision.fold;
            ie.printStackTrace();
        }

        //Make buttons invisible
        Platform.runLater(() -> {
            gameScreen.setActionsVisible(false);
            gameScreen.stopTimer();
        });

        return decision;
    }

    /**
     * Called from ButtonListeners-class to notify the client that a decision has been made
     */
    public void setDecision(Decision.Move move, long moveSize) {
        if (!decisionBlockingQueue.isEmpty() || !validMove(move, moveSize))
            return;

        switch (move) {
            case BET:
                if (moveSize == stackSizes.get(this.ID))
                    decisionBlockingQueue.add(new Decision(Decision.Move.ALL_IN));
                else
                    decisionBlockingQueue.add(new Decision(move, moveSize));
                break;
            case RAISE:
                if (moveSize == stackSizes.get(this.ID))
                    decisionBlockingQueue.add(new Decision(Decision.Move.ALL_IN));
                else
                    decisionBlockingQueue.add(new Decision(move, moveSize - highestAmountPutOnTable));
                break;
            case CALL:case CHECK:case FOLD:case ALL_IN: decisionBlockingQueue.add(new Decision(move));
        }

        Platform.runLater(() -> gameScreen.setErrorStateOfAmountTextField(false));
    }

    /**
     * Used when setting decisions without size (by the ButtonListeners)
     */
    public void setDecision(Decision.Move move) { setDecision(move, 0); }

    /**
     *  Check if a decision is valid (according to current stack size etc)
     * @param move The move
     * @param moveSize Size of the move
     * @return True if the move was valid
     */
    private boolean validMove(Decision.Move move, long moveSize) {
        if ((move == Decision.Move.BET || move == Decision.Move.RAISE) && moveSize > stackSizes.get(ID)) {
            logger.println("You don't have this much in your stack. Moving all in");
            decisionBlockingQueue.add(new Decision(Decision.Move.ALL_IN));
            return false;
        }
        else if (move == Decision.Move.RAISE && moveSize- highestAmountPutOnTable < Math.max(bigBlind, minimumRaise) &&
                (moveSize != stackSizes.get(ID))) {
            logger.println("Raise is too small");
            Platform.runLater(() -> gameScreen.setErrorStateOfAmountTextField(true));
            return false;
        }
        else if (move == Decision.Move.BET && moveSize < bigBlind) {
            logger.println("Bet is too small, must be a minimum of " + bigBlind);
            Platform.runLater(() -> gameScreen.setErrorStateOfAmountTextField(true));
            return false;
        }

        return true;
    }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        this.names = Optional.of(new HashMap<>(names));
        Platform.runLater(() ->gameScreen.setNames(names));
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        this.holeCards.put(userID, new Card[]{card1, card2});
        Platform.runLater(() -> gameScreen.setHandForUser(userID, card1, card2));
        showPercentagesIfAppropriate();
    }

    public void showPercentagesIfAppropriate() {
        System.out.println("Has " + holeCards.size() + " hole cards, " + gameState.get().getPlayersAllIn() + " all in, " + gameState.get().getPlayersLeftInHand() + " playing");
        if (gameState.isPresent() && this.holeCards.size() >= gameState.get().getPlayersAllIn() + gameState.get().getPlayersLeftInHand() && this.holeCards.size() > 1) {
            Map<Integer, Card[]> holeCardsStillInHand = new HashMap<>();
            this.holeCards.keySet().stream().filter(id -> {
                gamelogic.ai.Player player = gameState.get().players.stream().filter(p -> p.id == id).findAny().get();
                return player.isAllIn || player.isInHand;
            }).forEach(id -> holeCardsStillInHand.put(id, holeCards.get(id).clone()));
            gameScreen.showPercentages(holeCardsStillInHand, this.communityCards);
        }
    }

    @Override
    public void setFlop(Card card1, Card card2, Card card3) {
        try {
            gameState.get().makeGameStateChange(new GameState.CardDealtToTable(card1));
            gameState.get().makeGameStateChange(new GameState.CardDealtToTable(card2));
            gameState.get().makeGameStateChange(new GameState.CardDealtToTable(card3));
        } catch (IllegalDecisionException e) {
            e.printStackTrace();
        }
        communityCards.add(card1);
        communityCards.add(card2);
        communityCards.add(card3);

        showPercentagesIfAppropriate();
        Platform.runLater(() -> gameScreen.displayFlop(card1, card2, card3));
        newBettingRound();
    }

    @Override
    public void setTurn(Card turn) {
        try {
            gameState.get().makeGameStateChange(new GameState.CardDealtToTable(turn));
        } catch (IllegalDecisionException e) {
            e.printStackTrace();
        }
        communityCards.add(turn);
        showPercentagesIfAppropriate();
        Platform.runLater(() -> gameScreen.displayTurn(turn));
        newBettingRound();
    }

    @Override
    public void setRiver(Card river) {
        try {
            gameState.get().makeGameStateChange(new GameState.CardDealtToTable(river));
        } catch (IllegalDecisionException e) {
            e.printStackTrace();
        }
        communityCards.add(river);
        showPercentagesIfAppropriate();
        Platform.runLater(() -> gameScreen.displayRiver(river));
        newBettingRound();
    }

    @Override
    public void startNewHand() {
        communityCards.clear();
        Platform.runLater(() -> gameScreen.startNewHand());
        Thread.yield();

        gameState = Optional.empty();
        newBettingRound();
        holeCards.clear();
    }

    @Override
    public void playerBust(int playerID, int rank) {
        Platform.runLater(() -> gameScreen.bustPlayer(playerID, rank));
    }

    @Override
    public void gameOver(Statistics statistics) {
        Platform.runLater(() -> gameScreen.gameOver(statistics));
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        this.stackSizes = stackSizes;
        if (gameState.isPresent()) {
            Platform.runLater(() -> {
                gameScreen.updateStackSizes(stackSizes);

                //Updates the values of the slider
                gameScreen.updateSliderValues();
            });
        }
    }

    public void initGameState() {
        assert !gameState.isPresent();
        assert names.isPresent() : "GUI was sent a decision without receiving names";
        assert positions.isPresent() : "GUI was sent a decision without receiving positions";
        assert stackSizes != null : "GUI was sent a decision without receiving stackSizes";

        Map<Integer, Long> clonedStackSizes = new HashMap<>();
        stackSizes.forEach(clonedStackSizes::put);
        gameState = Optional.of(new GameState(amountOfPlayers, positions.get(),
                clonedStackSizes, names.get(), smallBlind, bigBlind, logger));
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        if (!gameState.isPresent()) {
            initGameState();
        }
        try {
            gameState.get().makeGameStateChange(new GameState.PlayerDecision(decision));
        } catch (IllegalDecisionException e) {
            assert false : "Illegal decision " + e;
        }
        switch (decision.move) {
            case SMALL_BLIND: case BIG_BLIND:
                highestAmountPutOnTable = decision.move == Decision.Move.BIG_BLIND ? bigBlind : smallBlind;
                break;
            case BET:
                highestAmountPutOnTable = decision.getSize();
                break;
            case RAISE:
                minimumRaise = decision.getSize();
                highestAmountPutOnTable += decision.getSize();
                break;
            case ALL_IN:
                break;
            case FOLD:
                showPercentagesIfAppropriate();
                break;
        }

        Platform.runLater(() -> gameScreen.playerMadeDecision(playerId, decision));

        GameState.NodeType nextNodeType = gameState.get().getNextNodeType();
        if (nextNodeType != GameState.NodeType.DEAL_COMMUNITY_CARD && nextNodeType != GameState.NodeType.TERMINAL) {
            Platform.runLater(() -> gameScreen.highlightPlayerTurn(gameState.get().currentPlayer.id));
        }

    }

    @Override
    public void showdown(String[] winnerStrings) {
        String winnerString = Arrays.stream(winnerStrings)
                .map(s -> s + "\n")
                .reduce("", String::concat)
                .trim();
        Platform.runLater(() -> gameScreen.showdown(new HashMap<>(holeCards), winnerString));
    }

    @Override
    public void setBigBlind(long bigBlind) {
        this.bigBlind = bigBlind;
        Platform.runLater(() -> gameScreen.setBigBlind(bigBlind));
    }

    @Override
    public void setSmallBlind(long smallBlind) {
        this.smallBlind = smallBlind;
        Platform.runLater(() -> gameScreen.setSmallBlind(smallBlind));
    }

    /**
     * Sends every numberOfPlayer's position, as a map indexed by the players' IDs.
     * A value of 0 corresponds to the small blind, 1 is big blind..
     * Sent at the start of each hand
     */
    @Override
    public void setPositions(Map<Integer, Integer> positions) {
        if (!playersSeated) {
            List<Integer> ids = positions.keySet().stream().sorted((i,j) -> positions.get(i).compareTo(positions.get(j))).collect(Collectors.toList());
            for (int i = 0; i < positions.size(); i++) {
                int playerID = ids.get((ids.indexOf(this.ID) + i) % positions.size());
                Platform.runLater(() -> gameScreen.insertPlayer(playerID, this.names.get().get(playerID)));
            }
            playersSeated = true;
        }

        this.positions = Optional.of(new HashMap<>(positions));
        Platform.runLater(() -> gameScreen.setPositions(positions));
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        this.amountOfPlayers = amountOfPlayers;
        Platform.runLater(() -> gameScreen.setNumberOfPlayers(amountOfPlayers));
    }

    @Override
    public void setLevelDuration(int levelDuration) {
    }

    public void newBettingRound() {
        minimumRaise = 0;
        highestAmountPutOnTable = 0;
        if (!gameState.isPresent()) {
            initGameState();
        }
        Platform.runLater(() -> gameScreen.newBettingRound());
        if (gameState.get().getPlayersLeftInHand() > 0) {
            gameScreen.highlightPlayerTurn(gameState.get().currentPlayer.id);
        }
    }

    /**
     * Prints the log message to the log field
     * @param message The message to be printed
     */
    public void printToLogField(String message) {
        Platform.runLater(() -> gameScreen.printToLogField(message));
    }

    public void preShowdownWinner(int winnerID) {
        Platform.runLater(() -> gameScreen.preShowdownWinner(winnerID));
    }

    @Override
    public void setChatListener(Consumer<String> chatListener) {
        Platform.runLater(() -> gameScreen.setChatListener(chatListener));
    }
}
