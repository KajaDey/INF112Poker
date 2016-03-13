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

    private int playersLeftInCurrentHand;
    private long minimumRaise; // If you want to raise, the minimum you need to raise by
    private long minimumBetThisBettingRound; // The amount the SimpleAI needs to put on the table to remain in the hand

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
        double raiseFactor = 1.0;

        if (randomModifier * (handQuality / 14.0) > 1 / contemptFactor) { // If the hand is considered "good"
            if (minimumBetThisBettingRound == 0) {
                if (stackSize >= minimumRaise) {
                    minimumBetThisBettingRound = 0;
                    return new Decision(Decision.Move.RAISE, minimumRaise);
                }
                else {
                    return new Decision(Decision.Move.CHECK);
                }
            }
            else {
                minimumBetThisBettingRound = 0;
                return new Decision(Decision.Move.CALL);
            }
        }
        else {
            if (minimumBetThisBettingRound == 0) {
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
        // Probably not need to set minimumBetTHisBettingRound, because it gets set once positions are set
        // TODO ensure that this is the case
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

        if (decision.move == Decision.Move.RAISE || decision.move == Decision.Move.BET) {
            minimumBetThisBettingRound += decision.size;
            minimumRaise = decision.size;
        }
        if (decision.move == Decision.Move.FOLD) {
            playersLeftInCurrentHand--;
        }
    }

    @Override
    public void showdown(Map<Integer, List<Card>> holeCards) {

    }

    @Override
    public void setBigBlind(int bigBlind) {
        this.bigBlindAmount = bigBlind;
        this.minimumRaise = bigBlind;
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
                minimumBetThisBettingRound = 0; // Is big blind
            }
            else {
                minimumBetThisBettingRound = smallBlindAmount; // Is small blind
            }
        }
        else {
            if (position == 1) {
                minimumBetThisBettingRound = bigBlindAmount - smallBlindAmount; // Is small blind
            }
            else if (position == 2) {
                minimumBetThisBettingRound = 0; // Is big blind
            }
            else {
                minimumBetThisBettingRound = bigBlindAmount;
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