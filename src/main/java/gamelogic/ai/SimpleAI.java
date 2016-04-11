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
        assert holeCards.size() == 2: "SimpleAI was asked to make a decision after receiving " + holeCards.size() + " hole cards.";
        assert stackSizes.get(playerId) > 0: "SimpleAI was asked to make a decicion after going all in (stacksize=" + stackSizes.get(playerId) + ")";

        assert minimumRaise > 0;
        assert stackSizes.get(playerId).equals(stackSizes.get(this.playerId)) :
                "AI: stacksize mismatch: " + stackSizes.get(playerId) + " != " + stackSizes.get(this.playerId);

        int handQuality = holeCards.get(0).rank + holeCards.get(1).rank;

        if (holeCards.get(0).suit == holeCards.get(1).suit) {
            handQuality *= 1.2;
        }

        int rankDistance = Math.abs(holeCards.get(0).rank - holeCards.get(1).rank);
        switch (rankDistance) {
            case 0: handQuality *= 2.0; break;
            case 1: handQuality *= 1.4; break;
            case 2: handQuality *= 1.2; break;
            case 3: handQuality *= 1.1; break;
            default:
        }
        handQuality *= Math.pow(0.95, amountOfPlayers);

        // Random modifier between 0.5 and 1.5
        double randomModifier = (Math.random() + Math.random()) / 2 + 0.5;

        if (randomModifier * (handQuality / 18.0) > 1 / contemptFactor) {
            // If the hand is considered "good", raise or bet if no one else has done it
            if (currentBet == 0) {
                Optional<Long> raiseAmount = getRaiseAmount(randomModifier, handQuality);
                if (raiseAmount.isPresent()) {
                    if(betHasBeenPlaced) {
                        return new Decision(Decision.Move.RAISE, raiseAmount.get());
                    }
                    else {
                        return new Decision(Decision.Move.BET, raiseAmount.get());
                    }
                }
                else {
                    return new Decision(Decision.Move.ALL_IN);
                }
            }
            // If someone has already raised, raise anyway if the hand is really good
            else if (randomModifier * (handQuality / 22.0) > 1 / contemptFactor) {
                Optional<Long> raiseAmount = getRaiseAmount(randomModifier, handQuality);
                if (raiseAmount.isPresent()) {
                    return new Decision(Decision.Move.RAISE, raiseAmount.get());
                }
                else { // Go all in
                    return new Decision(Decision.Move.ALL_IN);
                }
            }
            else {
                if (stackSizes.get(playerId) > currentBet) {
                    return new Decision(Decision.Move.CALL);
                }
                else {
                    return new Decision(Decision.Move.ALL_IN);
                }
            }
        }
        else if (randomModifier * (handQuality / 14.0) > 1 / contemptFactor) { // If the hand is decent
            if (currentBet == 0) {
                return new Decision(Decision.Move.CHECK);
            }
            else if (currentBet < stackSizes.get(playerId)  / 20 * randomModifier) {
                return new Decision(Decision.Move.CALL);
            }
            else {
                return new Decision(Decision.Move.FOLD);
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

        if (stackSizes.get(playerId) > raiseAmount + currentBet) {
            return Optional.of(raiseAmount);
        }
        else { // Go all in
            return Optional.empty();
        }
    }

    /**
     * Called whenever there is a new round, after the SimpleAI has gotten its new hole cards
     */
    public void newBettingRound() {
        betHasBeenPlaced = false;
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
                betHasBeenPlaced = true;
                minimumRaise = decision.size;

                stackSizes.compute(playerId, (key, val) -> val -= decision.size);
                if (playerId == this.playerId) {
                    currentBet = 0;
                }
                else {
                    currentBet = decision.size;
                }
                break;

            case CALL:
                stackSizes.compute(playerId, (key, val) -> val -= decision.size);
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
                minimumRaise = decision.size;
                break;
            case FOLD:
            break;
        }
    }

    @Override
    public void showdown(ShowdownStats showdownStats) { }

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
        assert positions.get(playerId) != null : "AI " + playerId + " received positions object which didn't contain its own position";
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        this.amountOfPlayers = amountOfPlayers;
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