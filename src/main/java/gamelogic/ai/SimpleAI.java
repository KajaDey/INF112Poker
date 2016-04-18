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

    /**
     * Gives a very rough estimate of the quality of a set of holeCards
     * @return A number between 7 and 60
     */
    public static double handQuality(Card card1, Card card2, List<Card> communityCards) {
        double handQuality = card1.rank + card2.rank;

        if (communityCards.size() > 0 ) {
            communityCards.sort(Card::compareTo);

            // Increase hand quality based on how likely a straight is
            short rankMask = 0;
            if (card1.rank == 14 || card2.rank == 14 || communityCards.get(communityCards.size() - 1).rank == 14) {
                rankMask = 0b0100_0000_0000_0010;
            }
            int rank = 2;
            //
            communityLoop: for (Card card : communityCards) {
                while (rank < 14) {
                    if (card.rank == rank) {
                        rankMask |= 1 << rank;
                        rank++;
                        continue communityLoop;
                    }
                    else if (card1.rank == rank || card2.rank == rank) {
                        rankMask |= 1 << rank;
                    }
                    rank++;
                }
            }

            // For each set of 5 consecutive cards (i.e., 4 through 9), checks how many cards you have in that range
            for (int i = 1; i < 11; i++) {
                int mask = rankMask & (0b0000_0000_0001_1111 << i);
                mask = mask - ((mask >>> 1) & 0x55555555);
                mask = (mask & 0x33333333) + ((mask >>> 2) & 0x33333333);
                int bitsSet =  (((mask + (mask >>> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;

                switch (bitsSet) {
                    // These increments may happen several times, increasing the handQuality if there are many outs for straights
                    case 3:
                        if (communityCards.size() == 3) {
                            handQuality += 2;
                        }
                        break;
                    case 4:
                        if (communityCards.size() == 3) {
                            handQuality += 8;
                        }
                        else if (communityCards.size() == 4) {
                            handQuality += 4;
                        }
                        break;
                    case 5:
                        handQuality += 25;
                        break;
                }
            }


            // Increase hand quality for chances for flush
            int ofSameSuit;
            if (card1.suit == card2.suit) {
                ofSameSuit = 2;
                for (Card card : communityCards) {
                    if (card.suit == card1.suit) {
                        ofSameSuit++;
                    }
                }
            }
            else {
                int ofCard1Suit = 1;
                int ofCard2Suit = 1;
                for (Card card : communityCards) {
                    if (card.suit == card1.suit) {
                        ofCard1Suit++;
                    }
                    else if (card.suit == card2.suit) {
                        ofCard2Suit++;
                    }
                }
                ofSameSuit = Math.max(ofCard1Suit, ofCard2Suit);
            }
            switch (ofSameSuit) {
                case 3:
                    if (communityCards.size() == 3) {
                        handQuality += 3;
                    }
                    break;
                case 4:
                    if (communityCards.size() == 3) {
                        handQuality += 15;
                    }
                    else if (communityCards.size() == 4) {
                        handQuality += 8;
                    }
                    break;
                case 5:
                    handQuality += 30;
                    break;
            }

            // Increase hand quality for chances for flush
            if (card1.suit == card2.suit) {
                int ofSameRank = 2;
                for (Card card : communityCards) {
                    if (card.suit == card1.suit) {
                        ofSameRank++;
                    }
                }
                handQuality += xOfAKindEval(ofSameRank, communityCards.size());

            }
            else {
                int ofCard1Rank = 1;
                int ofCard2Rank = 1;
                for (Card card : communityCards) {
                    if (card.rank == card1.rank) {
                        ofCard1Rank++;
                    }
                    else if (card.rank == card2.rank) {
                        ofCard2Rank++;
                    }
                }
                handQuality += xOfAKindEval(ofCard1Rank, communityCards.size());
                handQuality += xOfAKindEval(ofCard2Rank, communityCards.size());
            }

        }
        else {
            if (card1.suit == card2.suit) {
                handQuality *= 1.2;
            }
            int rankDistance = Math.abs(card1.rank - card2.rank);
            switch (rankDistance) {
                case 0:
                    handQuality = handQuality * 1.6 + 15;
                    break;
                case 1:
                    handQuality = handQuality * 1.4 + 6;
                    break;
                case 2:
                    handQuality = handQuality * 1.2 + 3;
                    break;
                case 3:
                    handQuality = handQuality * 1.05 + 1;
                    break;
                default:
            }
        }
        return handQuality;
    }

    public static double xOfAKindEval(int ofSameRank, int holeCardsGiven) {
        switch (ofSameRank) {
            case 2:
                switch (holeCardsGiven) {
                    case 3:
                        return 18;
                    case 4:
                        return 15;
                    case 5:
                        return 12;
                }
                break;
            case 3:
                switch (holeCardsGiven) {
                    case 3:
                        return 22;
                    case 4:
                        return 20;
                    case 5:
                        return 18;
                }
                break;
            case 4:
                return 40;
        }
        return 0;
    }

    @Override
    public Decision getDecision(long timeToThink) {
        assert bigBlindAmount > 0: "Ai was asked to make a decision without receving big blind";
        assert holeCards.size() == 2: "SimpleAI was asked to make a decision after receiving " + holeCards.size() + " hole cards.";
        assert stackSizes.get(playerId) > 0: "SimpleAI was asked to make a decicion after going all in (stacksize=" + stackSizes.get(playerId) + ")";

        assert minimumRaise > 0;
        assert stackSizes.get(playerId).equals(stackSizes.get(this.playerId)) :
                "AI: stacksize mismatch: " + stackSizes.get(playerId) + " != " + stackSizes.get(this.playerId);

        double handQuality = handQuality(holeCards.get(0), holeCards.get(1), new ArrayList<>(0)) * Math.pow(0.95, amountOfPlayers);;

        // Random modifier between 0.5 and 1.5
        double randomModifier = (Math.random() + Math.random()) / 2 + 0.5;
        AIDecision aiDecision;

        if (randomModifier * (handQuality / 18.0) > 1 / contemptFactor) {
            // If the hand is considered "good", raise or bet if no one else has done it
            if (currentBet == 0) {
                aiDecision = getRaiseAmount(randomModifier, handQuality, contemptFactor);
            }
            // If someone has already raised, raise anyway if the hand is really good
            else if (randomModifier * (handQuality / 22.0) > 1 / contemptFactor) {
                aiDecision = getRaiseAmount(randomModifier, handQuality, contemptFactor);
            }
            else {
                aiDecision = AIDecision.CALL;
            }
        }
        else if (randomModifier * (handQuality / 14.0) > 1 / contemptFactor) { // If the hand is decent
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
        return aiDecision.toRealDecision(currentBet, minimumRaise, stackSizes.get(playerId), 2 * minimumRaise, betHasBeenPlaced);
    }

    /**
     * Returns a raise decision, which becomes higher if the hand is good
     * May return a decision to raise higher than stacksize. This gets converted to an ALL_IN decision.
     * @param randomModifier Modifier that gets multipled by the handquality
     */
    public static AIDecision getRaiseAmount(double randomModifier, double handQuality, double contemptFactor) {
        if (randomModifier * (handQuality / 26.0) > 1 / contemptFactor) { // If the hand is really good
            return AIDecision.RAISE_POT;
        }
        else if (randomModifier * (handQuality / 22.0) > 1 / contemptFactor) { // If the hand is really good
            return AIDecision.RAISE_HALF_POT;
        }
        else {
            return AIDecision.RAISE_MINIMUM;
        }
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
                    stackSizes.compute(playerId, (key, val) -> val -= currentBet);
                    currentBet = 0;
                }
                break;

            case RAISE:
            case BET:
                stackSizes.put(playerId, stackSizes.get(playerId) - (currentBet + decision.size));
                if (playerId == this.playerId) {
                }
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
        this.bigBlindAmount = bigBlind;
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

    public enum AIDecision {
        FOLD, CHECK, CALL, RAISE_MINIMUM, RAISE_HALF_POT, RAISE_POT;

        public Decision toRealDecision(long currentBet, long minimumRaise, long stackSize, long pot, boolean betHasBeenPlaced) {
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