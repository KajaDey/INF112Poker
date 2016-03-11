package main.java.gamelogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 An AI player that works through the GameClient interface.
 It decides its move based solely on the information it gets through GameCLient
 Currently a WIP, may not work correctly
 */
public class AI implements GameClient {

    private final int playerId;
    private int amountOfPlayers;
    private List<Card> holeCards = new ArrayList<>();

    private int bigBlindAmount;
    private int smallBlindAmount;
    private int position; // 0 is dealer

    private Optional<Decision> lastDecision = Optional.empty();
    private long stackSize;

    private long minimumRaise; // If you want to raise, the minimum you need to raise by
    private long minimumBetThisBettingRound; // The amount the AI needs to put on the table to remain in the hand

    public AI(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public Decision getDecision() {
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
        if (randomModifier * (handQuality / 14.0) > 1) { // If the hand is considered "good"
            if (minimumBetThisBettingRound == 0) {
                if (stackSize >= minimumRaise) {
                    return new Decision(Decision.Move.RAISE, minimumRaise);
                }
                else {
                    return new Decision(Decision.Move.CALL);
                }
            }
            else {
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

    @Override
    public void setPlayerNames(Map<Integer, String> names) {

    }

    public int getID() {
        return playerId;
    }

    @Override
    public void setHandForClient(int userID, Card card1, Card card2) {
        if (this.getID() == userID) {
            assert holeCards.size() == 0;
            holeCards.add(card1);
            holeCards.add(card2);
        }
    }

    public void setHoleCards(Card card1, Card card2) {
        assert holeCards.size() == 0;
        holeCards.add(card1);
        holeCards.add(card2);
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
        assert stackSizes.size() > 2 && amountOfPlayers >= 2;
        this.stackSize = stackSizes.get(this.playerId);
    }

    @Override
    public void playerMadeDecision(Integer playerId, Decision decision) {

        if (decision.move == Decision.Move.RAISE || decision.move == Decision.Move.BET) {
            minimumBetThisBettingRound += decision.size;
            minimumRaise = decision.size;
        }
    }

    @Override
    public void showdown(Map<Integer, List<Card>> holeCards) {

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
    public void setStartChips(long startChips) {
        stackSize = startChips;
    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {
        this.amountOfPlayers = amountOfPlayers;
    }

    @Override
    public void setLevelDuration(int levelDuration) {

    }

    @Override
    public void setLastMove(Map<Integer,Decision> lastMove) {

    }

    @Override
    public void setFlop(Card card1, Card card2, Card card3) {

    }

    @Override
    public void setTurn(Card turn) {

    }

    @Override
    public void setRiver(Card river) {

    }
}