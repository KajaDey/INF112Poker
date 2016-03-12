package main.java.gamelogic;

import java.util.Optional;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Player extends User {

    private long stackSize;
    private int ID;
    private Hand hand;
    private Table table;
    private Optional<Decision> lastDecision = Optional.empty();

    public Player(String name, long stackSize, Table table, int ID) {
        super(name);
        this.stackSize = stackSize;
        this.table = table;
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public void act(Decision decision, Long currentBet) {
        switch (decision.move) {
            case BET:
                this.stackSize -= decision.size;
                break;
            case RAISE:
                this.stackSize -= (decision.size + currentBet);
                break;
            case CALL:
                this.stackSize -= currentBet;
                break;
        }

        this.lastDecision = Optional.of(decision);
    }

    public Optional<Decision> getLastDecision() {
        return lastDecision;
    }

    public long getStackSize() {
        return stackSize;
    }

    public void setStackSize(long stackSize) {
        this.stackSize = stackSize;
    }

    public void setHand(Card card1, Card card2) {
        this.hand = new Hand(card1, card2, table.getCommunityCards());
    }

    public boolean stillPlaying() {
        return stackSize > 0;
    }
}

