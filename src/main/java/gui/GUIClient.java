package gui;

import gamelogic.*;
import gamelogic.ai.GameState;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by ady on 08/03/16.
 */


public class GUIClient implements GameClient {

    private GameScreen gameScreen;

    private Optional<GameState> gameState = Optional.empty();
    private Optional<Map<Integer, Integer>> positions = Optional.empty();
    private Optional<Map<Integer, String>> names = Optional.empty();

    //Storage variables
    private int amountOfPlayers;
    private long minimumRaise = 0, highestAmountPutOnTable = 0;
    private Decision decision;
    private Map<Integer, Long> stackSizes;
    private long smallBlind, bigBlind;
    private int id;

    public GUIClient(int id, GameScreen gameScreen) {
        this.id = id;
        this.gameScreen = gameScreen;
    }


    @Override
    public synchronized Decision getDecision(long timeToThink){
        if (!gameState.isPresent()) {
            Map<Integer, Long> clonedStackSizes = new HashMap<>();
            stackSizes.forEach((id, stackSize) -> clonedStackSizes.put(id, stackSize));
            gameState = Optional.of(new GameState(amountOfPlayers, positions.get(),
                    clonedStackSizes, names.get(), smallBlind, bigBlind));
        }
        //Make buttons visible
        Decision.Move moveIfTimeRunOut = highestAmountPutOnTable == 0 ? Decision.Move.CHECK : Decision.Move.FOLD;
        Platform.runLater(() -> {
            gameScreen.setActionsVisible(true);
            gameScreen.startTimer(timeToThink, moveIfTimeRunOut);
        });

        try {
            wait();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Make buttons invisible
        Platform.runLater(() -> {
            gameScreen.setActionsVisible(false);
            gameScreen.stopTimer();
        });

        //Return decision
        return decision;
    }

    /**
     * Called from ButtonListeners-class to notify the client that a decision has been made
     *
     * @param move
     * @param moveSize
     */
    public synchronized void setDecision(Decision.Move move, long moveSize) {
        if (!validMove(move, moveSize))
            return;

        switch (move) {
            case BET:
                if (moveSize == stackSizes.get(this.id))
                    this.decision = new Decision(Decision.Move.ALL_IN);
                else
                    this.decision = new Decision(move, moveSize);
                break;
            case RAISE:
                if (moveSize == stackSizes.get(this.id))
                    this.decision = new Decision(Decision.Move.ALL_IN);
                else
                    this.decision = new Decision(move, moveSize - highestAmountPutOnTable);
                break;
            case CALL:case CHECK:case FOLD:case ALL_IN: this.decision = new Decision(move);
        }

        Platform.runLater(() -> gameScreen.setErrorStateOfAmountTextField(false));

        notifyAll();
    }

    /**
     *  Check if a decision is valid (according to current stack size etc)
     * @param move
     * @param moveSize
     * @return
     */
    private boolean validMove(Decision.Move move, long moveSize) {
        if ((move == Decision.Move.BET || move == Decision.Move.RAISE) && moveSize > stackSizes.get(id) ) {
            GUIMain.debugPrintln("You don't have this much in your stack. Stack size=" + stackSizes.get(id) + ", moveSize=" + moveSize);
            Platform.runLater(() -> gameScreen.setErrorStateOfAmountTextField(true));
            return false;
        }
        else if (move == Decision.Move.RAISE && moveSize- highestAmountPutOnTable < Math.max(bigBlind, minimumRaise) &&
                (moveSize != stackSizes.get(id))) {
            GUIMain.debugPrint("Raise is too small");
            Platform.runLater(() -> gameScreen.setErrorStateOfAmountTextField(true));
            return false;
        }
        else if (move == Decision.Move.BET && moveSize < bigBlind) {
            GUIMain.debugPrint("Bet is too small, must be a minimum of " + bigBlind);
            Platform.runLater(() -> gameScreen.setErrorStateOfAmountTextField(true));
            return false;
        }

        return true;
    }

    public void setDecision(Decision.Move move) { setDecision(move, 0); }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        this.names = Optional.of(new HashMap<>());
        names.forEach((id, name) -> this.names.get().put(id, name));
        Platform.runLater(() ->gameScreen.setNames(names));
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        Platform.runLater(() -> gameScreen.setHandForUser(userID, card1, card2));
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
        Platform.runLater(() -> gameScreen.displayRiver(river));
        newBettingRound();
    }

    @Override
    public void startNewHand() {
        Platform.runLater(() -> gameScreen.startNewHand());
        gameState = Optional.empty();
        newBettingRound();
    }

    @Override
    public void playerBust(int playerID, int rank) {
        Platform.runLater(() -> gameScreen.bustPlayer(playerID, rank));
    }

    @Override
    public void gameOver(Statistics stats) {
        Platform.runLater(() -> gameScreen.gameOver(stats));
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        this.stackSizes = stackSizes;
        Platform.runLater(() -> {
            gameScreen.updateStackSizes(stackSizes);

            //Updates the values of the slider
            gameScreen.updateSliderValues();
        });
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {

        if (!gameState.isPresent()) {
            Map<Integer, Long> clonedStackSizes = new HashMap<>();
            stackSizes.forEach((id, stackSize) -> clonedStackSizes.put(id, stackSize));
            gameState = Optional.of(new GameState(amountOfPlayers, positions.get(),
                    clonedStackSizes, names.get(), smallBlind, bigBlind));
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
        }
        try {
            gameState.get().makeGameStateChange(new GameState.PlayerDecision(decision));
        } catch (IllegalDecisionException e) {
            assert false : "Illegal decision " + e;
        }
        if (gameState.get().getPlayersLeftInHand() > 0) {
            gameScreen.highlightPlayerTurn(gameState.get().currentPlayer.id);
        }
        Platform.runLater(() -> gameScreen.playerMadeDecision(playerId, decision));
    }

    @Override
    public void showdown(ShowdownStats showdownStats) {
        Platform.runLater(() -> gameScreen.showdown(showdownStats));
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
     * Sends every player's position, as a map indexed by the players' IDs.
     * A value of 0 corresponds to the small blind, 1 is big blind..
     * Sent at the start of each hand
     */
    @Override
    public void setPositions(Map<Integer, Integer> positions) {
        this.positions = Optional.of(new HashMap<>());
        positions.forEach((id, pos) -> this.positions.get().put(id, pos));
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
        Platform.runLater(() -> gameScreen.newBettingRound());
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

    public void showHoleCards(Map<Integer, Card[]> holeCards) {
        // Platform.runLater(() -> gameScreen.showHoleCards(holeCards));
        // This is now done via setHandForClient()
    }

    public void highlightPlayerTurn(int id) {
        //gameScreen.highlightPlayerTurn(id);
        // This now done in playerMadeDecision()
    }
}
