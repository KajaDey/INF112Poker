package main.java.gamelogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by morten on 08.03.16.
 */
public class AI implements GameClient {

    private final int playerId;
    private List<Card> holeCards = new ArrayList<>();
    private int bigBlindAmount;
    private int smallBlindAmount;

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
            handQuality *= 1.5;
        }

        if (Math.random() * (handQuality / 14.0) > 1) {
            if (minimumBetThisBettingRound == 0) {
                if (stackSize >= minimumRaise) {
                    return new Decision(Decision.Move.RAISE, minimumRaise);
                }
            }
            else {
                if (stackSize >= minimumBetThisBettingRound) {
                    return new Decision(Decision.Move.CALL);
                }
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


        return new Decision(Decision.Move.CALL);
    }

    @Override
    public void setPlayerNames(Map<Integer, String> names) {

    }

    @Override
    public void setHoleCards(Card card1, Card card2) {
        assert holeCards.size() == 0;
        holeCards.add(card1);
        holeCards.add(card2);
    }

    @Override
    public void setStackSizes(Map<Integer, Long> stackSizes) {
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
    public void setPositions(Map<Integer, String> setPositions) {

    }

    @Override
    public void setStartChips(long startChips) {

    }

    @Override
    public void setAmountOfPlayers(int amountOfPlayers) {

    }

    @Override
    public void setLevelDuration(int levelDuration) {

    }

    @Override
    public void setLastMove(String lastMove) {

    }
}
