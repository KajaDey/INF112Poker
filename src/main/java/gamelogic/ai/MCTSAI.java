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

    private final double contemptFactor = 1.0;
    private final int playerId;
    private int amountOfPlayers;
    private List<Card> holeCards = new ArrayList<>();

    private long bigBlindAmount;
    private long smallBlindAmount;
    private int position; // 0 is dealer
    private Map<Integer, Integer> positions;

    private Optional<Decision> lastDecision = Optional.empty();

    // The AI keeps track of its stackSize by changing it in getDecision()
    private long stackSize;
    // The AI keeps track of the stack sizes of all players in stackSizes (Including its own entry)
    // by updating it in playerMadeDecision()
    private Map<Integer, Long> stackSizes;

    private boolean betHasBeenPlaced;
    private int playersLeftInCurrentHand;
    private long minimumRaise; // If you want to raise, the minimum you need to raise by
    private long currentBet; // The amount the SimpleAI needs to put on the table to remain in the hand

    public MCTSAI(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public Decision getDecision() {
        assert bigBlindAmount > 0 && smallBlindAmount > 0: "SimpleAI was asked to make a decision without receiving big and small blind";
        assert holeCards.size() == 2: "SimpleAI was asked to make a decision after receiving " + holeCards.size() + " hole cards.";
        assert stackSize > 0: "SimpleAI was asked to make a decicion after going all in (stacksize=" + stackSize + ")";

        assert minimumRaise > 0;
        assert stackSize == stackSizes.get(this.playerId) :
                "AI: stacksize mismatch: " + stackSize + " != " + stackSizes.get(this.playerId);
        assert positions != null : "AI was asked to make a decision without receiving positions";

        GameState gameState = new GameState(amountOfPlayers, positions, stackSizes, smallBlindAmount, bigBlindAmount);

        PokerMCTS mcts = new PokerMCTS(gameState, amountOfPlayers, playerId);
        return mcts.calculateFor(2000);
    }

    @Override
    public int getID() {
        return playerId;
    }

    /**
     * Returns a legal amount to raise by, which becomes higher if the hand is good
     * Also removes the appropriate amount of chips from stackSize
     * May go all in. Will return Optional.empty() if it goes all in
     * @param randomModifier Modifier that gets multipled by the handquality
     */
    public Optional<Long> getRaiseAmount(double randomModifier, int handQuality) {
        long raiseAmount;
        if (randomModifier * (handQuality / 26.0) > 1 / contemptFactor) { // If the hand is really good
            raiseAmount = minimumRaise * 4;
        }
        else if (randomModifier * (handQuality / 22.0) > 1 / contemptFactor) { // If the hand is really good
            raiseAmount = minimumRaise * 2;
        }
        else {
            raiseAmount = minimumRaise;
        }

        if (stackSize > raiseAmount + currentBet) {
            stackSize -= raiseAmount + currentBet;
            return Optional.of(raiseAmount);
        }
        else { // Go all in
            stackSize = 0;
            return Optional.empty();
        }
    }

    /**
     * Called whenever there is a new round, after the SimpleAI has gotten its new hole cards
     */
    public void newBettingRound() {
        minimumRaise = bigBlindAmount;
        betHasBeenPlaced = false;
    }

    @Override
    public void startNewHand() {
        playersLeftInCurrentHand = amountOfPlayers;
        holeCards.clear();
    }

    @Override
    public void gameOver(int winnerID) { }

    @Override
    public void setPlayerNames(Map<Integer, String> names) { }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        assert this.getID() == userID;

        holeCards = new ArrayList<Card>(2);
        assert holeCards.size() == 0;
        newBettingRound();
        holeCards.add(card1);
        holeCards.add(card2);
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        assert stackSizes.size() == amountOfPlayers;

        this.stackSize = stackSizes.get(this.playerId);
        this.stackSizes = stackSizes;
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        switch (decision.move) {
            case ALL_IN:
                betHasBeenPlaced = true;

                currentBet = Math.max(stackSizes.get(playerId), currentBet);
                minimumRaise = Math.max(stackSizes.get(playerId), minimumRaise);
                stackSizes.put(playerId, 0L);
                break;

            case BIG_BLIND:
            case SMALL_BLIND:
                betHasBeenPlaced = true;
                minimumRaise = decision.size;

                stackSizes.put(playerId, stackSizes.get(playerId) - decision.size);
                if (playerId == this.playerId) {
                    stackSize -= decision.size;
                    currentBet = 0;
                }
                else {
                    currentBet = decision.size;
                }
                break;

            case CALL:
                stackSizes.put(playerId, stackSizes.get(playerId) - currentBet);
                if (playerId == this.playerId) {
                    currentBet = 0;
                }
                break;

            case RAISE:
            case BET:
                stackSizes.put(playerId, stackSizes.get(playerId) - (currentBet + decision.size));
                if (playerId == this.playerId) {
                    currentBet = 0;
                }
                else {
                    currentBet += decision.size;
                }

                betHasBeenPlaced = true;
                minimumRaise = Math.max(decision.size, bigBlindAmount);
                break;
            case FOLD:
                playersLeftInCurrentHand--;
                break;
        }
    }

    @Override
    public void showdown(List<Integer> playersStillPlaying, int winnerID, Map<Integer, Card[]> holeCards, long pot) { }

    @Override
    public void setBigBlind(long bigBlind) {
        this.bigBlindAmount = bigBlind;
    }

    @Override
    public void setSmallBlind(long smallBlind) {
        this.smallBlindAmount = smallBlind;
    }

    @Override
    public void setPositions(Map<Integer, Integer> positions) {
        assert positions.size() == amountOfPlayers :
                "AI received positions " + positions.size() + " for players, but there are " + amountOfPlayers + " playing.";
        assert positions.get(playerId) != null : "AI received positions object which didn't contain its own position";

        position = positions.get(playerId);
        this.positions = positions;
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        this.amountOfPlayers = amountOfPlayers;
        this.playersLeftInCurrentHand = amountOfPlayers;
    }

    @Override
    public void setLevelDuration(int levelDuration) { }

    @Override
    public void setFlop(Card card1, Card card2, Card card3, long currentPotSize) {
        newBettingRound();
    }

    @Override
    public void setTurn(Card turn, long currentPotSize) {
        newBettingRound();
    }

    @Override
    public void setRiver(Card river, long currentPotSize) {
        newBettingRound();
    }
}