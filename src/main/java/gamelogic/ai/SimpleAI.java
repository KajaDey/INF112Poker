package gamelogic.ai;

import gamelogic.*;

import java.util.*;

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
    private List<Card> communityCards = new ArrayList<>();
    private long smallBlindAmount;
    private long bigBlindAmount;

    // The AI keeps track of the stack sizes of all players in stackSizes (Including its own entry)
    private Map<Integer, Long> stackSizes;

    private boolean betHasBeenPlaced;
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
    public Decision getDecision(long timeToThink) {
        assert bigBlindAmount > 0: "Ai was asked to make a decision without receving big blind";
        assert holeCards.size() == 2: "SimpleAI was asked to make a decision after receiving " + holeCards.size() + " hole cards.";
        assert stackSizes.get(playerId) > 0: "SimpleAI was asked to make a decicion after going all in (stacksize=" + stackSizes.get(playerId) + ")";

        assert minimumRaise > 0;
        assert stackSizes.get(playerId).equals(stackSizes.get(this.playerId)) :
                "AI: stacksize mismatch: " + stackSizes.get(playerId) + " != " + stackSizes.get(this.playerId);

        double handQuality = HandEstimator.handQuality(holeCards.get(0), holeCards.get(1), communityCards) * Math.pow(0.95, amountOfPlayers);;

        // Random modifier between 0.5 and 1.5
        double randomModifier = (Math.random() + Math.random()) / 2 + 0.5;
        AIDecision aiDecision;

        if (randomModifier * (handQuality / 20.0) > 1 / contemptFactor) {
            // If the hand is considered "good", raise or bet if no one else has done it
            if (currentBet == 0) {
                aiDecision = HandEstimator.getRaiseAmount(randomModifier, handQuality, contemptFactor);
            }
            // If someone has already raised, raise anyway if the hand is really good
            else if (randomModifier * (handQuality / 23.0) > 1 / contemptFactor) {
                aiDecision = HandEstimator.getRaiseAmount(randomModifier, handQuality, contemptFactor);
            }
            else {
                aiDecision = AIDecision.CALL;
            }
        }
        else if (randomModifier * (handQuality / 15.0) > 1 / contemptFactor) { // If the hand is decent
            if (currentBet == 0) {
                aiDecision = AIDecision.CHECK;
            }
            else if (currentBet < stackSizes.get(playerId)  / 20 * randomModifier) { // If it's a small call
                aiDecision = AIDecision.CALL;
            }
            else {
                aiDecision = AIDecision.FOLD;
            }
        }
        else {
            if (currentBet == 0) {
                aiDecision = AIDecision.CHECK;
            }
            else {
                aiDecision = AIDecision.FOLD;
            }
        }
        return aiDecision.toRealDecision(currentBet, minimumRaise, stackSizes.get(playerId), 2 * minimumRaise, false, betHasBeenPlaced);
    }

    /**
     * Called whenever there is a new round, after the SimpleAI has gotten its new hole cards
     */
    public void newBettingRound() {
        betHasBeenPlaced = false;
        minimumRaise = bigBlindAmount;
    }

    @Override
    public void startNewHand() {
        holeCards.clear();
    }

    @Override
    public void playerBust(int playerID, int rank) {
        amountOfPlayers--;
    }

    @Override
    public void gameOver(Statistics stats) { }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {
        assert names.size() == amountOfPlayers : "SimpleAI received names for " + names.size() + " players, but there are " + amountOfPlayers + " players playing.";
    }

    public int getID() {
        return playerId;
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        assert this.getID() == userID : "SimpleAI received cards for id " + userID + ", but AI's id is " + this.getID();

        holeCards = new ArrayList<>(2);
        assert holeCards.size() == 0;
        newBettingRound();
        holeCards.add(card1);
        holeCards.add(card2);
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        assert stackSizes.size() == amountOfPlayers;
        assert stackSizes.get(this.playerId) >= 0 : "AI was sent a stacksize of " + stackSizes.get(this.playerId);
        for (int playerId : stackSizes.keySet()) {
            assert stackSizes.get(playerId) >= 0 : "Player " + playerId + "'s stacksize is " + stackSizes.get(playerId);
        }
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
                long size = decision.move == Decision.Move.BIG_BLIND ? bigBlindAmount : smallBlindAmount;
                betHasBeenPlaced = true;
                minimumRaise = size;

                stackSizes.compute(playerId, (key, val) -> val -= size);
                if (playerId == this.playerId) {
                    currentBet = 0;
                }
                else {
                    currentBet = size;
                }
                break;

            case CALL:
                if (playerId == this.playerId) {
                    stackSizes.compute(playerId, (key, val) -> val -= currentBet);
                    currentBet = 0;
                }
                break;

            case RAISE:
            case BET:
                stackSizes.put(playerId, stackSizes.get(playerId) - (currentBet + decision.getSize()));
                if (playerId == this.playerId) {
                    currentBet = 0;
                }
                else {
                    currentBet += decision.getSize();
                }

                betHasBeenPlaced = true;
                minimumRaise = decision.getSize();
                break;
            case FOLD:
            break;
        }
    }

    @Override
    public void showdown(ShowdownStats showdownStats) { }

    @Override
    public void setBigBlind(long bigBlind) {
        this.bigBlindAmount = bigBlind;
    }

    @Override
    public void setSmallBlind(long smallBlind) { this.smallBlindAmount = smallBlind;
    }

    @Override
    public void setPositions(Map<Integer, Integer> positions) {
        assert positions.size() == amountOfPlayers :
        "AI received positions " + positions.size() + " for players, but there are " + amountOfPlayers + " playing.";
        assert positions.get(playerId) != null : "AI " + playerId + " received positions object which didn't contain its own position";
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        this.amountOfPlayers = amountOfPlayers;
    }

    @Override
    public void setLevelDuration(int levelDuration) { }

    @Override
    public void setFlop(Card card1, Card card2, Card card3) {
        communityCards.add(card1);
        communityCards.add(card2);
        communityCards.add(card3);
        newBettingRound();
    }

    @Override
    public void setTurn(Card turn) {
        communityCards.add(turn);
        newBettingRound();
    }

    @Override
    public void setRiver(Card river) {
        communityCards.add(river);
        newBettingRound();
    }

    public enum AIDecision {
        FOLD, CHECK, CALL, RAISE_MINIMUM, RAISE_HALF_POT, RAISE_POT;

        public Decision toRealDecision(long currentBet, long minimumRaise, long stackSize, long pot,
                                       boolean isOnlyPlayerInHand, boolean betHasBeenPlaced) {
            Decision.Move moneyMove = betHasBeenPlaced ? Decision.Move.RAISE : Decision.Move.BET;
            switch (this) {
                case FOLD:
                    return new Decision(Decision.Move.FOLD);
                case CHECK:
                    return new Decision(Decision.Move.CHECK);
                case CALL:
                    if (currentBet >= stackSize) {
                        return new Decision(Decision.Move.ALL_IN);
                    }
                    return new Decision(Decision.Move.CALL);
                case RAISE_MINIMUM:
                    if (currentBet + minimumRaise >= stackSize) {
                        return new Decision(Decision.Move.ALL_IN);
                    }
                    return new Decision(moneyMove, minimumRaise);
                case RAISE_HALF_POT:
                    if (currentBet + pot / 2 >= stackSize) {
                        return new Decision(Decision.Move.ALL_IN);
                    }
                    else if (pot / 2 >= minimumRaise) {
                        return new Decision(moneyMove, pot / 2);
                    }
                    else {
                        throw new IllegalArgumentException("Tried to turn a " + this + " into real decision, but minimum raise is too low (pot=" + pot + ", minimumRaise=" + minimumRaise + ")");
                    }
                case RAISE_POT:
                    if (currentBet + pot >= stackSize) {
                        return new Decision(Decision.Move.ALL_IN);
                    }
                    return new Decision(moneyMove, pot);
                default: throw new IllegalStateException();

            }
        }
    }
}