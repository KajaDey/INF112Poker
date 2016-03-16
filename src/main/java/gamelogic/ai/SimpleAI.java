package gamelogic.ai;

import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.GameClient;

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

    private long bigBlindAmount;
    private long smallBlindAmount;
    private int position; // 0 is dealer

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
        assert stackSize == stackSizes.get(this.playerId) :
                "AI: stacksize mismatch: " + stackSize + " != " + stackSizes.get(this.playerId);

        for (int id : stackSizes.keySet()) {
            System.out.println("AI: Player " + id + " has " + stackSizes.get(id) + " in stack");
        }

        int handQuality = holeCards.get(0).rank + holeCards.get(1).rank;

        if (holeCards.get(0).suit == holeCards.get(1).suit) {
            handQuality *= 1.2;
        }

        int rankDistance = Math.abs(holeCards.get(0).rank - holeCards.get(1).rank);
        switch (rankDistance) {
            case 0: handQuality *= 1.6; break;
            case 1: handQuality *= 1.4; break;
            case 2: handQuality *= 1.2; break;
            case 3: handQuality *= 1.1; break;
            default: ;
        }

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
                if (stackSize > currentBet) {
                    stackSize -= currentBet;
                    return new Decision(Decision.Move.CALL);
                }
                else {
                    stackSize = 0;
                    return new Decision(Decision.Move.ALL_IN);
                }
            }
        }
        else if (randomModifier * (handQuality / 14.0) > 1 / contemptFactor) { // If the hand is decent
            if (currentBet == 0) {
                return new Decision(Decision.Move.CHECK);
            }
            else if (currentBet < stackSize / 20 * randomModifier) {
                stackSize -= currentBet;
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

        if (stackSize >= raiseAmount + currentBet) {
            stackSize -= raiseAmount + currentBet;
            System.out.println("AI: Raising " + raiseAmount + " top of raise, " + (raiseAmount + currentBet) + " put on table");
            return Optional.of(raiseAmount);
        }
        else { // Go all in
            System.out.println("AI: Going all in with stacksize " + stackSize);
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
    public void gameOver(int winnerID) {

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
        assert stackSizes.size() == amountOfPlayers;

        System.out.println("AI: AI was sent a stacksize of " + stackSizes.get(this.playerId));
        this.stackSize = stackSizes.get(this.playerId);
        this.stackSizes = stackSizes;
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {
        switch (decision.move) {
            case ALL_IN:
                betHasBeenPlaced = true;

                currentBet = Math.max(stackSizes.get(playerId), currentBet);
                System.out.println("AI: Player " + playerId + " with stack " + stackSizes.get(playerId) + " went all in, currentBet=" + currentBet);
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
    public void showdown(List<Integer> playersStillPlaying, int winnerID, Map<Integer, Card[]> holeCards, long pot) {

    }

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
        /**
        if (positions.size() == 2) {
            if (position == 1) {
                stackSize -= Math.min(stackSize, bigBlindAmount); // Is big blind
                System.out.println("AI: AI is big blind, stacksize is now " + stackSize);
            }
            else {
                stackSize -= Math.min(stackSize, smallBlindAmount); // Is small blind
                System.out.println("AI: AI is small blind, stacksize is now " + stackSize);
            }
        }
        else {
            if (position == 1) {
                stackSize -= Math.min(stackSize, smallBlindAmount); // Is small blind
            }
            else if (position == 2) {
                stackSize -= Math.min(stackSize, bigBlindAmount); // Is big blind
            }
        } */
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