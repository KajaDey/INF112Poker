package main.java.gamelogic;

import java.util.Optional;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Player extends User {

    private Hand hand;
    private Table table;

    private int ID;
    private long stackSize;
    private long putOnTableThisRound = 0;

    public Player(String name, long stackSize, Table table, int ID) {
        super(name);
        this.stackSize = stackSize;
        this.table = table;
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public void act(Decision decision, long currentBet) {
        switch (decision.move) {
            case BET:
                assert putOnTableThisRound == 0 : "Player " + ID + " bet while putOnTableThisRound was != 0";
                this.putOnTableThisRound = decision.size;
                this.stackSize -= decision.size;
                break;
            case RAISE:
                long totalPutOnTable = ((currentBet-putOnTableThisRound) + decision.size);
                this.stackSize -= (totalPutOnTable + putOnTableThisRound);
                this.putOnTableThisRound += totalPutOnTable;
                System.out.println("New put on table: " + putOnTableThisRound);
                break;
            case CALL:
                this.stackSize -= (currentBet - putOnTableThisRound);
                this.putOnTableThisRound = currentBet;
                break;
        }
    }

    public long getAmountPutOnTableThisBettingRound() {
        return putOnTableThisRound;
    }

    public long getStackSize() {
        return stackSize;
    }

    public void setHand(Card card1, Card card2) {
        this.hand = new Hand(card1, card2, table.getCommunityCards());
    }

    public String cardsOnHand() {
        return hand.getHoleCards().get(0) + " " + hand.getHoleCards().get(1);
    }

    public boolean stillPlaying() {
        return stackSize > 0;
    }

    public void setAmountPutOnTableThisBettingRound(long amount) {
        this.putOnTableThisRound = amount;
    }

    public void incrementStack(long size) {
        stackSize += size;
    }
}

