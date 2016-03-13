package main.java.gamelogic;

import java.util.Optional;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Player extends User {

    private Hand hand;
    private Table table;
    private Optional<Decision> lastDecision = Optional.empty();

    private int ID;
    private long stackSize;
    private long putOnTableThisRound = 0;
    private String name;

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
                assert putOnTableThisRound == 0 : "Player bet while putOnTableThisRound was != 0";
                this.putOnTableThisRound = decision.size;
                this.stackSize -= decision.size;
                System.out.println("Player " + this.getName() + " bet " + decision.size + ". New stacksize is " + this.stackSize);

                break;
            case RAISE:
                this.putOnTableThisRound += decision.size;
                this.stackSize -= (decision.size);
                System.out.println("Player " + this.getName() + " raised " + decision.size + ". New stacksize is " + this.stackSize);

                break;
            case CALL:
                this.putOnTableThisRound = currentBet;
                this.stackSize -= (currentBet - putOnTableThisRound);
                System.out.println("Player " + this.getName() + " called " + currentBet + ". New stacksize is " + this.stackSize);

                break;
        }

        this.lastDecision = Optional.of(decision);
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

    public boolean stillPlaying() {
        return stackSize > 0;
    }

    public void setAmountPutOnTableThisBettingRound(long amount) {
        this.putOnTableThisRound = amount;
    }
}

