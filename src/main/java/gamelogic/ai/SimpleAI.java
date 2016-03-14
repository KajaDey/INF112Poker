package main.java.gamelogic.ai;

import main.java.gamelogic.Card;
import main.java.gamelogic.Decision;
import main.java.gamelogic.GameClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 An SimpleAI player that works through the GameClient interface.
 It decides its move based solely on the information it gets through GameCLient
 Currently a WIP, may not work correctly
 */
public class SimpleAI implements GameClient {

    private final double contemptFactor;

    private final int playerId;
    private int amountOfPlayers;
    private List<Card> holeCards = new ArrayList<>();

    private int bigBlindAmount;
    private int smallBlindAmount;
    private int position; // 0 is dealer

    private Optional<Decision> lastDecision = Optional.empty();
    private long stackSize;

    private boolean betHasBeenPlaced;
    private int playersLeftInCurrentHand;
    private long minimumRaise; // If you want to raise, the minimum you need to raise by
    private long currentBet; // The amount the SimpleAI needs to put on the table to remain in the hand

    public SimpleAI(int playerId) {
        this(playerId, 1.0);
    }
    /**
     * Construct a new SimpleAI with a contempt factor.
     * Default value is 1.0, higher values make it player more aggressively, i.e. raise/call more often.
     * Values higher than 2 will make it raise/call almost always
     */
    public SimpleAI(int playerId, double contemptFactor) {
        this.playerId = playerId;
        this.contemptFactor = contemptFactor;
    }

    @Override
    public Decision getDecision() {
        assert bigBlindAmount > 0 && smallBlindAmount > 0: "SimpleAI was asked to make a decision without receiving big and small blind";
        assert holeCards.size() == 2: "SimpleAI was asked to make a decision after receiving " + holeCards.size() + " hole cards.";
        assert stackSize > 0: "SimpleAI was asked to make a decicion after going all in (stacksize=" + stackSize + ")";

        assert minimumRaise > 0;

        int handQuality = holeCards.get(0).rank + holeCards.get(1).rank;

        if (holeCards.get(0).suit == holeCards.get(1).suit) {
            handQuality *= 1.2;
        }

        int rankDistance = Math.abs(holeCards.get(0).rank - holeCards.get(1).rank);
        switch (rankDistance) {
            case 0: handQuality *= 1.5; break;
            case 1: handQuality *= 1.4; break;
            case 2: handQuality *= 1.2; break;
            case 3: handQuality *= 1.1; break;
            default: ;
        }

        // Random modifier between 0.5 and 1.5
        double randomModifier = (Math.random() + Math.random()) / 2 + 0.5;

        if (randomModifier * (handQuality / 14.0) > 1 / contemptFactor) { // If the hand is considered "good"
            if (currentBet == 0) {
                if (stackSize >= minimumRaise) {

                    stackSize -= minimumRaise;
                    if(betHasBeenPlaced) {
                        return new Decision(Decision.Move.RAISE, minimumRaise);
                    }
                    else {
                        return new Decision(Decision.Move.BET, minimumRaise);
                    }
                }
                else {
                    return new Decision(Decision.Move.CHECK);
                }
            }
            else if (randomModifier * (handQuality / 20.0) > 1 / contemptFactor) { // If the hand is really good
                if (stackSize >= minimumRaise + currentBet) {
                    stackSize -= minimumRaise + currentBet;
                    return new Decision(Decision.Move.RAISE, minimumRaise);
                }
                else { // Go all in
                    long raiseBy = stackSize - currentBet;
                    System.out.println("AI: Going all in with stacksize " + stackSize);
                    stackSize = 0;
                    return new Decision(Decision.Move.RAISE, raiseBy);
                }
            }
            else {
                stackSize -= currentBet;
                return new Decision(Decision.Move.CALL);
            }
        }
        else {
            if (currentBet == 0) {
                return new Decision(Decision.Move.CHECK);
            }
            else {
                return new Decision(Decision.Move.FOLD);
            }
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
    public void setPlayerNames(Map<Integer, String> names) {

    }

    public int getID() {
        return playerId;
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        if (this.getID() == userID) {
            holeCards = new ArrayList<Card>(2);
            assert holeCards.size() == 0;
            newBettingRound();
            holeCards.add(card1);
            holeCards.add(card2);
        }
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        assert stackSizes.size() >= 2 && amountOfPlayers >= 2;
        this.stackSize = stackSizes.get(this.playerId);
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        switch (decision.move) {
            case CALL:
                if (playerId == this.playerId) {
                    currentBet = 0;
                }
                break;

            case RAISE:
            case BET:
                if (playerId == this.playerId) {
                    currentBet = 0;
                } else {
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
    public void showdown(List<Integer> playersStillPlaying, int winnerID, Map<Integer, Card[]> holeCards, long pot) {

    }

    @Override
    public void setBigBlind(int bigBlind) {
        this.bigBlindAmount = bigBlind;
    }

    @Override
    public void setSmallBlind(int smallBlind) {
        this.smallBlindAmount = smallBlind;
    }

    @Override
    public void setPositions(Map<Integer, Integer> positions) {
        assert positions.size() > 2 && amountOfPlayers >= 2;
        position = positions.get(playerId);
        if (positions.size() == 2) {
            if (position == 1) {
                stackSize -= Math.min(stackSize, bigBlindAmount); // Is big blind
            }
            else {
                stackSize -= Math.min(stackSize, smallBlindAmount); // Is small blind
            }
        }
        else {
            if (position == 1) {
                stackSize -= Math.min(stackSize, smallBlindAmount); // Is small blind
            }
            else if (position == 2) {
                stackSize -= Math.min(stackSize, bigBlindAmount); // Is big blind
            }
        }
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        this.amountOfPlayers = amountOfPlayers;
        this.playersLeftInCurrentHand = amountOfPlayers;
    }

    @Override
    public void setLevelDuration(int levelDuration) {

    }

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